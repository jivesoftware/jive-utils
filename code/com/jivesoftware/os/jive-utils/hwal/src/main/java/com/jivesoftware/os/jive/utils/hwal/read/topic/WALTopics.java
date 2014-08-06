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
package com.jivesoftware.os.jive.utils.hwal.read.topic;

import com.jivesoftware.os.jive.utils.hwal.read.WALCursorStore;
import com.jivesoftware.os.jive.utils.hwal.read.WALReaders;
import com.jivesoftware.os.jive.utils.hwal.read.partitions.WALTopicCursors;
import com.jivesoftware.os.jive.utils.permit.ConstantPermitConfig;
import com.jivesoftware.os.jive.utils.permit.PermitConfig;
import com.jivesoftware.os.jive.utils.permit.PermitProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author jonathan
 */
public class WALTopics {

    private final ConcurrentHashMap<CursorGroupAndTopic, WALTopicCursors> topicCursors = new ConcurrentHashMap<>();
    private final WALReaders walReaders;
    private final PermitProvider topicCursorPermitProvider;
    private final PermitConfig topicCursorPermitConfig;
    private final WALCursorStore cursorStore;

    public WALTopics(WALReaders walReaders, PermitProvider topicCursorPermitProvider, PermitConfig topicCursorPermitConfig, WALCursorStore cursorStore) {
        this.walReaders = walReaders;
        this.topicCursorPermitProvider = topicCursorPermitProvider;
        this.topicCursorPermitConfig = topicCursorPermitConfig;
        this.cursorStore = cursorStore;
    }

    public WALTopicCursors getWALTopicCursors(String cursorGroup, String topic, int numberOfPartitions) {
        CursorGroupAndTopic key = new CursorGroupAndTopic(cursorGroup, topic);
        WALTopicCursors cursors = topicCursors.get(key);
        if (cursors == null) {
            cursors = new WALTopicCursors(walReaders, cursorGroup, topic, topicCursorPermitProvider,
                    new ConstantPermitConfig(0, numberOfPartitions, topicCursorPermitConfig.getExpires()),
                    cursorStore);
            WALTopicCursors had = topicCursors.putIfAbsent(key, cursors);
            if (had != null) {
                cursors = had;
            }
        }
        return cursors;
    }

    public void removeWALTopicCursors(String readerGroup, String topicId) {
        CursorGroupAndTopic key = new CursorGroupAndTopic(readerGroup, topicId);
        WALTopicCursors removed = topicCursors.remove(key);
        if (removed != null) {
            removed.offline();
        }
    }

    public void online() {
        for (WALTopicCursors cursors : topicCursors.values()) {
            cursors.online();
        }
    }

    public void offline() {
        List<WALTopicCursors> offline = new ArrayList<>(topicCursors.values());
        topicCursors.clear();
        for (WALTopicCursors cursors : offline) {
            cursors.offline();
        }
    }

    static class CursorGroupAndTopic {

        private final String cursorGroup;
        private final String topic;

        public CursorGroupAndTopic(String cursorGroup, String topic) {
            this.cursorGroup = cursorGroup;
            this.topic = topic;
        }

        @Override
        public String toString() {
            return "CursorGroupAndTopic{" + "cursorGroup=" + cursorGroup + ", topic=" + topic + '}';
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + Objects.hashCode(this.cursorGroup);
            hash = 41 * hash + Objects.hashCode(this.topic);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CursorGroupAndTopic other = (CursorGroupAndTopic) obj;
            if (!Objects.equals(this.cursorGroup, other.cursorGroup)) {
                return false;
            }
            if (!Objects.equals(this.topic, other.topic)) {
                return false;
            }
            return true;
        }

    }

}
