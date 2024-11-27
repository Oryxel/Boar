package ac.boar.anticheat.packets;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.anticheat.utils.TeleportUtil;
import ac.boar.plugin.BoarPlugin;
import ac.boar.protocol.event.bedrock.BedrockPacketListener;
import ac.boar.protocol.event.bedrock.PacketReceivedEvent;
import ac.boar.protocol.event.java.PacketListener;
import ac.boar.protocol.event.java.PacketSendEvent;
import ac.boar.utils.math.Vec3d;
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;
import org.geysermc.geyser.entity.EntityDefinitions;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PositionElement;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;

public class WorldTeleportPacket implements BedrockPacketListener, PacketListener {
    @Override
    public void onPacketReceived(PacketReceivedEvent event) {
        final BoarPlayer player = event.getPlayer();

        if (event.getPacket() instanceof PlayerAuthInputPacket) {
            final PlayerAuthInputPacket packet = (PlayerAuthInputPacket) event.getPacket();

            final TeleportUtil.TeleportCache cache = player.teleportUtil.getOldestTeleport();
            if (cache == null) {
                return;
            }

            if (packet.getInputData().contains(PlayerAuthInputData.HANDLE_TELEPORT)) {
                player.teleportUtil.getTeleportQueue().poll();

                if (cache.getTransactionId() == player.lastReceivedId) {
                    double distance = packet.getPosition().sub(0, EntityDefinitions.PLAYER.offset(), 0).distanceSquared(cache.getPosition().toVector3f());

                    if (distance > (cache.isRelative() ? 0.001 : 0)) {
                        if (player.teleportUtil.getTeleportQueue().isEmpty()) {
                            player.teleportUtil.setbackTo(cache.getPosition());
                        }
                    } else {
                        BoarPlugin.LOGGER.info("Accepted teleport!");
                        cache.setAccepted(true);
                        player.lastTickWasTeleport = true;
                    }
                    return;
                }
            }

            if (player.lastReceivedId <= cache.getTransactionId()) {
                return;
            }

            // Resync.....
            TeleportUtil.TeleportCache teleport;
            while ((teleport = player.teleportUtil.getTeleportQueue().peek()) != null) {
                if (player.lastReceivedId < teleport.getTransactionId()) {
                    break;
                }
                player.teleportUtil.getTeleportQueue().poll();
                if (teleport.isAccepted()) {
                    return;
                }

                if (player.teleportUtil.getTeleportQueue().isEmpty()) {
                    player.teleportUtil.setbackTo(cache.getPosition());
                }
            }
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!(event.getPacket() instanceof ClientboundPlayerPositionPacket)) {
            return;
        }

        final BoarPlayer player = event.getPlayer();
        final ClientboundPlayerPositionPacket packet = (ClientboundPlayerPositionPacket) event.getPacket();
        if (!player.getSession().isSpawned()) {
            player.clientVelocity = new Vec3d(0, 0, 0);
        }

        double newX = packet.getPosition().getX() + (packet.getRelatives().contains(PositionElement.X) ? player.x : 0);
        double newY = packet.getPosition().getY() + (packet.getRelatives().contains(PositionElement.Y) ? player.y : 0);
        double newZ = packet.getPosition().getZ() + (packet.getRelatives().contains(PositionElement.Z) ? player.z : 0);

        player.teleportUtil.addTeleportToQueue(new Vec3d(newX, newY, newZ), !packet.getRelatives().isEmpty());
        player.teleportUtil.lastKnowValid = new Vec3d(newX, newY, newZ);
    }
}
