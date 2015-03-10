package com.djdch.dev.rdo.daemon.controller;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.djdch.dev.rdo.amqp.Connection;
import com.djdch.dev.rdo.amqp.Subscriber;
import com.djdch.dev.rdo.daemon.ApplicationLauncher;
import com.djdch.dev.rdo.daemon.exception.ApplicationException;
import com.djdch.dev.rdo.daemon.runnable.RequestHandler;
import com.djdch.dev.rdo.daemon.runnable.ShutdownHandler;
import com.djdch.dev.rdo.data.packet.metadata.Client;
import com.rabbitmq.client.ShutdownSignalException;

public class ApplicationController {
    private static final Logger logger = LogManager.getLogger();

    private static final int POOL_SIZE = 10;
    private static final int POOL_TIMEOUT = 30;

    private final ApplicationLauncher launcher;
    private final Connection connection;
    private final Subscriber subscriber;

    private Client broker;
    private ExecutorService pool;
    private boolean running;

    public ApplicationController(ApplicationLauncher launcher) {
        this.launcher = launcher;

        connection = new Connection();
        subscriber = new Subscriber(connection);
    }

    public void start() {
        try {
            logger.debug("Starting RemoteDoorOpenerDaemon");
            running = true;

            broker = Client.createNamedClient(launcher.getRoutingKey());

            Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler(this)));

            try {
                logger.debug("Creating ThreadPool with {} threads", POOL_SIZE);
                pool = Executors.newFixedThreadPool(POOL_SIZE);
            } catch (IllegalArgumentException e) {
                throw new ApplicationException("Exception occurred while creating ThreadPool.", e);
            }

            connection.setHostname(launcher.getHostname());

            try {
                logger.debug("Starting Connection");
                connection.connect();
            } catch (IOException e) {
                throw new ApplicationException("Exception occurred while starting Connection.", e);
            }

            subscriber.setExchange(launcher.getExchange());
            subscriber.setRoutingKey(launcher.getRoutingKey());

            try {
                logger.debug("Starting Subscriber");
                subscriber.start();
            } catch (IOException e) {
                throw new ApplicationException("Exception occurred while starting Subscriber.", e);
            }

            logger.info("RemoteDoorOpenerDaemon started");

            while (running) {
                String message = null;

                try {
                    logger.debug("Waiting for delivery");
                    message = subscriber.getDeliveryBody();
                } catch (InterruptedException e) {
                    throw new ApplicationException("Exception occurred while waiting for deliveries.", e);
                } catch (ShutdownSignalException e) {
                    if (running && connection.isConnected()) {
                        throw new ApplicationException("Shutdown signal received while waiting for deliveries.", e);
                    }
                }

                if (!running) {
                    return; // Already stopped, nothing to do.
                }

                try {
                    logger.debug("New delivery received, sending message to RequestHandler");
                    pool.submit(new RequestHandler(message, connection, broker));
                } catch (RejectedExecutionException e) {
                    throw new ApplicationException("Exception occurred while submitting message to RequestHandler.", e);
                }
            }
        } catch (ApplicationException e) {
            logger.fatal("Exception occurred while running application", e);
            stop();
        } catch (Exception e) {
            logger.fatal("Unexpected exception occurred while running application", e);
            stop();
            throw e;
        }
    }

    public void stop() {
        if (!running) {
            return; // Already stopped, nothing to do.
        }

        logger.debug("Stopping RemoteDoorOpenerDaemon");
        running = false;

        try {
            logger.debug("Stopping Subscriber");
            subscriber.stop();
        } catch (IOException e) {
            logger.fatal("Exception occurred while stopping Subscriber", e);
        }

        try {
            logger.debug("Stopping Connection");
            connection.disconnect();
        } catch (IOException e) {
            logger.fatal("Exception occurred while stopping Connection", e);
        }

        if (pool != null) {
            logger.debug("Shutting down ThreadPool");
            pool.shutdown();

            try {
                logger.debug("Waiting ThreadPool to terminate");
                boolean result = pool.awaitTermination(POOL_TIMEOUT, TimeUnit.SECONDS);

                if (!result) {
                    logger.warn("ThreadPool was not terminated before timeout elapsed");
                } else {
                    logger.debug("ThreadPool successfully terminated");
                }
            } catch (InterruptedException e) {
                logger.fatal("Exception occurred while waiting ThreadPool to terminate", e);
            }
        }

        logger.info("RemoteDoorOpenerDaemon stopped");
    }
}
