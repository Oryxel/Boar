package ac.boar.protocol;

import ac.boar.protocol.event.java.PacketListener;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public final class JavaPacketEvents {
    @Getter
    private static final List<PacketListener> listeners = new ArrayList<>();

    public static void register(final PacketListener listener) {
        JavaPacketEvents.listeners.add(listener);
    }

    public static void terminate() {
        JavaPacketEvents.listeners.clear();
    }
}
