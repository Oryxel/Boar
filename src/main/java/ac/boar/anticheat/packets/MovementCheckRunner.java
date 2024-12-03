package ac.boar.anticheat.packets;

import ac.boar.anticheat.prediction.ticker.PlayerTicker;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.bedrock.BedrockPacketListener;
import ac.boar.protocol.event.bedrock.PacketReceivedEvent;
import ac.boar.utils.MathUtil;
import ac.boar.utils.math.BoundingBox;
import ac.boar.utils.math.Vec3f;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;
import org.geysermc.geyser.entity.EntityDefinitions;
import org.geysermc.geyser.network.GameProtocol;

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

        player.bedrockRotation = packet.getRotation();

        if (player.teleportUtil.teleportInQueue()) {
            return;
        }

        player.inputData.clear();
        player.inputData.addAll(packet.getInputData());

        player.lastX = player.tick != 1 ? player.x : packet.getPosition().getX();
        player.lastY = player.tick != 1 ? player.y : packet.getPosition().getY() - EntityDefinitions.PLAYER.offset();
        player.lastZ = player.tick != 1 ? player.z : packet.getPosition().getZ();

        player.x = packet.getPosition().getX();
        player.y = packet.getPosition().getY() - EntityDefinitions.PLAYER.offset();
        player.z = packet.getPosition().getZ();

        if (player.boundingBox == null) {
            player.boundingBox = BoundingBox.getBoxAt(player.x, player.y, player.z, EntityDefinitions.PLAYER.width(), EntityDefinitions.PLAYER.height());
        }

        if (player.lastTickWasTeleport) {
            player.boundingBox = BoundingBox.getBoxAt(player.x, player.y, player.z, EntityDefinitions.PLAYER.width(), EntityDefinitions.PLAYER.height());
            return;
        }

        // It's fine for us to trust this value.... even if the player spoof it they will have to correct the movement
        // But we do want to check for funny value. Also, we will have to handle sneaking and eating ourselves, don't trust the client.
        player.movementInput = new Vec3f(MathUtil.toValue(packet.getMotion().getX(), 1), 0, MathUtil.toValue(packet.getMotion().getY(), 1));

        player.lastGliding = player.gliding;
        if (packet.getInputData().contains(PlayerAuthInputData.START_GLIDING)) {
            // TODO: prevent player from spoofing this.
            player.gliding = true;
        } else if (packet.getInputData().contains(PlayerAuthInputData.STOP_GLIDING)) {
            player.gliding = false;
        }

        player.lastSprinting = player.sprinting;
        if (packet.getInputData().contains(PlayerAuthInputData.START_SPRINTING)) {
            // Sprinting is only late when player stop sprinting (still moving at sprinting speed even tho already sent STOP_SPRINTING)
            // But START_SPRINTING is ALWAYS correct and never actually behind (I think)
            // Even if we're wrong about this, correct player movement.
            player.sprinting = player.movementInput.z > 0;
        } else if (packet.getInputData().contains(PlayerAuthInputData.STOP_SPRINTING)) {
            player.sprinting = false;
        }

        player.lastSneaking = player.sneaking;
        if (packet.getInputData().contains(PlayerAuthInputData.START_SNEAKING)) {
            player.sneaking = true;
            player.sprinting = false;
        } else if (packet.getInputData().contains(PlayerAuthInputData.STOP_SNEAKING)) {
            player.sneaking = false;
        }

        if (!player.sprinting) {
            player.sinceSprinting++;
        } else {
            player.sinceSprinting = 0;
        }

        if (!player.sneaking) {
            player.sinceSneaking++;
        } else {
            player.sinceSneaking = 0;
        }

        player.lastSwimming = player.swimming;
        if (packet.getInputData().contains(PlayerAuthInputData.START_SWIMMING)) {
            player.swimming = true;
        } else if (packet.getInputData().contains(PlayerAuthInputData.STOP_SWIMMING)) {
            player.swimming = false;
        }

        player.yaw = packet.getRotation().getY();
        player.pitch = packet.getRotation().getX();

        // The player will always have to be moving forward to sprint so don't let player do backwards sprinting.
        // Or the player sprinting status is just de-synced...
        if (player.movementInput.z < 0 && player.sprinting) {
            player.lastSprinting = true;
            player.sprinting = false;
            player.sinceSprinting = 1;
        }

        player.lastCanClimb = player.canClimb;
        player.canClimb = (player.isClimbing() /* || this.getBlockStateAtPos().isOf(Blocks.POWDER_SNOW) && PowderSnowBlock.canWalkOnPowderSnow(this) */);

        player.actualVelocity = new Vec3f(player.x - player.lastX, player.y - player.lastY, player.z - player.lastZ);
        new PlayerTicker(player).tick();

        // Possible patch for no-fall exploit on GeyserMC since geyser just check for delta.y > 0 and VERTICAL_COLLISION
        packet.setDelta(Vector3f.from(player.clientVelocity.x, player.clientVelocity.y, player.clientVelocity.z));
        if (!GameProtocol.isPre1_21_30(player.getSession())) {
            if (packet.getInputData().contains(PlayerAuthInputData.VERTICAL_COLLISION) && !player.collideY) {
                packet.getInputData().remove(PlayerAuthInputData.VERTICAL_COLLISION);
            } else if (!packet.getInputData().contains(PlayerAuthInputData.VERTICAL_COLLISION) && player.collideY) {
                packet.getInputData().add(PlayerAuthInputData.VERTICAL_COLLISION);
            }
        }

        if (!packet.getInputData().contains(PlayerAuthInputData.HORIZONTAL_COLLISION) && (player.collideX || player.collideZ)) {
            packet.getInputData().add(PlayerAuthInputData.HORIZONTAL_COLLISION);
        }
    }
}
