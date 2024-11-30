package ac.boar.anticheat.prediction.engine;

import ac.boar.anticheat.data.StatusEffect;
import ac.boar.anticheat.prediction.engine.base.PredictionEngine;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.utils.math.Vec3d;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.protocol.data.game.entity.Effect;

public class PredictionEngineNormal extends PredictionEngine {
    public PredictionEngineNormal(BoarPlayer player) {
        super(player);
    }

    @Override
    public Vec3d jump(Vec3d client) {
        Vec3d vec3d = client.clone();
        float f = player.getJumpVelocity();
        if (!(f <= 1.0E-5F)) {
            vec3d = new Vec3d(vec3d.x, Math.max(f, vec3d.y), vec3d.z);
            if (player.sprinting) {
                float g = player.yaw * 0.017453292F;
                vec3d = vec3d.add(new Vec3d(-Math.sin(g) * 0.2, 0.0, Math.cos(g) * 0.2));
            }
        }
        return vec3d;
    }

    @Override
    public boolean canJump() {
        return player.onGround;
    }

    @Override
    public Vec3d travel(Vec3d client, Vec3d movementInput) {
        Vector3i blockPos = player.getVelocityAffectingPos();
        float f = player.onGround ? /* worldManager.blockAt(player.getSession(), blockPos).block().getSlipperiness() */ 0.6F : 1.0F;
        return this.applyMovementInput(client, movementInput, f);
    }

    @Override
    public Vec3d applyEndOfTick(Vec3d vec3d) {
        float f = player.onGround ? /* worldManager.blockAt(player.getSession(), blockPos).block().getSlipperiness() */ 0.6F : 1.0F;
        float g = f * 0.91F;
        double d = vec3d.y;
        StatusEffect statusEffect = player.getStatusEffect(Effect.LEVITATION);
        if (statusEffect != null) {
            d += (0.05 * (double)(statusEffect.getAmplifier() + 1) - vec3d.y) * 0.2;
        } /* else if (!this.getWorld().isChunkLoaded(blockPos)) {
            if (this.getY() > (double)this.getWorld().getBottomY()) {
                d = -0.1;
            } else {
                d = 0.0;
            }
        } */ else {
            d -= player.getEffectiveGravity();
        }

        return new Vec3d(vec3d.x * g, d * 0.98D, vec3d.z * g);
    }

    private Vec3d applyMovementInput(Vec3d client, Vec3d movementInput, float slipperiness) {
        Vec3d vec3d = client.add(movementInputToVelocity(movementInput, player.getMovementSpeed(slipperiness), player.yaw));
        // this.setVelocity(this.applyClimbingSpeed(this.getVelocity())); climbing...
        // this.move(MovementType.SELF, this.getVelocity()); // collision
//        if ((this.horizontalCollision || this.jumping) && (this.isClimbing() || this.getBlockStateAtPos().isOf(Blocks.POWDER_SNOW) && PowderSnowBlock.canWalkOnPowderSnow(this))) {
//            vec3d = new Vec3d(vec3d.x, 0.2, vec3d.z);
//        }

        return vec3d;
    }

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
}
