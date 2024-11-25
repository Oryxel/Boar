package ac.boar.anticheat.compensated;

import ac.boar.anticheat.compensated.cache.EntityCache;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.java.PacketSendEvent;
import ac.boar.utils.math.BoundingBox;
import ac.boar.utils.math.Vec3d;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.geysermc.geyser.entity.EntityDefinition;
import org.geysermc.geyser.entity.type.Entity;
import org.geysermc.geyser.registry.Registries;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddEntityPacket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Getter
public class CompensatedEntity {
    private final BoarPlayer player;
    private final Map<Integer, EntityCache> map = new ConcurrentHashMap<>();

    public Entity getGeyserEntity(long id) {
        return player.getSession().getEntityCache().getEntityByGeyserId(id);
    }

    public EntityCache getEntityCache(long id) {
        Entity entity = getGeyserEntity(id);
        if (entity == null) {
            return null;
        }

        final EntityCache cache = this.map.get(entity.javaId());
        return cache;
    }

    public void queueRelativeUpdate(PacketSendEvent event, int id, double relX, double relY, double relZ) {
        final EntityCache cache = this.map.get(id);
        if (cache == null) {
            return;
        }

        final Vec3d pos = cache.getUtdPosition();
        final Vec3d vec3d = new Vec3d(pos.getX() + relX, pos.getY() + relY, pos.getZ() + relZ);
        queuePositionUpdate(event, id, vec3d);
    }

    public void queuePositionUpdate(PacketSendEvent event, int id, Vec3d vec3d) {
        final EntityCache cache = this.map.get(id);
        if (cache == null) {
            return;
        }

        final EntityDefinition definition = cache.getDefinition();
        final BoundingBox newBox = BoundingBox.getBoxAt(vec3d.x, vec3d.y, vec3d.z, definition.width(), definition.height());

        cache.setUtdPosition(vec3d.clone());
        player.sendTransaction();
        player.latencyUtil.addTransactionToQueue(player.lastSentId, () -> {
            cache.setPosition(vec3d);
            cache.setBoundingBox(cache.getBoundingBox().union(newBox));
        });

        player.latencyUtil.addTransactionToQueue(player.lastSentId + 1, () -> cache.setBoundingBox(newBox));
        event.getPostTasks().add(player::sendTransaction);
    }

    public void addEntity(final ClientboundAddEntityPacket packet) {
        final EntityDefinition<?> definition = Registries.ENTITY_DEFINITIONS.get(packet.getType());
        if (definition == null) {
            return;
        }

        final EntityCache cache = new EntityCache(packet.getType(), definition);
        cache.setPosition(new Vec3d(packet.getX(), packet.getY(), packet.getZ()));
        cache.setUtdPosition(cache.getPosition().clone());
        cache.setBoundingBox(BoundingBox.getBoxAt(packet.getX(), packet.getY(), packet.getZ(), definition.width(), definition.height()));
        this.map.put(packet.getEntityId(), cache);

        player.sendTransaction();
    }

    public void removeEntity(final int id) {
        this.map.remove(id);
    }
}
