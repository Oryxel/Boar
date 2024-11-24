package ac.boar.anticheat.prediction.engine.base;

import ac.boar.anticheat.prediction.engine.data.Vector;
import ac.boar.anticheat.prediction.engine.data.VectorType;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.utils.math.Vec3d;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class PredictionEngine {
    protected final BoarPlayer player;

    protected abstract Vec3d jump(Vec3d vec3d);
    protected abstract boolean canJump();
    protected abstract void travel(Vec3d movementInput);

    public final List<Vector> gatherAllPossibilities() {
        List<Vector> vectors = new ArrayList<>();
        vectors.add(new Vector(player.clientVelocity, VectorType.NORMAL));
        addVelocityToPossibilities(vectors);

        apply003ToPossibilities(vectors);
        addJumpingToPossibilities(vectors);

        return vectors;
    }

    protected final void addVelocityToPossibilities(final List<Vector> vectors) {
        for (final Map.Entry<Long, Vec3d> entry : player.queuedVelocities.entrySet()) {
            final Vector vector = new Vector(entry.getValue(), VectorType.NORMAL);
            vector.setTransactionId(entry.getKey());
            vectors.add(vector);
        }
    }

    protected final void apply003ToPossibilities(final List<Vector> vectors) {
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

    protected final void addJumpingToPossibilities(List<Vector> vectors) {
        if (!canJump()) {
            return;
        }

        for (Vector vector : vectors) {
            vector.setVelocity(jump(vector.getVelocity()));
        }
    }
}
