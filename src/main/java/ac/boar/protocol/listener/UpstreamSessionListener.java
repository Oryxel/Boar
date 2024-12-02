package ac.boar.protocol.listener;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.GeyserPacketEvents;
import ac.boar.protocol.event.bedrock.geyser.GeyserPacketListener;
import ac.boar.protocol.event.bedrock.geyser.GeyserSendEvent;
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
        final GeyserSendEvent event = new GeyserSendEvent(player, packet, false);
        for (final GeyserPacketListener listener : GeyserPacketEvents.getListeners()) {
            listener.onPacketSend(event);
        }

        if (event.isCancelled()) {
            return;
        }

        super.sendPacket(packet);

        event.getPostTasks().forEach(Runnable::run);
        event.getPostTasks().clear();
    }

    @Override
    public void sendPacketImmediately(@NonNull BedrockPacket packet) {
        final GeyserSendEvent event = new GeyserSendEvent(player, packet, true);
        for (final GeyserPacketListener listener : GeyserPacketEvents.getListeners()) {
            listener.onPacketSend(event);
        }

        if (event.isCancelled()) {
            return;
        }

        super.sendPacketImmediately(packet);

        event.getPostTasks().forEach(Runnable::run);
        event.getPostTasks().clear();
    }
}
