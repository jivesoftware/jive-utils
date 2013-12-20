/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jivesoftware.os.jive.utils.row.column.value.store.inmemory;

/**
 *
 * @author jonathan.colt
 */
class Timestamped<V> {

    private long timestamp;
    private V value;
    private boolean tombstone;

    public void set(V value, Long timestamp) {
        if (this.timestamp <= timestamp) {
            this.timestamp = timestamp;
            this.value = value;
            this.tombstone = false;
        }
    }

    public boolean isTombstone() {
        return tombstone;
    }

    public void tombstone(Long timestamp) {
        if (this.timestamp <= timestamp) {
            this.timestamp = timestamp;
            this.value = null;
            this.tombstone = true;
        }
    }

    public V getValue() {
        if (tombstone) {
            return null;
        } else {
            return value;
        }
    }

    Long getTimestamp() {
        return timestamp;
    }
}
