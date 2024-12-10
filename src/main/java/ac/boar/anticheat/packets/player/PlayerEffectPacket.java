package ac.boar.anticheat.packets.player;

import ac.boar.anticheat.data.StatusEffect;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.anticheat.utils.EntityUtil;
import ac.boar.protocol.event.geyser.GeyserPacketListener;
import ac.boar.protocol.event.geyser.GeyserSendEvent;
import org.cloudburstmc.protocol.bedrock.packet.MobEffectPacket;
import org.geysermc.mcprotocollib.protocol.data.game.entity.Effect;

public class PlayerEffectPacket implements GeyserPacketListener {
    @Override
    public void onPacketSend(GeyserSendEvent event) {
        final BoarPlayer player = event.getPlayer();

        if (event.getPacket() instanceof MobEffectPacket packet) {
            if (packet.getRuntimeEntityId() != player.runtimeEntityId) {
                return;
            }

            Effect effect = EntityUtil.toJavaEffect(packet.getEffectId());
            if (effect == null) {
                return;
            }

            player.sendTransaction();

            if (packet.getEvent() == MobEffectPacket.Event.ADD) {
                player.latencyUtil.addTransactionToQueue(player.lastSentId, () -> {
                    player.statusMap.put(effect, new StatusEffect(effect, packet.getAmplifier(), packet.getDuration()));
                });
            } else if (packet.getEvent() == MobEffectPacket.Event.REMOVE) {
                player.latencyUtil.addTransactionToQueue(player.lastSentId, () -> {
                    player.statusMap.remove(effect);
                });
            }
        }
    }
}
