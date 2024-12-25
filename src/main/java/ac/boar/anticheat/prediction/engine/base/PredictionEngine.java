package ac.boar.anticheat.prediction.engine.base;

import ac.boar.anticheat.check.api.Check;
import ac.boar.anticheat.check.api.impl.OffsetHandlerCheck;
import ac.boar.anticheat.prediction.engine.data.Vector;
import ac.boar.anticheat.prediction.engine.data.VectorType;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.anticheat.utils.collisions.Collisions;
import ac.boar.utils.MathUtil;
import ac.boar.utils.math.Vec3f;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
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
    protected abstract Vec3f jump(boolean sprinting, Vec3f vec3f);
    protected abstract boolean canJump();

    public final List<Vector> gatherAllPossibilities() {
        List<Vector> vectors = new ArrayList<>();
        vectors.add(new Vector(player.eotVelocity, VectorType.NORMAL));
        addVelocityToPossibilities(vectors);
        addJumpingToPossibilities(vectors);

        addTravelToPossibilities(vectors);
        return vectors;
    }

    public final void move() {
        List<Vector> possibilities = this.gatherAllPossibilities();
        Vec3f beforeCollision = Vec3f.ZERO, afterCollision = Vec3f.ZERO;
        double closetOffset = Double.MAX_VALUE;

        boolean movementSlowed = player.movementMultiplier.lengthSquared() > 1.0E-7;
        for (Vector vector : possibilities) {
            Vec3f bc = Collisions.adjustMovementForSneaking(player, vector.getVelocity());
            if (movementSlowed) {
                bc = bc.mul(player.movementMultiplier);
            }

            final Vec3f ac = Collisions.adjustMovementForCollisions(player, player.boundingBox, bc, true);

            double offset = ac.squaredDistanceTo(player.actualVelocity);
            if (offset <= closetOffset) {
                closetOffset = offset;
                player.closetVector = vector;
                afterCollision = ac;
                beforeCollision = bc;
            }

            if (vector.getType() == VectorType.VELOCITY) {
                player.postPredictionVelocities.put(vector.getTransactionId(), ac);
            }
        }

        player.movementMultiplier = Vec3f.ZERO;

        resetVelocities();

        boolean bl = afterCollision.x != beforeCollision.x;
        boolean bl2 = afterCollision.z != beforeCollision.z;
        player.verticalCollision = afterCollision.y != beforeCollision.y;
        player.horizontalCollision = bl || bl2;

        player.wasGround = player.onGround;
        player.onGround = beforeCollision.y < 0 && player.verticalCollision;
        player.predictedVelocity = afterCollision.clone();

        Vec3f clientVelocity = afterCollision.clone();
        double offset = afterCollision.distanceTo(player.actualVelocity);

        if (offset < 1e-4) {
            clientVelocity = player.actualVelocity.clone();
        }

        if (movementSlowed) {
            clientVelocity = Vec3f.ZERO;
        }

        if (bl) {
            clientVelocity.x = 0;
        }

        Vector3i lv4 = player.getLandingPos();
        BlockState lv5 = player.compensatedWorld.getBlockState(lv4);
        if (player.verticalCollision) {
            if (!player.sneaking && ((lv5.block() instanceof BedBlock) || lv5.is(Blocks.SLIME_BLOCK)) && beforeCollision.y < 0) {
                clientVelocity.y = -beforeCollision.y * (lv5.is(Blocks.SLIME_BLOCK) ? 1 : 0.75F);
            } else {
                clientVelocity.y = 0;
            }
        }

        if (bl2) {
            clientVelocity.z = 0;
        }

        float f = player.getVelocityMultiplier();
        player.eotVelocity = this.applyEndOfTick(clientVelocity.mul(f, 1, f));

        for (Map.Entry<Class<?>, Check> entry : player.checkHolder.entrySet()) {
            Check v = entry.getValue();
            if (v instanceof OffsetHandlerCheck) {
                ((OffsetHandlerCheck) v).onPredictionComplete(offset);
            }
        }

    }

    protected final Vec3f updateVelocity(Vec3f client, Vec3f movementInput, float speed) {
        return client.add(MathUtil.movementInputToVelocity(movementInput, speed, player.yaw));
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

    protected void addTravelToPossibilities(List<Vector> vectors) {
        if (player.uncertainSprinting) {
            final List<Vector> possibilities = new ArrayList<>();

            for (Vector old : vectors) {
                possibilities.add(new Vector(travel(false, old.getVelocity(), player.movementInput), old.getType(), old.getTransactionId()));

                final Vector vector1 = new Vector(travel(true, old.getVelocity(), player.movementInput), old.getType(), old.getTransactionId());
                vector1.setSprinting(true);
                possibilities.add(vector1);
            }

            vectors.clear();
            vectors.addAll(possibilities);
            return;
        }
        
        for (Vector vector : vectors) {
            vector.setVelocity(travel(player.sprinting, vector.getVelocity(), player.movementInput));
        }
    }

    protected void addJumpingToPossibilities(List<Vector> vectors) {
        if (!canJump()) {
            return;
        }

        if (player.uncertainSprinting) {
            final List<Vector> possibilities = new ArrayList<>();
            for (Vector old : vectors) {
                possibilities.add(new Vector(jump(false, old.getVelocity()), old.getType(), old.getTransactionId()));

                final Vector vector1 = new Vector(jump(true, old.getVelocity()), old.getType(), old.getTransactionId());
                vector1.setSprinting(true);
                possibilities.add(vector1);
            }
            vectors.clear();
            vectors.addAll(possibilities);
            return;
        }

        for (Vector vector : vectors) {
            vector.setVelocity(jump(player.sprinting, vector.getVelocity()));
        }
    }

    protected void addVelocityToPossibilities(final List<Vector> vectors) {
        for (final Map.Entry<Long, Vec3f> entry : player.queuedVelocities.entrySet()) {
            final Vector vector = new Vector(entry.getValue(), VectorType.VELOCITY, entry.getKey());
            vectors.add(vector);
        }
    }
}
