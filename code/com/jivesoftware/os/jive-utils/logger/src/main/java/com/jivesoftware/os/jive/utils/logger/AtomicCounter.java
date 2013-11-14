/*
 * Copyright 2013 Jive Software, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.jivesoftware.os.jive.utils.logger;

import java.util.concurrent.atomic.AtomicLong;

public final class AtomicCounter implements AtomicCounterMXBean {

    private ValueType type;
    private AtomicLong value = new AtomicLong(0L);

    public AtomicCounter() {
    }

    public AtomicCounter(ValueType type) {
        this.type = type;
    }

    public String toJsonString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"type\":\"");
        sb.append(type.name());
        sb.append("\",");
        sb.append("\"value\":");
        sb.append(value.get());
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
        return value.get();
    }

    public void setValue(long value) {
        this.value.set(value);
    }

    @Override
    public String getType() {
        return type.name();
    }

    public void reset() {
        this.value.set(0);
    }

    public void inc() {
        value.incrementAndGet();
    }

    public void inc(long amount) {
        value.addAndGet(amount);
    }

    public void dec() {
        value.decrementAndGet();
    }

    public void dec(long amount) {
        value.addAndGet(-amount);
    }

    public void set(long value) {
        this.value.set(value);
    }

    public long getCount() {
        return value.get();
    }
}
