package ac.boar.anticheat.utils;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.utils.math.Vec3f;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.PredictionType;
import org.cloudburstmc.protocol.bedrock.packet.CorrectPlayerMovePredictionPacket;
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
@Getter
public final class TeleportUtil {
    private final BoarPlayer player;
    private final Queue<TeleportCache> teleportQueue = new ConcurrentLinkedQueue<>();
    public Vec3f lastKnowValid = Vec3f.ZERO;

    public void addTeleportToQueue(Vec3f vec3f, Vec3f velocity, boolean immediate, boolean simulation) {
        this.player.sendTransaction(immediate);

        final TeleportCache teleportCache = new TeleportCache(vec3f, this.player.lastSentId, simulation);
        this.teleportQueue.add(teleportCache);

        player.teleportUtil.lastKnowValid = new Vec3f(vec3f.toVector3f());
        player.latencyUtil.addTransactionToQueue(player.lastSentId, () -> {
            player.queuedVelocities.clear();
            player.clientVelocity = velocity;
            System.out.println("reset!");
        });
    }

    public void setbackWithVelocity(long id) {
        if (teleportInQueue()) {
            return;
        }

        Vec3f vec3f = player.postPredictionVelocities.get(id);

        CorrectPlayerMovePredictionPacket packet = new CorrectPlayerMovePredictionPacket();
        packet.setDelta(vec3f.toVector3f());
        packet.setPosition(lastKnowValid.toVector3f());
        packet.setTick(player.tick);
        packet.setOnGround(player.onGround);
        packet.setPredictionType(PredictionType.PLAYER);
        this.addTeleportToQueue(lastKnowValid, vec3f, true, true);

        this.player.getBedrockSession().sendPacketImmediately(packet);
    }

    public void setbackTo(Vec3f vec3f) {
        // Server won't know about this if we sent it like this, well they don't need to anyway.
        // As long as we handle thing correctly, it won't be a problem
        // If we do not however, server will likely set player back for 'Moved too quickly'
        // Also this (prob) going to prevent respawn tp, if there is one.

        MovePlayerPacket movePlayerPacket = new MovePlayerPacket();
        movePlayerPacket.setRuntimeEntityId(player.runtimeEntityId);
        movePlayerPacket.setPosition(Vector3f.from(vec3f.x, vec3f.y, vec3f.z));
        movePlayerPacket.setRotation(player.bedrockRotation);
        movePlayerPacket.setOnGround(player.onGround);
        movePlayerPacket.setMode(MovePlayerPacket.Mode.TELEPORT);
        movePlayerPacket.setTeleportationCause(MovePlayerPacket.TeleportationCause.BEHAVIOR);
        this.addTeleportToQueue(vec3f, Vec3f.ZERO, true, false);

        this.player.getBedrockSession().sendPacketImmediately(movePlayerPacket);
    }

    public boolean teleportInQueue() {
        return !this.teleportQueue.isEmpty();
    }

    @RequiredArgsConstructor
    @Getter
    @Setter
    public static class TeleportCache {
        private final Vec3f position;
        private final long transactionId;
        private final boolean simulation;
    }
}
