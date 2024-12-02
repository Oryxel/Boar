package ac.boar.anticheat.prediction.engine.data;

import ac.boar.utils.math.Vec3f;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Vector {
    private Vec3f velocity;
    private VectorType type;
    private long transactionId = -1; // if this vector is velocity or explosion...

    public boolean sprinting = false;

    public Vector(Vec3f vec3F, VectorType type) {
        this.type = type;
        this.velocity = vec3F;
    }

    public Vector(Vec3f vec3F, VectorType type, long transactionId) {
        this.type = type;
        this.velocity = vec3F;
        this.transactionId = transactionId;
    }
}
