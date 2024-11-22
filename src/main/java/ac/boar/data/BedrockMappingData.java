package ac.boar.data;

import ac.boar.utils.math.BoundingBox;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BedrockMappingData {

    private static Gson GSON = new Gson();
    private static Map<Integer, List<BoundingBox>> collisionMap = new HashMap<>();
    private static Map<Integer, BlockMappedData> blockCollisionMappings = new HashMap<>();

    public static void load() throws Exception {
        {
            final JsonObject blockCollisions = readJson("1.21.0-block-collisions.json");

            final JsonObject shapes = blockCollisions.getAsJsonObject("shapes");
            for (String element : shapes.keySet()) {
                final int id = Integer.parseInt(element);
                final JsonArray array = shapes.getAsJsonArray(element);

                if (array.isEmpty()) {
                    collisionMap.put(id, List.of(new BoundingBox(0, 0, 0, 0, 0, 0)));
                    continue;
                }

                final List<BoundingBox> collisions = new ArrayList<>();
                for (int i = 0; i < array.size(); i++) {
                    final JsonArray collision = array.get(i).getAsJsonArray();

                    collisions.add(new BoundingBox(collision.get(0).getAsDouble(),
                            collision.get(1).getAsDouble(), collision.get(2).getAsDouble(), collision.get(3).getAsDouble(),
                            collision.get(4).getAsDouble(), collision.get(5).getAsDouble()));
                }

                collisionMap.put(id, collisions);
            }


        }
    }

    private static JsonObject readJson(final String file) {
        try (final InputStream inputStream = BedrockMappingData.class.getClassLoader().getResourceAsStream("/data/" + file)) {
            if (inputStream == null) {
                return null;
            }

            return GSON.fromJson(new InputStreamReader(inputStream), JsonObject.class);
        } catch (IOException e) {
            return null;
        }
    }

    public record BlockMappedData(int stateId, List<BoundingBox> box) {
    }

}
