package ac.boar.anticheat.packets;

import ac.boar.protocol.event.bedrock.BedrockPacketListener;
import ac.boar.protocol.event.bedrock.PacketReceivedEvent;
import ac.boar.utils.GeyserUtil;
import org.cloudburstmc.protocol.bedrock.packet.NetworkStackLatencyPacket;

public class LatencyPacket implements BedrockPacketListener {
    @Override
    public void onPacketReceived(PacketReceivedEvent event) {
        if (!(event.getPacket() instanceof NetworkStackLatencyPacket)) {
            return;
        }

        final NetworkStackLatencyPacket packet = (NetworkStackLatencyPacket) event.getPacket();
        long id = packet.getTimestamp();
        if (id >= 0 || ((double) id / GeyserUtil.MAGIC_FORM_IMAGE_HACK_TIMESTAMP) % 10 == 0 || ((double) GeyserUtil.MAGIC_FORM_IMAGE_HACK_TIMESTAMP / id) % 10 == 0) {
            return;
        }

        boolean cancelled = event.getPlayer().latencyUtil.confirmTransaction(Math.abs(id / event.getPlayer().getMagnitude()));
        event.setCancelled(cancelled);
    }
}
