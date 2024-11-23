package ac.boar.anticheat.utils;

import ac.boar.anticheat.user.api.BoarPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public final class LatencyUtil {
    private final BoarPlayer player;
    @Getter
    private final List<Long> sentTransactions = new ArrayList<>();
    private final Map<Long, Runnable> map = new ConcurrentHashMap<>();

    public void addTransactionToQueue(long id, Runnable runnable) {
        if (id <= player.lastReceivedId) {
            runnable.run();
            return;
        }

        this.map.put(id, runnable);
    }

    public void confirmTransaction(long id) {
        if (!this.sentTransactions.contains(id)) {
            return;
        }

        this.sentTransactions.remove(id);

        Iterator<Map.Entry<Long, Runnable>> iterator = this.map.entrySet().iterator();

        Map.Entry<Long, Runnable> entry;
        while (iterator.hasNext() && (entry = iterator.next()) != null && entry.getKey() <= id) {
            entry.getValue().run();
        }

        player.lastReceivedId = id;
        player.lastRespondTime = System.currentTimeMillis();
    }
}
