package ac.boar.anticheat.packets;

import ac.boar.anticheat.prediction.ticker.PlayerTicker;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.bedrock.BedrockPacketListener;
import ac.boar.protocol.event.bedrock.PacketReceivedEvent;
import ac.boar.utils.MathUtil;
import ac.boar.utils.math.BoundingBox;
import ac.boar.utils.math.Vec3d;
import org.bukkit.Bukkit;
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

        player.lastX = player.tick != 1 ? player.x : packet.getPosition().getX();
        player.lastY = player.tick != 1 ? player.y : packet.getPosition().getY() - EntityDefinitions.PLAYER.offset();
        player.lastZ = player.tick != 1 ? player.z : packet.getPosition().getZ();

        player.x = packet.getPosition().getX();
        player.y = packet.getPosition().getY() - EntityDefinitions.PLAYER.offset();
        player.z = packet.getPosition().getZ();

        if (player.boundingBox == null) {
            player.boundingBox = BoundingBox.getBoxAt(player.x, player.y, player.z, EntityDefinitions.PLAYER.width(), EntityDefinitions.PLAYER.height());
        }

        player.lastSneaking = player.sneaking;

        if (packet.getInputData().contains(PlayerAuthInputData.START_SPRINTING)) {
            player.lastSprinting = player.sprinting;
            player.sprinting = true;
        } else if (packet.getInputData().contains(PlayerAuthInputData.STOP_SPRINTING)) {
            player.lastSprinting = player.sprinting;
            player.sprinting = false;
        }

        if (packet.getInputData().contains(PlayerAuthInputData.START_SNEAKING)) {
            player.lastSneaking = player.sneaking;
            player.sneaking = true;
        } else if (packet.getInputData().contains(PlayerAuthInputData.STOP_SNEAKING)) {
            player.lastSneaking = player.sneaking;
            player.sneaking = false;
        }

        if (packet.getInputData().contains(PlayerAuthInputData.START_SWIMMING)) {
            player.lastSwimming = player.swimming;
            player.swimming = true;
        } else if (packet.getInputData().contains(PlayerAuthInputData.STOP_SWIMMING)) {
            player.lastSwimming = player.swimming;
            player.swimming = false;
        }

        player.yaw = packet.getRotation().getY();
        player.pitch = packet.getRotation().getX();

        // It's fine for us to trust this value.... even if the player spoof it they will have to correct the movement
        // But we do want to check for funny value. Also, we will have to handle sneaking and eating ourselves, don't trust the client.
        // The player will always have to be moving forward to sprint so don't let player do backwards sprinting.
        player.movementInput = new Vec3d(MathUtil.toValue(packet.getMotion().getX(), 1), 0, player.sprinting ? 1 : MathUtil.toValue(packet.getMotion().getY(), 1));

        if (player.lastTickWasTeleport || player.teleportUtil.teleportInQueue()) {
            player.boundingBox = BoundingBox.getBoxAt(player.x, player.y, player.z, EntityDefinitions.PLAYER.width(), EntityDefinitions.PLAYER.height());
            return;
        }

        player.actualVelocity = new Vec3d(player.x - player.lastX, player.y - player.lastY, player.z - player.lastZ);

        new PlayerTicker(player).tick();

        if (player.actualVelocity.length() > 0) {
            Bukkit.broadcastMessage("EOT: " + packet.getDelta().toString());
        }

        player.boundingBox = BoundingBox.getBoxAt(player.x, player.y, player.z, EntityDefinitions.PLAYER.width(), EntityDefinitions.PLAYER.height());
    }
}
