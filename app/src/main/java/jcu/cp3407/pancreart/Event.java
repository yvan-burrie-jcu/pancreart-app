package jcu.cp3407.pancreart;

import org.json.*;

public class Event {

    public long userId;

    public long owner; // todo: enum 0 = pod, 1 = app, 2 = cloud

    public enum Type {
        GLUCOSE_READING,
        INSULIN_INJECTION,
    }

    public Type type;

    public long time;

    public double amount;

    public Event(long userId, long owner, Type type, long time, double amount) {
        this.userId = userId;
        this.owner = owner;
        this.type = type;
        this.time = time;
        this.amount = amount;
    }

    public Event(JSONObject buffer) throws JSONException {
        userId = buffer.getLong("userId");
        owner = buffer.getLong("owner");
        int typeBuffer = buffer.getInt("type");
        if (typeBuffer == Type.GLUCOSE_READING.ordinal()) {
            type = Type.GLUCOSE_READING;
        } else if (typeBuffer == Type.INSULIN_INJECTION.ordinal()) {
            type = Type.INSULIN_INJECTION;
        }
        time = buffer.getLong("time");
        amount = buffer.getDouble("amount");
    }

    public JSONObject serialise() throws JSONException {
        JSONObject buffer = new JSONObject();
        buffer.put("userId", userId);
        buffer.put("owner", owner);
        buffer.put("type", type.ordinal());
        buffer.put("time", time);
        buffer.put("amount", amount);
        return buffer;
    }
}
