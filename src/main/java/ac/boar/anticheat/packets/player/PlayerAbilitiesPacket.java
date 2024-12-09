package ac.boar.anticheat.packets.player;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.geyser.GeyserPacketListener;
import ac.boar.protocol.event.geyser.GeyserSendEvent;
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer;
import org.cloudburstmc.protocol.bedrock.data.AttributeData;
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateAttributesPacket;
import org.geysermc.geyser.entity.attribute.GeyserAttributeType;

public class PlayerAbilitiesPacket implements GeyserPacketListener {
    @Override
    public void onPacketSend(GeyserSendEvent event) {
        final BoarPlayer player = event.getPlayer();
        if (event.getPacket() instanceof UpdateAbilitiesPacket updateAbilitiesPacket) {
            if (updateAbilitiesPacket.getUniqueEntityId() != player.runtimeEntityId) {
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

        if (event.getPacket() instanceof UpdateAttributesPacket updateAttributesPacket) {
            if (updateAttributesPacket.getRuntimeEntityId() != player.runtimeEntityId) {
                return;
            }

            for (AttributeData attributeData : updateAttributesPacket.getAttributes()) {
                if (!attributeData.getName().equalsIgnoreCase(GeyserAttributeType.MOVEMENT_SPEED.getBedrockIdentifier())) {
                    continue;
                }

                player.latencyUtil.addTransactionToQueue(player.lastSentId + 1, () -> {
                    player.abilities.setWalkSpeed(attributeData.getValue());
                });
                event.getPostTasks().add(() -> player.sendTransaction(true));
            }
        }
    }
}
