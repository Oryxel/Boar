package ac.boar.anticheat;

import ac.boar.anticheat.geyser.GeyserSessionJoinEvent;
import ac.boar.anticheat.user.BoarPlayerManager;
import ac.boar.protocol.BedrockPacketEvents;
import lombok.Getter;

@Getter
public class Boar {
    @Getter
    private static Boar instance = new Boar();
    private Boar() {}

    private BoarPlayerManager playerManager;

    public void init() {
        new GeyserSessionJoinEvent();

        BedrockPacketEvents.init();

        this.playerManager = new BoarPlayerManager();
    }

    public void shutdown() {
        this.playerManager.clear();

        BedrockPacketEvents.terminate();
    }
}
