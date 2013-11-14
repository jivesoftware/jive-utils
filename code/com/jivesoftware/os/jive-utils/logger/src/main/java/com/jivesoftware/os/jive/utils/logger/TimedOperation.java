// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tsu,
// "The Art of War"

package com.jivesoftware.os.jive.utils.logger;

import java.text.MessageFormat;

/**
 * Used to gather operation metric data.<br/>
 * The metric data is used to mine the health and performance of the system with high granularity.<br/>
 * <p>
 * Timed operation will measure 4 metrics:<br/>
 * 1. The number of times the operation was executed (name).<br/>
 * 2. The number of times the operation has finished in each status (name>NONE, name>SUCCESS, name>FAILED, etc).<br/>
 * 3. The time the operation execution took (name).<br/>
 * 4. The time the operation execution took and finished in each status (name>NONE, name>SUCCESS, name>FAILED, etc).<br/>
 * It is also possible to add metric counts to the operation using {@link #inc(String, long)} where the
 * subName will be added to the name of the operation (name>subName).<br/>
 * </p>
 * <p>
 * <b>Example</b><br/>
 * <pre>
 * try(TimedOperation op = LOG.startTimedOperation("loadData>step1", tenantId)){
 *     // load data code...
 *     op.setSuccessful();
 *     return data;
 * }l
 * </pre>
 * {@link TimedOperation} will automatically measure the time between the start and the close of
 * the operation at the end of the scope.<br/>
 * If the operation suceed it will reach the {@link #setSuccessful()} method to indicate it was successful, if
 * the operation failed by exception it will not reach the method so the operation will be NONE.<br/>
 * You can pass {@link TimedOperation.Status#FAILED} to {@link MetricLogger#startTimedOperation} as the initial
 * operation status so if exception is thrown the operation status will be FAILED and only if it reaches
 * the end it will be SUCCESS.<br/>
 * </p>
 * <p>
 * <b>Metrics granularity</b><br/>
 * 1. MetrixLogger - Encapsulates the name of the class that collects the metrics, sets the component
 * part of the system for the collected matrics (part of the heirarchiel name of the operation).<br/>
 * 2. Name - Custom free text name that can have heirarchiel structure (using '>' splitter), this
 * allows creation of metrics for operations and sub-operation to increase mining granularity.<br/>
 * 3. Tenant - Optional, ability to split metrics for each tenant.<br/>
 * 4. Status - Split the metric data to successful and failed operations.<br/>
 * </p>
 * <p>
 * <b>Important of Tenant</b><br/>
 * In a multi tenant environment we want to have the ability to slice the metrics by tenant to understand
 * the real expirience that a specific tenant is having.<br/>
 * In the most simplest terms we want to be sure each tenant is receiving the SLA we are aspiring for.
 * </p>d
 * <p>
 * <b>Important of Status</b><br/>
 * The most critical factor for the health of the system, seperating the successful and failed operation
 * and factoring the elapsed times of the operations is the best indication if the system is working properly.<br/>
 * Additionally having seperate status for validation failures allows to minigate the false alarms for
 * large amount of failures and incorrect timing data.
 * </p>
 * <p>
 * <b>Backend support</b><br/>
 * TODO: Need work to make this data actually visible by the backend ops services.
 * </p>
 */
public final class TimedOperation implements AutoCloseable {

    //region: Fields and Consts

    /**
     * The logger used for the timer logging
     */
    private final MetricLogger logger;

    /**
     * the name of the operation
     */
    private final String name;

    /**
     * the name of the operation for status
     */
    private String nameStatus;

    /**
     * The current status of the operation
     */
    private Status status = Status.NONE;

    /**
     * The tenant that is executing the operation.<br/>
     * Used for the ability to slice metric data per tenant.
     */
    private Object tenantId;

    /**
     * is the timed operation has been stopped so it won't call stop twice
     */
    private boolean stopped;
    //endregion

    /**
     * Init new timed operation using the given logger and name.
     *
     * @param logger the logger to log the operation with
     * @param name the name of the operation (NOT NULL)
     * @param initialStatus the initial status to set on the operation.
     */
    public TimedOperation(MetricLogger logger, String name, Status initialStatus) {
        this.logger = logger;
        this.name = name;
        this.status = initialStatus;
        startOperation();
    }

    /**
     * Init new timed operation using the given logger and name.
     *
     * @param logger the logger to log the operation with
     * @param name the name of the operation (NOT NULL)
     */
    public TimedOperation(MetricLogger logger, String name) {
        this.logger = logger;
        this.name = name;
        startOperation();
    }

