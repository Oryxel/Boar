package ac.boar.anticheat.packets.level;

import ac.boar.anticheat.compensated.cache.BoarEntity;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.bedrock.geyser.GeyserPacketListener;
import ac.boar.protocol.event.bedrock.geyser.GeyserSendEvent;
import ac.boar.utils.math.Vec3f;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityLinkData;
import org.cloudburstmc.protocol.bedrock.packet.*;

public class LevelEntityPacket implements GeyserPacketListener {
    @Override
    public void onPacketSend(GeyserSendEvent event) {
        final BoarPlayer player = event.getPlayer();

        if (event.getPacket() instanceof AddEntityPacket) {
            player.compensatedEntity.addEntity((AddEntityPacket) event.getPacket());
        }

        if (event.getPacket() instanceof MoveEntityAbsolutePacket absolute) {
            player.compensatedEntity.queuePositionUpdate(null, event, absolute.getRuntimeEntityId(), new Vec3f(absolute.getPosition()));
        }

        if (event.getPacket() instanceof MoveEntityDeltaPacket delta) {
            player.compensatedEntity.queueDeltaUpdate(event, delta.getRuntimeEntityId(), new Vec3f(delta.getX(), delta.getY(), delta.getZ()), delta.getFlags());
        }

        if (event.getPacket() instanceof RemoveEntityPacket remove) {
            player.compensatedEntity.removeEntity(remove.getUniqueEntityId());
        }

        if (event.getPacket() instanceof SetEntityLinkPacket link) {
            EntityLinkData data = link.getEntityLink();

            if (data.getTo() != player.runtimeEntityId) {
                return;
            }

            BoarEntity vehicle = player.compensatedEntity.getEntityCache(data.getFrom());
            if (vehicle == null) {
                return;
            }

            if (data.getType() == EntityLinkData.Type.REMOVE) {
                player.compensatedEntity.dismount(vehicle);
            } else {
                player.sendTransaction();
                player.compensatedEntity.setRiding(true);
                player.latencyUtil.addTransactionToQueue(player.lastSentId, () -> player.compensatedEntity.setVehicle(vehicle));
            }
        }
    }
}
