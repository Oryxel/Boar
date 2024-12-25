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
                    "SPRINTING=" + player.closetVector.isSprinting() + ", SNEAKING=" + player.sneaking + ", ST" + player.sinceTeleport + ", IW=" + player.touchingWater);

            Bukkit.broadcastMessage("Before Travel: " + player.closetVector.getVelocity().toVector3f().toString());
            Bukkit.broadcastMessage("CV: " + player.prevEOT.toVector3f().toString());

            Bukkit.broadcastMessage("A EOT: " + player.eotVelocity.toVector3f().toString());

            double eotOffset = player.claimedEOT.distance(player.eotVelocity.toVector3f());
            Bukkit.broadcastMessage("EOT O: " + (eotOffset > 1e-4 ? "§b" : "§a") + eotOffset  + "," + player.claimedEOT.toString());

            Bukkit.broadcastMessage(player.x + "," + player.y + "," + player.z);
            Bukkit.broadcastMessage((offset > 1e-4 ? "§c" : "§a") + "O:" + player.claimedEOT.distance(player.eotVelocity.toVector3f())
                    + ", EOT: " + player.claimedEOT.toString());

//            if (player.sprinting != player.closetVector.sprinting) {
//                Bukkit.broadcastMessage("sprinting speed not match -> since sprinting:" + player.sinceSprinting);
//            }
        }
    }
}
