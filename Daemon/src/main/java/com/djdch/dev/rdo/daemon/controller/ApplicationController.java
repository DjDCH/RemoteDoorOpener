package com.djdch.dev.rdo.daemon.controller;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.djdch.dev.rdo.amqp.Subscriber;
import com.djdch.dev.rdo.daemon.ApplicationLauncher;
import com.djdch.dev.rdo.daemon.runnable.ShutdownHandler;
import com.rabbitmq.client.ShutdownSignalException;

public class ApplicationController {
    private static final Logger logger = LogManager.getLogger();

    private final ApplicationLauncher launcher;
    private final Subscriber subscriber;

    private boolean running;

    public ApplicationController(ApplicationLauncher launcher) {
        this.launcher = launcher;

        this.subscriber = new Subscriber();
    }

    public void start() {
        logger.debug("Starting RemoteDoorOpenerDaemon");
        running = true;

        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler(this)));

        subscriber.setHostname(launcher.getHostname());
        subscriber.setExchange(launcher.getExchange());

        try {
            logger.debug("Connecting subscriber");
            subscriber.connect();
        } catch (IOException e) {
            logger.fatal("Exception occurred while connecting subscriber", e);
            stop();
        }

        try {
            logger.debug("Subscribing consumer");
            subscriber.subscribe();
        } catch (IOException e) {
            logger.fatal("Exception occurred while subscribing consumer", e);
            stop();
        }

        logger.info("RemoteDoorOpenerDaemon started");

        while (running) {
            String message = null;
            try {
                logger.debug("Waiting for delivery");
                message = subscriber.getDeliveryBody();
            } catch (InterruptedException e) {
                logger.fatal("Exception occurred while waiting for deliveries", e);
                stop();
            } catch (ShutdownSignalException e) {
                if (running && subscriber.isConnected()) {
                    logger.fatal("Shutdown signal received while waiting for deliveries", e);
                    stop();
                }
            }

            if (!running) {
                return; // Already stopped, nothing to do.
            }

            logger.info(String.format("Received `%s`", message));
        }
    }

    public void stop() {
        if (!running) {
            return; // Already stopped, nothing to do.
        }

        logger.debug("Stopping RemoteDoorOpenerDaemon");
        running = false;

        logger.debug("Disconnecting subscriber");
        try {
            subscriber.disconnect();
        } catch (IOException e) {
            logger.fatal("Exception occurred while disconnecting subscriber", e);
        }

        logger.info("RemoteDoorOpenerDaemon stopped");
    }
}
