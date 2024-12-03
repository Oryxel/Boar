package ac.boar.anticheat.packets;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.bedrock.geyser.GeyserPacketListener;
import ac.boar.protocol.event.bedrock.geyser.GeyserSendEvent;
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer;
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket;

public class AbilitiesUpdatePacket implements GeyserPacketListener {
    @Override
    public void onPacketSend(GeyserSendEvent event) {
        if (!(event.getPacket() instanceof UpdateAbilitiesPacket)) {
            return;
        }
        final BoarPlayer player = event.getPlayer();

        final UpdateAbilitiesPacket updateAbilitiesPacket = (UpdateAbilitiesPacket) event.getPacket();
        if (updateAbilitiesPacket.getUniqueEntityId() != player.getSession().getPlayerEntity().getGeyserId()) {
            return;
        }

        event.getPostTasks().add(() -> player.sendTransaction(true));
        player.latencyUtil.addTransactionToQueue(player.lastSentId + 1, () -> {
            player.abilities.getAbilities().clear();
            for (AbilityLayer layer : updateAbilitiesPacket.getAbilityLayers()) {
                if (layer.getLayerType() == AbilityLayer.Type.BASE) {
                    player.abilities.setWalkSpeed(layer.getWalkSpeed());
                }

                player.abilities.getAbilities().addAll(layer.getAbilityValues());
            }
        });
    }
}
