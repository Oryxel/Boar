package ac.boar.anticheat.prediction.engine;

import ac.boar.anticheat.data.StatusEffect;
import ac.boar.anticheat.prediction.engine.base.PredictionEngine;
import ac.boar.anticheat.prediction.engine.data.Vector;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.utils.MathUtil;
import ac.boar.utils.math.Vec3f;
import org.cloudburstmc.math.TrigMath;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.protocol.data.game.entity.Effect;

import java.util.ArrayList;
import java.util.List;

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
    protected void applyTravelToPossibilities(List<Vector> vectors) {
        final List<Vector> list = new ArrayList<>();

        // Is this my fault (maybe it is)? Sometimes player won't stop sprinting 3-4 ticks after sending STOP_SPRINTING.
        // Also in a BUNCH of cases (ex: slamming your head against the wall) sprinting going to desync.
        // Fine, let's allow player sprint if ticks since sprinting is < 6. and also let player choose to NOT sprint.
        for (Vector vector : vectors) {
            list.add(new Vector(travel(false, vector.getVelocity().clone(), player.movementInput), vector.getType(), vector.getTransactionId()));

            if (player.sinceSprinting < 6) {
                Vector vector1 = new Vector(travel(true, vector.getVelocity().clone(), player.movementInput), vector.getType(), vector.getTransactionId());
                vector1.setSprinting(true);
                list.add(vector1);
            }
        }

        vectors.clear();
        vectors.addAll(list);
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
        vec3F = applyClimbingSpeed(vec3F);

        return vec3F;
    }

    private Vec3f applyClimbingSpeed(Vec3f motion) {
        if (player.lastCanClimb) {
            // this.onLanding();
            float d = /* MathUtil.clamp(motion.x, -0.20000076F, 0.20000076F) */ motion.x;
            float e = /* MathUtil.clamp(motion.z, -0.20000076F, 0.20000076F) */ motion.z;
            float g = Math.max(motion.y, -0.20000076F);
            if (g < 0.0 && /* !this.getBlockStateAtPos().isOf(Blocks.SCAFFOLDING) && this.isHoldingOntoLadder() */ player.movementInput.z < 0 && (player.sneaking || player.lastSneaking)) {
                g = 0.0F;
            }

            motion = new Vec3f(d, g, e);
        }

        return motion;
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
