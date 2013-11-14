package com.jivesoftware.os.server.http.jetty.jersey.server.provider;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jivesoftware.os.server.http.jetty.jersey.server.JerseyEndpoints;
import java.io.IOException;
import java.util.Objects;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class DefaultJerseyMapperTest {

    private final ObjectMapper mapper = new JerseyEndpoints().getMapper();

    @DataProvider
    public Object[][] values() {
        return new Object[][] {
            { new Types(true, (byte) 1, (short) 2, (char) 3, 4, 5L, 6.0f, 7.0, "abc") },
            { new Types(false, (byte) 8, (short) 9, (char) 10, 11, 12L, 13.0f, 14.0, "def") },
        };
    }

    @Test(dataProvider = "values")
    public void roundtrip(Types in) throws IOException {
        String serialized = mapper.writeValueAsString(in);

        assertEquals(mapper.readValue(serialized, Types.class), in);

        {
            ObjectNode n = mapper.convertValue(in, ObjectNode.class);
            assertEquals(n.get("z").getClass(), BooleanNode.class);

            assertEquals(n.get("b").getClass(), IntNode.class);
            assertEquals(n.get("s").getClass(), IntNode.class);
            assertEquals(n.get("c").getClass(), TextNode.class);
            assertEquals(n.get("i").getClass(), IntNode.class);
            assertEquals(n.get("j").getClass(), LongNode.class);

            assertEquals(n.get("f").getClass(), DoubleNode.class);
            assertEquals(n.get("d").getClass(), DoubleNode.class);

            assertEquals(n.get("t").getClass(), TextNode.class);
        }

        {
            ObjectNode n = mapper.readValue(serialized, ObjectNode.class);
            assertEquals(n.get("z").getClass(), BooleanNode.class);

            assertEquals(n.get("b").getClass(), TextNode.class);
            assertEquals(n.get("s").getClass(), TextNode.class);
            assertEquals(n.get("c").getClass(), TextNode.class);
            assertEquals(n.get("i").getClass(), TextNode.class);
            assertEquals(n.get("j").getClass(), TextNode.class);

            assertEquals(n.get("f").getClass(), TextNode.class);
            assertEquals(n.get("d").getClass(), TextNode.class);

            assertEquals(n.get("t").getClass(), TextNode.class);
        }
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    private static class Types {
        boolean z;

        byte b;
        short s;
        char c;
        int i;
        long j;

        float f;
        double d;

        String t;

        // Jackson
        @SuppressWarnings("UnusedDeclaration")
        public Types() {
        }

        private Types(boolean z, byte b, short s, char c, int i, long j, float f, double d, String t) {
            this.b = b;
            this.z = z;
            this.s = s;
            this.c = c;
            this.i = i;
            this.j = j;
            this.f = f;
            this.d = d;
            this.t = t;
        }

        @Override
        public String toString() {
            return "Types{" +
                "z=" + z +
                ", b=" + b +
                ", s=" + s +
                ", c=" + (long) c + // cast char to long, otherwise results in invalid UTF-8
                ", i=" + i +
                ", j=" + j +
                ", f=" + f +
                ", d=" + d +
                ", o='" + t + '\'' +
                '}';
        }

        @Override
        public int hashCode() {
            return Objects.hash(z, b, s, c, i, j, f, d, t);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final Types other = (Types) obj;
            return Objects.equals(this.z, other.z)
                && Objects.equals(this.b, other.b)
                && Objects.equals(this.s, other.s)
                && Objects.equals(this.c, other.c)
                && Objects.equals(this.i, other.i)
                && Objects.equals(this.j, other.j)
                && Objects.equals(this.f, other.f)
                && Objects.equals(this.d, other.d)
                && Objects.equals(this.t, other.t);
        }
    }
}
