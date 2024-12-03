package ac.boar.anticheat.prediction.engine;

import ac.boar.anticheat.prediction.engine.base.PredictionEngine;
import ac.boar.anticheat.prediction.engine.data.Vector;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.utils.MathUtil;
import ac.boar.utils.math.Vec3f;
import org.cloudburstmc.math.GenericMath;
import org.cloudburstmc.math.TrigMath;

import java.util.List;

public class PredictionEngineElytra extends PredictionEngine {
    public PredictionEngineElytra(BoarPlayer player) {
        super(player);
    }

    @Override
    protected void applyTravelToPossibilities(List<Vector> vectors) {
        for (Vector vector : vectors) {
            vector.setVelocity(travel(false, vector.getVelocity().clone(), player.movementInput));
        }
    }

    @Override
    public Vec3f travel(boolean sprinting, Vec3f client, Vec3f movementInput) {
        if (player.onGround) {
            player.gliding = false;
        }

        Vec3f oldVelocity = client.clone();
        Vec3f vec3d = MathUtil.getRotationVector(player.pitch, player.yaw);
        float f = player.pitch * 0.017453292F;
        float d = (float) GenericMath.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
        float e = oldVelocity.horizontalLength();
        float g = player.getEffectiveGravity();
        float h = MathUtil.square((float) Math.cos(f));
        oldVelocity = oldVelocity.add(0.0F, g * (-1.0F + h * 0.75F), 0.0F);
        float i;
        if (oldVelocity.y < 0.0 && d > 0.0) {
            i = oldVelocity.y * -0.1F * h;
            oldVelocity = oldVelocity.add(vec3d.x * i / d, i, vec3d.z * i / d);
        }

        if (f < 0.0F && d > 0.0) {
            i = e * (-TrigMath.sin(f)) * 0.04F;
            oldVelocity = oldVelocity.add(-vec3d.x * i / d, i * 3.2F, -vec3d.z * i / d);
        }

        if (d > 0.0) {
            oldVelocity = oldVelocity.add((vec3d.x / d * e - oldVelocity.x) * 0.1F, 0.0F, (vec3d.z / d * e - oldVelocity.z) * 0.1F);
        }

        return oldVelocity.mul(0.9900000095367432F, 0.9800000190734863F, 0.9900000095367432F);
    }

    @Override
    public Vec3f applyEndOfTick(Vec3f vec3F) {
        return vec3F;
    }

    @Override
    protected Vec3f jump(Vec3f vec3F) {
        return vec3F;
    }

    @Override
    protected boolean canJump() {
        return false;
    }
}
