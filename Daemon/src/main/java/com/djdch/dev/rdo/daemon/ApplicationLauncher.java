package com.djdch.dev.rdo.daemon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.djdch.dev.rdo.daemon.controller.ApplicationController;
import com.djdch.dev.rdo.daemon.serial.SerialUtil;

public class ApplicationLauncher {
    private static final Logger logger = LogManager.getLogger();

    private static final String HOSTNAME = "localhost";
    private static final String EXCHANGE_NAME = "world";
    private static final String ROUTING_KEY = "country.state.city.address";

    private final ApplicationController controller;

    private String hostname;
    private String exchange;
    private String routingKey;
    private String serial;

    public ApplicationLauncher() {
        this.controller = new ApplicationController(this);
    }

    public void launch(String args[]) {
        hostname = HOSTNAME;
        exchange = EXCHANGE_NAME;
        routingKey = ROUTING_KEY;

        if (SerialUtil.getPorts().length > 0) {
            serial = SerialUtil.getPorts()[0];
        } else {
            logger.warn("No serial device were found");
        }

        controller.start();
    }

    public String getHostname() {
        return hostname;
    }

    public String getExchange() {
        return exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getSerial() {
        return serial;
    }
}
