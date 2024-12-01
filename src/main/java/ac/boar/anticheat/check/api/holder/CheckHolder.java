package ac.boar.anticheat.check.api.holder;

import ac.boar.anticheat.check.api.Check;
import ac.boar.anticheat.check.impl.combat.ReachA;
import ac.boar.anticheat.check.impl.prediction.PredictionA;
import ac.boar.anticheat.check.impl.velocity.VelocityA;
import ac.boar.anticheat.check.impl.timer.TimerA;
import ac.boar.anticheat.user.api.BoarPlayer;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

@RequiredArgsConstructor
public class CheckHolder extends HashMap<Class, Check> {
    private final BoarPlayer player;

    public void init() {
        this.put(PredictionA.class, new PredictionA(player));
        this.put(VelocityA.class, new VelocityA(player));

        this.put(ReachA.class, new ReachA(player));
        this.put(TimerA.class, new TimerA(player));
    }
}
