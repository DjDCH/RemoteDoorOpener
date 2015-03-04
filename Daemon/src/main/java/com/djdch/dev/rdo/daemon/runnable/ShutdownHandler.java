package com.djdch.dev.rdo.daemon.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.djdch.dev.rdo.daemon.controller.ApplicationController;
import com.djdch.dev.rdo.daemon.log4j.StaticShutdownCallbackRegistry;

public class ShutdownHandler implements Runnable {
    private static final Logger logger = LogManager.getLogger();

    private final ApplicationController controller;

    public ShutdownHandler(ApplicationController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        try {
            logger.debug("ShutdownHandler invoked");
            controller.stop();
        } finally {
            logger.debug("Shutting down Log4j manually");
            StaticShutdownCallbackRegistry.invoke();
        }
    }
}
