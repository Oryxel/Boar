package ac.boar.anticheat.prediction.ticker.base;

import ac.boar.anticheat.collision.Collision;
import ac.boar.anticheat.player.BoarPlayer;
import ac.boar.anticheat.prediction.engine.base.PredictionEngine;
import ac.boar.anticheat.prediction.engine.data.Vector;
import ac.boar.anticheat.prediction.engine.data.VectorType;
import ac.boar.anticheat.prediction.engine.impl.PredictionEngineElytra;
import ac.boar.anticheat.prediction.engine.impl.PredictionEngineNormal;
import ac.boar.anticheat.util.math.Vec3f;

import java.util.Iterator;
import java.util.Map;

public class LivingTicker extends EntityTicker {
    public LivingTicker(BoarPlayer player) {
        super(player);
    }

    @Override
    public void tick() {
        super.tick();
        tickMovement();

        player.boundingBox = player.dimensions.getBoxAt(player.x, player.y, player.z);
    }

    public void tickMovement() {
        player.movementInput = player.movementInput.multiply(0.98F);
        if (player.movementInput.x != 0 && player.movementInput.z != 0) {
            player.movementInput = player.movementInput.multiply(0.70710677F);
        }

        this.travel();
//        if (!this.getWorld().isClient() || this.isLogicalSideForUpdatingMovement()) {
//            this.tickBlockCollision();
//        }

    }

    public void travel() {
        final PredictionEngine engine;
        if (player.touchingWater || player.isInLava()) {
            engine = null;
        } else if (player.gliding) {
            engine = new PredictionEngineElytra(player);
        } else {
            engine = new PredictionEngineNormal(player);
        }

        if (player.wasFlying || player.flying) {
            player.eotVelocity = player.claimedEOT;
            player.predictedVelocity = player.actualVelocity;
            return;
        }

        // TODO: Implement unimplemented engine.
        if (engine == null) {
            return;
        }

        final boolean isThereMovementMultiplier = player.movementMultiplier.lengthSquared() > 1.0E-7;

        double closetOffset = Double.MAX_VALUE;
        Vec3f beforeCollision = Vec3f.ZERO, afterCollision = Vec3f.ZERO;
        for (final Vector vector : engine.gatherAllPossibilities()) {
            Vec3f movement = vector.getVelocity();
            if (isThereMovementMultiplier) {
                movement = movement.multiply(player.movementMultiplier);
            }

            // vec3f = this.adjustMovementForSneaking(movement, type);
            final Vec3f lv2 = Collision.adjustMovementForCollisions(player, movement, true);
            final double offset = lv2.distanceTo(player.actualVelocity);
            if (offset < closetOffset) {
                closetOffset = offset;
                beforeCollision = movement;
                afterCollision = lv2;
                player.closetVector = vector;
            }
        }

        player.predictedVelocity = afterCollision;
        boolean bl = beforeCollision.x != afterCollision.x;
        boolean bl2 = beforeCollision.z != afterCollision.z;
        player.horizontalCollision = bl || bl2;

        player.verticalCollision = beforeCollision.y != afterCollision.y;
        player.wasGround = player.onGround;
        player.onGround = player.verticalCollision && beforeCollision.y < 0;

        Vec3f eotVelocity = afterCollision.clone();
        if (isThereMovementMultiplier) {
            player.movementMultiplier = eotVelocity = Vec3f.ZERO;
        }

        if (player.horizontalCollision) {
            eotVelocity = new Vec3f(bl ? 0 : eotVelocity.x, eotVelocity.y, bl2 ? 0 : eotVelocity.z);
        }

//        Block lv7 = lv5.getBlock();
        if (player.verticalCollision) {
            eotVelocity.y = 0;
            // lv7.onEntityLand(this.getWorld(), this);
        }

        float f = player.getVelocityMultiplier();
        eotVelocity = eotVelocity.multiply(f, 1, f);
        player.eotVelocity = engine.applyEndOfTick(eotVelocity);

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
}
