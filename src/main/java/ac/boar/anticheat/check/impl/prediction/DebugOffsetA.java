package ac.boar.anticheat.check.impl.prediction;

import ac.boar.anticheat.check.api.CheckInfo;
import ac.boar.anticheat.check.api.impl.OffsetHandlerCheck;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.utils.math.Vec3f;
import org.bukkit.Bukkit;

@CheckInfo(name = "DebugOffset")
public class DebugOffsetA extends OffsetHandlerCheck {
    public DebugOffsetA(BoarPlayer player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(double offset) {
        Vec3f predicted = player.predictedVelocity;

        if (player.actualVelocity.length() > 0 || offset > 1e-4) {
            Bukkit.broadcastMessage((offset > 1e-4 ? "§c" : "§a") + "O:" + offset + ", T: " + player.closetVector.getType() + ", P: " +
                    predicted.x + "," + predicted.y + "," + predicted.z);

            Bukkit.broadcastMessage("§7A: " + player.actualVelocity.x + "," + player.actualVelocity.y + "," + player.actualVelocity.z + ", " +
                    "SPRINTING=" + player.closetVector.isSprinting() + ", SNEAKING=" + player.sneaking + ", SS" + player.sinceSprinting +
                    ", SN" + player.sinceSneaking);
        }

        if (player.actualVelocity.length() > 0) {
            Bukkit.broadcastMessage(player.x + "," + player.y + "," + player.z);
        }
    }
}