    /**
     * Init new timed operation using the given logger, name and tenant.
     *
     * @param logger the logger to log the operation with
     * @param name the name of the operation (NOT NULL)
     * @param tenantId The tenant that is executing the operation.
     */
    public TimedOperation(MetricLogger logger, String name, Object tenantId) {
        this.logger = logger;
        this.name = name;
        this.tenantId = tenantId;
        startOperation();
    }

    /**
     * Init new timed operation using the given logger, name and tenant.
     *
     * @param logger the logger to log the operation with
     * @param name the name of the operation (NOT NULL)
     * @param tenantId The tenant that is executing the operation.
     */
    public TimedOperation(MetricLogger logger, String name, Object tenantId, Status initialStatus) {
        this.logger = logger;
        this.name = name;
        this.tenantId = tenantId;
        this.status = initialStatus;
        startOperation();
    }

    /**
     * Set operation status to successful.
     */
    public void setSuccessful() {
        status = Status.SUCCESSFUL;
    }

    /**
     * Set operation status to failed.
     */
    public void setFailed() {
        status = Status.FAILED;
    }

    /**
     * Set the status of the operation to indicate if it is successful, failed, etc.
     *
     * @param status the status to set.
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Set the tenant of the operation.
     */
    public void setTenantId(Object tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * Increments a named long by an amount. Counts are not guaranteed to be exact.<br/>
     * The subName is added to the name of the timed operation.
     *
     * @param subName the subName to add to the timed operation "name>subName".
     * @param amount the amount to add.
     */
    public void inc(String subName, long amount) {
        logger.inc(name + ">" + subName, amount);
    }

    /**
     * Stops a named timer and logs the timer operation.
     *
     * @return the elapsed time in msec
     */
    public long stop() {
        return stop(null);
    }

    /**
     * Stops a named timer with recorded stop name and logs the timer operation.
     *
     * @param recordedName optional name to record special stop
     * @return the elapsed time in msec
     */
    public long stop(String subName) {
        stopped = true;
        long elapse = 0;
        try {
            String nameSetStatus = name + ">" + status;
            logger.inc(nameSetStatus);
            if (subName == null) {
                elapse = logger.stopTimer(name);
                logger.stopTimer(nameStatus, nameSetStatus);
            } else {
                elapse = logger.stopTimer(name, name + ">" + subName);
                logger.stopTimer(nameStatus, name + ">" + subName + ">" + status);
            }
            if (logger.isDebugEnabled()) {
                logMessage(elapse, subName);
            }
        } catch (Exception e) {
            logger.error("Exception in timed operation: " + e.getMessage(), e);
        }
        return elapse;
    }

    @Override
    public void close() {

        // make sure stop is not called twice if was called explicitly
        if (!stopped) {
            stop();
        }
    }

    //region: Private methods

    /**
     * Start the the operation timer and log the needed data.
     */
    private void startOperation() {
        nameStatus = name + ">status";
        logger.startTimer(name);
        logger.startTimer(nameStatus);
        logger.inc(name);
        logger.debug("Start timed operation... [{}] [Status: {}] [Tenant: {}]", name, status, tenantId);
    }

    /**
     * Log the timed operation as log message for simple log tracking.
     *
     * @param recordedName optional name to record special stop
     */
    private void logMessage(long elapse, String recordedName) {
        String msg = MessageFormat.format("Timed operation complete [{0}{1}{2}] [{3}] [elapse: {4} msec] [Tenant: {5}]",
            name, recordedName != null ? ">>" : null, recordedName, status, elapse, tenantId);
        logger.debug(msg);
    }
    //endregion

    //region: Status enum

    /**
     * The possible statuses of the operation: None, Success, Failed, FailedValidation.<br/>
     */
    public enum Status {

        /**
         * The operation status is unset.
         */
        NONE(0),

        /**
         * The operation is successful without error.
         */
        SUCCESSFUL(1),

        /**
         * Operation failed.
         */
        FAILED(2),

        /**
         * Operation failed on input validation.
         */
        FAILED_VALIDATION(3);

        /**
         * Used to have a numeric value for the status.
         */
        private final int code;

        /**
         * Init.
         */
        private Status(int code) {
            this.code = code;
        }

        /**
         * Get the code for the status.
         *
         * @return code value
         */
        public int getCode() {
            return code;
        }
    }
    //endregion
}
