/*
 * Copyright 2014 Jive Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jivesoftware.os.jive.utils.hwal.read.partitions;

/**
 *
 * @author jonathan
 */
public class Cursor {

    public final String tenant;
    public final String readerGroup;
    public final String cursorGroup;
    public final String topic;
    public final String owner;
    public final int partition;
    public final long cursor;

    public Cursor(String tenant, String readerGroup, String cursorGroup, String topic, String owner, int partition, long cursor) {
        this.tenant = tenant;
        this.readerGroup = readerGroup;
        this.cursorGroup = cursorGroup;
        this.topic = topic;
        this.owner = owner;
        this.partition = partition;
        this.cursor = cursor;
    }

    @Override
    public String toString() {
        return "CursorLag{"
                + "tenant=" + tenant
                + ", readerGroup=" + readerGroup
                + ", cursorGroup=" + cursorGroup
                + ", topic=" + topic
                + ", owner=" + owner
                + ", partition=" + partition
                + ", cursor=" + cursor
                + '}';
    }

}
