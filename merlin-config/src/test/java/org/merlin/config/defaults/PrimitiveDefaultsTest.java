package org.merlin.config.defaults;

import java.util.HashMap;
import org.merlin.config.BindInterfaceToConfiguration;
import org.merlin.config.Config;
import org.merlin.config.MapBackConfiguration;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class PrimitiveDefaultsTest {

    private static <T extends Config> T makeInstance(Class<T> config) {
        MapBackConfiguration mapBackConfiguration = new MapBackConfiguration(new HashMap<String, String>());
        T impl = new BindInterfaceToConfiguration<>(mapBackConfiguration, config)
            .bind();
        impl.applyDefaults();
        return impl;
    }

    private static interface BooleanConfig extends Config {
        @BooleanDefault(true)
        boolean isPrimitive();

        @BooleanDefault(true)
        Boolean isClassValue();
    }

    @Test
    public void booleans() {
        BooleanConfig config = makeInstance(BooleanConfig.class);

        assertTrue(config.isPrimitive());
        assertTrue(config.isClassValue());
    }

    private static interface ByteConfig extends Config {
        @ByteDefault(0x01)
        byte getPrimitive();

        @ByteDefault(0x01)
        Byte getClassValue();
    }

    @Test
    public void bytes() {
        ByteConfig config = makeInstance(ByteConfig.class);

        assertEquals((Byte) config.getPrimitive(), Byte.valueOf((byte) 0x1));
        assertEquals(config.getClassValue(), Byte.valueOf((byte) 0x1));
    }

    private static interface DoubleConfig extends Config {
        @DoubleDefault(Double.NaN)
        double getPrimitive();

        @DoubleDefault(Double.NaN)
        Double getClassValue();
    }

    @Test
    public void doubles() {
        DoubleConfig config = makeInstance(DoubleConfig.class);

        assertEquals(config.getPrimitive(), Double.NaN);
        assertEquals(config.getClassValue(), Double.NaN);
    }

    private static interface FloatConfig extends Config {
        @FloatDefault(Float.NaN)
        float getPrimitive();

        @FloatDefault(Float.NaN)
        Float getClassValue();
    }

    @Test
    public void floats() {
        FloatConfig config = makeInstance(FloatConfig.class);

        assertEquals(config.getPrimitive(), Float.NaN);
        assertEquals(config.getClassValue(), Float.NaN);
    }

    private static interface IntConfig extends Config {
        @IntDefault(0)
        int getPrimitive();

        @IntDefault(0)
        Integer getClassValue();
    }

    @Test
    public void ints() {
        IntConfig config = makeInstance(IntConfig.class);

        assertEquals((Integer) config.getPrimitive(), (Integer) 0);
        assertEquals(config.getClassValue(), (Integer) 0);
    }

    private static interface LongConfig extends Config {
        @LongDefault(Integer.MAX_VALUE + 1L)
        long getPrimitive();

        @LongDefault(Integer.MAX_VALUE + 1L)
        Long getClassValue();
    }

    @Test
    public void longs() {
        LongConfig config = makeInstance(LongConfig.class);

        assertEquals((Long) config.getPrimitive(), (Long) (Integer.MAX_VALUE + 1L));
        assertEquals(config.getClassValue(), (Long) (Integer.MAX_VALUE + 1L));
    }

    private static interface ShortConfig extends Config {
        @ShortDefault(2)
        short getPrimitive();

        @ShortDefault(2)
        Short getClassValue();
    }

    @Test
    public void shorts() {
        ShortConfig config = makeInstance(ShortConfig.class);

        assertEquals((Short) config.getPrimitive(), Short.valueOf((short) 2));
        assertEquals(config.getClassValue(), Short.valueOf((short) 2));
    }

    private static interface ClassConfig extends Config {
        @ClassDefault(Object.class)
        Class<?> getValue();
    }

    @Test
    public void clazz() {
        ClassConfig config = makeInstance(ClassConfig.class);

        assertEquals(config.getValue(), Object.class);
    }

    private static interface StringConfig extends Config {
        @StringDefault("hello")
        String getValue();
    }

    @Test
    public void string() {
        StringConfig config = makeInstance(StringConfig.class);

        assertEquals(config.getValue(), "hello");
    }

}
