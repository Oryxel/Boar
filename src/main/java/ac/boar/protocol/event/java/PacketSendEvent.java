package ac.boar.protocol.event.java;

import ac.boar.anticheat.user.api.BoarPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.geysermc.mcprotocollib.network.packet.Packet;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
@Setter
public final class PacketSendEvent {
    private final BoarPlayer player;
    private final Packet packet;
    private final List<Runnable> postTasks = new ArrayList<>();
    private boolean cancelled;
}