package ac.boar.anticheat;

import ac.boar.anticheat.event.GeyserSessionJoinEvent;
import ac.boar.anticheat.packets.*;
import ac.boar.anticheat.packets.level.JavaLevelPacket;
import ac.boar.anticheat.packets.other.FinalPacketListener;
import ac.boar.anticheat.packets.other.LatencyPacket;
import ac.boar.anticheat.packets.player.*;
import ac.boar.anticheat.packets.level.LevelEntityPacket;
import ac.boar.anticheat.user.BoarPlayerManager;
import ac.boar.plugin.BoarPlugin;
import ac.boar.protocol.BedrockPacketEvents;
import ac.boar.protocol.GeyserPacketEvents;
import ac.boar.protocol.JavaPacketEvents;
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
        BedrockPacketEvents.register(new PlayerInventoryPacket());
        BedrockPacketEvents.register(new PlayerTeleportPacket());
        BedrockPacketEvents.register(new MovementCheckRunner());
        BedrockPacketEvents.register(new PacketCheckRunner());
        BedrockPacketEvents.register(new FinalPacketListener());

        GeyserPacketEvents.register(new PlayerTeleportPacket());
        GeyserPacketEvents.register(new JavaLevelPacket());
        GeyserPacketEvents.register(new PlayerEffectPacket());
        GeyserPacketEvents.register(new LevelEntityPacket());
        GeyserPacketEvents.register(new PlayerVelocityPacket());
        GeyserPacketEvents.register(new PlayerAbilitiesPacket());
        GeyserPacketEvents.register(new FinalPacketListener());

        JavaPacketEvents.register(new JavaLevelPacket());
        JavaPacketEvents.register(new PlayerAbilitiesPacket());

        this.playerManager = new BoarPlayerManager();
    }

    public void shutdown() {
        BoarPlugin.LOGGER.info("Shutting down...");
        this.playerManager.clear();

        JavaPacketEvents.terminate();
        BedrockPacketEvents.terminate();
        GeyserPacketEvents.terminate();
    }
}
