package ac.boar.protocol.listener;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.GeyserPacketEvents;
import ac.boar.protocol.event.bedrock.geyser.GeyserPacketListener;
import ac.boar.protocol.event.bedrock.geyser.GeyserSendEvent;
import ac.boar.utils.GeyserUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.data.AuthoritativeMovementMode;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket;
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

        if (packet instanceof StartGamePacket startGamePacket) {
            startGamePacket.setAuthoritativeMovementMode(AuthoritativeMovementMode.SERVER_WITH_REWIND);
            startGamePacket.setRewindHistorySize(20);

            player.runtimeEntityId = startGamePacket.getRuntimeEntityId();
            player.javaId = player.getSession().getPlayerEntity().getEntityId();
            getSession().sendPacket(startGamePacket);

            GeyserUtil.hookJavaSession(player);
            player.loadBlockMappings();

            player.compensatedWorld.loadDimension();
            return;
        }

        super.sendPacket(event.getPacket());
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

        super.sendPacketImmediately(event.getPacket());
        event.getPostTasks().forEach(Runnable::run);
        event.getPostTasks().clear();
    }
}
