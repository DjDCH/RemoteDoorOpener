package com.djdch.dev.rdo.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.djdch.dev.rdo.amqp.Publisher;

public class RemoteDoorOpenerClient {
    private static final Logger logger = LogManager.getLogger();

    private static final String HOSTNAME = "localhost";
    private static final String EXCHANGE_NAME = "world";

    public static void main(String[] args) {
        try {
            Publisher publisher = new Publisher();

            publisher.setHostname(HOSTNAME);
            publisher.setExchange(EXCHANGE_NAME);

            publisher.connect();

            publisher.publish("Hello World!");

            Thread.sleep(2000); // Wait for basic.return

            publisher.disconnect();
        } catch (Exception e) {
            logger.fatal("Unexpected exception occurred", e);
        }
    }
}
