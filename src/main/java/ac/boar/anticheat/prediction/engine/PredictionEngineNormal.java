package ac.boar.anticheat.prediction.engine;

import ac.boar.anticheat.prediction.engine.base.PredictionEngine;
import ac.boar.anticheat.prediction.engine.data.Vector;
import ac.boar.anticheat.prediction.engine.data.VectorType;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.utils.math.Vec3d;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.WorldManager;

import java.util.ArrayList;
import java.util.List;

public class PredictionEngineNormal implements PredictionEngine {
    private final BoarPlayer player;

    public PredictionEngineNormal(BoarPlayer player) {
        this.player = player;
    }

    @Override
    public List<Vector> gatherAllPossibilities() {
        List<Vector> vectors = new ArrayList<>();
        vectors.add(new Vector(player.clientVelocity, VectorType.NORMAL));

        apply003ToPossibilities(vectors);
        return vectors;
    }

    private void apply003ToPossibilities(final List<Vector> vectors) {
        for (final Vector vector : vectors) {
            final Vec3d vec3d = vector.getVec3d();
            double d = vec3d.x;
            double e = vec3d.y;
            double f = vec3d.z;
            if (Math.abs(vec3d.x) < 0.003) {
                d = 0.0;
            }

            if (Math.abs(vec3d.y) < 0.003) {
                e = 0.0;
            }

            if (Math.abs(vec3d.z) < 0.003) {
                f = 0.0;
            }

            vector.setVec3d(new Vec3d(d, e, f));
        }
    }

    @Override
    public void travel() {

    }

    private Vec3d travelNormal(Vec3d movementInput) {
//        // WorldManager worldManager = player.getSession().getGeyser().getWorldManager();
//
//        Vector3i blockPos = player.getVelocityAffectingPos();
//        float f = player.onGround ? /* worldManager.blockAt(player.getSession(), blockPos).block().getSlipperiness() */ 0.6F : 1.0F;
//        float g = f * 0.91F;
//        Vec3d vec3d = this.applyMovementInput(movementInput, f);
//        double d = vec3d.y;
////        StatusEffectInstance statusEffectInstance = this.getStatusEffect(StatusEffects.LEVITATION);
////        if (statusEffectInstance != null) {
////            d += (0.05 * (double)(statusEffectInstance.getAmplifier() + 1) - vec3d.y) * 0.2;
////        } else if (!this.getWorld().isChunkLoaded(blockPos)) {
////            if (this.getY() > (double)this.getWorld().getBottomY()) {
////                d = -0.1;
////            } else {
////                d = 0.0;
////            }
////        } else {
////            d -= player.getEffectiveGravity();
////        }
//
//        return new Vec3d(vec3d.x * g, d * 0.98D, vec3d.z * g);
        return Vec3d.ZERO;
    }

//    private Vec3d applyMovementInput(Vec3d movementInput, float slipperiness) {
//        this.updateVelocity(this.getMovementSpeed(slipperiness), movementInput);
//        this.setVelocity(this.applyClimbingSpeed(this.getVelocity()));
//        this.move(MovementType.SELF, this.getVelocity());
//        Vec3d vec3d = this.getVelocity();
//        if ((this.horizontalCollision || this.jumping) && (this.isClimbing() || this.getBlockStateAtPos().isOf(Blocks.POWDER_SNOW) && PowderSnowBlock.canWalkOnPowderSnow(this))) {
//            vec3d = new Vec3d(vec3d.x, 0.2, vec3d.z);
//        }
//
//        return vec3d;
//    }

    protected static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7) {
            return Vec3d.ZERO;
        } else {
            Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);
            double f = Math.sin(yaw * 0.017453292F), g = Math.cos(yaw * 0.017453292F);
            return new Vec3d(vec3d.x * g - vec3d.z * f, vec3d.y, vec3d.z * g + vec3d.x * f);
        }
    }


    @Override
    public Vec3d applyEndOfTick() {
        return null;
    }
}
