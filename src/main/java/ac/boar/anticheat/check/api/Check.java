package ac.boar.anticheat.check.api;

import ac.boar.anticheat.user.api.BoarPlayer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Check {
    protected final BoarPlayer player;

    private final String name = getClass().getDeclaredAnnotation(CheckInfo.class).name(),
                type = getClass().getDeclaredAnnotation(CheckInfo.class).type();
    private final int maxVl = getClass().getDeclaredAnnotation(CheckInfo.class).maxVl();
    private int vl = 0;

    public void flag() {
        this.vl++;
    }
}
