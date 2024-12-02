package ac.boar.anticheat.prediction.engine;

import ac.boar.anticheat.data.StatusEffect;
import ac.boar.anticheat.prediction.engine.base.PredictionEngine;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.utils.math.Vec3f;
import org.cloudburstmc.math.TrigMath;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.protocol.data.game.entity.Effect;

public class PredictionEngineNormal extends PredictionEngine {
    public PredictionEngineNormal(BoarPlayer player) {
        super(player);
    }

    @Override
    public Vec3f jump(Vec3f client) {
        Vec3f vec3F = client.clone();
        float f = player.getJumpVelocity();
        if (!(f <= 1.0E-5F)) {
            vec3F = new Vec3f(vec3F.x, Math.max(f, vec3F.y), vec3F.z);
            if (player.sprinting) {
                float g = player.yaw * 0.017453292F;
                vec3F = vec3F.add(new Vec3f(-TrigMath.sin(g) * 0.2F, 0.0F, TrigMath.cos(g) * 0.2F));
            }
        }
        return vec3F;
    }

    @Override
    public boolean canJump() {
        return player.onGround;
    }

    @Override
    public Vec3f travel(boolean sprinting, Vec3f client, Vec3f movementInput) {
        Vector3i blockPos = player.getVelocityAffectingPos();
        float f = player.onGround ? /* worldManager.blockAt(player.getSession(), blockPos).block().getSlipperiness() */ 0.6F : 1.0F;
        return this.applyMovementInput(sprinting, client, movementInput, f);
    }

    @Override
    public Vec3f applyEndOfTick(Vec3f vec3F) {
        float f = player.lastGround ? /* worldManager.blockAt(player.getSession(), blockPos).block().getSlipperiness() */ 0.6F : 1.0F;
        float g = f * 0.91F;
        float d = vec3F.y;
        StatusEffect statusEffect = player.getStatusEffect(Effect.LEVITATION);
        if (statusEffect != null) {
            d += (0.05F * (statusEffect.getAmplifier() + 1) - vec3F.y) * 0.2F;
        } /* else if (!this.getWorld().isChunkLoaded(blockPos)) {
            if (this.getY() > (double)this.getWorld().getBottomY()) {
                d = -0.1;
            } else {
                d = 0.0;
            }
        } */ else {
            d -= player.getEffectiveGravity();
        }

        return new Vec3f(vec3F.x * g, d * 0.98F, vec3F.z * g);
    }

    private Vec3f applyMovementInput(boolean sprinting, Vec3f client, Vec3f movementInput, float slipperiness) {
        Vec3f vec3F = client.add(movementInputToVelocity(movementInput, player.getMovementSpeed(sprinting, slipperiness), player.yaw));
        // this.setVelocity(this.applyClimbingSpeed(this.getVelocity())); climbing...
        // this.move(MovementType.SELF, this.getVelocity()); // collision
//        if ((this.horizontalCollision || this.jumping) && (this.isClimbing() || this.getBlockStateAtPos().isOf(Blocks.POWDER_SNOW) && PowderSnowBlock.canWalkOnPowderSnow(this))) {
//            vec3f = new Vec3f(vec3f.x, 0.2, vec3f.z);
//        }

        return vec3F;
    }

    protected static Vec3f movementInputToVelocity(Vec3f movementInput, float speed, float yaw) {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7) {
            return Vec3f.ZERO;
        } else {
            Vec3f vec3F = (d > 1.0 ? movementInput.normalize() : movementInput).mul(speed);
            float f = TrigMath.sin(yaw * 0.017453292F), g = TrigMath.cos(yaw * 0.017453292F);
            return new Vec3f(vec3F.x * g - vec3F.z * f, vec3F.y, vec3F.z * g + vec3F.x * f);
        }
    }
}
