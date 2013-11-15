package org.merlin.config.type.list;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * Generic Lists don't play well with Merlin. Strongly typed lists with String constructors do.
 *
 */
public class StringList extends ArrayList<String> {

    public StringList(String valueString) {
        for (String value : valueString.split(",")) {
            add(value.trim());
        }
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (Iterator<String> iter = iterator(); iter.hasNext();) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(", ");
            }
        }
        return buffer.toString();
    }

}
