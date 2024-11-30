package ac.boar.anticheat.check.impl.prediction;

import ac.boar.anticheat.check.api.CheckInfo;
import ac.boar.anticheat.check.api.impl.OffsetHandlerCheck;
import ac.boar.anticheat.prediction.engine.data.VectorType;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.utils.math.Vec3d;
import org.bukkit.Bukkit;

import java.util.Iterator;
import java.util.Map;

@CheckInfo(name = "Velocity")
public class VelocityA extends OffsetHandlerCheck {
    public VelocityA(BoarPlayer player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(double offset) {
        if (player.closetVector.getType() == VectorType.VELOCITY) {
            return;
        }

        Iterator<Map.Entry<Long, Vec3d>> iterator = player.queuedVelocities.entrySet().iterator();

//        Map.Entry<Long, Vec3d> entry;
//        while (iterator.hasNext() && (entry = iterator.next()) != null) {
//            if (entry.getKey() < player.lastReceivedId) {
//                break;
//            }
//
//            Bukkit.broadcastMessage("fail VelocityA!");
//            iterator.remove();
//        }
    }
}
