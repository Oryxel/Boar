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

    public abstract Vec3d travel(Vec3d vec3d, Vec3d movementInput);
    public abstract Vec3d applyEndOfTick(Vec3d vec3d);

    protected abstract Vec3d jump(Vec3d vec3d);
    protected abstract boolean canJump();

    public final List<Vector> gatherAllPossibilities() {
        List<Vector> vectors = new ArrayList<>();
        vectors.add(new Vector(player.clientVelocity, VectorType.NORMAL));
        addVelocityToPossibilities(vectors);
        addExplosionToPossibilities(vectors);

        addJumpingToPossibilities(vectors);

        return vectors;
    }

    // Normally it's actually addVelocity, but since geyser translate this to set motion so yeah.
    // Not entirely their fault, they can't track client velocity and add velocity, bedrock doesn't seem to support add motion either.
    // We can prob properly translate explosion using the prediction engine but eh not worth it.
    private void addExplosionToPossibilities(final List<Vector> vectors) {
        for (final Map.Entry<Long, Vec3d> entry : player.queuedExplosions.entrySet()) {
            final Vector vector = new Vector(entry.getValue(), VectorType.EXPLOSION);
            vector.setTransactionId(entry.getKey());
            vectors.add(vector);
        }
    }

    private void addVelocityToPossibilities(final List<Vector> vectors) {
        for (final Map.Entry<Long, Vec3d> entry : player.queuedVelocities.entrySet()) {
            final Vector vector = new Vector(entry.getValue(), VectorType.VELOCITY);
            vector.setTransactionId(entry.getKey());
            vectors.add(vector);
        }
    }

    private void addJumpingToPossibilities(List<Vector> vectors) {
        if (!canJump()) {
            return;
        }

        final List<Vector> list = new ArrayList<>();
        for (Vector vector : vectors) {
            Vector v = new Vector(jump(vector.getVelocity()), vector.getType());
            v.setTransactionId(vector.getTransactionId());
            list.add(vector);
            list.add(v);
        }

        vectors.clear();
        vectors.addAll(list);
    }
}
