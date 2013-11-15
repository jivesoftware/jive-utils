package org.merlin.config;

import org.merlin.config.defaults.BooleanDefault;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ConfigGetterTest {

    public static final Object[] NO_ARGS = new Object[0];

    static interface TestConfig extends Config {
        @BooleanDefault(true)
        boolean isOn();

        boolean getNoDefault();
    }

    @DataProvider
    public Object[][] impl() {
        ConfigHandler handler = new ConfigHandler(mock(Configuration.class), TestConfig.class, "", mock(ObjectStringMapper.class));

        return new Object[][] {
            { handler }
        };
    }

    @Test(dataProvider = "impl")
    public void simpleGet(ConfigHandler handler) throws Exception {
        ConfigMethod getter = new ConfigGetter(TestConfig.class, TestConfig.class.getMethod("isOn"));
        when(handler.getConfiguration().getProperty("on")).thenReturn("true");

        assertTrue((Boolean) getter.invoke(handler, NO_ARGS));
    }

    @Test(dataProvider = "impl")
    public void annotationDefaultGet(ConfigHandler handler) throws Exception {
        ConfigMethod getter = ConfigMethod.getInstance(TestConfig.class, TestConfig.class.getMethod("isOn"));
        //        ConfigGetter getter = new ConfigGetter(TestConfig.class, TestConfig.class.getMethod("isOn"));
        when(handler.getConfiguration().getProperty("on")).thenReturn(null);

        assertTrue((Boolean) getter.invoke(handler, NO_ARGS));
    }

    @Test(dataProvider = "impl")
    public void runtimeDefaultGet(ConfigHandler handler) throws Exception {
        ConfigGetter getter = new ConfigGetter(TestConfig.class, TestConfig.class.getMethod("isOn"));
        when(handler.getConfiguration().getProperty("on")).thenReturn(null);

        assertFalse((Boolean) getter.invoke(handler, new Object[] { false }));
    }

    @Test(dataProvider = "impl", expectedExceptions = ConfigurationRuntimeException.class, expectedExceptionsMessageRegExp = ".*getNoDefault.*")
    public void getWithNoValueOrDefaultProducesHelpfulMessage(ConfigHandler handler) throws Exception {
        ConfigGetter getter = new ConfigGetter(TestConfig.class, TestConfig.class.getMethod("getNoDefault"));
        when(handler.getConfiguration().getProperty("noDefault")).thenReturn(null);

        getter.invoke(handler, NO_ARGS);
    }

}
