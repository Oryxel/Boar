package ac.boar.anticheat.packets;

import ac.boar.anticheat.prediction.ticker.PlayerTicker;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.bedrock.BedrockPacketListener;
import ac.boar.protocol.event.bedrock.PacketReceivedEvent;
import ac.boar.utils.MathUtil;
import ac.boar.utils.math.BoundingBox;
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

        // This DOES happen, sometimes it failed to add the adapter, force player to rejoin...
        if (player.getJavaSession() == null) {
            player.getSession().disconnect("Failed to add MCProtocolLib adapter, please rejoin!");
            return;
        }

        player.lastX = player.tick != 0 ? player.x : packet.getPosition().getX();
        player.lastY = player.tick != 0 ? player.y : packet.getPosition().getY() - EntityDefinitions.PLAYER.offset();
        player.lastZ = player.tick != 0 ? player.z : packet.getPosition().getZ();

        player.x = packet.getPosition().getX();
        player.y = packet.getPosition().getY() - EntityDefinitions.PLAYER.offset();
        player.z = packet.getPosition().getZ();

        player.lastSprinting = player.sprinting;
        player.lastSneaking = player.sneaking;
        player.lastSwimming = player.swimming;
        player.sprinting = packet.getInputData().contains(PlayerAuthInputData.START_SPRINTING) || packet.getInputData().contains(PlayerAuthInputData.SPRINTING);
        player.sneaking = packet.getInputData().contains(PlayerAuthInputData.START_SNEAKING) || packet.getInputData().contains(PlayerAuthInputData.SNEAKING);

        if (player.boundingBox == null) {
            player.boundingBox = BoundingBox.getBoxAt(player.x, player.y, player.z, EntityDefinitions.PLAYER.width(), EntityDefinitions.PLAYER.height());
        }

        if (packet.getInputData().contains(PlayerAuthInputData.START_SWIMMING)) {
            player.swimming = true;
        } else if (packet.getInputData().contains(PlayerAuthInputData.STOP_SWIMMING)) {
            player.swimming = false;
        }

        player.yaw = packet.getRotation().getY();
        player.pitch = packet.getRotation().getX();

        player.claimedClientVelocity = new Vec3d(packet.getDelta());

        // It's fine for us to trust this value.... even if the player spoof it they will have to correct the movement
        // But we do want to check for funny value. Also, we will have to handle sneaking and eating ourselves, don't trust the client.
        // Also, the player will always have to be moving forward to sprint so don't let player do that.
        player.movementInput = new Vec3d(MathUtil.toValue(packet.getMotion().getX(), 1), 0, player.sprinting ? 1 : MathUtil.toValue(packet.getMotion().getY(), 1));

        if (player.lastTickWasTeleport) {
            return;
        }

        player.actualVelocity = new Vec3d(player.x - player.lastX, player.y - player.lastY, player.z - player.lastZ);

        new PlayerTicker(player).tick();

        player.boundingBox = BoundingBox.getBoxAt(player.x, player.y, player.z, EntityDefinitions.PLAYER.width(), EntityDefinitions.PLAYER.height());

        // This is a lot more useful than you think it is.
        if (player.teleportUtil.teleportInQueue()) {
            event.setCancelled(true);
        }
    }
}
