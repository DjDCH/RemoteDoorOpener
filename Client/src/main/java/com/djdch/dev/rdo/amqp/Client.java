package com.djdch.dev.rdo.amqp;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public abstract class Client {
    private static final Logger logger = LogManager.getLogger();

    protected ConnectionFactory factory;
    protected Connection connection;
    protected Channel channel;

    protected String hostname;
    protected String exchange;

    protected boolean connected;

    public Client() {
        factory = null;
        connection = null;
        channel = null;

        hostname = null;
        exchange = null;

        connected = false;
    }

    public abstract void connect() throws IOException;

    public abstract void disconnect() throws IOException;

    protected void clientConnect() throws IOException {
        if (hostname == null) {
            throw new IllegalStateException("Hostname is null.");
        }

        if (exchange == null) {
            throw new IllegalStateException("Exchange is null.");
        }

        logger.debug("Creating connection factory");
        factory = new ConnectionFactory();
        factory.setHost(hostname);

        logger.debug("Establishing connection");
        connection = factory.newConnection();

        logger.debug("Creating channel");
        channel = connection.createChannel();

        logger.debug("Declaring exchange");
        channel.exchangeDeclare(exchange, "fanout", false, true, null);

        connected = true;
    }

    public void clientDisconnect() throws IOException {
        if (connected) {
            try {
                logger.debug("Closing channel");
                channel.close();

                logger.debug("Closing connection");
                connection.close();
            } finally {
                factory = null;
                connection = null;
                channel = null;

                connected = false;
            }
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        if (connected) {
            throw new IllegalStateException("Cannot set Hostname while connected.");
        }

        this.hostname = hostname;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        if (connected) {
            throw new IllegalStateException("Cannot set Exchange while connected.");
        }

        this.exchange = exchange;
    }
}
