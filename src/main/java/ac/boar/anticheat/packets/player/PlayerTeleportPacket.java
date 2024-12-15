package ac.boar.anticheat.packets.player;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.anticheat.utils.TeleportUtil;
import ac.boar.plugin.BoarPlugin;
import ac.boar.protocol.event.bedrock.BedrockPacketListener;
import ac.boar.protocol.event.bedrock.PacketReceivedEvent;
import ac.boar.protocol.event.bedrock.geyser.GeyserPacketListener;
import ac.boar.protocol.event.bedrock.geyser.GeyserSendEvent;
import ac.boar.utils.math.Vec3f;
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData;
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;

import java.util.Queue;

public class PlayerTeleportPacket implements BedrockPacketListener, GeyserPacketListener {
    @Override
    public void onPacketReceived(PacketReceivedEvent event) {
        final BoarPlayer player = event.getPlayer();
        if (!(event.getPacket() instanceof PlayerAuthInputPacket packet)) {
            return;
        }

        Queue<TeleportUtil.TeleportCache> queue = player.teleportUtil.getTeleportQueue();
        if (queue.isEmpty()) {
            return;
        }

        // We can't check for player.lastReceivedId == cache.getTransactionId() since bedrock teleport is different.
        // Instead of respond with an extra move packet like java, it just simply set position
        // and add HANDLE_TELEPORT to input data next tick to let us know that it accepted the teleport.
        // Which also means player will be in the position of the latest teleport they got and accept that one, not every teleport like Java.
        TeleportUtil.TeleportCache temp;
        TeleportUtil.TeleportCache cache = null;
        while ((temp = queue.peek()) != null) {
            if (player.lastReceivedId < temp.getTransactionId()) {
                break;
            }

            cache = queue.poll();
        }

        if (cache == null) {
            return;
        }

        // This is not precise as java, since it being sent this tick instead of right away (also because of floating point I think?), we can't check for 0
        // I will use 0.1 just to be safe, I have seen it reach 2e-6 in some cases, but I haven't test enough to know.
        double distance = packet.getPosition().distanceSquared(cache.getPosition().toVector3f());
        if ((packet.getInputData().contains(PlayerAuthInputData.HANDLE_TELEPORT)) && distance < 0.1) {
            BoarPlugin.LOGGER.info("Accepted teleport, d=" + distance);
            player.lastTickWasTeleport = true;
        } else {
            // This is not the latest teleport, just ignore this one, we only force player to accept the latest one.
            // We don't want to teleport player to old teleport position when they're supposed to teleport to the latest tone.
            if (!queue.isEmpty()) {
                return;
            }
            // Set player back to where they're supposed to be.
            player.teleportUtil.setbackTo(cache.getPosition());
        }
    }

    @Override
    public void onPacketSend(GeyserSendEvent event) {
        if (!(event.getPacket() instanceof MovePlayerPacket packet)) {
            return;
        }

        final BoarPlayer player = event.getPlayer();
        if (packet.getMode() == MovePlayerPacket.Mode.HEAD_ROTATION) {
            return;
        }

        if (player.runtimeEntityId != packet.getRuntimeEntityId()) {
            return;
        }

        if (packet.getMode() == MovePlayerPacket.Mode.TELEPORT) {
            BoarPlugin.LOGGER.info("Ground: "  + packet.isOnGround());
            player.teleportUtil.addTeleportToQueue(new Vec3f(packet.getPosition()), Vec3f.ZERO, event.isImmediate());
        }
    }
}
