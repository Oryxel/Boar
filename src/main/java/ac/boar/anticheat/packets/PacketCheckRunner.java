package ac.boar.anticheat.packets;

import ac.boar.anticheat.check.api.Check;
import ac.boar.anticheat.check.api.impl.PacketCheck;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.BedrockPacketListener;
import ac.boar.protocol.event.PacketReceivedEvent;

import java.util.Map;

public class PacketCheckRunner implements BedrockPacketListener {
    @Override
    public void onPacketReceived(PacketReceivedEvent event) {
        final BoarPlayer player = event.getPlayer();
        for (Map.Entry<Class, Check> entry : player.checkHolder.entrySet()) {
            Check v = entry.getValue();
            if (v instanceof PacketCheck) {
                ((PacketCheck) v).onPacketReceived(event);
            }
        }
    }
}
