package ac.boar.anticheat.prediction.engine.data;

import ac.boar.utils.math.Vec3d;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Vector {
    private Vec3d velocity;
    private VectorType type;
    private long transactionId = -1; // if this vector is velocity or explosion...

    public Vector(Vec3d vec3d, VectorType type) {
        this.type = type;
        this.velocity = vec3d;
    }
}
