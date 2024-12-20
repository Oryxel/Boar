package ac.boar.protocol.listener;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.BedrockPacketEvents;
import ac.boar.protocol.event.bedrock.BedrockPacketListener;
import ac.boar.protocol.event.bedrock.PacketReceivedEvent;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketHandler;
import org.cloudburstmc.protocol.common.PacketSignal;

@RequiredArgsConstructor
public final class MITMBedrockListener implements BedrockPacketHandler {
    private final BoarPlayer player;
    private final BedrockPacketHandler oldHandler;

    @Override
    public PacketSignal handlePacket(BedrockPacket packet) {
        boolean cancelled = false;

        final PacketReceivedEvent event = new PacketReceivedEvent(player, packet);
        for (final BedrockPacketListener listener : BedrockPacketEvents.getListeners()) {
            listener.onPacketReceived(event);
            cancelled = event.isCancelled();
        }

        if (cancelled) {
            return PacketSignal.HANDLED;
        }

        return this.oldHandler.handlePacket(event.getPacket());
    }
}