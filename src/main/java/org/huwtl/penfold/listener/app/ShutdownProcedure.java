package org.huwtl.penfold.listener.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.SECONDS;

class ShutdownProcedure implements Runnable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownProcedure.class);

    private static final int TIMEOUT = 90;

    private final ExecutorService executorService;

    private final Thread shutdownThread;

    public ShutdownProcedure(ExecutorService executorService)
    {
        this.executorService = executorService;
        shutdownThread = new Thread(this, format("shutdown-procedure:%s", executorService));
    }

    public void registerShutdownHook()
    {
        getRuntime().addShutdownHook(shutdownThread);
    }

    public void removeShutdownHook()
    {
        getRuntime().removeShutdownHook(shutdownThread);
    }

    @Override public void run()
    {
        LOGGER.info("Shutdown started");

        if (!executorService.isTerminated())
        {
            terminateExecutor();
        }
        else
        {
            LOGGER.info("executor-service is already terminated");
        }

        LOGGER.info("Shutdown completed");
    }

    public void runAndRemoveHook()
    {
        run();
        removeShutdownHook();
    }

    private void terminateExecutor()
    {
        try
        {
            stopAcceptingNewJobs();
            waitForRunningJobsToTerminate();
        }
        catch (InterruptedException e)
        {
            forceShutdown();
        }
    }

    private void stopAcceptingNewJobs()
    {
        LOGGER.info("No new jobs accepted");

        if (!executorService.isShutdown())
        {
            executorService.shutdown();
        }
        else
        {
            LOGGER.info("executor-service is already shutdown");
        }
    }

    private void waitForRunningJobsToTerminate() throws InterruptedException
    {
        LOGGER.info("Terminating all executor-service jobs. Timeout is {} {}", TIMEOUT, SECONDS);

        if (executorService.awaitTermination(TIMEOUT, SECONDS))
        {
            LOGGER.info("All jobs terminated normally");
        }
        else
        {
            LOGGER.warn("Running jobs did not complete within timeout. Forcing shutdown.");

            executorService.shutdownNow();
        }
    }

    private void forceShutdown()
    {
        LOGGER.warn("Shutdown thread was interrupted. Forcing executor shutdown.");

        executorService.shutdownNow();

        currentThread().interrupt();
    }
}
