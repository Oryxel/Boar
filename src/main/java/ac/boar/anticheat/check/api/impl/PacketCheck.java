package ac.boar.anticheat.check.api.impl;

import ac.boar.anticheat.check.api.Check;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.PacketReceivedEvent;

public class PacketCheck extends Check {
    public PacketCheck(BoarPlayer player) {
        super(player);
    }

    public void onPacketReceived(final PacketReceivedEvent event) {
    }
}
