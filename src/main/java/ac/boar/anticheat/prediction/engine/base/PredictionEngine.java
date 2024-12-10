package ac.boar.anticheat.prediction.engine.base;

import ac.boar.anticheat.check.api.Check;
import ac.boar.anticheat.check.api.impl.OffsetHandlerCheck;
import ac.boar.anticheat.prediction.engine.data.Vector;
import ac.boar.anticheat.prediction.engine.data.VectorType;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.anticheat.utils.collisions.Collisions;
import ac.boar.utils.math.Vec3f;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.type.BedBlock;
import org.geysermc.geyser.level.block.type.BlockState;

import java.util.ArrayList;
import java.util.Iterator;
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

    public final double move() {
        List<Vector> possibilities = this.gatherAllPossibilities();
        Vec3f beforeCollision = Vec3f.ZERO, afterCollision = Vec3f.ZERO;
        double closetOffset = Double.MAX_VALUE;
        for (Vector vector : possibilities) {
            final Vec3f bc = Collisions.adjustMovementForSneaking(player, vector.getVelocity());
            final Vec3f ac = Collisions.adjustMovementForCollisions(player, player.boundingBox, bc);

            double offset = ac.squaredDistanceTo(player.actualVelocity);
            if (offset < closetOffset) {
                closetOffset = offset;
                player.closetVector = vector;
                afterCollision = ac;
                beforeCollision = bc;
            }

            if (vector.getType() == VectorType.VELOCITY) {
                player.postPredictionVelocities.put(vector.getTransactionId(), ac);
            }
        }

        resetVelocities();

        player.collideX = afterCollision.x != beforeCollision.x;
        player.collideZ = afterCollision.z != beforeCollision.z;
        player.collideY = afterCollision.y != beforeCollision.y;

        player.lastGround = player.onGround;
        player.onGround = beforeCollision.y < 0 && player.collideY;
        player.predictedVelocity = afterCollision.clone();

        Vec3f clientVelocity = afterCollision.clone();
        double offset = afterCollision.distanceTo(player.actualVelocity);
        offset -= player.extraUncertainOffset;
        player.extraUncertainOffset = 0;

        if (offset < 1e-4) {
            clientVelocity = player.actualVelocity.clone();
        }

        if (player.collideX) {
            clientVelocity.x = 0;
        }

        Vector3i lv4 = player.getLandingPos();
        BlockState lv5 = player.getSession().getGeyser().getWorldManager().blockAt(player.getSession(), lv4);
        if (player.collideY) {
            if (!player.sneaking && ((lv5.block() instanceof BedBlock) || lv5.is(Blocks.SLIME_BLOCK)) && beforeCollision.y < 0) {
                clientVelocity.y = -beforeCollision.y * (lv5.is(Blocks.SLIME_BLOCK) ? 1 : 0.66F);
            } else {
                clientVelocity.y = 0;
            }
        }

        if (player.collideZ) {
            clientVelocity.z = 0;
        }

        player.clientVelocity = this.applyEndOfTick(clientVelocity);

        for (Map.Entry<Class, Check> entry : player.checkHolder.entrySet()) {
            Check v = entry.getValue();
            if (v instanceof OffsetHandlerCheck) {
                ((OffsetHandlerCheck) v).onPredictionComplete(offset);
            }
        }

        return offset;
    }

    private void resetVelocities() {
        if (player.closetVector.getType() == VectorType.VELOCITY) {
            Iterator<Map.Entry<Long, Vec3f>> iterator = player.queuedVelocities.entrySet().iterator();

            Map.Entry<Long, Vec3f> entry;
            while (iterator.hasNext() && (entry = iterator.next()) != null) {
                if (entry.getKey() > player.closetVector.getTransactionId()) {
                    break;
                } else {
                    iterator.remove();
                }
            }
        }
    }

    protected void applyTravelToPossibilities(final List<Vector> vectors) {
    }

    protected void addClimbingToPossibilities(final List<Vector> vectors) {
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

    protected void addVelocityToPossibilities(final List<Vector> vectors) {
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
