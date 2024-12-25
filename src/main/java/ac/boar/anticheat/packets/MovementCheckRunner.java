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

import static org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData.*;

public class MovementCheckRunner implements BedrockPacketListener {
    @Override
    public void onPacketReceived(PacketReceivedEvent event) {
        final BoarPlayer player = event.getPlayer();
        if (!(event.getPacket() instanceof PlayerAuthInputPacket packet)) {
            return;
        }

        player.tick = packet.getTick();
        player.bedrockRotation = packet.getRotation();

        // This DOES happen, sometimes it failed to add the adapter, force player to rejoin...
        if (player.getTcpSession() == null) {
            player.disconnect("SessionAdapter failed to inject!");
            return;
        }

        player.inputData.clear();
        player.inputData.addAll(packet.getInputData());

        // It's fine for us to trust this value.... even if the player spoof it they will have to correct the movement
        // But we do want to check for funny value. Also, we will have to handle sneaking and eating ourselves, don't trust the client.
        player.movementInput = new Vec3f(MathUtil.toValue(packet.getMotion().getX(), 1), 0, MathUtil.toValue(packet.getMotion().getY(), 1));

        updateInputData(player);
        player.tick();

        if (player.teleportUtil.teleportInQueue()) {
            return;
        }

        player.prevX = player.tick != 1 ? player.x : packet.getPosition().getX();
        player.prevY = player.tick != 1 ? player.y : packet.getPosition().getY() - EntityDefinitions.PLAYER.offset();
        player.prevZ = player.tick != 1 ? player.z : packet.getPosition().getZ();
        player.x = packet.getPosition().getX();
        player.y = packet.getPosition().getY() - EntityDefinitions.PLAYER.offset();
        player.z = packet.getPosition().getZ();

        player.actualVelocity = new Vec3f(player.x - player.prevX, player.y - player.prevY, player.z - player.prevZ);

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

        player.lastClaimedEOT = player.claimedEOT.clone();
        player.claimedEOT = packet.getDelta();

        player.prevEOT = player.eotVelocity.clone();
        // Is this EOT fault or travel/collision fault? This is for debugging that.
        // player.clientVelocity = new Vec3f(player.lastClaimedEOT);

        new PlayerTicker(player).tick();
//        if (packet.getMotion().length() > 0) {
//            Bukkit.broadcastMessage(player.movementInput.x + "," + player.movementInput.z + ", A: " + packet.getMotion().toString());
//        }

        correctPlayerAuthInput(player, packet);
    }

    private void updateInputData(BoarPlayer player) {
        player.wasGliding = player.gliding;
        player.wasSprinting = player.sprinting;
        player.wasSneaking = player.sneaking;
        player.wasSwimming = player.swimming;

        for (final PlayerAuthInputData input : player.inputData) {
            switch (input) {
                // TODO: Prevent player from spoofing gliding.
                case START_GLIDING -> player.gliding = true;
                case STOP_GLIDING -> player.gliding = false;

                // Don't let player do backwards sprinting!
                case START_SPRINTING -> player.sprinting = player.movementInput.z > 0;
                // Fun fact, minecraft bedrock actually somehow managed to send both START_SPRINTING and STOP_SPRINTING on the same tick. nice!
                // Maybe for other status too, not just sprinting, haven't test long enough to know.
                case STOP_SPRINTING -> player.sprinting = false;

                case START_SNEAKING -> player.sneaking = true;
                case STOP_SNEAKING -> player.sneaking = false;

                case START_SWIMMING -> player.swimming = true;
                case STOP_SWIMMING -> player.swimming = false;
            }
        }

        player.uncertainSprinting = player.inputData.contains(START_SPRINTING) && player.inputData.contains(STOP_SPRINTING);

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
        packet.setDelta(Vector3f.from(player.eotVelocity.x, player.eotVelocity.y, player.eotVelocity.z));
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
