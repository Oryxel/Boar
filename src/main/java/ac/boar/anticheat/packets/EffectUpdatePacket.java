package ac.boar.anticheat.packets;

import ac.boar.anticheat.data.StatusEffect;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.java.PacketListener;
import ac.boar.protocol.event.java.PacketSendEvent;
import org.geysermc.geyser.entity.type.Entity;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRemoveMobEffectPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundUpdateMobEffectPacket;

public class EffectUpdatePacket implements PacketListener {
    @Override
    public void onPacketSend(PacketSendEvent event) {
        final BoarPlayer player = event.getPlayer();

        if (event.getPacket() instanceof ClientboundUpdateMobEffectPacket packet) {
            Entity entity = player.getSession().getEntityCache().getEntityByJavaId(packet.getEntityId());
            if (entity == null) {
                return;
            }

            if (entity != player.getSession().getPlayerEntity()) {
                return;
            }

            player.sendTransaction();
            player.latencyUtil.addTransactionToQueue(player.lastSentId, () -> {
                player.statusMap.put(packet.getEffect(), new StatusEffect(packet.getEffect(), packet.getAmplifier(), packet.getDuration()));
            });
        }

        if (event.getPacket() instanceof ClientboundRemoveMobEffectPacket packet) {
            Entity entity = player.getSession().getEntityCache().getEntityByJavaId(packet.getEntityId());
            if (entity == null) {
                return;
            }

            if (entity != player.getSession().getPlayerEntity()) {
                return;
            }

            player.sendTransaction();
            player.latencyUtil.addTransactionToQueue(player.lastSentId, () -> {
                player.statusMap.remove(packet.getEffect());
            });
        }
    }
}
