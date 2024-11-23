package ac.boar.anticheat.prediction.engine.base;

import ac.boar.anticheat.prediction.engine.data.Vector;
import ac.boar.utils.math.Vec3d;

import java.util.List;

public interface PredictionEngine {
    List<Vector> gatherAllPossibilities();
    Vec3d applyEndOfTick();
    void travel();
}
