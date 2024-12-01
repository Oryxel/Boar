package ac.boar.protocol.listener;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.GeyserPacketEvents;
import ac.boar.protocol.event.bedrock.geyser.GeyserPacketListener;
import ac.boar.protocol.event.bedrock.geyser.GeyserReceivedEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.geysermc.geyser.session.UpstreamSession;

public class UpstreamSessionListener extends UpstreamSession {
    private final BoarPlayer player;

    public UpstreamSessionListener(BoarPlayer player, BedrockServerSession session) {
        super(session);
        this.player = player;
    }

    @Override
    public void sendPacket(@NonNull BedrockPacket packet) {
        final GeyserReceivedEvent event = new GeyserReceivedEvent(player, packet);
        for (final GeyserPacketListener listener : GeyserPacketEvents.getListeners()) {
            listener.onPacketReceived(event);
        }

        if (event.isCancelled()) {
            return;
        }

        super.sendPacket(packet);
    }

    @Override
    public void sendPacketImmediately(@NonNull BedrockPacket packet) {
        final GeyserReceivedEvent event = new GeyserReceivedEvent(player, packet);
        for (final GeyserPacketListener listener : GeyserPacketEvents.getListeners()) {
            listener.onPacketReceived(event);
        }

        if (event.isCancelled()) {
            return;
        }

        super.sendPacketImmediately(packet);
    }
}
