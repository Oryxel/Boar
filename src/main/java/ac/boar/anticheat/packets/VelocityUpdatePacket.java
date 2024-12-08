package ac.boar.anticheat.packets;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.bedrock.geyser.GeyserPacketListener;
import ac.boar.protocol.event.bedrock.geyser.GeyserSendEvent;
import ac.boar.utils.math.Vec3f;
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket;

public class VelocityUpdatePacket implements GeyserPacketListener {
    @Override
    public void onPacketSend(GeyserSendEvent event) {
        final BoarPlayer player = event.getPlayer();

        // Yes only this, there no packet for explosion (for bedrock), geyser also translate explosion directly to SetEntityMotionPacket
        // Not really how it works on java, but doesn't matter, not ours problems
        if (event.getPacket() instanceof SetEntityMotionPacket packet) {
            if (packet.getRuntimeEntityId() != player.getSession().getPlayerEntity().getGeyserId()) {
                return;
            }

            player.queuedVelocities.put(player.lastSentId + 1, new Vec3f(packet.getMotion()));
            event.getPostTasks().add(player::sendTransaction);
        }
    }
}
