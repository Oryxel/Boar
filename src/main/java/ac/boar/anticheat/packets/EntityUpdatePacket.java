package ac.boar.anticheat.packets;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.geyser.GeyserPacketListener;
import ac.boar.protocol.event.geyser.GeyserSendEvent;
import ac.boar.utils.math.Vec3f;
import org.cloudburstmc.protocol.bedrock.packet.AddEntityPacket;
import org.cloudburstmc.protocol.bedrock.packet.MoveEntityAbsolutePacket;
import org.cloudburstmc.protocol.bedrock.packet.MoveEntityDeltaPacket;
import org.cloudburstmc.protocol.bedrock.packet.RemoveEntityPacket;

public class EntityUpdatePacket implements GeyserPacketListener {
    @Override
    public void onPacketSend(GeyserSendEvent event) {
        final BoarPlayer player = event.getPlayer();

        if (event.getPacket() instanceof AddEntityPacket) {
            player.compensatedEntity.addEntity((AddEntityPacket) event.getPacket());
        }

        if (event.getPacket() instanceof MoveEntityAbsolutePacket absolute) {
            player.compensatedEntity.queuePositionUpdate(event, absolute.getRuntimeEntityId(), new Vec3f(absolute.getPosition()));
        }

        if (event.getPacket() instanceof MoveEntityDeltaPacket delta) {
            player.compensatedEntity.queuePositionUpdate(event, delta.getRuntimeEntityId(), new Vec3f(delta.getX(), delta.getY(), delta.getZ()));
        }

        if (event.getPacket() instanceof RemoveEntityPacket remove) {
            player.compensatedEntity.removeEntity(remove.getUniqueEntityId());
        }
    }
}
