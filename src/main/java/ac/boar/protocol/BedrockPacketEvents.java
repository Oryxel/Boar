package ac.boar.protocol;

import ac.boar.protocol.event.bedrock.BedrockPacketListener;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public final class BedrockPacketEvents {
    @Getter
    private static List<BedrockPacketListener> listeners = new ArrayList<>();

    public static void register(final BedrockPacketListener listener) {
        BedrockPacketEvents.listeners.add(listener);
    }

    public static void terminate() {
        BedrockPacketEvents.listeners.clear();
    }
}
