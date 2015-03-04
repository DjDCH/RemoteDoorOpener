package com.djdch.dev.rdo.daemon.runnable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.djdch.dev.rdo.daemon.controller.ApplicationController;

public class ShutdownHandler implements Runnable {
    private static final Logger logger = LogManager.getLogger();

    private final ApplicationController controller;

    public ShutdownHandler(ApplicationController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        logger.debug("ShutdownHandler invoked");
        controller.stop();
    }
}
