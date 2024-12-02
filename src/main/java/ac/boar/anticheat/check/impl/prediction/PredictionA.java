package ac.boar.anticheat.check.impl.prediction;

import ac.boar.anticheat.check.api.CheckInfo;
import ac.boar.anticheat.check.api.impl.OffsetHandlerCheck;
import ac.boar.anticheat.user.api.BoarPlayer;

@CheckInfo(name = "Prediction")
public class PredictionA extends OffsetHandlerCheck {
    public PredictionA(BoarPlayer player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(double offset) {
        if (System.currentTimeMillis() - player.joinedTime < 2000L) {
            return;
        }

        if (offset > 1e-4) {
            player.teleportUtil.setBackWithSimulation();
        }
    }
}
