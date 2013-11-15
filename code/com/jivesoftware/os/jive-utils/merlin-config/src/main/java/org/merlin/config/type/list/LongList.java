package org.merlin.config.type.list;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Generic Lists don't play well with Merlin. Strongly typed lists with String constructors do.
 *
 */
public class LongList extends ArrayList<Long> {

    public LongList(String valueString) {
        for (String value : valueString.split(",")) {
            add(Long.valueOf(value.trim()));
        }
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (Iterator<Long> iter = iterator(); iter.hasNext();) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(", ");
            }
        }
        return buffer.toString();
    }


}
