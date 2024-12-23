package ac.boar.anticheat.packets;

import ac.boar.anticheat.prediction.ticker.PlayerTicker;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.bedrock.BedrockPacketListener;
import ac.boar.protocol.event.bedrock.PacketReceivedEvent;
import ac.boar.utils.MathUtil;
import ac.boar.utils.math.Vec3f;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;
import org.geysermc.geyser.entity.EntityDefinitions;

public class MovementCheckRunner implements BedrockPacketListener {
    @Override
    public void onPacketReceived(PacketReceivedEvent event) {
        final BoarPlayer player = event.getPlayer();
        if (!(event.getPacket() instanceof PlayerAuthInputPacket packet)) {
            return;
        }

        player.tick();
        player.tick = packet.getTick();
        player.bedrockRotation = packet.getRotation();

        // This DOES happen, sometimes it failed to add the adapter, force player to rejoin...
        if (player.getJavaSession() == null) {
            player.disconnect("SessionAdapter failed to inject!");
            return;
        }

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

        player.actualVelocity = new Vec3f(player.x - player.lastX, player.y - player.lastY, player.z - player.lastZ);

        player.yaw = packet.getRotation().getY();
        player.pitch = packet.getRotation().getX();

        if (player.boundingBox == null) {
            player.updateBoundingBox();
        }

        if (player.lastTickWasTeleport) {
            player.sinceTeleport = 0;
            return;
        }
        player.sinceTeleport++;

        // It's fine for us to trust this value.... even if the player spoof it they will have to correct the movement
        // But we do want to check for funny value. Also, we will have to handle sneaking and eating ourselves, don't trust the client.
        player.movementInput = new Vec3f(MathUtil.toValue(packet.getMotion().getX(), 1), 0, MathUtil.toValue(packet.getMotion().getY(), 1));
        player.lastClaimedEOT = player.claimedEOT.clone();
        player.claimedEOT = packet.getDelta();

        player.lastClientVelocity = player.clientVelocity.clone();
        // Is this EOT fault or travel/collision fault? This is for debugging that.
        // player.clientVelocity = new Vec3f(player.lastClaimedEOT);

        updateInputData(player);

        new PlayerTicker(player).tick();
//        if (packet.getMotion().length() > 0) {
//            Bukkit.broadcastMessage(player.movementInput.x + "," + player.movementInput.z + ", A: " + packet.getMotion().toString());
//        }

        correctPlayerAuthInput(player, packet);
    }

    private void updateInputData(BoarPlayer player) {
        player.wasGliding = player.gliding;
        if (player.inputData.contains(PlayerAuthInputData.START_GLIDING)) {
            // TODO: prevent player from spoofing this.
            player.gliding = true;
        } else if (player.inputData.contains(PlayerAuthInputData.STOP_GLIDING)) {
            player.gliding = false;
        }

        player.wasSprinting = player.sprinting;
        if (player.inputData.contains(PlayerAuthInputData.START_SPRINTING)) {
            // Sprinting is only late when player stop sprinting (still moving at sprinting speed even tho already sent STOP_SPRINTING)
            // But START_SPRINTING is ALWAYS correct and never actually behind (I think)
            // Don't let player do backwards sprinting!
            player.sprinting = player.movementInput.z > 0;
        } else if (player.inputData.contains(PlayerAuthInputData.STOP_SPRINTING)) {
            player.sprinting = false;
        }

        player.wasSneaking = player.sneaking;
        if (player.inputData.contains(PlayerAuthInputData.START_SNEAKING)) {
            player.sneaking = true;
            player.sprinting = false;
        } else if (player.inputData.contains(PlayerAuthInputData.STOP_SNEAKING)) {
            player.sneaking = false;
        }

        player.wasSwimming = player.swimming;
        if (player.inputData.contains(PlayerAuthInputData.START_SWIMMING)) {
            player.swimming = true;
        } else if (player.inputData.contains(PlayerAuthInputData.STOP_SWIMMING)) {
            player.swimming = false;
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

        // The player will always have to be moving forward to sprint so don't let player do backwards sprinting.
        // Or the player sprinting status is just de-synced...
        if (player.movementInput.z <= 0 && player.sprinting) {
            player.sprinting = false;
            player.sinceSprinting = 1;
        }
    }

    // Possible patch for no-fall exploit on GeyserMC since geyser just check for delta.y > 0 and VERTICAL_COLLISION
    private void correctPlayerAuthInput(BoarPlayer player, PlayerAuthInputPacket packet) {
        packet.setDelta(Vector3f.from(player.clientVelocity.x, player.clientVelocity.y, player.clientVelocity.z));
        if (packet.getInputData().contains(PlayerAuthInputData.VERTICAL_COLLISION) && !player.verticalCollision) {
            packet.getInputData().remove(PlayerAuthInputData.VERTICAL_COLLISION);
        } else if (!packet.getInputData().contains(PlayerAuthInputData.VERTICAL_COLLISION) && player.verticalCollision) {
            packet.getInputData().add(PlayerAuthInputData.VERTICAL_COLLISION);
        }

        if (!packet.getInputData().contains(PlayerAuthInputData.HORIZONTAL_COLLISION) && player.horizontalCollision) {
            packet.getInputData().add(PlayerAuthInputData.HORIZONTAL_COLLISION);
        }
    }
}
