package ac.boar.anticheat.packets;

import ac.boar.anticheat.check.api.Check;
import ac.boar.anticheat.check.api.impl.PacketCheck;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.bedrock.BedrockPacketListener;
import ac.boar.protocol.event.bedrock.PacketReceivedEvent;
import ac.boar.protocol.event.java.PacketListener;
import ac.boar.protocol.event.java.PacketSendEvent;
import ac.boar.utils.math.Vec3d;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;

import java.util.Map;

public class PacketCheckRunner implements PacketListener, BedrockPacketListener {
    @Override
    public void onPacketSend(PacketSendEvent event) {
        final BoarPlayer player = event.getPlayer();
        for (Map.Entry<Class, Check> entry : player.checkHolder.entrySet()) {
            Check v = entry.getValue();
            if (v instanceof PacketCheck) {
                ((PacketCheck) v).onPacketSend(event);
            }
        }
    }

    @Override
    public void onPacketReceived(PacketReceivedEvent event) {
        final BoarPlayer player = event.getPlayer();
        for (Map.Entry<Class, Check> entry : player.checkHolder.entrySet()) {
            Check v = entry.getValue();
            if (v instanceof PacketCheck) {
                ((PacketCheck) v).onPacketReceived(event);
            }
        }

        // This is a lot more useful than you think it is.
        if (player.teleportUtil.teleportInQueue()) {
            event.setCancelled(true);
        }

        player.lastTickWasTeleport = false;
        if (event.getPacket() instanceof PlayerAuthInputPacket) {
            if (event.isCancelled() || player.teleportUtil.teleportInQueue()) {
                return;
            }

            player.teleportUtil.lastKnowValid = new Vec3d(player.x, player.y, player.z);
        }
    }
}
