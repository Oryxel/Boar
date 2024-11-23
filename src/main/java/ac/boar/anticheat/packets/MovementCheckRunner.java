package ac.boar.anticheat.packets;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.bedrock.BedrockPacketListener;
import ac.boar.protocol.event.bedrock.PacketReceivedEvent;
import ac.boar.utils.math.Vec3d;
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;
import org.geysermc.geyser.entity.EntityDefinitions;

public class MovementCheckRunner implements BedrockPacketListener {
    @Override
    public void onPacketReceived(PacketReceivedEvent event) {
        final BoarPlayer player = event.getPlayer();
        if (!(event.getPacket() instanceof PlayerAuthInputPacket)) {
            return;
        }

        player.tick();

        final PlayerAuthInputPacket packet = (PlayerAuthInputPacket) event.getPacket();
        if (packet.getTick() != packet.getTick()) {
            player.getSession().disconnect("Invalid movement packet!");
            return;
        }

        player.lastX = player.tick == 0 ? player.x : packet.getPosition().getX();
        player.lastY = player.tick == 0 ? player.y : packet.getPosition().getY() - EntityDefinitions.PLAYER.offset();
        player.lastZ = player.tick == 0 ? player.z : packet.getPosition().getZ();

        player.x = packet.getPosition().getX();
        player.y = packet.getPosition().getY() - EntityDefinitions.PLAYER.offset();
        player.z = packet.getPosition().getZ();

        player.lastSprinting = player.sprinting;
        player.lastSneaking = player.sneaking;
        player.sprinting = packet.getInputData().contains(PlayerAuthInputData.START_SPRINTING) || packet.getInputData().contains(PlayerAuthInputData.SPRINTING);
        player.sneaking = packet.getInputData().contains(PlayerAuthInputData.START_SNEAKING) || packet.getInputData().contains(PlayerAuthInputData.SNEAKING);

        player.yaw = packet.getRotation().getY();
        player.pitch = packet.getRotation().getX();

        player.claimedClientVelocity = new Vec3d(packet.getDelta());
        player.claimedMovementInput = new Vec3d(packet.getMotion().getX(), 0, packet.getMotion().getY());

        player.actualVelocity = new Vec3d(player.x - player.lastX, player.y - player.lastY, player.z - player.lastZ);
        player.lastTickWasTeleport = false;

        if (player.teleportUtil.teleportInQueue()) {
            event.setCancelled(true);
        }
    }
}
