package ac.boar.anticheat.utils.collisions;

import ac.boar.utils.math.BoundingBox;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.ChestType;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.block.type.ChestBlock;
import org.geysermc.geyser.level.block.type.DoorBlock;
import org.geysermc.geyser.level.block.type.TrapDoorBlock;
import org.geysermc.geyser.level.physics.Direction;

import java.util.ArrayList;
import java.util.List;

public class BedrockCollision {
    public static List<BoundingBox> getBoundingBox(BlockState state) {
        if (state.is(Blocks.ENDER_CHEST)) {
            return List.of(new BoundingBox(0.025F, 0F, 0.025F, 0.975F, 0.95F, 0.975F));
        }
        
        if (state.block() instanceof ChestBlock) {
            float f = 0.025F;
            final BoundingBox DOUBLE_NORTH_SHAPE = new BoundingBox(f, 0, 0, 1 - f, 0.95F, 1 - f);
            final BoundingBox DOUBLE_SOUTH_SHAPE = new BoundingBox(f, 0, f, 1 - f, 0.95F, 1);
            final BoundingBox DOUBLE_WEST_SHAPE = new BoundingBox(0, 0, f, 1 - f, 0.95F, 1 - f);
            final BoundingBox DOUBLE_EAST_SHAPE = new BoundingBox(f, 0, f, 1, 0.95F, 1 - f);
            final BoundingBox SINGLE_SHAPE = new BoundingBox(f, 0, f, 1 - f, 0.95F, 1 - f);

            final ChestType type = state.getValue(Properties.CHEST_TYPE);
            Direction facing = state.getValue(Properties.HORIZONTAL_FACING);
            if (type == ChestType.LEFT) {
                facing = switch (facing) {
                    case SOUTH -> Direction.WEST;
                    case WEST -> Direction.NORTH;
                    case EAST -> Direction.SOUTH;
                    default -> Direction.EAST;
                };
            } else {
                facing = switch (facing) {
                    case SOUTH -> Direction.EAST;
                    case WEST -> Direction.SOUTH;
                    case EAST -> Direction.NORTH;
                    default -> Direction.WEST;
                };
            }

            BoundingBox box;
            if (type == ChestType.SINGLE) {
                box = SINGLE_SHAPE;
            } else {
                switch (facing) {
                    case SOUTH -> box = DOUBLE_SOUTH_SHAPE;
                    case WEST -> box = DOUBLE_WEST_SHAPE;
                    case EAST -> box = DOUBLE_EAST_SHAPE;
                    default -> box = DOUBLE_NORTH_SHAPE;
                }
            }

            return List.of(box);
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

        if (state.is(Blocks.CONDUIT)) {
            float f = 0.25F;
            return List.of(new BoundingBox(f, 0, f, 1 - f, 0.5F, 1 - f));
        }

        if (state.is(Blocks.CACTUS)) {
            float f = 0.0625F;
            return List.of(new BoundingBox(f, 0, f, 1 - f, 1, 1 - f));
        }

        if (state.block() instanceof TrapDoorBlock) {
            final BoundingBox EAST_SHAPE = new BoundingBox(0, 0, 0, 0.1825F, 1, 1);
            final BoundingBox WEST_SHAPE = new BoundingBox(0.8175F, 0, 0, 1, 1, 1);
            final BoundingBox SOUTH_SHAPE = new BoundingBox(0, 0, 0, 1, 1, 0.1825F);
            final BoundingBox NORTH_SHAPE = new BoundingBox(0, 0, 0.8175F, 1, 1, 1);

            final BoundingBox OPEN_BOTTOM_SHAPE = new BoundingBox(0, 0, 0, 1, 0.1825F, 1);
            final BoundingBox OPEN_TOP_SHAPE = new BoundingBox(0, 0.8175F, 0, 1, 1, 1);

            BoundingBox box;
            if (!state.getValue(Properties.OPEN)) {
                box = state.getValue(Properties.HALF).equalsIgnoreCase("top") ? OPEN_TOP_SHAPE : OPEN_BOTTOM_SHAPE;
            } else {
                switch (state.getValue(Properties.HORIZONTAL_FACING)) {
                    case SOUTH -> box = SOUTH_SHAPE;
                    case WEST -> box = WEST_SHAPE;
                    case EAST -> box = EAST_SHAPE;
                    default -> box = NORTH_SHAPE;
                }
            }

            return List.of(box);
        }

        if (state.block() instanceof DoorBlock) {
            final BoundingBox NORTH_SHAPE = new BoundingBox(0, 0, 0, 1, 1, 0.1825F);
            final BoundingBox SOUTH_SHAPE = new BoundingBox(0, 0, 0.8175F, 1, 1, 1);
            final BoundingBox EAST_SHAPE = new BoundingBox(0.8175F, 0, 0, 1, 1, 1);
            final BoundingBox WEST_SHAPE = new BoundingBox(0, 0, 0, 0.1825F, 1, 1);

            Direction direction = state.getValue(Properties.HORIZONTAL_FACING);
            boolean bl = !state.getValue(Properties.OPEN);
            boolean bl2 = state.getValue(Properties.DOOR_HINGE).equalsIgnoreCase("right");

            BoundingBox box;
            switch (direction) {
                case SOUTH -> box = bl ? NORTH_SHAPE : (bl2 ? WEST_SHAPE : EAST_SHAPE);
                case WEST -> box = bl ? EAST_SHAPE : (bl2 ? NORTH_SHAPE : SOUTH_SHAPE);
                case NORTH -> box = bl ? SOUTH_SHAPE : (bl2 ? EAST_SHAPE : WEST_SHAPE);
                default -> box = bl ? WEST_SHAPE : (bl2 ? SOUTH_SHAPE : NORTH_SHAPE);
            }

            return List.of(box);
        }

        return List.of();
    }
}
