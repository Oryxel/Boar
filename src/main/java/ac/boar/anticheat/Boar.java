package ac.boar.anticheat;

import ac.boar.anticheat.geyser.GeyserSessionJoinEvent;
import ac.boar.anticheat.packets.LatencyPacket;
import ac.boar.anticheat.packets.MovementCheckRunner;
import ac.boar.anticheat.packets.WorldTeleportPacket;
import ac.boar.anticheat.user.BoarPlayerManager;
import ac.boar.data.BedrockMappingData;
import ac.boar.plugin.BoarPlugin;
import ac.boar.protocol.BedrockPacketEvents;
import ac.boar.protocol.JavaPacketEvents;
import lombok.Getter;

@Getter
public class Boar {
    @Getter
    private static Boar instance = new Boar();
    private Boar() {}

    private BoarPlayerManager playerManager;

    public void init() {
        BoarPlugin.LOGGER.info("Loading mappings.....");
        BedrockMappingData.load();

        BoarPlugin.LOGGER.info("Initialing events....");
        new GeyserSessionJoinEvent();
        BedrockPacketEvents.register(new LatencyPacket());
        BedrockPacketEvents.register(new WorldTeleportPacket());
        BedrockPacketEvents.register(new MovementCheckRunner());

        this.playerManager = new BoarPlayerManager();
    }

    public void shutdown() {
        BoarPlugin.LOGGER.info("Shutting down...");
        this.playerManager.clear();

        BedrockPacketEvents.terminate();
        JavaPacketEvents.terminate();
    }
}
