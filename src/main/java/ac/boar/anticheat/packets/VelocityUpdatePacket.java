package ac.boar.anticheat.packets;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.java.PacketListener;
import ac.boar.protocol.event.java.PacketSendEvent;
import ac.boar.utils.math.Vec3d;
import org.cloudburstmc.math.vector.Vector3d;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetEntityMotionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundExplodePacket;

public class VelocityUpdatePacket implements PacketListener {
    @Override
    public void onPacketSend(PacketSendEvent event) {
        final BoarPlayer player = event.getPlayer();
        if (event.getPacket() instanceof ClientboundSetEntityMotionPacket) {
            final ClientboundSetEntityMotionPacket packet = (ClientboundSetEntityMotionPacket) event.getPacket();
            if (packet.getEntityId() != player.getSession().getPlayerEntity().getEntityId()) {
                return;
            }

            player.queuedVelocities.put(player.lastSentId + 1, new Vec3d(packet.getMotionX(), packet.getMotionY(), packet.getMotionZ()));
            event.getPostTasks().add(player::sendTransaction);
        }

        if (event.getPacket() instanceof ClientboundExplodePacket) {
            final ClientboundExplodePacket packet = (ClientboundExplodePacket) event.getPacket();

            final Vector3d vector3d = packet.getPlayerKnockback();
            player.queuedExplosions.put(player.lastSentId + 1, new Vec3d(vector3d.getX(), vector3d.getY(), vector3d.getZ()));
            event.getPostTasks().add(player::sendTransaction);
        }
    }
}
