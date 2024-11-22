package ac.boar.anticheat.packets;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.bedrock.BedrockPacketListener;
import ac.boar.protocol.event.bedrock.PacketReceivedEvent;
import ac.boar.utils.math.Vec3d;
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket;
import org.geysermc.geyser.session.GeyserSession;

public class MovementCheckRunner extends BedrockPacketListener {
    @Override
    public void onPacketReceived(PacketReceivedEvent event) {
        if (!(event.getPacket() instanceof MovePlayerPacket)) {
            return;
        }

        final BoarPlayer player = event.getPlayer();
        player.tick++;

        final MovePlayerPacket packet = (MovePlayerPacket) event.getPacket();
        if (packet.getTick() != packet.getTick()) {
            ((GeyserSession)player.getConnection()).disconnect("Invalid movement packet!");
            return;
        }

        player.lastX = player.tick == 0 ? player.x : packet.getPosition().getX();
        player.lastY = player.tick == 0 ? player.y : packet.getPosition().getY();
        player.lastZ = player.tick == 0 ? player.z : packet.getPosition().getZ();

        player.x = packet.getPosition().getX();
        player.y = packet.getPosition().getY();
        player.z = packet.getPosition().getZ();

        player.actualVelocity = new Vec3d(player.x - player.lastX, player.y - player.lastY, player.z - player.lastZ);
    }
}
