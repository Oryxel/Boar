package ac.boar.anticheat.packets;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.java.PacketListener;
import ac.boar.protocol.event.java.PacketSendEvent;
import ac.boar.utils.math.Vec3d;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundEntityPositionSyncPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundMoveEntityPosPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundMoveEntityPosRotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRemoveEntitiesPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddEntityPacket;

public class EntityUpdatePacket implements PacketListener {
    @Override
    public void onPacketSend(PacketSendEvent event) {
        final BoarPlayer player = event.getPlayer();

        if (event.getPacket() instanceof ClientboundAddEntityPacket) {
            player.compensatedEntity.addEntity((ClientboundAddEntityPacket) event.getPacket());
        }

        if (event.getPacket() instanceof ClientboundRemoveEntitiesPacket) {
            final ClientboundRemoveEntitiesPacket packet = (ClientboundRemoveEntitiesPacket) event.getPacket();
            for (int i : packet.getEntityIds()) {
                player.compensatedEntity.removeEntity(i);
            }
        }

        if (event.getPacket() instanceof ClientboundEntityPositionSyncPacket) {
            final ClientboundEntityPositionSyncPacket packet = (ClientboundEntityPositionSyncPacket) event.getPacket();
            player.compensatedEntity.queuePositionUpdate(event, packet.getId(), new Vec3d(packet.getPosition().getX(), packet.getPosition().getY(), packet.getPosition().getZ()));
        }

        if (event.getPacket() instanceof ClientboundMoveEntityPosRotPacket) {
            final ClientboundMoveEntityPosRotPacket packet = (ClientboundMoveEntityPosRotPacket) event.getPacket();
            player.compensatedEntity.queueRelativeUpdate(event, packet.getEntityId(), packet.getMoveX(), packet.getMoveY(), packet.getMoveZ());
        }

        if (event.getPacket() instanceof ClientboundMoveEntityPosPacket) {
            final ClientboundMoveEntityPosPacket packet = (ClientboundMoveEntityPosPacket) event.getPacket();
            player.compensatedEntity.queueRelativeUpdate(event, packet.getEntityId(), packet.getMoveX(), packet.getMoveY(), packet.getMoveZ());
        }
    }
}
