package ac.boar.anticheat.compensated.cache;

import ac.boar.utils.math.BoundingBox;
import ac.boar.utils.math.Vec3d;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.geysermc.geyser.entity.EntityDefinition;
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
@Getter
@Setter
public class EntityCache {
    private final EntityType type;
    private final EntityDefinition definition;
    private Vec3d position = Vec3d.ZERO, utdPosition = Vec3d.ZERO;
    private BoundingBox boundingBox = new BoundingBox(0, 0, 0, 0, 0, 0);

    private Queue<Vec3d> oldPositions = new ConcurrentLinkedQueue<>();
}
