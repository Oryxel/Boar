package ac.boar.anticheat.packets;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.anticheat.utils.TeleportUtil;
import ac.boar.protocol.event.bedrock.BedrockPacketListener;
import ac.boar.protocol.event.bedrock.PacketReceivedEvent;
import ac.boar.protocol.event.java.PacketListener;
import ac.boar.protocol.event.java.PacketSendEvent;
import ac.boar.utils.math.Vec3d;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.PlayerActionType;
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerActionPacket;
import org.geysermc.geyser.entity.EntityDefinitions;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PositionElement;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;

public class WorldTeleportPacket implements BedrockPacketListener, PacketListener {
    @Override
    public void onPacketReceived(PacketReceivedEvent event) {
        final BoarPlayer player = event.getPlayer();

        if (event.getPacket() instanceof PlayerActionPacket) {
            if (((PlayerActionPacket) event.getPacket()).getAction() != PlayerActionType.HANDLED_TELEPORT) {
                return;
            }

            final TeleportUtil.TeleportCache cache = player.teleportUtil.getOldestTeleport();
            if (cache == null) {
                return;
            }

            if (cache.getTransactionId() == player.lastReceivedId) {
                player.teleportUtil.acceptTeleportBeforehand = true;
            }
        }

        if (event.getPacket() instanceof MovePlayerPacket) {
            final MovePlayerPacket packet = (MovePlayerPacket) event.getPacket();

            final TeleportUtil.TeleportCache cache = player.teleportUtil.getOldestTeleport();
            if (cache == null) {
                return;
            }

            if (player.teleportUtil.acceptTeleportBeforehand) {
                player.teleportUtil.getTeleportQueue().poll();

                Vector3f cachePosition = Vector3f.from(cache.getPosition().x, cache.getPosition().y, cache.getPosition().z);
                if (packet.getPosition().distanceSquared(cachePosition) >= (cache.isRelative() ? 0.001 : 0)) {
                    if (player.teleportUtil.getTeleportQueue().isEmpty()) {
                        player.teleportUtil.setbackTo(cache.getPosition());
                    }
                } else {
                    cache.setAccepted(true);
                    player.lastTickWasTeleport = true;
                }
                return;
            }

            if (player.teleportUtil.teleportInQueue()) {
                event.setCancelled(true);
                return;
            }

            if (player.lastReceivedId - cache.getTransactionId() > 1) {
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
        if (!(event.packet() instanceof ClientboundPlayerPositionPacket)) {
            return;
        }

        final BoarPlayer player = event.player();
        final ClientboundPlayerPositionPacket packet = (ClientboundPlayerPositionPacket) event.packet();
        if (!player.getSession().isSpawned()) {
            player.clientVelocity = new Vec3d(0, 0, 0);
        }

        double newX = packet.getPosition().getX() + (packet.getRelatives().contains(PositionElement.X) ? player.x : 0);
        double newY = packet.getPosition().getY() + (packet.getRelatives().contains(PositionElement.Y) ? player.y - EntityDefinitions.PLAYER.offset() : 0);
        double newZ = packet.getPosition().getZ() + (packet.getRelatives().contains(PositionElement.Z) ? player.z : 0);

        player.teleportUtil.addTeleportToQueue(new Vec3d(newX, newY, newZ), !packet.getRelatives().isEmpty());
        player.teleportUtil.lastKnowValid = new Vec3d(newX, newY, newZ);
    }
}
