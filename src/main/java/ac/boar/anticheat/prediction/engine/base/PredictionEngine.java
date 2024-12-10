package ac.boar.anticheat.prediction.engine.base;

import ac.boar.anticheat.prediction.engine.data.Vector;
import ac.boar.anticheat.prediction.engine.data.VectorType;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.utils.math.Vec3f;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class PredictionEngine {
    protected final BoarPlayer player;

    public abstract Vec3f travel(boolean sprinting, Vec3f vec3f, Vec3f movementInput);

    public abstract Vec3f applyEndOfTick(Vec3f vec3f);

    protected abstract Vec3f jump(Vec3f vec3f);

    protected abstract boolean canJump();

    public final List<Vector> gatherAllPossibilities() {
        List<Vector> vectors = new ArrayList<>();
        vectors.add(new Vector(player.clientVelocity, VectorType.NORMAL));
        addVelocityToPossibilities(vectors);
        addJumpingToPossibilities(vectors);

        applyTravelToPossibilities(vectors);
        addClimbingToPossibilities(vectors);

        return vectors;
    }

    protected void applyTravelToPossibilities(final List<Vector> vectors) {
    }

    private void addClimbingToPossibilities(final List<Vector> vectors) {
        if (!player.lastCanClimb && !player.canClimb) {
            return;
        }

        final List<Vector> list = new ArrayList<>();
        for (Vector vector : vectors) {
            list.add(vector);

            Vector vector1 = vector.clone();
            vector1.setVelocity(new Vec3f(vector1.getVelocity().x, 0.20000076F, vector1.getVelocity().z));
            list.add(vector1);
        }

        vectors.clear();
        vectors.addAll(list);
    }

    private void addVelocityToPossibilities(final List<Vector> vectors) {
        for (final Map.Entry<Long, Vec3f> entry : player.queuedVelocities.entrySet()) {
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
