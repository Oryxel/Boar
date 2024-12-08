package ac.boar.anticheat.utils;

import ac.boar.anticheat.user.api.BoarPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
public final class LatencyUtil {
    private final BoarPlayer player;
    @Getter
    private final List<Long> sentTransactions = new ArrayList<>();
    private final Map<Long, List<Runnable>> map = new ConcurrentHashMap<>();

    public void addTransactionToQueue(long id, Runnable runnable) {
        if (id <= player.lastReceivedId) {
            runnable.run();
            return;
        }

        if (!this.map.containsKey(id)) {
            List<Runnable> list = new CopyOnWriteArrayList<>();
            list.add(runnable);
            this.map.put(id, list);
            return;
        }

        this.map.get(id).add(runnable);
    }

    public boolean confirmTransaction(long id) {
        if (!this.sentTransactions.contains(id)) {
            return false;
        }

        this.sentTransactions.remove(id);

        Iterator<Map.Entry<Long, List<Runnable>>> iterator = this.map.entrySet().iterator();

        Map.Entry<Long, List<Runnable>> entry;
        while (iterator.hasNext() && (entry = iterator.next()) != null && entry.getKey() <= id) {
            entry.getValue().forEach(Runnable::run);
            iterator.remove();
        }

        player.lastReceivedId = id;
        player.lastRespondTime = System.currentTimeMillis();
        return true;
    }
}
