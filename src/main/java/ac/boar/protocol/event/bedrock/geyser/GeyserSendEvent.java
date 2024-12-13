package ac.boar.protocol.event.bedrock.geyser;

import ac.boar.anticheat.user.api.BoarPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
@Setter
public final class GeyserSendEvent {
    private final BoarPlayer player;
    private final BedrockPacket packet;
    private final boolean immediate;
    private final List<Runnable> postTasks = new ArrayList<>();
    private boolean cancelled;
}
