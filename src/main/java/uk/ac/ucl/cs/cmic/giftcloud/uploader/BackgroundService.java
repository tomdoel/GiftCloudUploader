/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.util.List;

public abstract class BackgroundService<T_taskType, T_resultType> extends StatusObservable<BackgroundService.ServiceStatus> implements Runnable {

    public enum ServiceStatus {
        INITIALIZED,
        RUNNING,
        STOP_REQUESTED,
        COMPLETE
    }

    enum BackgroundThreadTermination {
        STOP_WHEN_LIST_EMPTY(true),
        CONTINUE_UNTIL_TERMINATED(false);

        private final boolean stopWhenEmpty;

        BackgroundThreadTermination(final boolean stopWhenEmpty) {
            this.stopWhenEmpty = stopWhenEmpty;
        }

        boolean getStopWhenEmpty() {
            return stopWhenEmpty;
        }
    }

    private final BackgroundThreadTermination backgroundThreadTermination;
    private final BackgroundServiceTaskList<T_taskType, T_resultType> backgroundServicePendingList;
    private long maximumThreadCompletionWaitTime;
    private final BackgroundServiceFailureList<T_taskType> backgroundServiceFailureList;
    protected final GiftCloudReporter reporter;
    private Thread serviceThread = null;
    private ServiceStatus serviceStatus = ServiceStatus.INITIALIZED;

    public BackgroundService(final BackgroundThreadTermination backgroundThreadTermination, final BackgroundServiceTaskList<T_taskType, T_resultType> backgroundServicePendingList, final long maximumThreadCompletionWaitTime, final GiftCloudReporter reporter) {
        this.backgroundThreadTermination = backgroundThreadTermination;
        this.backgroundServicePendingList = backgroundServicePendingList;
        this.maximumThreadCompletionWaitTime = maximumThreadCompletionWaitTime;
        this.backgroundServiceFailureList = new BackgroundServiceFailureList<T_taskType>();
        this.reporter = reporter;
    }

    abstract protected void processItem(final T_resultType backgroundServiceResult) throws Exception;
    abstract protected void notifySuccess(final BackgroundServiceTaskWrapper<T_taskType, T_resultType> taskWrapper);
    abstract protected void notifyFailure(final BackgroundServiceTaskWrapper<T_taskType, T_resultType> taskWrapper);

    public final synchronized void start() {

        if (isRunning()) {
            return;
        }

        // If the thread is still waiting to end from a previous stop() then we block and wait, up to the timeout limit
        if (!waitForThreadCompletion(maximumThreadCompletionWaitTime)) {
            reporter.silentWarning("A background service has been re-started, but the previous thread has not yet terminated. I am starting a new thread anyway.");
        }

        updateServiceStatus(ServiceStatus.RUNNING);

        serviceThread = new Thread(this);
        serviceThread.start();
    }

    public final synchronized void stop() {
        if (serviceThread != null) {
            updateServiceStatus(ServiceStatus.STOP_REQUESTED);
            serviceThread.interrupt();
        }
    }

    public final void run() {

        doPreprocessing();

        // An InterruptedException is only received if the thread is currently blocking. If this happens the interrupted
        // flag is not set. If the thread is not blocking, the interrupted flag is set but an exception does not occur.
        // Therefore we must check both for the interrupted flag and for the exception in order to correctly process an interruption.
        while (!serviceThread.isInterrupted() && continueProcessing()) {
            try {
                final BackgroundServiceTaskWrapper<T_taskType, T_resultType> backgroundServiceResult = backgroundServicePendingList.take();
                try {
                    processItem(backgroundServiceResult.getResult());
                    notifySuccess(backgroundServiceResult);

                } catch (Throwable e) {
                    reporter.silentLogException(e, "Service failed with the following error:" + e.getLocalizedMessage());
                    backgroundServiceResult.addError(e);

                    if (backgroundServiceResult.shouldRetry()) {
                        backgroundServicePendingList.retryTask(backgroundServiceResult.getTask(), backgroundServiceResult.getErrorRecord());
                    } else {
                        backgroundServiceFailureList.addFailure(backgroundServiceResult.getTask(), backgroundServiceResult.getErrorRecord());
                        notifyFailure(backgroundServiceResult);
                    }
                }

            } catch (InterruptedException e) {
                // The interrupted flag is not set if an InterruptedException was received
                serviceThread.interrupt();
            }
        }

        updateServiceStatus(ServiceStatus.COMPLETE);

        doPostprocessing();

        // We leave all remaining items on the queue so they can be processed if the thread is restarted
    }

    /**
     * Perform any processing which should occur when the thread starts
     */
    protected void doPreprocessing() {
    }

    /**
     * Perform any processing which should occur when the thread starts
     */
    protected void doPostprocessing() {
    }

    public final boolean isRunning() {
        return (serviceStatus == ServiceStatus.RUNNING);
    }

    public final List<BackgroundServiceFailureList<T_taskType> .FailureRecord> getFailures() {
        return backgroundServiceFailureList.getFailures();
    }

    /**
     * Waits for the thread to complete, up to the specified timeout limit.
     * @return true if the thread has completed or was never started
     */
    public final boolean waitForThreadCompletion(final long maximumThreadCompletionWaitTime) {
        if (serviceThread == null) {
            return true;
        } else {
            try {
                serviceThread.join(maximumThreadCompletionWaitTime);
            } catch (InterruptedException e) {
            }
            return !serviceThread.isAlive();
        }
    }

    private void updateServiceStatus(final ServiceStatus requestedServiceStatus) {

        // The synchronization block ensure that if the thread completes before the service status is set to
        // STOP_REQUESTED, then we keep the status as STOPPED
        synchronized (serviceStatus) {
            if (!(serviceStatus == ServiceStatus.COMPLETE && requestedServiceStatus == ServiceStatus.STOP_REQUESTED)) {
                serviceStatus = requestedServiceStatus;
                notifyStatusChanged(serviceStatus);
            }
        }
    }

    /**
     * BackgroundService calls this method to determine whether to make a further blocking take() call or to terminate
     * @return
     */
    private final boolean continueProcessing() {
        if (backgroundThreadTermination.getStopWhenEmpty()) {
            return !backgroundServicePendingList.isEmpty();
        } else {
            return true;
        }
    }
}
