package ac.boar.anticheat.prediction.ticker;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.utils.math.BoundingBox;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.translator.collision.BlockCollision;
import org.geysermc.geyser.util.BlockUtils;

public class PlayerTicker extends EntityTicker {
    public PlayerTicker(BoarPlayer player) {
        super(player);
    }

    @Override
    public void tickMovement() {
        if (player.sneaking) {
            player.movementInput = player.movementInput.multiply(0.3);
        }

        // Player got instantly moved out of the block instead of slowly move out like when I test on single player.
        // Just give player extra offset, they're supposed to move 1 block anyway.
        player.extraUncertainOffset += wouldCollideAt(player.lastX, player.lastY, player.lastZ);

        super.tickMovement();
    }

    private double wouldCollideAt(double x, double y, double z) {
        Vector3i pos = Vector3i.from(x, y, z);
        BoundingBox box = player.boundingBox.expand(1);

        BlockCollision collision = BlockUtils.getCollisionAt(player.getSession(), pos);
        if (collision == null) {
            return 0;
        }

        boolean intersects = collision.checkIntersection(pos, new org.geysermc.geyser.level.physics.BoundingBox(
                box.minX + ((box.maxX - box.minX) / 2), box.minY + ((box.maxY - box.minY) / 2),
                box.minZ + ((box.maxZ - box.minZ) / 2), box.maxX - box.minX, box.maxY - box.minY,
                box.maxZ - box.minZ
        ));

        if (!intersects) {
            return 0;
        }

        return 1D;
    }
}
