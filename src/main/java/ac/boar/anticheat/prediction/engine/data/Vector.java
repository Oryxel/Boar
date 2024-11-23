package ac.boar.anticheat.prediction.engine.data;

import ac.boar.utils.math.Vec3d;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Vector {
    private Vec3d velocity;
    private VectorType type;
}
