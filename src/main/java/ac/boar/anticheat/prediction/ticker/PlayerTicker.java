package ac.boar.anticheat.prediction.ticker;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.utils.math.BoundingBox;
import org.geysermc.geyser.entity.EntityDefinitions;

public class PlayerTicker extends LivingEntityTicker {
    public PlayerTicker(BoarPlayer player) {
        super(player);
    }

    @Override
    public void tick() {
        super.tick();

        player.boundingBox = BoundingBox.getBoxAt(player.x, player.y, player.z, EntityDefinitions.PLAYER.width(), player.getHeight());
        player.postPredictionVelocities.clear();
    }

    @Override
    public void tickMovement() {
        if (player.lastSneaking || player.sneaking) {
            player.movementInput = player.movementInput.mul(0.3);
        }

        super.tickMovement();
    }
}
