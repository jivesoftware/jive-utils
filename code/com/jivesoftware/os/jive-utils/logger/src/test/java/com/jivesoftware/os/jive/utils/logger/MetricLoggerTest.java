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
package com.jivesoftware.os.jive.utils.logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressStaticInitializationFor({"org.apache.log4j.LogManager" })
@PrepareForTest({LogManager.class, Logger.class })
@PowerMockIgnore("javax.management.*")
public class MetricLoggerTest extends PowerMockTestCase {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    @BeforeMethod
    public void setUp() throws Exception {
        PowerMockito.mockStatic(LogManager.class);
        Mockito.when(LogManager.getLogger(CountersAndTimers.class.getName())).thenReturn(PowerMockito.mock(Logger.class));
    }

    @Test
    public void testDelegationForTraceCalls() throws Exception {
        executeDelegationTest(Level.TRACE, "logged.traces", new LoggingCallExecutor() {
            @Override
            public void levelEnabledSetup(Logger underlyingLogger, boolean isEnabled) {
                Mockito.when(underlyingLogger.isEnabledFor(Level.TRACE)).thenReturn(isEnabled);
                Mockito.when(underlyingLogger.isTraceEnabled()).thenReturn(isEnabled);
            }

            @Override
            public void logSimpleMessage(MetricLogger logger, String message) {
                logger.trace(message);
            }

            @Override
            public void logSingleOptionFormattedMessage(MetricLogger logger, String format, Object o) {
                logger.trace(format, o);
            }

            @Override
            public void logTwoOptionFormattedMessage(MetricLogger logger, String format, Object o1, Object o2) {
                logger.trace(format, o1, o2);
            }

            @Override
            public void logMultiOptionFormattedMessage(MetricLogger logger, String format, Object[] objects) {
                logger.trace(format, objects);
            }

            @Override
            public void logMessageWithException(MetricLogger logger, String message, Throwable throwable) {
                logger.trace(message, throwable);
            }
        });
    }

    @Test
    public void testDelegationForDebugCalls() throws Exception {
        executeDelegationTest(Level.DEBUG, "logged.debugs", new LoggingCallExecutor() {
            @Override
            public void levelEnabledSetup(Logger underlyingLogger, boolean isEnabled) {
                Mockito.when(underlyingLogger.isEnabledFor(Level.DEBUG)).thenReturn(isEnabled);
                Mockito.when(underlyingLogger.isDebugEnabled()).thenReturn(isEnabled);
            }

            @Override
            public void logSimpleMessage(MetricLogger logger, String message) {
                logger.debug(message);
            }

            @Override
            public void logSingleOptionFormattedMessage(MetricLogger logger, String format, Object o) {
                logger.debug(format, o);
            }

            @Override
            public void logTwoOptionFormattedMessage(MetricLogger logger, String format, Object o1, Object o2) {
                logger.debug(format, o1, o2);
            }

            @Override
            public void logMultiOptionFormattedMessage(MetricLogger logger, String format, Object[] objects) {
                logger.debug(format, objects);
            }

            @Override
            public void logMessageWithException(MetricLogger logger, String message, Throwable throwable) {
                logger.debug(message, throwable);
            }
        });
    }

    @Test
    public void testDelegationForInfoCalls() throws Exception {
        executeDelegationTest(Level.INFO, "logged.infos", new LoggingCallExecutor() {
            @Override
            public void levelEnabledSetup(Logger underlyingLogger, boolean isEnabled) {
                Mockito.when(underlyingLogger.isEnabledFor(Level.INFO)).thenReturn(isEnabled);
                Mockito.when(underlyingLogger.isInfoEnabled()).thenReturn(isEnabled);
            }

            @Override
            public void logSimpleMessage(MetricLogger logger, String message) {
                logger.info(message);
            }

            @Override
            public void logSingleOptionFormattedMessage(MetricLogger logger, String format, Object o) {
                logger.info(format, o);
            }

            @Override
            public void logTwoOptionFormattedMessage(MetricLogger logger, String format, Object o1, Object o2) {
                logger.info(format, o1, o2);
            }

            @Override
            public void logMultiOptionFormattedMessage(MetricLogger logger, String format, Object[] objects) {
                logger.info(format, objects);
            }

            @Override
            public void logMessageWithException(MetricLogger logger, String message, Throwable throwable) {
                logger.info(message, throwable);
            }
        });
    }

    @Test
    public void testDelegationForWarnCalls() throws Exception {
        executeDelegationTest(Level.WARN, "logged.warns", new LoggingCallExecutor() {
            @Override
            public void levelEnabledSetup(Logger underlyingLogger, boolean isEnabled) {
                Mockito.when(underlyingLogger.isEnabledFor(Level.WARN)).thenReturn(isEnabled);
            }

            @Override
            public void logSimpleMessage(MetricLogger logger, String message) {
                logger.warn(message);
            }

            @Override
            public void logSingleOptionFormattedMessage(MetricLogger logger, String format, Object o) {
                logger.warn(format, o);
            }

            @Override
            public void logTwoOptionFormattedMessage(MetricLogger logger, String format, Object o1, Object o2) {
                logger.warn(format, o1, o2);
            }

            @Override
            public void logMultiOptionFormattedMessage(MetricLogger logger, String format, Object[] objects) {
                logger.warn(format, objects);
            }

            @Override
            public void logMessageWithException(MetricLogger logger, String message, Throwable throwable) {
                logger.warn(message, throwable);
            }
        });
    }

