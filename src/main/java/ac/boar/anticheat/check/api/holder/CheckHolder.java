package ac.boar.anticheat.check.api.holder;

import ac.boar.anticheat.check.api.Check;
import ac.boar.anticheat.check.impl.combat.TestReachA;
import ac.boar.anticheat.user.api.BoarPlayer;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

@RequiredArgsConstructor
public class CheckHolder extends HashMap<Class, Check> {
    private final BoarPlayer player;

    public void init() {
        this.put(TestReachA.class, new TestReachA(player));
    }
}
