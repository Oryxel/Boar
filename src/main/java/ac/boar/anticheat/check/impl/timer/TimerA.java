package ac.boar.anticheat.check.impl.timer;

import ac.boar.anticheat.check.api.CheckInfo;
import ac.boar.anticheat.check.api.impl.PacketCheck;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.bedrock.PacketReceivedEvent;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;

@CheckInfo(name = "Timer", type = "A")
public class TimerA extends PacketCheck {
    private long ms = 0, balance;

    public TimerA(BoarPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceived(PacketReceivedEvent event) {
        if (System.currentTimeMillis() - player.joinedTime < 5000L) {
            return;
        }

        if (event.getPacket() instanceof PlayerAuthInputPacket) {
            if (player.lastTickWasTeleport) {
                return;
            }

            if (this.ms == 0) {
                this.ms = System.nanoTime();
                return;
            }

            balance += (long) 4e7;
            balance -= System.nanoTime() - this.ms;

            this.ms = System.nanoTime();
        }
    }
}
