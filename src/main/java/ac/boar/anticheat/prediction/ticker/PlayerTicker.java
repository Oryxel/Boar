package ac.boar.anticheat.prediction.ticker;

import ac.boar.anticheat.user.api.BoarPlayer;

public class PlayerTicker extends EntityTicker {
    public PlayerTicker(BoarPlayer player) {
        super(player);
    }

    @Override
    public void tickMovement() {
        if (player.lastSneaking || player.sneaking) {
            player.movementInput = player.movementInput.mul(0.3);
        }

        super.tickMovement();
    }
}
