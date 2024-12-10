package ac.boar.anticheat.compensated;

import ac.boar.anticheat.compensated.cache.EntityCache;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.geyser.GeyserSendEvent;
import ac.boar.utils.math.BoundingBox;
import ac.boar.utils.math.Vec3f;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cloudburstmc.protocol.bedrock.packet.AddEntityPacket;
import org.geysermc.geyser.entity.EntityDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Getter
@Setter
public class CompensatedEntity {
    private final BoarPlayer player;
    private final Map<Long, EntityCache> map = new ConcurrentHashMap<>();

    private EntityCache vehicle;
    private boolean riding;

    public EntityCache getEntityCache(long id) {
        return this.map.get(id);
    }

    public void queuePositionUpdate(GeyserSendEvent event, long id, Vec3f vec3f) {
        final EntityCache cache = this.map.get(id);
        if (cache == null) {
            return;
        }

        cache.getOldPositions().add(cache.getUtdPosition().clone());

        final EntityDefinition definition = cache.getDefinition();
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

        final EntityCache cache = new EntityCache(definition.entityType(), definition);
        cache.setPosition(new Vec3f(packet.getPosition()));
        cache.setUtdPosition(cache.getPosition().clone());
        cache.setBoundingBox(BoundingBox.getBoxAt(packet.getPosition().getX(), packet.getPosition().getY(), packet.getPosition().getZ(), definition.width(), definition.height()));
        this.map.put(packet.getRuntimeEntityId(), cache);

        player.sendTransaction();
    }

    public void removeEntity(final long id) {
        EntityCache cache = this.map.remove(id);
        if (cache == vehicle) {
            dismount(vehicle);
        }
    }

    public void dismount(EntityCache cache) {
        if (cache != vehicle) {
            return;
        }
        player.sendTransaction();
        player.latencyUtil.addTransactionToQueue(player.lastSentId, () -> vehicle = null);
        riding = false;
    }
}
