package ac.boar.protocol.event.bedrock;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ac.boar.anticheat.user.api.BoarPlayer;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;

@RequiredArgsConstructor
@Getter
@Setter
public final class PacketReceivedEvent {
    private final BoarPlayer player;
    private final BedrockPacket packet;
    private boolean cancelled;
}
