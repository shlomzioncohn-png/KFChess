package bus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {

    private final Map<String, List<EventListener>> subscribers = new ConcurrentHashMap<>();

    public void subscribe(String topic, EventListener listener) {
        subscribers.computeIfAbsent(topic, t -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public void publish(String topic, Object payload) {
        List<EventListener> listeners = subscribers.get(topic);
        if (listeners == null) return;
        for (EventListener listener : listeners) {
            listener.onEvent(topic, payload);
        }
    }
}