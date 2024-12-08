package ac.boar.anticheat;

import ac.boar.anticheat.event.GeyserSessionJoinEvent;
import ac.boar.anticheat.packets.*;
import ac.boar.anticheat.user.BoarPlayerManager;
import ac.boar.plugin.BoarPlugin;
import ac.boar.protocol.BedrockPacketEvents;
import ac.boar.protocol.GeyserPacketEvents;
import lombok.Getter;

@Getter
public class Boar {
    @Getter
    private static final Boar instance = new Boar();
    private BoarPlayerManager playerManager;

    private Boar() {
    }

    public void init() {
        BoarPlugin.LOGGER.info("Initialing events....");
        new GeyserSessionJoinEvent();

        BedrockPacketEvents.register(new LatencyPacket());
        BedrockPacketEvents.register(new WorldTeleportPacket());
        BedrockPacketEvents.register(new MovementCheckRunner());
        BedrockPacketEvents.register(new PacketCheckRunner());
        BedrockPacketEvents.register(new FinalPassthroughPacketListener());

        GeyserPacketEvents.register(new WorldTeleportPacket());
        GeyserPacketEvents.register(new EffectUpdatePacket());
        GeyserPacketEvents.register(new EntityUpdatePacket());
        GeyserPacketEvents.register(new VelocityUpdatePacket());
        GeyserPacketEvents.register(new AbilitiesUpdatePacket());
        GeyserPacketEvents.register(new FinalPassthroughPacketListener());

        this.playerManager = new BoarPlayerManager();
    }

    public void shutdown() {
        BoarPlugin.LOGGER.info("Shutting down...");
        this.playerManager.clear();

        BedrockPacketEvents.terminate();
        GeyserPacketEvents.terminate();
    }
}
