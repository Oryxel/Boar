package ac.boar.protocol.event.java;

import ac.boar.anticheat.user.api.BoarPlayer;
import org.geysermc.mcprotocollib.network.packet.Packet;

public record PacketSendEvent(BoarPlayer player, Packet packet) {
}
