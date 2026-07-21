package bus;

public interface EventListener {
    void onEvent(String topic, Object payload);
}