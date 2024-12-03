package ac.boar.anticheat.check.impl.velocity;

import ac.boar.anticheat.check.api.CheckInfo;
import ac.boar.anticheat.check.api.impl.OffsetHandlerCheck;
import ac.boar.anticheat.prediction.engine.data.VectorType;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.utils.math.Vec3f;
import org.bukkit.Bukkit;

import java.util.Iterator;
import java.util.Map;

@CheckInfo(name = "Velocity", type = "A")
public class VelocityA extends OffsetHandlerCheck {
    public VelocityA(BoarPlayer player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(double offset) {
        if (System.currentTimeMillis() - player.joinedTime < 5000L) {
            return;
        }

        if (player.closetVector.getType() == VectorType.VELOCITY) {
            return;
        }

        Iterator<Map.Entry<Long, Vec3f>> iterator = player.queuedVelocities.entrySet().iterator();

        Map.Entry<Long, Vec3f> entry;
        while (iterator.hasNext() && (entry = iterator.next()) != null) {
            if (player.lastReceivedId - entry.getKey() < 2) {
                break;
            }

            Bukkit.broadcastMessage("fail VelocityA!");
            iterator.remove();
        }
    }
}
