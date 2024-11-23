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
        final BoarPlayer player = event.player();

        if (event.packet() instanceof ClientboundAddEntityPacket) {
            player.compensatedEntity.addEntity((ClientboundAddEntityPacket) event.packet());
        }

        if (event.packet() instanceof ClientboundRemoveEntitiesPacket) {
            final ClientboundRemoveEntitiesPacket packet = (ClientboundRemoveEntitiesPacket) event.packet();
            for (int i : packet.getEntityIds()) {
                player.compensatedEntity.removeEntity(i);
            }
        }

        if (event.packet() instanceof ClientboundEntityPositionSyncPacket) {
            final ClientboundEntityPositionSyncPacket packet = (ClientboundEntityPositionSyncPacket) event.packet();
            player.compensatedEntity.queuePositionUpdate(packet.getId(), new Vec3d(packet.getPosition().getX(), packet.getPosition().getY(), packet.getPosition().getZ()));
        }

        if (event.packet() instanceof ClientboundMoveEntityPosRotPacket) {
            final ClientboundMoveEntityPosRotPacket packet = (ClientboundMoveEntityPosRotPacket) event.packet();
            player.compensatedEntity.queueRelativeUpdate(packet.getEntityId(), packet.getMoveX(), packet.getMoveY(), packet.getMoveZ());
        }

        if (event.packet() instanceof ClientboundMoveEntityPosPacket) {
            final ClientboundMoveEntityPosPacket packet = (ClientboundMoveEntityPosPacket) event.packet();
            player.compensatedEntity.queueRelativeUpdate(packet.getEntityId(), packet.getMoveX(), packet.getMoveY(), packet.getMoveZ());
        }
    }
}
