package ac.boar.anticheat.prediction;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.utils.math.Vec3f;

public class StupidWorkAround {
    // Screw it, this going to stay here until I figure out how to correctly calculate collision that have 1e-4 accuracy for Bedrock player.
    // Yes I know this is bad, this going to stay here regardless, mainly due to my incompetent
    public static Vec3f postPredictionPatch(final BoarPlayer player, Vec3f after) {
        Vec3f fixed = after.clone();

        // This going to allow for velocity bypass, TODO: fix collision and delete this abomination
        if (player.collideX || player.collideZ) {
            float lengthPrediction = after.length();
            float lengthActual = player.actualVelocity.length();

            if (lengthPrediction > lengthActual && lengthPrediction - lengthActual < 0.05) {
                fixed = player.actualVelocity.clone();
            }
        }

        return fixed;
    }
}
