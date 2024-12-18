package ac.boar.anticheat.compensated;

import ac.boar.anticheat.compensated.cache.BoarEntity;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.bedrock.geyser.GeyserSendEvent;
import ac.boar.utils.math.BoundingBox;
import ac.boar.utils.math.Vec3f;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cloudburstmc.protocol.bedrock.packet.AddEntityPacket;
import org.cloudburstmc.protocol.bedrock.packet.MoveEntityDeltaPacket;
import org.geysermc.geyser.entity.EntityDefinition;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Getter
@Setter
public class CompensatedEntity {
    private final BoarPlayer player;
    private final Map<Long, BoarEntity> map = new ConcurrentHashMap<>();

    private BoarEntity vehicle;
    private boolean riding;

    public BoarEntity getEntityCache(long id) {
        return this.map.get(id);
    }

    public void queueDeltaUpdate(GeyserSendEvent event, long id, Vec3f vec3f, Set<MoveEntityDeltaPacket.Flag> flags) {
        final BoarEntity cache = this.map.get(id);
        if (cache == null) {
            return;
        }

        float x = vec3f.x, y = vec3f.y, z = vec3f.z;
        if (!flags.contains(MoveEntityDeltaPacket.Flag.HAS_X)) {
            x = cache.getUtdPosition().x;
        }
        if (!flags.contains(MoveEntityDeltaPacket.Flag.HAS_Y)) {
            y = cache.getUtdPosition().y;
        }
        if (!flags.contains(MoveEntityDeltaPacket.Flag.HAS_Z)) {
            z = cache.getUtdPosition().z;
        }

        if (!flags.contains(MoveEntityDeltaPacket.Flag.HAS_X) && !flags.contains(MoveEntityDeltaPacket.Flag.HAS_Y) && !flags.contains(MoveEntityDeltaPacket.Flag.HAS_Z)) {
            return;
        }

        queuePositionUpdate(cache, event, id, new Vec3f(x, y, z));
    }

    public void queuePositionUpdate(BoarEntity entity, GeyserSendEvent event, long id, Vec3f vec3f) {
        final BoarEntity cache = entity == null ? this.map.get(id) : entity;
        if (cache == null) {
            return;
        }

        cache.getOldPositions().add(cache.getUtdPosition().clone());

        final EntityDefinition<?> definition = cache.getDefinition();
        final BoundingBox newBox = BoundingBox.getBoxAt(vec3f.x, vec3f.y, vec3f.z, definition.width(), definition.height());
        cache.setUtdPosition(vec3f.clone());

        // We need 2 transaction to check, if player receive the first they could already have received the update packet
        // Or they lag right before they receive the actual update position packet so we can't be sure
        // But if player respond to the transaction AFTER the position packet they 100% already receive the packet.
        player.sendTransaction(event.isImmediate());
        player.latencyUtil.addTransactionToQueue(player.lastSentId, () -> {
            cache.setBoundingBox(cache.getBoundingBox().union(newBox));
            cache.setPosition(vec3f);
        });

        player.latencyUtil.addTransactionToQueue(player.lastSentId + 1, () -> {
            cache.setBoundingBox(newBox);

            if (cache.getOldPositions().size() > 4) {
                cache.getOldPositions().poll();
            }
        });

        event.getPostTasks().add(() -> player.sendTransaction(event.isImmediate()));
    }

    public void addEntity(final AddEntityPacket packet) {
        final EntityDefinition<?> definition = player.getSession().getEntityCache().getEntityByGeyserId(packet.getRuntimeEntityId()).getDefinition();
        if (definition == null) {
            return;
        }

        final BoarEntity cache = new BoarEntity(definition.entityType(), definition);
        cache.setPosition(new Vec3f(packet.getPosition()));
        cache.setUtdPosition(cache.getPosition().clone());
        cache.setBoundingBox(BoundingBox.getBoxAt(packet.getPosition().getX(), packet.getPosition().getY(), packet.getPosition().getZ(), definition.width(), definition.height()));
        this.map.put(packet.getRuntimeEntityId(), cache);

        player.sendTransaction();
    }

    public void removeEntity(final long id) {
        BoarEntity cache = this.map.remove(id);
        if (cache == vehicle) {
            dismount(vehicle);
        }
    }

    public void dismount(BoarEntity cache) {
        if (cache != vehicle) {
            return;
        }
        player.sendTransaction();
        player.latencyUtil.addTransactionToQueue(player.lastSentId, () -> vehicle = null);
        riding = false;
    }
}
