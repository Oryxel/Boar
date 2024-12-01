package ac.boar.anticheat.utils;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.utils.math.Vec3d;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket;
import org.geysermc.geyser.entity.EntityDefinitions;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
@Getter
public final class TeleportUtil {
    private final BoarPlayer player;
    private final Queue<TeleportCache> teleportQueue = new ConcurrentLinkedQueue<>();
    public Vec3d lastKnowValid = Vec3d.ZERO;

    public void addTeleportToQueue(Vec3d vec3d, boolean immediate, boolean silent) {
        this.player.sendTransaction(immediate);

        final TeleportCache teleportCache = new TeleportCache(vec3d, this.player.lastSentId, silent);
        this.teleportQueue.add(teleportCache);
    }

    public void setBackWithSimulation() {
        setBackWithVelocity(lastKnowValid.add(player.predictedVelocity), player.clientVelocity);
    }

    public void forceResyncToLastValid() {
        setbackTo(this.lastKnowValid);
    }

    public void setBackWithVelocity(Vec3d vec3d, Vec3d motion) {
        setbackTo(vec3d);
        sendVelocity(motion);
    }

    public void setbackTo(Vec3d vec3d) {
        this.addTeleportToQueue(vec3d, false,true);

        // Server won't know about this if we sent it like this, well they don't need to anyway.
        // As long as we handle thing correctly, it won't be a problem
        // If we do not however, server will likely set player back for 'Moved too quickly'
        // Also this (prob) going to prevent respawn tp, if there is one.

        MovePlayerPacket movePlayerPacket = new MovePlayerPacket();
        movePlayerPacket.setRuntimeEntityId(player.getSession().getPlayerEntity().getGeyserId());
        movePlayerPacket.setPosition(Vector3f.from(vec3d.x, vec3d.y + EntityDefinitions.PLAYER.offset(), vec3d.z));
        movePlayerPacket.setRotation(player.getSession().getPlayerEntity().getBedrockRotation());
        movePlayerPacket.setOnGround(player.onGround);
        movePlayerPacket.setMode(MovePlayerPacket.Mode.TELEPORT);
        movePlayerPacket.setTeleportationCause(MovePlayerPacket.TeleportationCause.BEHAVIOR);

        this.player.getBedrockSession().sendPacket(movePlayerPacket);
    }

    private void sendVelocity(Vec3d vec3d) {
        SetEntityMotionPacket motionPacket = new SetEntityMotionPacket();
        motionPacket.setRuntimeEntityId(player.getSession().getPlayerEntity().getGeyserId());
        motionPacket.setMotion(Vector3f.from(vec3d.x, vec3d.y, vec3d.z));

        player.sendTransaction();
        player.queuedVelocities.put(player.lastSentId, vec3d);
    }

    public TeleportCache getOldestTeleport() {
        if (this.teleportQueue.isEmpty()) {
            return null;
        }

        return this.teleportQueue.peek();
    }

    public boolean teleportInQueue() {
        return !this.teleportQueue.isEmpty() || !this.player.getSession().isSpawned();
    }

    @RequiredArgsConstructor
    @Getter
    @Setter
    public static class TeleportCache {
        private final Vec3d position;
        private final long transactionId;
        private final boolean silent;
    }
}
