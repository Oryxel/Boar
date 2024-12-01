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

    public abstract Vec3d travel(boolean sprinting, Vec3d vec3d, Vec3d movementInput);
    public abstract Vec3d applyEndOfTick(Vec3d vec3d);

    protected abstract Vec3d jump(Vec3d vec3d);
    protected abstract boolean canJump();

    public final List<Vector> gatherAllPossibilities() {
        List<Vector> vectors = new ArrayList<>();
        vectors.add(new Vector(player.clientVelocity, VectorType.NORMAL));
        addVelocityToPossibilities(vectors);
        addJumpingToPossibilities(vectors);

        applyTravelToPossibilities(vectors);

        return vectors;
    }

    private void applyTravelToPossibilities(final List<Vector> vectors) {
        final List<Vector> list = new ArrayList<>();

        // Is this my fault? Sometimes player won't stop sprinting 3-4 ticks after sending STOP_SPRINTING.
        // Also in a BUNCH of cases (ex: slamming your head against the wall) sprinting going to desync?
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

    private void addVelocityToPossibilities(final List<Vector> vectors) {
        for (final Map.Entry<Long, Vec3d> entry : player.queuedVelocities.entrySet()) {
            final Vector vector = new Vector(entry.getValue(), VectorType.VELOCITY, entry.getKey());
            vectors.add(vector);
        }
    }

    private void addJumpingToPossibilities(List<Vector> vectors) {
        if (!canJump()) {
            return;
        }

        final List<Vector> list = new ArrayList<>();
        for (Vector vector : vectors) {
            list.add(vector);
            list.add(new Vector(jump(vector.getVelocity()), vector.getType(), vector.getTransactionId()));
        }

        vectors.clear();
        vectors.addAll(list);
    }
}