    @Test
    public void testDelegationForErrorCalls() throws Exception {
        executeDelegationTest(Level.ERROR, "logged.errors", new LoggingCallExecutor() {
            @Override
            public void levelEnabledSetup(Logger underlyingLogger, boolean isEnabled) {
                Mockito.when(underlyingLogger.isEnabledFor(Level.ERROR)).thenReturn(isEnabled);
            }

            @Override
            public void logSimpleMessage(MetricLogger logger, String message) {
                logger.error(message);
            }

            @Override
            public void logSingleOptionFormattedMessage(MetricLogger logger, String format, Object o) {
                logger.error(format, o);
            }

            @Override
            public void logTwoOptionFormattedMessage(MetricLogger logger, String format, Object o1, Object o2) {
                logger.error(format, o1, o2);
            }

            @Override
            public void logMultiOptionFormattedMessage(MetricLogger logger, String format, Object[] objects) {
                logger.error(format, objects);
            }

            @Override
            public void logMessageWithException(MetricLogger logger, String message, Throwable throwable) {
                logger.error(message, throwable);
            }
        });
    }

    private void executeDelegationTest(Level level, String counterName, LoggingCallExecutor executor) {
        Exception exception = new Exception();
        String enabledLoggerName = RandomStringUtils.randomAlphanumeric(20);

        Logger enabledLogger = createLogger(enabledLoggerName);
        MetricLogger enabledMetricLogger = new MetricLogger(enabledLoggerName, LoggerSummary.INSTANCE);

        executor.levelEnabledSetup(enabledLogger, true);

        executor.logSimpleMessage(enabledMetricLogger, "simplemessage");
        Mockito.verify(enabledLogger, Mockito.atLeast(1)).callAppenders(
                new MetricLogEvent(enabledLoggerName, enabledLogger, level, null, "simplemessage", null));

        executor.logSingleOptionFormattedMessage(enabledMetricLogger, "format {}", "value");
        Mockito.verify(enabledLogger, Mockito.atLeast(1)).callAppenders(
                new MetricLogEvent(enabledLoggerName, enabledLogger, level, null, "format value", null));

        executor.logTwoOptionFormattedMessage(enabledMetricLogger, "format {} {}", "value1", "value2");
        Mockito.verify(enabledLogger, Mockito.atLeast(1)).callAppenders(
                new MetricLogEvent(enabledLoggerName, enabledLogger, level, null, "format value1 value2", null));

        executor.logMultiOptionFormattedMessage(enabledMetricLogger, "format {} {} {}",
                new Object[]{"value1", "value2", "value3"});
        Mockito.verify(enabledLogger, Mockito.atLeast(1)).callAppenders(
                new MetricLogEvent(enabledLoggerName, enabledLogger, level, null, "format value1 value2 value3", null));

        executor.logMessageWithException(enabledMetricLogger, "exception message", exception);
        Mockito.verify(enabledLogger, Mockito.atLeast(1)).callAppenders(
                new MetricLogEvent(enabledLoggerName, enabledLogger, level, null, "exception message", exception));

        CountersAndTimers cat = CountersAndTimers.getOrCreate(enabledLoggerName);
        Assert.assertEquals(cat.counter(ValueType.COUNT, counterName).getCount(), 5l);

        // Logger with level disabled
        String disabledLoggerName = RandomStringUtils.randomAlphanumeric(20);

        Logger disabledLogger = createLogger(disabledLoggerName);
        MetricLogger disabledMetricLogger = new MetricLogger(disabledLoggerName, LoggerSummary.INSTANCE);

        executor.levelEnabledSetup(disabledLogger, false);

        executor.logSimpleMessage(disabledMetricLogger, "simplemessage");
        executor.logSingleOptionFormattedMessage(disabledMetricLogger, "format {}", "value");
        executor.logTwoOptionFormattedMessage(disabledMetricLogger, "format {} {}", "value1", "value2");
        executor.logMultiOptionFormattedMessage(disabledMetricLogger, "format {} {} {}",
                new Object[]{"value1", "value2", "value3"});
        executor.logMessageWithException(disabledMetricLogger, "exception message", exception);

        Mockito.verify(disabledLogger, Mockito.never()).log(Mockito.anyString(), Matchers.<Priority>any(), Mockito.anyString(),
                Matchers.<Throwable>any());

        cat = CountersAndTimers.getOrCreate(disabledLoggerName);
        Assert.assertEquals(cat.counter(ValueType.COUNT, counterName).getCount(), 0l);
    }

    private Logger createLogger(String loggerName) {
        Logger logger = PowerMockito.mock(Logger.class);
        Mockito.when(LogManager.getLogger(loggerName)).thenReturn(logger);

        return logger;
    }

    private interface LoggingCallExecutor {

        void levelEnabledSetup(Logger underlyingLogger, boolean isEnabled);

        void logSimpleMessage(MetricLogger logger, String message);

        void logSingleOptionFormattedMessage(MetricLogger logger, String format, Object o);

        void logTwoOptionFormattedMessage(MetricLogger logger, String format, Object o1, Object o2);

        void logMultiOptionFormattedMessage(MetricLogger logger, String format, Object[] objects);

        void logMessageWithException(MetricLogger logger, String message, Throwable throwable);
    }
}
