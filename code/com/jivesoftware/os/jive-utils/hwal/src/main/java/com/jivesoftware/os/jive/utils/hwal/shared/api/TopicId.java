package com.jivesoftware.os.jive.utils.hwal.shared.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class TopicId implements Comparable<TopicId> {

    private final int id;

    private TopicId(int id) {
        this.id = id;
    }

    @JsonCreator
    public static TopicId of(@JsonProperty("id") int id) {
        return new TopicId(id);
    }

    public int getId() {
        return id;
    }

    @Override
    public int compareTo(TopicId o) {
        return Integer.compare(id, o.id);
    }

    public TopicId next() {
        return new TopicId(id + 1);
    }

    public TopicId prev() {
        return id > 0 ? new TopicId(id - 1) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TopicId that = (TopicId) o;

        if (id != that.id) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
