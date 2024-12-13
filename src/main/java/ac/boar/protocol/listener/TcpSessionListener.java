package ac.boar.protocol.listener;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.JavaPacketEvents;
import ac.boar.protocol.event.java.PacketListener;
import ac.boar.protocol.event.java.PacketSendEvent;
import lombok.RequiredArgsConstructor;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.*;
import org.geysermc.mcprotocollib.network.packet.Packet;

import java.util.List;

@RequiredArgsConstructor
public final class TcpSessionListener extends SessionAdapter {
    private final BoarPlayer player;
    private final List<SessionListener> listeners;

    @Override
    public void packetReceived(Session session, Packet packet) {
        if (session != player.getJavaSession()) return;

        final PacketSendEvent event = new PacketSendEvent(player, packet);
        for (final PacketListener listener : JavaPacketEvents.getListeners()) {
            listener.onPacketSend(event);
        }
        if (!event.isCancelled()) {
            listeners.forEach(l -> l.packetReceived(session, packet));
        }

        event.getPostTasks().forEach(Runnable::run);
        event.getPostTasks().clear();
    }

    @Override
    public void packetSending(PacketSendingEvent event) {
        listeners.forEach(l -> l.packetSending(event));
    }

    @Override
    public void connected(ConnectedEvent event) {
        listeners.forEach(l -> l.connected(event));
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        listeners.forEach(l -> l.disconnected(event));
    }

    @Override
    public void packetError(PacketErrorEvent event) {
        listeners.forEach(l -> l.packetError(event));
    }
}
