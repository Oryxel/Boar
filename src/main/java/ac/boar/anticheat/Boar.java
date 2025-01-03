package ac.boar.anticheat;

import ac.boar.anticheat.packets.other.FinalPacketListener;
import ac.boar.anticheat.packets.player.*;
import ac.boar.anticheat.packets.world.*;
import lombok.Getter;

import ac.boar.anticheat.packets.other.NetworkLatencyPacket;
import ac.boar.anticheat.packets.MovementCheckRunner;

import ac.boar.anticheat.player.manager.BoarPlayerManager;
import ac.boar.geyser.GeyserSessionJoinEvent;
import ac.boar.protocol.PacketEvents;

@Getter
public class Boar {
    @Getter
    private static final Boar instance = new Boar();
    private Boar() {}

    private BoarPlayerManager playerManager;

    public void init() {
        this.playerManager = new BoarPlayerManager();
        new GeyserSessionJoinEvent();

        PacketEvents.getApi().getCloudburst().register(new NetworkLatencyPacket());
        PacketEvents.getApi().getCloudburst().register(new WorldSimulationPacket());
        PacketEvents.getApi().getCloudburst().register(new AttributeSimulationPacket());
        PacketEvents.getApi().getCloudburst().register(new PlayerEffectPacket());
        PacketEvents.getApi().getCloudburst().register(new PlayerTeleportPacket());
        PacketEvents.getApi().getCloudburst().register(new PlayerVelocityPacket());
        PacketEvents.getApi().getCloudburst().register(new MovementCheckRunner());
        PacketEvents.getApi().getCloudburst().register(new FinalPacketListener());

        PacketEvents.getApi().getMcpl().register(new WorldSimulationPacket());
        PacketEvents.getApi().getMcpl().register(new AttributeSimulationPacket());
    }

    public void terminate() {
        PacketEvents.getApi().terminate();
        this.playerManager.clear();
    }
}
