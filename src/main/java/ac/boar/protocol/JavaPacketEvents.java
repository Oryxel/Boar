package ac.boar.protocol;

import lombok.Getter;
import ac.boar.protocol.event.java.PacketListener;

import java.util.ArrayList;
import java.util.List;

public class JavaPacketEvents {
    @Getter
    private static List<PacketListener> listeners = new ArrayList<>();

    public static void register(final PacketListener listener) {
        JavaPacketEvents.listeners.add(listener);
    }

    public static void terminate() {
        JavaPacketEvents.listeners.clear();
    }
}
