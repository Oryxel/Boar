package ac.boar.anticheat.packets;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.java.PacketListener;
import ac.boar.protocol.event.java.PacketSendEvent;
import org.geysermc.geyser.entity.type.Entity;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundUpdateMobEffectPacket;

public class EffectUpdatePacket implements PacketListener {
    @Override
    public void onPacketSend(PacketSendEvent event) {
        final BoarPlayer player = event.player();

        if (event.packet() instanceof ClientboundUpdateMobEffectPacket) {
            final ClientboundUpdateMobEffectPacket packet = (ClientboundUpdateMobEffectPacket) event.packet();
            Entity entity = player.getSession().getEntityCache().getEntityByJavaId(packet.getEntityId());
            if (entity == null) {
                return;
            }

            if (entity != player.getSession().getPlayerEntity()) {
                return;
            }


        }
    }
}
