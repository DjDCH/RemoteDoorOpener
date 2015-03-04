package com.djdch.dev.rdo.daemon;

import com.djdch.dev.rdo.daemon.controller.ApplicationController;

public class ApplicationLauncher {

    private static final String HOSTNAME = "localhost";
    private static final String EXCHANGE_NAME = "world";

    private final ApplicationController controller;

    private String hostname;
    private String exchange;

    public ApplicationLauncher() {
        this.controller = new ApplicationController(this);
    }

    public void launch(String args[]) {
        hostname = HOSTNAME;
        exchange = EXCHANGE_NAME;

        controller.start();
    }

    public String getHostname() {
        return hostname;
    }

    public String getExchange() {
        return exchange;
    }
}
