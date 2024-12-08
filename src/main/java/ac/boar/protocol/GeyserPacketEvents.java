package ac.boar.protocol;

import ac.boar.protocol.event.geyser.GeyserPacketListener;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class GeyserPacketEvents {
    @Getter
    private static final List<GeyserPacketListener> listeners = new CopyOnWriteArrayList<>();

    public static void register(final GeyserPacketListener listener) {
        GeyserPacketEvents.listeners.add(listener);
    }

    public static void terminate() {
        GeyserPacketEvents.listeners.clear();
    }
}
