package ac.boar.anticheat.prediction.ticker;

import ac.boar.anticheat.check.api.Check;
import ac.boar.anticheat.check.api.impl.OffsetHandlerCheck;
import ac.boar.anticheat.prediction.engine.PredictionEngineElytra;
import ac.boar.anticheat.prediction.engine.PredictionEngineNormal;
import ac.boar.anticheat.prediction.engine.base.PredictionEngine;
import ac.boar.anticheat.prediction.engine.data.Vector;
import ac.boar.anticheat.prediction.engine.data.VectorType;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.anticheat.utils.collisions.Collisions;
import ac.boar.utils.MathUtil;
import ac.boar.utils.math.BoundingBox;
import ac.boar.utils.math.Vec3f;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.cloudburstmc.math.GenericMath;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.Ability;
import org.geysermc.geyser.entity.EntityDefinitions;
import org.geysermc.geyser.level.block.Fluid;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class EntityTicker {
    protected final BoarPlayer player;

    public void tick() {
        baseTick();
        tickMovement();
    }

    public void baseTick() {
        player.wasInPowderSnow = player.inPowderSnow;
        player.inPowderSnow = false;
        updateWaterState();
        updateSubmergedInWaterState();
        updateSwimming();
    }

    public void tickMovement() {
        PredictionEngine engine;
        if (player.gliding) {
            engine = new PredictionEngineElytra(player);
        } else {
            engine = new PredictionEngineNormal(player);
        }
//        if ((this.isTouchingWater() || this.isInLava()) && this.shouldSwimInFluids() && !this.canWalkOnFluid(fluidState)) {
//            this.travelInFluid(movementInput);
//        } else if (this.isGliding()) {
//            this.travelGliding();
//        } else {
//            this.travelMidAir(movementInput);
//        }

        if (player.abilities.getAbilities().contains(Ability.MAY_FLY)) {
            player.clientVelocity = engine.applyEndOfTick(player.actualVelocity);
            return;
        }

        player.movementInput = player.movementInput.mul(0.98F);

        if (player.movementInput.x != 0 && player.movementInput.z != 0) {
            player.movementInput = player.movementInput.mul(1F / (float) GenericMath.sqrt(2));
        }

        List<Vector> possibilities = engine.gatherAllPossibilities();
        Vec3f beforeCollision = Vec3f.ZERO, afterCollision = Vec3f.ZERO;
        double closetOffset = Double.MAX_VALUE;
        for (Vector vector : possibilities) {
            final Vec3f bc = Collisions.adjustMovementForSneaking(player, vector.getVelocity());
            final Vec3f ac = Collisions.adjustMovementForCollisions(player, player.boundingBox.offset(0, 1e-5f, 0), bc);

            double offset = ac.squaredDistanceTo(player.actualVelocity);
            if (offset < closetOffset) {
                closetOffset = offset;
                player.closetVector = vector;
                afterCollision = ac;
                beforeCollision = bc;
            }
        }

        player.collideX = afterCollision.x != beforeCollision.x;
        player.collideZ = afterCollision.z != beforeCollision.z;
        player.collideY = afterCollision.y != beforeCollision.y;

        Vec3f clientVelocity = afterCollision.clone();

        player.lastGround = player.onGround;
        player.onGround = beforeCollision.y < 0 && player.collideY;
        double offset = afterCollision.distanceTo(player.actualVelocity);
        offset -= player.extraUncertainOffset;
        player.extraUncertainOffset = 0;

        if (offset < 1e-4) {
            clientVelocity = player.actualVelocity.clone();
        }

        player.predictedVelocity = afterCollision.clone();

        if (player.actualVelocity.length() > 0) {
            Bukkit.broadcastMessage((offset > 1e-4 ? "§c" : "§a") + "O:" + offset + ", T: " + player.closetVector.getType() + ", P: " + afterCollision.x + "," + afterCollision.y + "," + afterCollision.z);

            Bukkit.broadcastMessage("§7A: " + player.actualVelocity.x + "," + player.actualVelocity.y + "," + player.actualVelocity.z + ", " +
                    "SPRINTING=" + player.closetVector.isSprinting() + ", SNEAKING=" + player.sneaking + ", SS" + player.sinceSprinting +
                    ", SN" + player.sinceSneaking + ", PS" + possibilities.size());
        }

        if (player.collideX) {
            clientVelocity.x = 0;
        }

        if (player.collideY) {
            clientVelocity.y = 0;
        }

        if (player.collideZ) {
            clientVelocity.z = 0;
        }

        if (player.closetVector.getType() == VectorType.VELOCITY) {
            Iterator<Map.Entry<Long, Vec3f>> iterator = player.queuedVelocities.entrySet().iterator();

            Map.Entry<Long, Vec3f> entry;
            while (iterator.hasNext() && (entry = iterator.next()) != null) {
                if (entry.getKey() > player.closetVector.getTransactionId()) {
                    break;
                } else {
                    iterator.remove();
                }
            }
        }

        player.clientVelocity = engine.applyEndOfTick(clientVelocity);

        for (Map.Entry<Class, Check> entry : player.checkHolder.entrySet()) {
            Check v = entry.getValue();
            if (v instanceof OffsetHandlerCheck) {
                ((OffsetHandlerCheck) v).onPredictionComplete(offset);
            }
        }

        if (player.actualVelocity.length() > 0) {
            Bukkit.broadcastMessage(player.x + "," + player.y + "," + player.z);
        }
        player.boundingBox = BoundingBox.getBoxAt(player.x, player.y, player.z, EntityDefinitions.PLAYER.width(), player.getHeight());
    }

    private boolean updateWaterState() {
        player.fluidHeight.clear();
        this.checkWaterState();
        double d = 0.0023333333333333335;
//        double d = this.getWorld().getDimension().ultrawarm() ? 0.007 : 0.0023333333333333335;
        boolean bl = this.updateMovementInFluid(Fluid.LAVA, d);
        return player.touchingWater || bl;
    }

    public void updateSwimming() {
        if (player.swimming) {
            player.swimming = player.sprinting && player.touchingWater;
            //this.setSwimming(this.isSprinting() && this.isTouchingWater() && !this.hasVehicle());
        } else {
            player.swimming = player.sprinting && player.submergedInWater;
            //this.setSwimming(this.isSprinting() && this.isSubmergedInWater() && !this.hasVehicle() && this.getWorld().getFluidState(this.blockPos).isIn(FluidTags.WATER));
        }
    }

    private void updateSubmergedInWaterState() {
        player.submergedInWater = player.submergedFluidTag.containsKey(Fluid.WATER);
        player.submergedFluidTag.clear();
        double d = this.getEyeY();
//        Entity entity = this.getVehicle();
//        if (entity instanceof AbstractBoatEntity abstractBoatEntity) {
//            if (!abstractBoatEntity.isSubmergedInWater() && abstractBoatEntity.getBoundingBox().maxY >= d && abstractBoatEntity.getBoundingBox().minY <= d) {
//                return;
//            }
//        }

        Vector3i blockPos = Vector3i.from(player.x, d, player.z);
//        FluidState fluidState = this.getWorld().getFluidState(blockPos);
//        double e = (double)((float)blockPos.getY() + fluidState.getHeight(this.getWorld(), blockPos));
//        if (e > d) {
//            Stream var10000 = fluidState.streamTags();
//            Set var10001 = this.submergedFluidTag;
//            Objects.requireNonNull(var10001);
//            var10000.forEach(var10001::add);
//        }

    }

    public double getEyeY() {
        return EntityDefinitions.PLAYER.offset();
//        return this.pos.y + (double)this.standingEyeHeight;
    }

    private void checkWaterState() {
//        Entity var2 = this.getVehicle();
//        if (var2 instanceof AbstractBoatEntity abstractBoatEntity) {
//            if (!abstractBoatEntity.isSubmergedInWater()) {
//                this.touchingWater = false;
//                return;
//            }
//        }

        if (this.updateMovementInFluid(Fluid.WATER, 0.014)) {
            if (!player.touchingWater && player.tick > 1) {
                // this.onSwimmingStart();
            }

            player.onLanding();
            player.touchingWater = true;
        } else {
            player.touchingWater = false;
        }
    }

    public boolean isRegionUnloaded() {
//        Box box = this.getBoundingBox().expand(1.0);
//        int i = MathHelper.floor(box.minX);
//        int j = MathHelper.ceil(box.maxX);
//        int k = MathHelper.floor(box.minZ);
//        int l = MathHelper.ceil(box.maxZ);
//        return !this.getWorld().isRegionLoaded(i, k, j, l);
        return false;
    }

    public boolean updateMovementInFluid(Fluid tag, double speed) {
        if (this.isRegionUnloaded()) {
            return false;
        } else {
            BoundingBox box = player.boundingBox.contract(0.001F);
            int i = MathUtil.floor(box.minX);
            int j = MathUtil.ceil(box.maxX);
            int k = MathUtil.floor(box.minY);
            int l = MathUtil.ceil(box.maxY);
            int m = MathUtil.floor(box.minZ);
            int n = MathUtil.ceil(box.maxZ);
            double d = 0.0;
            boolean bl = this.isPushedByFluids();
            boolean bl2 = false;
            Vec3f vec3F = Vec3f.ZERO;
            int o = 0;
            Vector3i vector3i;

            for(int p = i; p < j; ++p) {
                for(int q = k; q < l; ++q) {
                    for(int r = m; r < n; ++r) {
                        vector3i = Vector3i.from(p, q, r);

                        // I guess this is a TODO.
//                        FluidState fluidState = this.getWorld().getFluidState(mutable);
//                        if (fluidState.isIn(tag)) {
//                            double e = (double)((float)q + fluidState.getHeight(this.getWorld(), mutable));
//                            if (e >= box.minY) {
//                                bl2 = true;
//                                d = Math.max(e - box.minY, d);
//                                if (bl) {
//                                    Vec3f vec3f2 = fluidState.getVelocity(this.getWorld(), mutable);
//                                    if (d < 0.4) {
//                                        vec3f2 = vec3f2.mul(d);
//                                    }
//
//                                    vec3f = vec3f.add(vec3f2);
//                                    ++o;
//                                }
//                            }
//                        }
                    }
                }
            }

            if (vec3F.length() > 0.0) {
                if (o > 0) {
                    vec3F = vec3F.mul(1.0 / (double)o);
                }

//                if (!(this instanceof PlayerEntity)) {
//                    vec3f = vec3f.normalize();
//                }

                Vec3f vec3f3 = player.clientVelocity;
                vec3F = vec3F.mul(speed);
                if (Math.abs(vec3f3.x) < 0.003 && Math.abs(vec3f3.z) < 0.003 && vec3F.length() < 0.0045000000000000005) {
                    vec3F = vec3F.normalize().mul(0.0045000000000000005);
                }

                player.clientVelocity = player.clientVelocity.add(vec3f3);
            }

            player.fluidHeight.put(tag, d);
            return bl2;
        }
    }

    public boolean isPushedByFluids() {
        return true;
    }
}
