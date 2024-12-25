package ac.boar.anticheat.packets.player;

import ac.boar.anticheat.data.AttributeData;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.bedrock.geyser.GeyserPacketListener;
import ac.boar.protocol.event.bedrock.geyser.GeyserSendEvent;
import ac.boar.protocol.event.java.PacketListener;
import ac.boar.protocol.event.java.PacketSendEvent;
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer;
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket;
import org.geysermc.geyser.entity.attribute.GeyserAttributeType;
import org.geysermc.mcprotocollib.protocol.data.game.entity.attribute.Attribute;
import org.geysermc.mcprotocollib.protocol.data.game.entity.attribute.AttributeModifier;
import org.geysermc.mcprotocollib.protocol.data.game.entity.attribute.AttributeType;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundUpdateAttributesPacket;

public class PlayerAbilitiesPacket implements GeyserPacketListener, PacketListener {
    @Override
    public void onPacketSend(GeyserSendEvent event) {
        final BoarPlayer player = event.getPlayer();
        if (event.getPacket() instanceof UpdateAbilitiesPacket updateAbilitiesPacket) {
            if (updateAbilitiesPacket.getUniqueEntityId() != player.runtimeEntityId) {
                return;
            }

            event.getPostTasks().add(() -> player.sendTransaction(event.isImmediate()));
            player.latencyUtil.addTransactionToQueue(player.lastSentId + 1, () -> {
                player.abilities.getAbilities().clear();
                for (AbilityLayer layer : updateAbilitiesPacket.getAbilityLayers()) {
                    if (layer.getLayerType() == AbilityLayer.Type.BASE) {
                        player.attributes.get(GeyserAttributeType.MOVEMENT_SPEED).setBaseValue(layer.getWalkSpeed());
                    }

                    player.abilities.getAbilities().addAll(layer.getAbilityValues());
                }
            });
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        final BoarPlayer player = event.getPlayer();
        if (!(event.getPacket() instanceof ClientboundUpdateAttributesPacket attributesPacket) || attributesPacket.getEntityId() != player.javaEntityId) {
            return;
        }

        for (final Attribute data : attributesPacket.getAttributes()) {
            if (data.getType() != AttributeType.Builtin.MOVEMENT_SPEED) {
                return;
            }

            final AttributeData data1 = player.attributes.get(GeyserAttributeType.MOVEMENT_SPEED);
            player.sendTransaction();
            player.latencyUtil.addTransactionToQueue(player.lastSentId, () -> {
                data1.getModifiers().clear();
                data1.setBaseValue((float) data.getValue());
                player.hasSprintingAttribute = false;
                for (final AttributeModifier modifier : data.getModifiers()) {
                    final String id = modifier.toString().split(",")[0].replace("AttributeModifier(id=", "");
                    if (id.equals("minecraft:sprinting")) {
                        player.hasSprintingAttribute = true;
                    } else {
                        data1.getModifiers().put(id, new ac.boar.anticheat.data.AttributeModifier(id, modifier.getAmount(), modifier.getOperation()));
                    }
                }
            });
        }
    }
}
