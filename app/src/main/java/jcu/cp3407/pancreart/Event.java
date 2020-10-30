package jcu.cp3407.pancreart;

public class Event {
    private int eventType;
    private int userId;
    private int time;
    private float amount;

    public void Event(int userId, int time, int eventType, float amount) {
        this.userId = userId;
        this.time = time;
        this.amount = amount;
        this.eventType = eventType;
    }


}
