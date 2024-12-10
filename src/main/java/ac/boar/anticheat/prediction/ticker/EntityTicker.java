package ac.boar.anticheat.prediction.ticker;

import ac.boar.anticheat.user.api.BoarPlayer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EntityTicker {
    protected final BoarPlayer player;

    public void tick() {
        baseTick();
    }

    public void baseTick() {
        player.wasInPowderSnow = player.inPowderSnow;
        player.inPowderSnow = false;

        // TODO
        // updateWaterState();
        // updateSubmergedInWaterState();
        // updateSwimming();
    }
}
