package ac.boar.protocol.event.bedrock.geyser;

import ac.boar.anticheat.user.api.BoarPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;

@RequiredArgsConstructor
@Getter
@Setter
public final class GeyserReceivedEvent {
    private final BoarPlayer player;
    private final BedrockPacket packet;
    private final boolean immediate;
    private boolean cancelled;
}
