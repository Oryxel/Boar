package ac.boar.anticheat.prediction.ticker;

import ac.boar.anticheat.prediction.engine.PredictionEngineNormal;
import ac.boar.anticheat.prediction.engine.base.PredictionEngine;
import ac.boar.anticheat.prediction.engine.data.Vector;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.anticheat.utils.Collisions;
import ac.boar.utils.MathUtil;
import ac.boar.utils.math.BoundingBox;
import ac.boar.utils.math.Vec3d;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.entity.EntityDefinitions;
import org.geysermc.geyser.level.block.Fluid;

@RequiredArgsConstructor
public class EntityTicker {
    private final BoarPlayer player;

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
//        if ((this.isTouchingWater() || this.isInLava()) && this.shouldSwimInFluids() && !this.canWalkOnFluid(fluidState)) {
//            this.travelInFluid(movementInput);
//        } else if (this.isGliding()) {
//            this.travelGliding();
//        } else {
//            this.travelMidAir(movementInput);
//        }

        engine = new PredictionEngineNormal(player);

        player.movementInput = player.movementInput.multiply(0.98F);

        if (player.movementInput.x != 0 && player.movementInput.z != 0) {
            player.movementInput = player.movementInput.multiply(1D / Math.sqrt(2));
        }

        Vec3d beforeCollision = Vec3d.ZERO, afterCollision = Vec3d.ZERO;
        double closetOffset = Double.MAX_VALUE;
        for (Vector vector : engine.gatherAllPossibilities()) {
            final Vec3d bc = engine.travel(vector.getVelocity(), player.movementInput);
            final Vec3d ac = Collisions.adjustMovementForCollisions(player, player.boundingBox, bc);

            double offset = ac.squaredDistanceTo(player.actualVelocity);
            if (offset < closetOffset) {
                closetOffset = offset;
                player.closetVector = vector;
                afterCollision = ac;
                beforeCollision = bc;
            }
        }

        Vec3d clientVelocity = afterCollision.clone();

        player.onGround = beforeCollision.y < 0 && afterCollision.y != beforeCollision.y;

        double offset = afterCollision.distanceTo(player.actualVelocity);
        // We're aiming for 1e-3 -> 1e-4 accuracy
        if (offset > 1e-4) {

        } else if (offset < 1e-4) {
            clientVelocity = player.actualVelocity.clone();
        }

        if (clientVelocity.length() > 1e-9) {
            Bukkit.broadcastMessage((offset > 1e-4 ? "§c" : "§a") + "O:" + offset + ", P: " + afterCollision.x + "," + afterCollision.y + "," + afterCollision.z);
        }

        if (beforeCollision.x != afterCollision.x) {
            clientVelocity.x = 0;
        }

        if (beforeCollision.z != afterCollision.z) {
            clientVelocity.z = 0;
        }

        player.clientVelocity = engine.applyEndOfTick(clientVelocity);
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
            BoundingBox box = player.boundingBox.contract(0.001);
            int i = MathUtil.floor(box.minX);
            int j = MathUtil.ceil(box.maxX);
            int k = MathUtil.floor(box.minY);
            int l = MathUtil.ceil(box.maxY);
            int m = MathUtil.floor(box.minZ);
            int n = MathUtil.ceil(box.maxZ);
            double d = 0.0;
            boolean bl = this.isPushedByFluids();
            boolean bl2 = false;
            Vec3d vec3d = Vec3d.ZERO;
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
//                                    Vec3d vec3d2 = fluidState.getVelocity(this.getWorld(), mutable);
//                                    if (d < 0.4) {
//                                        vec3d2 = vec3d2.multiply(d);
//                                    }
//
//                                    vec3d = vec3d.add(vec3d2);
//                                    ++o;
//                                }
//                            }
//                        }
                    }
                }
            }

            if (vec3d.length() > 0.0) {
                if (o > 0) {
                    vec3d = vec3d.multiply(1.0 / (double)o);
                }

//                if (!(this instanceof PlayerEntity)) {
//                    vec3d = vec3d.normalize();
//                }

                Vec3d vec3d3 = player.clientVelocity;
                vec3d = vec3d.multiply(speed);
                if (Math.abs(vec3d3.x) < 0.003 && Math.abs(vec3d3.z) < 0.003 && vec3d.length() < 0.0045000000000000005) {
                    vec3d = vec3d.normalize().multiply(0.0045000000000000005);
                }

                player.clientVelocity = player.clientVelocity.add(vec3d3);
            }

            player.fluidHeight.put(tag, d);
            return bl2;
        }
    }

    public boolean isPushedByFluids() {
        return true;
    }
}
