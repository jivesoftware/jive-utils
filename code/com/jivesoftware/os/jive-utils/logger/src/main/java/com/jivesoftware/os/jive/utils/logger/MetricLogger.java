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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an wrapper around a logback logger that makes it as easy as possible for the developer to gather metrics.
 *
 * @author jonathan
 */
public final class MetricLogger {

    final CountersAndTimers countersAndTimers;

    final LazyCounter loggerErrorsCount;

    final LazyCounter loggerWarnsCount;

    final LazyCounter loggerDebugsCount;

    final LazyCounter loggerTracesCount;

    final LazyCounter loggerInfosCount;

    final Logger logger;

    final String fullQualifiedClassName;

    final LoggerSummary loggerSummary;

    MetricLogger(String fullQualifiedClassName, LoggerSummary loggerSummary) {
        this.fullQualifiedClassName = fullQualifiedClassName;
        countersAndTimers = CountersAndTimers.getOrCreate(fullQualifiedClassName);
        logger = (Logger) LoggerFactory.getLogger(fullQualifiedClassName);
        loggerErrorsCount = new LazyCounter(countersAndTimers, ValueType.COUNT, "logged.errors");
        loggerWarnsCount = new LazyCounter(countersAndTimers, ValueType.COUNT, "logged.warns");
        loggerDebugsCount = new LazyCounter(countersAndTimers, ValueType.COUNT, "logged.debugs");
        loggerTracesCount = new LazyCounter(countersAndTimers, ValueType.COUNT, "logged.traces");
        loggerInfosCount = new LazyCounter(countersAndTimers, ValueType.COUNT, "logged.infos");
        this.loggerSummary = loggerSummary;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return logger.getName();
    }

    /** Is the underlying logback logger enabled at Level.TRACE level. */
    public boolean isTraceEnabled() {
        return logger.isEnabledFor(Level.TRACE);
    }

