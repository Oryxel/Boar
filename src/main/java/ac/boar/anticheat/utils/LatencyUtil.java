package ac.boar.anticheat.utils;

import ac.boar.anticheat.user.api.BoarPlayer;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RequiredArgsConstructor
public final class LatencyUtil {
    private final BoarPlayer player;
    private final Map<Long, Runnable> map = new HashMap<>();

    public void addTransactionToQueue(long id, Runnable runnable) {
        if (id <= player.lastReceivedId) {
            runnable.run();
            return;
        }

        this.map.put(id, runnable);
    }

    public void confirmTransaction(long id) {
        boolean found = false;
        for (Map.Entry<Long, Runnable> entry : this.map.entrySet()) {
            if (entry.getKey() == id) {
                found = true;
                break;
            }
        }

        if (!found) {
            return;
        }

        Iterator<Map.Entry<Long, Runnable>> iterator = this.map.entrySet().iterator();

        Map.Entry<Long, Runnable> entry;
        while (iterator.hasNext() && (entry = iterator.next()) != null && entry.getKey() <= id) {
            entry.getValue().run();
        }

        player.lastReceivedId = id;
        player.lastRespondTime = System.currentTimeMillis();
    }
}
