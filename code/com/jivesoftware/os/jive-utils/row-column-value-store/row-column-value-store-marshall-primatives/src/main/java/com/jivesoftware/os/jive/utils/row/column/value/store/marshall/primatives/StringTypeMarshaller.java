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
package com.jivesoftware.os.jive.utils.row.column.value.store.marshall.primatives;

import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.TypeMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.UtilLexMarshaller;

/**
 * Marshall String to and from bytes
 */
public class StringTypeMarshaller implements TypeMarshaller<String> {

    @Override
    public String fromBytes(byte[] bytes) throws Exception {
        return new String(bytes, TypeMarshaller.DEFAULT_CHARSET);
    }

    @Override
    public byte[] toBytes(String t) throws Exception {
        return t.getBytes(TypeMarshaller.DEFAULT_CHARSET);
    }

    @Override
    public String fromLexBytes(byte[] lexBytes) throws Exception {
        return UtilLexMarshaller.stringFromLex(lexBytes);
    }

    @Override
    public byte[] toLexBytes(String t) throws Exception {
        return UtilLexMarshaller.stringToLex(t);
    }
}
