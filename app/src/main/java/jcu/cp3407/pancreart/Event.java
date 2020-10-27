package jcu.cp3407.pancreart;

public class Event {
    private enum Type {
        GLUCOSE_READING,
        INSULIN_INJECTION
    }
    private int userId;
    private int time;
    private Type eventType;
    private float amount;

    public void Event(int userId, int time, Type eventType, float amount) {
        this.userId = userId;
        this.time = time;
        this.amount = amount;
        this.eventType = eventType;
    }
}
