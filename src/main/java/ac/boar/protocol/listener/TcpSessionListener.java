package ac.boar.protocol.listener;

import ac.boar.protocol.event.java.PacketSendEvent;
import lombok.RequiredArgsConstructor;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.JavaPacketEvents;
import ac.boar.protocol.event.java.PacketListener;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;

@RequiredArgsConstructor
public class TcpSessionListener extends SessionAdapter {
    private final BoarPlayer player;

    @Override
    public void packetReceived(Session session, Packet packet) {
        if (session != player.getJavaSession()) return;

        for (final PacketListener listener : JavaPacketEvents.getListeners()) {
            final PacketSendEvent event = new PacketSendEvent(player, packet);
            listener.onPacketSend(event);
        }
    }
}