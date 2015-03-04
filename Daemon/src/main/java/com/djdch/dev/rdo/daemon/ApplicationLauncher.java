package com.djdch.dev.rdo.daemon;

import com.djdch.dev.rdo.daemon.controller.ApplicationController;

public class ApplicationLauncher {

    private static final String HOSTNAME = "localhost";
    private static final String EXCHANGE_NAME = "world";
    private static final String ROUTING_KEY = "country.state.city.address";

    private final ApplicationController controller;

    private String hostname;
    private String exchange;
    private String routingKey;

    public ApplicationLauncher() {
        this.controller = new ApplicationController(this);
    }

    public void launch(String args[]) {
        hostname = HOSTNAME;
        exchange = EXCHANGE_NAME;
        routingKey = ROUTING_KEY;

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
}
