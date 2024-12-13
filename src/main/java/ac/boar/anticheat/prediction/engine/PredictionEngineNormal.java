package ac.boar.anticheat.prediction.engine;

import ac.boar.anticheat.data.StatusEffect;
import ac.boar.anticheat.prediction.engine.base.PredictionEngine;
import ac.boar.anticheat.prediction.engine.data.Vector;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.anticheat.utils.BlockUtil;
import ac.boar.utils.math.Vec3f;
import org.cloudburstmc.math.TrigMath;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.mcprotocollib.protocol.data.game.entity.Effect;

import java.util.ArrayList;
import java.util.List;

public class PredictionEngineNormal extends PredictionEngine {
    public PredictionEngineNormal(BoarPlayer player) {
        super(player);
    }

    @Override
    public Vec3f jump(Vec3f client) {
        Vec3f vec3f = client.clone();
        float f = player.getJumpVelocity();
        if (!(f <= 1.0E-5F)) {
            vec3f = new Vec3f(vec3f.x, Math.max(f, vec3f.y), vec3f.z);
            if (player.sprinting) {
                float g = player.yaw * 0.017453292F;
                vec3f = vec3f.add(new Vec3f(-TrigMath.sin(g) * 0.2F, 0.0F, TrigMath.cos(g) * 0.2F));
            }
        }
        return vec3f;
    }

    @Override
    public boolean canJump() {
        return player.inputData.contains(PlayerAuthInputData.START_JUMPING);
    }

    @Override
    public Vec3f travel(boolean sprinting, Vec3f client, Vec3f movementInput) {
        Vector3i blockPos = player.getVelocityAffectingPos();
        float slipperiness = BlockUtil.getBlockSlipperiness(player.getSession().getGeyser().getWorldManager().blockAt(player.getSession(), blockPos));
        float f = player.onGround ? slipperiness : 1.0F;
        return this.applyMovementInput(sprinting, client, movementInput, f);
    }

    @Override
    public Vec3f applyEndOfTick(Vec3f vec3f) {
        Vector3i blockPos = player.getVelocityAffectingPos();
        float slipperiness = BlockUtil.getBlockSlipperiness(player.getSession().getGeyser().getWorldManager().blockAt(player.getSession(), blockPos));
        float f = player.lastGround ? slipperiness : 1.0F;
        float g = f * 0.91F;
        float d = vec3f.y;
        StatusEffect statusEffect = player.getStatusEffect(Effect.LEVITATION);
        if (statusEffect != null) {
            d += (0.05F * (statusEffect.getAmplifier() + 1) - vec3f.y) * 0.2F;
        } /* else if (!this.getWorld().isChunkLoaded(blockPos)) {
            if (this.getY() > (double)this.getWorld().getBottomY()) {
                d = -0.1;
            } else {
                d = 0.0;
            }
        } */ else {
            d -= player.getEffectiveGravity();
        }

        return new Vec3f(vec3f.x * g, d * 0.98F, vec3f.z * g);
    }

    private Vec3f applyMovementInput(boolean sprinting, Vec3f client, Vec3f movementInput, float slipperiness) {
        Vec3f vec3f = updateVelocity(client, movementInput, player.getMovementSpeed(sprinting, slipperiness));
        vec3f = applyClimbingSpeed(vec3f);

        return vec3f;
    }

    private Vec3f applyClimbingSpeed(Vec3f motion) {
        if (player.canClimb && player.climbingSpeed > 0.2) {
            // this.onLanding();
            float d = /* MathUtil.clamp(motion.x, -0.20000076F, 0.20000076F) */ motion.x;
            float e = /* MathUtil.clamp(motion.z, -0.20000076F, 0.20000076F) */ motion.z;
            float g = Math.max(motion.y, -0.20000076F);
            if (g < 0.0 && !player.inputData.contains(PlayerAuthInputData.WANT_UP) && (player.sneaking || player.lastSneaking)) {
                g = 0.0F;
            }

            motion = new Vec3f(d, g, e);
        }

        return motion;
    }
}
