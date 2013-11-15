package org.merlin.config.type.list;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Generic Lists don't play well with Merlin. Strongly typed lists with String constructors do.
 *
 */
public class UriList extends ArrayList<URI> {

    public UriList(String valueString) throws URISyntaxException {
        for (String value : valueString.split(",")) {
            add(new URI(value.trim()));
        }
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (Iterator<URI> iter = iterator(); iter.hasNext();) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(", ");
            }
        }
        return buffer.toString();
    }

}
