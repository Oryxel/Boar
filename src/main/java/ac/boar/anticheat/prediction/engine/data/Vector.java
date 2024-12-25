package ac.boar.anticheat.prediction.engine.data;

import ac.boar.utils.math.Vec3f;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Vector implements Cloneable {
    private Vec3f velocity;
    private VectorType type;
    private long transactionId = -1; // if this vector is velocity or explosion...

    public Vector(Vec3f vec3f, VectorType type) {
        this.type = type;
        this.velocity = vec3f;
    }

    public Vector(Vec3f vec3f, VectorType type, long transactionId) {
        this.type = type;
        this.velocity = vec3f;
        this.transactionId = transactionId;
    }

    @Override
    public Vector clone() {
        return new Vector(velocity, type, transactionId);
    }
}
