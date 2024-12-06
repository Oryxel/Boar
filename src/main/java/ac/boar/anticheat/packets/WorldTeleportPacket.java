package ac.boar.anticheat.packets;

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

public class WorldTeleportPacket implements BedrockPacketListener, GeyserPacketListener {
    @Override
    public void onPacketReceived(PacketReceivedEvent event) {
        final BoarPlayer player = event.getPlayer();
        if (!(event.getPacket() instanceof PlayerAuthInputPacket)) {
            return;
        }

        final PlayerAuthInputPacket packet = (PlayerAuthInputPacket) event.getPacket();

        Queue<TeleportUtil.TeleportCache> queue = player.teleportUtil.getTeleportQueue();
        if (queue.isEmpty()) {
            return;
        }

        // We can't check for player.lastReceivedId == cache.getTransactionId() bedrock teleport seems to be different.
        // Player doesn't seem to respond right away, instead it just simply set position and add HANDLE_TELEPORT to next tick.
        // This seems to be the case after debugging, and also it seems like it what ViaBedrock does.
        // Which also means player will accept the latest teleport they got, not in order each by each like java!
        TeleportUtil.TeleportCache temp = null;
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
        if (packet.getInputData().contains(PlayerAuthInputData.HANDLE_TELEPORT) && distance < 0.1) {
            BoarPlugin.LOGGER.info("Accepted teleport, d=" + distance);
            player.lastTickWasTeleport = true;

            // Server don't know about this teleport, cancel it.
            if (cache.isSilent()) {
                event.setCancelled(true);
            }
        } else {
            // This is not the latest teleport, just ignore this one, we only force player to accept the latest one.
            // We don't want to teleport player to old teleport position when they're supposed to teleport to the latest tone.
            if (!queue.isEmpty()) {
                return;
            }
            // Set player back to where they're supposed to be position.
            player.teleportUtil.setbackTo(cache.getPosition());
        }
    }

    @Override
    public void onPacketSend(GeyserSendEvent event) {
        if (!(event.getPacket() instanceof MovePlayerPacket)) {
            return;
        }

        final BoarPlayer player = event.getPlayer();
        final MovePlayerPacket packet = (MovePlayerPacket) event.getPacket();
        if (packet.getMode() != MovePlayerPacket.Mode.TELEPORT && packet.getMode() != MovePlayerPacket.Mode.RESPAWN) {
            return;
        }

        if (player.getSession().getPlayerEntity().getGeyserId() != packet.getRuntimeEntityId()) {
            return;
        }

        if (packet.getMode() == MovePlayerPacket.Mode.TELEPORT) {
            player.teleportUtil.addTeleportToQueue(new Vec3f(packet.getPosition()), event.isImmediate(), false);
        }

        player.teleportUtil.lastKnowValid = new Vec3f(packet.getPosition());
        player.latencyUtil.addTransactionToQueue(player.lastSentId, () -> {
            player.queuedVelocities.clear();
            player.clientVelocity = player.predictedVelocity = Vec3f.ZERO;
        });
    }
}
