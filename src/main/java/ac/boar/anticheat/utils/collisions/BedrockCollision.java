package ac.boar.anticheat.utils.collisions;

import ac.boar.utils.math.BoundingBox;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.type.BlockState;

import java.util.ArrayList;
import java.util.List;

public class BedrockCollision {
    // Some of the bounding boxes on bedrock is different, this method is for that
    // Not entirely accurate, I get the value through debugging... but better than nothing.
    public static List<BoundingBox> getBoundingBox(BlockState state) {
        if (state.is(Blocks.ENDER_CHEST)) {
            return List.of(new BoundingBox(0F, 0F, 0F, 0.95F, 0.95F, 0.95F));
        }

        if (state.is(Blocks.CAULDRON)) {
            float f = 0.125F;
            List<BoundingBox> boxes = new ArrayList<>();
            boxes.add(new BoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.3125F, 1.0F));
            boxes.add(new BoundingBox(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F));
            boxes.add(new BoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f));
            boxes.add(new BoundingBox(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F));
            boxes.add(new BoundingBox(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F));
            return boxes;
        }

        return List.of();
    }
}
