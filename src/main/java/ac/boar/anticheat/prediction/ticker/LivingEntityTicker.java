package ac.boar.anticheat.prediction.ticker;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.anticheat.utils.BlockUtil;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.block.type.BlockState;

public class LivingEntityTicker extends EntityTicker {
    public LivingEntityTicker(BoarPlayer player) {
        super(player);
    }

    @Override
    public void tick() {
        super.tick();
        tickBlockCollision();
    }

    public void tickBlockCollision() {
        if (player.onGround) {
            Vector3i lv = player.getLandingPos();
            BlockState lv2 = player.getSession().getGeyser().getWorldManager().blockAt(player.getSession(), lv);

            BlockUtil.onSteppedOn(player, lv, lv2);
        }
    }
}
