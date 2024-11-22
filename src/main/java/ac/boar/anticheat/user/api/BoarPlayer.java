package ac.boar.anticheat.user.api;

import ac.boar.utils.GeyserUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cloudburstmc.protocol.bedrock.BedrockSession;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.mcprotocollib.network.Session;

@RequiredArgsConstructor
@Getter
@Setter
public class BoarPlayer {
    private final GeyserConnection connection;
    private final long joinedTime = System.currentTimeMillis();

    private BedrockSession bedrockSession;
    private Session javaSession;

    public void init() {
        GeyserUtil.hookGeyserPlayer(this);
    }
}
