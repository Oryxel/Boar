package ac.boar.anticheat.check.api;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.utils.ChatUtil;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Check {
    protected final BoarPlayer player;

    private final String name = getClass().getDeclaredAnnotation(CheckInfo.class).name(),
            type = getClass().getDeclaredAnnotation(CheckInfo.class).type();
    private final int maxVl = getClass().getDeclaredAnnotation(CheckInfo.class).maxVl();
    private int vl = 0;

    public void fail() {
        fail("");
    }

    public void fail(String verbose) {
        this.vl++;
        ChatUtil.alert("§3" + player.getSession().getPlayerEntity().getDisplayName() + "§7 failed §6" + name + "(" + type + ") §7x" + vl + " " + verbose);
    }
}
