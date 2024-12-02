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

public class WorldTeleportPacket implements BedrockPacketListener, GeyserPacketListener {
    @Override
    public void onPacketReceived(PacketReceivedEvent event) {
        final BoarPlayer player = event.getPlayer();

        if (event.getPacket() instanceof PlayerAuthInputPacket) {
            final PlayerAuthInputPacket packet = (PlayerAuthInputPacket) event.getPacket();

            final TeleportUtil.TeleportCache cache = player.teleportUtil.getOldestTeleport();
            if (cache == null) {
                return;
            }

            if (packet.getInputData().contains(PlayerAuthInputData.HANDLE_TELEPORT) && player.lastReceivedId >= cache.getTransactionId()) {
                player.teleportUtil.getTeleportQueue().poll();

                double distance = packet.getPosition().distanceSquared(cache.getPosition().toVector3f());
                if (distance > 0) {
                    if (player.teleportUtil.getTeleportQueue().isEmpty()) {
                        player.teleportUtil.setbackTo(cache.getPosition());
                    }
                } else {
                    BoarPlugin.LOGGER.info("Accepted teleport!");
                    player.lastTickWasTeleport = true;

                    // Server don't know about this teleport, cancel it.
                    if (cache.isSilent()) {
                        event.setCancelled(true);
                    }
                }

                return;
            }

            if (player.lastReceivedId - cache.getTransactionId() > 5) {
                return;
            }

            // Resync.....
            TeleportUtil.TeleportCache teleport;
            while ((teleport = player.teleportUtil.getTeleportQueue().peek()) != null) {
                if (player.lastReceivedId < teleport.getTransactionId()) {
                    break;
                }

                player.teleportUtil.getTeleportQueue().poll();
                if (player.teleportUtil.getTeleportQueue().isEmpty()) {
                    player.teleportUtil.setbackTo(cache.getPosition());
                }
            }

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
