package ac.boar.anticheat.prediction.engine.base;

import ac.boar.anticheat.prediction.engine.data.Vector;
import ac.boar.utils.math.Vec3d;

import java.util.List;

public interface PredictionEngine {
    List<Vector> gatherAllPossibilities();
    Vec3d jump(Vec3d vec3d);
    boolean canJump();
    void travel(Vec3d movementInput);

    default void apply003ToPossibilities(final List<Vector> vectors) {
        for (final Vector vector : vectors) {
            final Vec3d vec3d = vector.getVelocity();
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

            vector.setVelocity(new Vec3d(d, e, f));
        }
    }

    default void applyJumpingToPossibilities(List<Vector> vectors) {
        if (!canJump()) {
            return;
        }

        for (Vector vector : vectors) {
            vector.setVelocity(jump(vector.getVelocity()));
        }
    }
}
