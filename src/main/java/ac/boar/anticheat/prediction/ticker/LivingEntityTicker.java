package ac.boar.anticheat.prediction.ticker;

import ac.boar.anticheat.check.api.Check;
import ac.boar.anticheat.check.api.impl.OffsetHandlerCheck;
import ac.boar.anticheat.prediction.engine.PredictionEngineElytra;
import ac.boar.anticheat.prediction.engine.PredictionEngineNormal;
import ac.boar.anticheat.prediction.engine.base.PredictionEngine;
import ac.boar.anticheat.prediction.engine.data.Vector;
import ac.boar.anticheat.prediction.engine.data.VectorType;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.anticheat.utils.BlockUtil;
import ac.boar.anticheat.utils.collisions.Collisions;
import ac.boar.utils.math.Vec3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.Ability;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.type.BedBlock;
import org.geysermc.geyser.level.block.type.BlockState;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LivingEntityTicker extends EntityTicker {
    public LivingEntityTicker(BoarPlayer player) {
        super(player);
    }

    @Override
    public void tick() {
        super.tick();
        tickMovement();
        tickBlockCollision();
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
            player.movementInput = player.movementInput.mul(0.70710677F);
        }

        List<Vector> possibilities = engine.gatherAllPossibilities();
        Vec3f beforeCollision = Vec3f.ZERO, afterCollision = Vec3f.ZERO;
        double closetOffset = Double.MAX_VALUE;
        for (Vector vector : possibilities) {
            final Vec3f bc = Collisions.adjustMovementForSneaking(player, vector.getVelocity());
            final Vec3f ac = Collisions.adjustMovementForCollisions(player, player.boundingBox, bc);

            double offset = ac.squaredDistanceTo(player.actualVelocity);
            if (offset < closetOffset) {
                closetOffset = offset;
                player.closetVector = vector;
                afterCollision = ac;
                beforeCollision = bc;
            }

            if (vector.getType() == VectorType.VELOCITY) {
                player.postPredictionVelocities.put(vector.getTransactionId(), ac);
            }
        }

        resetVelocities();

        player.collideX = afterCollision.x != beforeCollision.x;
        player.collideZ = afterCollision.z != beforeCollision.z;
        player.collideY = afterCollision.y != beforeCollision.y;

        player.lastGround = player.onGround;
        player.onGround = beforeCollision.y < 0 && player.collideY;
        player.predictedVelocity = afterCollision.clone();

        Vec3f clientVelocity = afterCollision.clone();
        double offset = afterCollision.distanceTo(player.actualVelocity);
        offset -= player.extraUncertainOffset;
        player.extraUncertainOffset = 0;

        if (offset < 1e-4) {
            clientVelocity = player.actualVelocity.clone();
        }

        if (player.collideX) {
            clientVelocity.x = 0;
        }

        Vector3i lv4 = player.getLandingPos();
        BlockState lv5 = player.getSession().getGeyser().getWorldManager().blockAt(player.getSession(), lv4);
        if (player.collideY) {
            if (!player.sneaking && ((lv5.block() instanceof BedBlock) || lv5.is(Blocks.SLIME_BLOCK)) && clientVelocity.y < 0) {
                clientVelocity.y = -clientVelocity.y * (lv5.is(Blocks.SLIME_BLOCK) ? 1 : 0.66F);
            } else {
                clientVelocity.y = 0;
            }
        }

        if (player.collideZ) {
            clientVelocity.z = 0;
        }

        player.clientVelocity = engine.applyEndOfTick(clientVelocity);

        for (Map.Entry<Class, Check> entry : player.checkHolder.entrySet()) {
            Check v = entry.getValue();
            if (v instanceof OffsetHandlerCheck) {
                ((OffsetHandlerCheck) v).onPredictionComplete(offset);
            }
        }
    }

    private void resetVelocities() {
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
    }

    private void tickBlockCollision() {
        if (player.onGround) {
            Vector3i lv = player.getLandingPos();
            BlockState lv2 = player.getSession().getGeyser().getWorldManager().blockAt(player.getSession(), lv);

            BlockUtil.onSteppedOn(player, lv, lv2);
        }
    }
}
