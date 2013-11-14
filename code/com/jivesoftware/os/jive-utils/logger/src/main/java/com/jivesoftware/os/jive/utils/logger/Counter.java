package com.jivesoftware.os.jive.utils.logger;

public final class Counter implements CounterMXBean {

    private ValueType type;
    private long value;

    public Counter() {
    }

    public Counter(ValueType type) {
        this.type = type;
    }

    public String toJsonString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"type\":\"");
        sb.append(type.name());
        sb.append("\",");
        sb.append("\"value\":");
        sb.append(value);
        sb.append("}");
        return sb.toString();
    }

    public ValueType getValueType() {
        return type;
    }

    public void setType(ValueType type) {
        this.type = type;
    }

    @Override
    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return type.name();
    }

    public void reset() {
        this.value = 0;
    }

    public void inc() {
        value++;
    }

    public void inc(long amount) {
        value += amount;
    }

    public void dec() {
        value--;
    }

    public void dec(long amount) {
        value -= amount;
    }

    public void set(long value) {
        this.value = value;
    }

    public long getCount() {
        return value;
    }
}
