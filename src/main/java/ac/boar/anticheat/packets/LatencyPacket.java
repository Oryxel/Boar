package ac.boar.anticheat.packets;

import ac.boar.protocol.event.bedrock.BedrockPacketListener;
import ac.boar.protocol.event.bedrock.PacketReceivedEvent;
import org.cloudburstmc.protocol.bedrock.packet.NetworkStackLatencyPacket;

public class LatencyPacket implements BedrockPacketListener {
    @Override
    public void onPacketReceived(PacketReceivedEvent event) {
        if (!(event.getPacket() instanceof NetworkStackLatencyPacket)) {
            return;
        }

        final NetworkStackLatencyPacket packet = (NetworkStackLatencyPacket) event.getPacket();
        long id = packet.getTimestamp() / 1000000L;

        event.getPlayer().latencyUtil.confirmTransaction(id);
    }
}
