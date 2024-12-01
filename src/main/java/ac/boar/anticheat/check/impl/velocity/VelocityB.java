package ac.boar.anticheat.check.impl.velocity;

import ac.boar.anticheat.check.api.CheckInfo;
import ac.boar.anticheat.check.api.impl.OffsetHandlerCheck;
import ac.boar.anticheat.prediction.engine.data.VectorType;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.utils.math.Vec3d;
import org.bukkit.Bukkit;

import java.util.Iterator;
import java.util.Map;

@CheckInfo(name = "Velocity", type = "B")
public class VelocityB extends OffsetHandlerCheck {
    public VelocityB(BoarPlayer player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(double offset) {
        if (player.closetVector.getType() == VectorType.EXPLOSION) {
            return;
        }

        Iterator<Map.Entry<Long, Vec3d>> iterator = player.queuedExplosions.entrySet().iterator();

        Map.Entry<Long, Vec3d> entry;
        while (iterator.hasNext() && (entry = iterator.next()) != null) {
            if (player.lastReceivedId - entry.getKey() < 2) {
                break;
            }

            Bukkit.broadcastMessage("fail VelocityB!");
            iterator.remove();
        }
    }
}
