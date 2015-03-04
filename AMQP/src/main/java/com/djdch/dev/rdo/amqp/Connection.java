package com.djdch.dev.rdo.amqp;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.djdch.dev.rdo.amqp.internal.ConnectionCycle;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

public class Connection implements ConnectionCycle {
    private static final Logger logger = LogManager.getLogger();

    private final AtomicReference<State> state;

    private ConnectionFactory factory;
    private com.rabbitmq.client.Connection connection;

    private String hostname;

    public Connection() {
        state = new AtomicReference<>(State.INITIALIZED);

        factory = null;
        connection = null;

        hostname = null;
    }

    public void connect() throws IOException {
        if (state.compareAndSet(State.INITIALIZED, State.CONNECTING)) {
            if (hostname == null) {
                throw new IllegalStateException("Hostname is null.");
            }

            logger.debug("Creating connection factory");
            factory = new ConnectionFactory();
            factory.setHost(hostname);

            logger.debug("Establishing connection");
            connection = factory.newConnection();

            state.set(State.CONNECTED);
        }
    }

    public void disconnect() throws IOException {
        if (state.compareAndSet(State.CONNECTED, State.DISCONNECTING)) {
            try {
                if (connection.isOpen()) {
                    logger.debug("Closing connection");
                    connection.close();
                } else {
                    logger.warn("Connection is already closed");
                }
            } finally {
                factory = null;
                connection = null;

                state.set(State.DISCONNECTED);
            }
        }
    }

    public Channel createChannel() throws IOException {
        return connection.createChannel();
    }

    @Override
    public boolean isInitialized() {
        return state.get() == State.INITIALIZED;
    }

    @Override
    public boolean isConnected() {
        return state.get() == State.CONNECTED;
    }

    @Override
    public boolean isDisconnected() {
        return state.get() == State.DISCONNECTED;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        if (!isInitialized()) {
            throw new IllegalStateException("Cannot set hostname while connected.");
        }

        this.hostname = hostname;
    }
}
