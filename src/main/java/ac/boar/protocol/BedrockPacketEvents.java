package ac.boar.protocol;

import ac.boar.protocol.event.BedrockPacketListener;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class BedrockPacketEvents {
    @Getter
    private static final List<BedrockPacketListener> listeners = new CopyOnWriteArrayList<>();

    public static void register(final BedrockPacketListener listener) {
        BedrockPacketEvents.listeners.add(listener);
    }

    public static void terminate() {
        BedrockPacketEvents.listeners.clear();
    }
}
