package ac.boar.anticheat.check.api.impl;

import ac.boar.anticheat.check.api.Check;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.bedrock.PacketReceivedEvent;
import ac.boar.protocol.event.java.PacketSendEvent;

public class PacketCheck extends Check {
    public PacketCheck(BoarPlayer player) {
        super(player);
    }

    public void onPacketReceived(final PacketReceivedEvent event) {
    }

    public void onPacketSend(final PacketSendEvent event) {
    }
}
