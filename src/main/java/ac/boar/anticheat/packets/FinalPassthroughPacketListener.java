package ac.boar.anticheat.packets;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.bedrock.BedrockPacketListener;
import ac.boar.protocol.event.bedrock.PacketReceivedEvent;
import ac.boar.protocol.event.bedrock.geyser.GeyserPacketListener;
import ac.boar.protocol.event.bedrock.geyser.GeyserSendEvent;
import ac.boar.utils.math.Vec3f;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;
import org.geysermc.geyser.entity.EntityDefinitions;

public class FinalPassthroughPacketListener implements GeyserPacketListener, BedrockPacketListener {
    @Override
    public void onPacketReceived(PacketReceivedEvent event) {
        final BoarPlayer player = event.getPlayer();

        if (event.getPacket() instanceof PlayerAuthInputPacket) {
            player.lastTickWasTeleport = false;
            // This packet doesn't matter, player supposed to be in the teleported position by now.
            // Cancel it don't let any position pass through unless they properly accept it.
            // Geyser also do this, but we made it stricter by checking for lastReceivedId, player can't accept it if they're still lagging.
            if (player.teleportUtil.teleportInQueue()) {
                event.setCancelled(true);
            }

            if (event.isCancelled()) {
                return;
            }

            player.teleportUtil.lastKnowValid = new Vec3f(player.x, player.y + EntityDefinitions.PLAYER.offset(), player.z);
        }
    }

    @Override
    public void onPacketSend(GeyserSendEvent event) {
    }
}