    /**
     * Logs a String message at Level.TRACE.
     *
     * @param msg null is ok
     */
    public void trace(String msg) {
        if (!isTraceEnabled()) {
            return;
        }

        loggerSummary.traces++;
        loggerTracesCount.inc();
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.TRACE, null, msg, null));
    }

    /**
     * Logs a message at Level.TRACE after having substituted the 'arg' into the 'messagePattern' at location designated by '{}'.
     *
     * Example: String fieldName = "foo"; LOG.trace("fieldName:{}", fieldName);
     *
     * Yields: "fieldName:foo"
     *
     * @param messagePattern null is ok.
     * @param arg null is ok.
     */
    public void trace(String messagePattern, Object arg) {
        if (!isTraceEnabled()) {
            return;
        }

        loggerSummary.traces++;
        loggerTracesCount.inc();
        String msgStr = MessageFormatter.format(messagePattern, arg);
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.TRACE, null, msgStr, null));
    }

    /**
     * Logs a message at Level.TRACE after having substituted the 'arg1' and 'arg2' into the 'messagePattern' at locations designated by '{}'s.
     *
     * Example: String fieldName = "foo"; String value = "bar"; LOG.trace("fieldName:{}={}", fieldName, value);
     *
     * Yields: "fieldName:foo=bar"
     *
     * @param messagePattern null is ok.
     * @param arg1 null is ok.
     * @param arg2 null is ok.
     */
    public void trace(String messagePattern, Object arg1, Object arg2) {
        if (isTraceEnabled()) {
            loggerSummary.traces++;
            loggerTracesCount.inc();
            String msgStr = MessageFormatter.format(messagePattern, arg1, arg2);
            logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.TRACE, null, msgStr, null));
        }
    }

    /**
     * Logs a message at Level.TRACE after having substituted the 'arg1' and 'arg2' into the 'messagePattern' at locations designated by '{}'s.
     *
     * Example: String fieldName = "foo"; String value = "bar"; LOG.trace("fieldName:{}={}", fieldName, value);
     *
     * Yields: "fieldName:foo=bar"
     *
     * @param messagePattern null is ok.
     * @param arg1 null is ok.
     * @param arg2 null is ok.
     */
    public void trace(String messagePattern, Object arg1, Object arg2, Object arg3) {
        if (isTraceEnabled()) {
            loggerSummary.traces++;
            loggerTracesCount.inc();
            String msgStr = MessageFormatter.format(messagePattern, arg1, arg2, arg3);
            logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.TRACE, null, msgStr, null));
        }
    }

    /**
     * Logs a message at Level.TRACE after having substituted the 'arg1' and 'arg2' into the 'messagePattern' at locations designated by '{}'s.
     *
     * Example: String fieldName = "foo"; String value = "bar"; LOG.trace("fieldName:{}={}", fieldName, value);
     *
     * Yields: "fieldName:foo=bar"
     *
     * @param messagePattern null is ok.
     * @param arg1 null is ok.
     * @param arg2 null is ok.
     */
    public void trace(String messagePattern, Object arg1, Object arg2, Object arg3, Object arg4) {
        if (isTraceEnabled()) {
            loggerSummary.traces++;
            loggerTracesCount.inc();
            String msgStr = MessageFormatter.format(messagePattern, arg1, arg2, arg3, arg4);
            logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.TRACE, null, msgStr, null));
        }
    }

    /**
     * Logs a message at Level.TRACE with an exception after having substituted the elements of 'argArray' into the 'messagePattern'
     * at locations designated by '{}'s.
     *
     * Example: String fieldName = "foo"; String value = "bar"; LOG.trace("fieldName:{}={}", new Object[]{fieldName,value});
     *
     * Yields: "fieldName:foo=bar"
     *
     * @param messagePattern null is ok.
     * @param argArray null is ok.
     * @param t null is NOT ok.
     */
    public void trace(String messagePattern, Object[] argArray, Throwable t) {
        if (!isTraceEnabled()) {
            return;
        }

        loggerSummary.traces++;
        loggerTracesCount.inc();
        String msgStr = MessageFormatter.arrayFormat(messagePattern, argArray);
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.TRACE, null, msgStr, t));
    }

    /**
     * Logs a message at Level.TRACE after having substituted the elements of 'argArray' into the 'messagePattern' at locations designated by '{}'s.
     *
     * Example: String fieldName = "foo"; String value = "bar"; LOG.trace("fieldName:{}={}", new Object[]{fieldName,value});
     *
     * Yields: "fieldName:foo=bar"
     *
     * @param messagePattern null is ok.
     * @param argArray null is ok.
     */
    public void trace(String messagePattern, Object... argArray) {
        if (!isTraceEnabled()) {
            return;
        }

        loggerSummary.traces++;
        loggerTracesCount.inc();
        String msgStr = MessageFormatter.arrayFormat(messagePattern, argArray);
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.TRACE, null, msgStr, null));
    }

    /**
     * Logs a String message at Level.TRACE with an exception.
     *
     * @param msg null is ok.
     * @param t null is NOT ok.
     */
    public void trace(String msg, Throwable t) {
        if (!isTraceEnabled()) {
            return;
        }

        loggerSummary.traces++;
        loggerTracesCount.inc();
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.TRACE, null, msg, t));
    }

    /** Is the underlying logback logger enabled at Level.DEBUG level. */
    public boolean isDebugEnabled() {
        return logger.isEnabledFor(Level.DEBUG);
    }

    /**
     * Logs a String message at Level.DEBUG.
     *
     * @param msg null is ok.
     */
    public void debug(String msg) {
        if (!logger.isDebugEnabled()) {
            return;
        }

        loggerSummary.debugs++;
        loggerDebugsCount.inc();
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.DEBUG, null, msg, null));
    }

    /**
     * Logs a message at Level.DEBUG after having substituted the 'arg' into the 'messagePattern' at location designated by '{}'.
     *
     * Example: String fieldName = "foo"; LOG.trace("fieldName:{}", fieldName);
     *
     * Yields: "fieldName:foo"
     *
     * @param messagePattern null is ok.
     * @param arg null is ok.
     */
    public void debug(String messagePattern, Object arg) {
        if (!logger.isDebugEnabled()) {
            return;
        }

        loggerSummary.debugs++;
        loggerDebugsCount.inc();
        String msgStr = MessageFormatter.format(messagePattern, arg);
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.DEBUG, null, msgStr, null));
    }

    /**
     * Logs a message at Level.DEBUG after having substituted the 'arg1' and 'arg2' into the 'messagePattern' at locations designated by '{}'s.
     *
     * Example: String fieldName = "foo"; String value = "bar"; LOG.trace("fieldName:{}={}", fieldName, value);
     *
     * Yields: "fieldName:foo=bar"
     *
     * @param messagePattern null is ok.
     * @param arg1 null is ok.
     * @param arg2 null is ok.
     */
    public void debug(String messagePattern, Object arg1, Object arg2) {
        if (logger.isDebugEnabled()) {
            loggerSummary.debugs++;
            loggerDebugsCount.inc();
            String msgStr = MessageFormatter.format(messagePattern, arg1, arg2);
            logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.DEBUG, null, msgStr, null));
        }
    }

    /** Shorthand for {@link #debug(String, Object[])} */
    public void debug(String messagePattern, Object arg1, Object arg2, Object arg3) {
        if (logger.isDebugEnabled()) {
            loggerSummary.debugs++;
            loggerDebugsCount.inc();
            String msgStr = MessageFormatter.format(messagePattern, arg1, arg2, arg3);
            logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.DEBUG, null, msgStr, null));
        }
    }

    /** Shorthand for {@link #debug(String, Object[])} */
    public void debug(String messagePattern, Object arg1, Object arg2, Object arg3, Object arg4) {
        if (logger.isDebugEnabled()) {
            loggerSummary.debugs++;
            loggerDebugsCount.inc();
            String msgStr = MessageFormatter.format(messagePattern, arg1, arg2, arg3, arg4);
            logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.DEBUG, null, msgStr, null));
        }
    }

    /**
     * Logs a message at Level.DEBUG with an exception after having substituted the elements of 'argArray' into the 'messagePattern'
     * at locations designated by '{}'s.
     *
     * Example: String fieldName = "foo"; String value = "bar"; LOG.trace("fieldName:{}={}", new Object[]{fieldName,value});
     *
     * Yields: "fieldName:foo=bar"
     *
     * @param messagePattern null is ok.
     * @param argArray null is ok.
     * @param t null is NOT ok.
     */
    public void debug(String messagePattern, Object[] argArray, Throwable t) {
        if (!logger.isDebugEnabled()) {
            return;
        }

        loggerSummary.debugs++;
        loggerDebugsCount.inc();
        String msgStr = MessageFormatter.arrayFormat(messagePattern, argArray);
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.DEBUG, null, msgStr, t));
    }

    /**
     * Logs a message at Level.DEBUG after having substituted the elements of 'argArray' into the 'messagePattern' at locations designated by '{}'s.
     *
     * Example: String fieldName = "foo"; String value = "bar"; LOG.trace("fieldName:{}={}", new Object[]{fieldName,value});
     *
     * Yields: "fieldName:foo=bar"
     *
     * @param messagePattern null is ok.
     * @param argArray null is ok.
     */
    public void debug(String messagePattern, Object... argArray) {
        if (!logger.isDebugEnabled()) {
            return;
        }

        loggerSummary.debugs++;
        loggerDebugsCount.inc();
        String msgStr = MessageFormatter.arrayFormat(messagePattern, argArray);
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.DEBUG, null, msgStr, null));
    }

    /**
     * Logs a String message at Level.DEBUG with an exception.
     *
     * @param msg null is ok.
     * @param t null is NOT ok.
     */
    public void debug(String msg, Throwable t) {
        if (!logger.isDebugEnabled()) {
            return;
        }

        loggerSummary.debugs++;
        loggerDebugsCount.inc();
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.DEBUG, null, msg, t));
    }

    /** Is the underlying logback logger enabled at Level.WARN level. */
    public boolean isWarnEnabled() {
        return logger.isEnabledFor(Level.WARN);
    }

    /**
     * Logs a String message at Level.WARN.
     *
     * @param msg null is ok
     */
    public void warn(String msg) {
        if (!logger.isEnabledFor(Level.WARN)) {
            return;
        }

        loggerSummary.warns++;
        loggerWarnsCount.inc();
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.WARN, null, msg, null));
        loggerSummary.lastNWarns.add(msg);
    }

    /**
     * Logs a message at Level.WARN after having substituted the 'arg' into the 'messagePattern' at location designated by '{}'.
     *
     * Example: String fieldName = "foo"; LOG.trace("fieldName:{}", fieldName);
     *
     * Yields: "fieldName:foo"
     *
     * @param messagePattern null is ok.
     * @param arg null is ok.
     */
    public void warn(String messagePattern, Object arg) {
        if (!logger.isEnabledFor(Level.WARN)) {
            return;
        }

        loggerSummary.warns++;
        loggerWarnsCount.inc();
        String msgStr = MessageFormatter.format(messagePattern, arg);
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.WARN, null, msgStr, null));
        loggerSummary.lastNWarns.add(String.format(messagePattern, arg));
    }

    /**
     * Logs a message at Level.WARN after having substituted the 'arg1' and 'arg2' into the 'messagePattern' at locations designated by '{}'s.
     *
     * Example: String fieldName = "foo"; String value = "bar"; LOG.trace("fieldName:{}={}", fieldName, value);
     *
     * Yields: "fieldName:foo=bar"
     *
     * @param messagePattern null is ok.
     * @param arg1 null is ok.
     * @param arg2 null is ok.
     */
    public void warn(String messagePattern, Object arg1, Object arg2) {
        if (!logger.isEnabledFor(Level.WARN)) {
            return;
        }

        loggerSummary.warns++;
        loggerWarnsCount.inc();
        String msgStr = MessageFormatter.format(messagePattern, arg1, arg2);
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.WARN, null, msgStr, null));
        loggerSummary.lastNWarns.add(String.format(messagePattern, arg1, arg2));
    }

    /**
     * Logs a message at Level.WARN with an exception after having substituted the elements of 'argArray' into the 'messagePattern'
     * at locations designated by '{}'s.
     *
     * Example: String fieldName = "foo"; String value = "bar"; LOG.trace("fieldName:{}={}", new Object[]{fieldName,value});
     *
     * Yields: "fieldName:foo=bar"
     *
     * @param messagePattern null is ok.
     * @param argArray null is ok.
     * @param t null is NOT ok.
     */
    public void warn(String messagePattern, Object[] argArray, Throwable t) {
        if (!logger.isEnabledFor(Level.WARN)) {
            return;
        }

        loggerSummary.warns++;
        loggerWarnsCount.inc();
        String msgStr = MessageFormatter.arrayFormat(messagePattern, argArray);
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.WARN, null, msgStr, t));
        loggerSummary.lastNWarns.add(String.format(messagePattern, argArray));
    }

    /**
     * Logs a message at Level.WARN after having substituted the elements of 'argArray' into the 'messagePattern' at locations designated by '{}'s.
     *
     * Example: String fieldName = "foo"; String value = "bar"; LOG.trace("fieldName:{}={}", new Object[]{fieldName,value});
     *
     * Yields: "fieldName:foo=bar"
     *
     * @param messagePattern null is ok.
     * @param argArray null is ok.
     */
    public void warn(String messagePattern, Object... argArray) {
        if (!logger.isEnabledFor(Level.WARN)) {
            return;
        }

        loggerSummary.warns++;
        loggerWarnsCount.inc();
        String msgStr = MessageFormatter.arrayFormat(messagePattern, argArray);
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.WARN, null, msgStr, null));
        loggerSummary.lastNWarns.add(String.format(messagePattern, argArray));
    }

    /**
     * Logs a String message at Level.WARN with an exception.
     *
     * @param msg null is ok.
     * @param t null is NOT ok.
     */
    public void warn(String msg, Throwable t) {
        if (!logger.isEnabledFor(Level.WARN)) {
            return;
        }

        loggerSummary.warns++;
        loggerWarnsCount.inc();
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.WARN, null, msg, t));
        loggerSummary.lastNWarns.add(msg + (t != null ? " " + t.toString() : ""));
    }

    /** Is the underlying logback logger enabled at Level.INFO level. */
    public boolean isInfoEnabled() {
        return logger.isEnabledFor(Level.INFO);
    }

    /**
     * Logs a String message at Level.INFO.
     *
     * @param msg null is ok
     */
    public void info(String msg) {
        if (!logger.isInfoEnabled()) {
            return;
        }

        loggerSummary.infos++;
        loggerInfosCount.inc();
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.INFO, null, msg, null));
        loggerSummary.lastNInfos.add(msg);
    }

    /**
     * Logs a message at Level.INFO after having substituted the 'arg' into the 'messagePattern' at location designated by '{}'.
     *
     * Example: String fieldName = "foo"; LOG.trace("fieldName:{}", fieldName);
     *
     * Yields: "fieldName:foo"
     *
     * @param messagePattern null is ok.
     * @param arg null is ok.
     */
    public void info(String messagePattern, Object arg) {
        if (!logger.isInfoEnabled()) {
            return;
        }

        loggerSummary.infos++;
        loggerInfosCount.inc();
        String msgStr = MessageFormatter.format(messagePattern, arg);
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.INFO, null, msgStr, null));
        loggerSummary.lastNInfos.add(String.format(messagePattern, arg));
    }

    /**
     * Logs a message at Level.INFO after having substituted the 'arg1' and 'arg2' into the 'messagePattern' at locations designated by '{}'s.
     *
     * Example: String fieldName = "foo"; String value = "bar"; LOG.trace("fieldName:{}={}", fieldName, value);
     *
     * Yields: "fieldName:foo=bar"
     *
     * @param messagePattern null is ok.
     * @param arg1 null is ok.
     * @param arg2 null is ok.
     */
    public void info(String messagePattern, Object arg1, Object arg2) {
        if (!logger.isInfoEnabled()) {
            return;
        }

        loggerSummary.infos++;
        loggerInfosCount.inc();
        String msgStr = MessageFormatter.format(messagePattern, arg1, arg2);
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.INFO, null, msgStr, null));
        loggerSummary.lastNInfos.add(String.format(messagePattern, arg1, arg2));
    }

    /**
     * Logs a message at Level.INFO with an exception after having substituted the elements of 'argArray' into the 'messagePattern'
     * at locations designated by '{}'s.
     *
     * Example: String fieldName = "foo"; String value = "bar"; LOG.trace("fieldName:{}={}", new Object[]{fieldName,value});
     *
     * Yields: "fieldName:foo=bar"
     *
     * @param messagePattern null is ok.
     * @param argArray null is ok.
     */
    public void info(String messagePattern, Object[] argArray, Throwable t) {
        if (!logger.isInfoEnabled()) {
            return;
        }

        loggerSummary.infos++;
        loggerInfosCount.inc();
        String msgStr = MessageFormatter.arrayFormat(messagePattern, argArray);
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.INFO, null, msgStr, t));
        loggerSummary.lastNInfos.add(String.format(messagePattern, argArray));
    }

    /**
     * Logs a message at Level.INFO after having substituted the elements of 'argArray' into the 'messagePattern' at locations designated by '{}'s.
     *
     * Example: String fieldName = "foo"; String value = "bar"; LOG.trace("fieldName:{}={}", new Object[]{fieldName,value});
     *
     * Yields: "fieldName:foo=bar"
     *
     * @param messagePattern null is ok.
     * @param argArray null is ok.
     */
    public void info(String messagePattern, Object... argArray) {
        if (!logger.isInfoEnabled()) {
            return;
        }

        loggerSummary.infos++;
        loggerInfosCount.inc();
        String msgStr = MessageFormatter.arrayFormat(messagePattern, argArray);
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.INFO, null, msgStr, null));
        loggerSummary.lastNInfos.add(String.format(messagePattern, argArray));
    }

    /**
     * Logs a String message at Level.INFO with an exception.
     *
     * @param messagePattern null is ok.
     * @param t null is NOT ok.
     */
    public void info(String messagePattern, Throwable t) {
        if (!logger.isInfoEnabled()) {
            return;
        }

        loggerSummary.infos++;
        loggerInfosCount.inc();
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.INFO, null, messagePattern, t));
        loggerSummary.lastNInfos.add(messagePattern + (t != null ? " " + t.toString() : ""));
    }

    /** Is the underlying logback logger enabled at Level.ERROR level. */
    public boolean isErrorEnabled() {
        return logger.isEnabledFor(Level.ERROR);
    }

    /**
     * Logs a String message at Level.ERROR.
     *
     * @param msg null is ok
     */
    public void error(String msg) {
        if (!logger.isEnabledFor(Level.ERROR)) {
            return;
        }

        loggerSummary.errors++;
        loggerErrorsCount.inc();
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.ERROR, null, msg, null));
        loggerSummary.lastNErrors.add(msg);
    }

    /**
     * Logs a String message at Level.ERROR.
     *
     * @param tags null is ok
     * @param msg null is ok
     */
    public void error(String[] tags, String msg) {
        if (!logger.isEnabledFor(Level.ERROR)) {
            return;
        }

        loggerSummary.errors++;
        loggerErrorsCount.inc();
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.ERROR, tags, msg, null));
        loggerSummary.lastNErrors.add(msg);
    }

    /**
     * Logs a message at Level.ERROR after having substituted the 'arg' into the 'messagePattern' at location designated by '{}'.
     *
     * Example: String fieldName = "foo"; LOG.trace("fieldName:{}", fieldName);
     *
     * Yields: "fieldName:foo"
     *
     * @param messagePattern null is ok.
     * @param arg null is ok.
     */
    public void error(String messagePattern, Object arg) {
        if (!logger.isEnabledFor(Level.ERROR)) {
            return;
        }

        loggerSummary.errors++;
        loggerErrorsCount.inc();
        String msgStr = MessageFormatter.format(messagePattern, arg);
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.ERROR, null, msgStr, null));
        loggerSummary.lastNErrors.add(String.format(messagePattern, arg));
    }

    /**
     * Logs a message at Level.ERROR after having substituted the 'arg1' and 'arg2' into the 'messagePattern' at locations designated by '{}'s.
     *
     * Example: String fieldName = "foo"; String value = "bar"; LOG.trace("fieldName:{}={}", fieldName, value);
     *
     * Yields: "fieldName:foo=bar"
     *
     * @param messagePattern null is ok.
     * @param arg1 null is ok.
     * @param arg2 null is ok.
     */
    public void error(String messagePattern, Object arg1, Object arg2) {
        if (!logger.isEnabledFor(Level.ERROR)) {
            return;
        }

        loggerSummary.errors++;
        loggerErrorsCount.inc();
        String msgStr = MessageFormatter.format(messagePattern, arg1, arg2);
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.ERROR, null, msgStr, null));
        loggerSummary.lastNErrors.add(String.format(messagePattern, arg1, arg2));
    }

    /**
     * Logs a message at Level.ERROR with an exception after having substituted the elements of 'argArray' into the 'messagePattern'
     * at locations designated by '{}'s.
     *
     * Example: String fieldName = "foo"; String value = "bar"; LOG.trace("fieldName:{}={}", new Object[]{fieldName,value});
     *
     * Yields: "fieldName:foo=bar"
     *
     * @param messagePattern null is ok.
     * @param argArray null is ok.
     * @param t null is NOT ok.
     */
    public void error(String messagePattern, Object[] argArray, Throwable t) {
        if (!logger.isEnabledFor(Level.ERROR)) {
            return;
        }

        loggerSummary.errors++;
        loggerErrorsCount.inc();
        String msgStr = MessageFormatter.arrayFormat(messagePattern, argArray);
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.ERROR, null, msgStr, t));
        loggerSummary.lastNErrors.add(String.format(messagePattern, argArray));
    }

    /**
     * Logs a message at Level.ERROR after having substituted the elements of 'argArray' into the 'messagePattern' at locations designated by '{}'s.
     *
     * Example: String fieldName = "foo"; String value = "bar"; LOG.trace("fieldName:{}={}", new Object[]{fieldName,value});
     *
     * Yields: "fieldName:foo=bar"
     *
     * @param messagePattern null is ok.
     * @param argArray null is ok.
     */
    public void error(String messagePattern, Object... argArray) {
        if (!logger.isEnabledFor(Level.ERROR)) {
            return;
        }

        loggerSummary.errors++;
        loggerErrorsCount.inc();
        String msgStr = MessageFormatter.arrayFormat(messagePattern, argArray);
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.ERROR, null, msgStr, null));
        loggerSummary.lastNErrors.add(String.format(messagePattern, argArray));
    }

    /**
     * Logs a message at Level.ERROR after having substituted the elements of 'argArray' into the 'messagePattern' at locations designated by '{}'s.
     *
     * Example: String fieldName = "foo"; String value = "bar"; LOG.trace("fieldName:{}={}", new Object[]{fieldName,value});
     *
     * Yields: "fieldName:foo=bar"
     *
     * @param tags null is ok.
     * @param messagePattern null is ok.
     * @param argArray null is ok.
     */
    public void error(String[] tags, String messagePattern, Object[] argArray) {
        if (!logger.isEnabledFor(Level.ERROR)) {
            return;
        }

        loggerSummary.errors++;
        loggerErrorsCount.inc();
        String msgStr = MessageFormatter.arrayFormat(messagePattern, argArray);
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.ERROR, tags, msgStr, null));
        loggerSummary.lastNErrors.add(String.format(messagePattern, argArray));
    }

    /**
     * Logs a String message at Level.ERROR with an exception.
     *
     * @param messagePattern null is ok.
     * @param t null is NOT ok.
     */
    public void error(String messagePattern, Throwable t) {
        if (!logger.isEnabledFor(Level.ERROR)) {
            return;
        }

        loggerSummary.errors++;
        loggerErrorsCount.inc();
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.ERROR, null, messagePattern, t));
        loggerSummary.lastNErrors.add(messagePattern + (t != null ? " " + t.toString() : ""));
    }

    /**
     * Logs a String[] of tags with String message at Level.ERROR with an exception.
     *
     * @param messagePattern null is ok.
     * @param t null is NOT ok.
     */
    public void error(String[] tags, String messagePattern, Throwable t) {
        if (!logger.isEnabledFor(Level.ERROR)) {
            return;
        }

        loggerSummary.errors++;
        loggerErrorsCount.inc();
        logger.callAppenders(new MetricLogEvent(fullQualifiedClassName, logger, Level.ERROR, tags, messagePattern, t));
        loggerSummary.lastNErrors.add(messagePattern + (t != null ? " " + t.toString() : ""));
    }

    /**
     * Allows you set the ValueType the name and the value for a specific metric.
     *
     * Metric names can be organized hierarchically by using the greater than separator. For example: LOG.set(ValueType.COUNT, "foo>bar>a", 1);
     * LOG.set(ValueType.COUNT, "foo>bar>b", 2); LOG.set(ValueType.COUNT, "foo>bazz>c", 3);
     *
     * Yields: foo |-- bar | |-- a = 1 | |-- b = 2 |-- baz | |-- c = 3
     *
     * @param type null NOT ok.
     * @param name null NOT ok.
     * @param value null NOT ok.
     */
    public void set(ValueType type, String name, long value) {
        countersAndTimers.counter(type, name).set(value);
    }

    /**
     * Same as set(ValueType type, String name, long value) except it uses an AtomicLong. This is notably slower than the simple set(ValueType type, String
     * name, long value) form. Only use if you absolutely have to have an accurate count.
     *
     * @param type null NOT ok.
     * @param name null NOT ok.
     * @param value null NOT ok.
     */
    public void setAtomic(ValueType type, String name, long value) {
        countersAndTimers.atomicCounter(type, name).set(value);
    }

    /**
     * Increments a named long. Counts are not guaranteed to be exact.
     *
     * Metric names can be organized hierarchically by using the greater than separator. For example: LOG.inc("foo>bar>a"); LOG.inc("foo>bar>b");
     * LOG.inc("foo>bazz>c");
     *
     * Yields: foo |-- bar | |-- a = 1 | |-- b = 1 |-- baz | |-- c = 1
     *
     * @param name null NOT ok.
     */
    public void inc(String name) {
        countersAndTimers.counter(ValueType.COUNT, name).inc();
    }

    /**
     * Increments a named long by an amount. Counts are not guaranteed to be exact.
     *
     * Metric names can be organized hierarchically by using the greater than separator. For example: LOG.inc("foo>bar>a",10); LOG.inc("foo>bar>b",20);
     * LOG.inc("foo>bazz>c",20);
     *
     * Yields: foo |-- bar | |-- a = 10 | |-- b = 20 |-- baz | |-- c = 30
     *
     * @param name null NOT ok.
     * @param amount negative numbers are ok.
     */
    public void inc(String name, long amount) {
        countersAndTimers.counter(ValueType.COUNT, name).inc(amount);
    }

    /**
     * Opposite of {@link #inc(String name)}
     *
     * @param name null NOT ok.
     */
    public void dec(String name) {
        countersAndTimers.counter(ValueType.COUNT, name).dec();
    }

    /**
     * Opposite of {@link #inc(String name, long amount)}
     *
     * @param name of the counter
     * @param amount for the counter
     */
    public void dec(String name, long amount) {
        countersAndTimers.counter(ValueType.COUNT, name).dec(amount);
    }

    /**
     * Increments a named AtomicLong. Counts guaranteed to be exact.
     *
     * Metric names can be organized hierarchically by using the greater than separator. For example: LOG.incAtomic("foo>bar>a"); LOG.incAtomic("foo>bar>b");
     * LOG.incAtomic("foo>bazz>c");
     *
     * Yields: foo |-- bar | |-- a = 1 | |-- b = 1 |-- baz | |-- c = 1
     *
     * @param name null NOT ok.
     */
    public void incAtomic(String name) {
        countersAndTimers.atomicCounter(ValueType.COUNT, name).inc();
    }

    /**
     * Increments a named AtomicLong by an amount. Counts are guaranteed to be exact.
     *
     * Metric names can be organized hierarchically by using the greater than separator. For example: LOG.incAtomic("foo>bar>a",10);
     * LOG.incAtomic("foo>bar>b",20); LOG.incAtomic("foo>bazz>c",20);
     *
     * Yields: foo |-- bar | |-- a = 10 | |-- b = 20 |-- baz | |-- c = 30
     *
     * @param name null NOT ok.
     * @param amount negative numbers are ok.
     */
    public void incAtomic(String name, long amount) {
        countersAndTimers.atomicCounter(ValueType.COUNT, name).inc(amount);
    }

    /**
     * Opposite of {@link #incAtomic(String name)}
     *
     * @param name null NOT ok.
     */
    public void decAtomic(String name) {
        countersAndTimers.atomicCounter(ValueType.COUNT, name).dec();
    }

    /**
     * Opposite of {@link #incAtomic(String name, long amount)}
     *
     * @param name null NOT ok.
     * @param amount null NOT ok.
     */
    public void decAtomic(String name, long amount) {
        countersAndTimers.atomicCounter(ValueType.COUNT, name).dec(amount);
    }

    /**
     * Starts a named timer. Each time a time is started and stopped its elapse is added as a sample to
     * org.apache.commons.math.stat.descriptive.SummaryStatistics;
     *
     * Each threads timings are captured discreetly. If the same thread re-enters start before stop is called it will not advance the start time.
     *
     * Metric names can be organized hierarchically by using the greater than separator. For example: try { LOG.startTimer("foo>bar>a");
     * LOG.startTimer("foo>bar>b"); LOG.startTimer("foo>bazz>c");
     *
     * ....... } finally { LOG.stopTimer("foo>bar>a"); LOG.stopTimer("foo>bar>b"); LOG.stopTimer("foo>bazz>c"); }
     *
     * Yields: foo |-- bar | |-- a = 234325 (millis) | |-- b = 234328 (millis) |-- baz | |-- c = 234333 (millis)
     *
     * @param name null NOT ok.
     */
    public void startTimer(String name) {
        countersAndTimers.startTimer(name);
    }

    /**
     * Stops a named timer. See: {@link #startTimer(String name)} This is a convenience method that delegates to
     * {@link #stopTimer(java.lang.String, java.lang.String)}
     *
     * @param name null NOT ok.
     * @return elapse in millis
     */
    public long stopTimer(String name) {
        return countersAndTimers.stopTimer(name, name);
    }


    /**
     * Starts a named timer. Each time a time is started and stopped its elapse is added as a sample to
     * org.apache.commons.math.stat.descriptive.SummaryStatistics;
     *
     * Each threads timings are captured discreetly. If the same thread re-enters start before stop is called it will not advance the start time.
     *
     * Metric names can be organized hierarchically by using the greater than separator. For example: try { LOG.startTimer("foo>bar>a");
     * LOG.startTimer("foo>bar>b"); LOG.startTimer("foo>bazz>c");
     *
     * ....... } finally { LOG.stopTimer("foo>bar>a"); LOG.stopTimer("foo>bar>b"); LOG.stopTimer("foo>bazz>c"); }
     *
     * Yields: foo |-- bar | |-- a = 234325 (nanos) | |-- b = 234328 (nanos) |-- baz | |-- c = 234333 (nanos)
     *
     * @param name null NOT ok.
     */
    public void startNanoTimer(String name) {
        countersAndTimers.startNanoTimer(name);
    }

    /**
     * Stops a named timer. See: {@link #startTimer(String name)} This is a convenience method that delegates to
     * {@link #stopTimer(java.lang.String, java.lang.String)}
     *
     * @param name null NOT ok.
     * @return elapse in millis
     */
    public long stopNanoTimer(String name) {
        return countersAndTimers.stopNanoTimer(name, name);
    }

    /**
     * Stops a named timer. See: {@link #startTimer(String name)}
     *
     * When you have a method with multiply returns this method allows you to return the timings around each distinct return.
     *
     * Example: try { logger.startTimer("foo"); if (bar) { // do if stuff stopTimer("foo","firstReturn"); return; } else { // do else stuff
     * stopTimer("foo","secondReturn"); return; } } finally { stopTimer("foo","all"); }
     *
     * @param name null NOT ok.
     * @param recordedName null NOT ok.
     * @return elapse in millis
     */
    public long stopTimer(String name, String recordedName) {
        return countersAndTimers.stopTimer(name, recordedName);
    }

    /**
     * Start named timed operation and return {@link TimedOperation} for additional functionallity.<br/>
     * The name parameter is combined with the logger name so there is no need to excplicitly provide it.<br/>
     * {@link TimedOperation} is {@link AutoCloseable} to it can be used inside try block.<br/>
     *
     * @param name the name of the named timed operation
     * @return new operation object
     */
    public TimedOperation startTimedOperation(String name) {
        return new TimedOperation(this, name);
    }

    /**
     * Start named timed operation and return {@link TimedOperation} for additional functionallity.<br/>
     * The name parameter is combined with the logger name so there is no need to excplicitly provide it.<br/>
     * {@link TimedOperation} is {@link AutoCloseable} to it can be used inside try block.<br/>
     *
     * @param name the name of the named timed operation
     * @param initialStatus the initial status to set on the operation.
     * @return new operation object
     */
    public TimedOperation startTimedOperation(String name, TimedOperation.Status initialStatus) {
        return new TimedOperation(this, name, initialStatus);
    }

    /**
     * Start named timed operation and return {@link TimedOperation} for additional functionallity.<br/>
     * The name parameter is combined with the logger name so there is no need to excplicitly provide it.<br/>
     * {@link TimedOperation} is {@link AutoCloseable} to it can be used inside try block.<br/>
     *
     * @param name the name of the named timed operation
     * @param tenantId The tenant that is executing the operation.
     * @return new operation object
     */
    public TimedOperation startTimedOperation(String name, Object tenantId) {
        return new TimedOperation(this, name, tenantId);
    }

    /**
     * Start named timed operation and return {@link TimedOperation} for additional functionallity.<br/>
     * The name parameter is combined with the logger name so there is no need to excplicitly provide it.<br/>
     * {@link TimedOperation} is {@link AutoCloseable} to it can be used inside try block.<br/>
     *
     * @param name the name of the named timed operation
     * @param tenantId The tenant that is executing the operation.
     * @param initialStatus the initial status to set on the operation.
     * @return new operation object
     */
    public TimedOperation startTimedOperation(String name, Object tenantId, TimedOperation.Status initialStatus) {
        return new TimedOperation(this, name, tenantId, initialStatus);
    }

    /**
     * Log a Metrics Event
     *
     * @param name Name of the metric. Try to keep this unique: [jira project]_[feature name]_[event name]
     * @return Metric object which you'll put metric properties onto, and then send().
     */
    public Metric metric(String name) {
        return MetricEvent.metric(name);
    }
}
