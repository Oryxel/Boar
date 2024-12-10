package ac.boar.anticheat.prediction.ticker;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.anticheat.utils.BlockUtil;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.block.type.BlockState;

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

    protected final void tickBlockCollision() {
        if (player.onGround) {
            Vector3i lv = player.getLandingPos();
            BlockState lv2 = player.getSession().getGeyser().getWorldManager().blockAt(player.getSession(), lv);

            BlockUtil.onSteppedOn(player, lv, lv2);
        }
    }
}
