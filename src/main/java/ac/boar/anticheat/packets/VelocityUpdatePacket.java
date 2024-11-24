package ac.boar.anticheat.packets;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.java.PacketListener;
import ac.boar.protocol.event.java.PacketSendEvent;
import ac.boar.utils.math.Vec3d;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetEntityMotionPacket;

public class VelocityUpdatePacket implements PacketListener {
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!(event.getPacket() instanceof ClientboundSetEntityMotionPacket)) {
            return;
        }

        final BoarPlayer player = event.getPlayer();
        final ClientboundSetEntityMotionPacket packet = (ClientboundSetEntityMotionPacket) event.getPacket();
        if (packet.getEntityId() != player.getSession().getPlayerEntity().getEntityId()) {
            return;
        }

        player.sendTransaction();
        player.queuedVelocities.put(player.lastSentId + 1, new Vec3d(packet.getMotionX(), packet.getMotionY(), packet.getMotionZ()));
        event.getPostTasks().add(player::sendTransaction);
    }
}
