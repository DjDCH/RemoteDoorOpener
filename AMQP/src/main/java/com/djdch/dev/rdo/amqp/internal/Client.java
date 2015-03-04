package com.djdch.dev.rdo.amqp.internal;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.djdch.dev.rdo.amqp.Connection;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public abstract class Client implements LifeCycle {
    private static final Logger logger = LogManager.getLogger();

    protected final Connection connection;

    protected final AtomicReference<State> state;
    protected final Channel channel;

    protected String queue;
    protected String exchange;
    protected String routingKey;
    protected QueueingConsumer consumer;

    public Client(Connection connection) {
        this.connection = connection;

        channel = new Channel(connection);
        state = new AtomicReference<>(State.INITIALIZED);

        queue = null;
        exchange = null;
        routingKey = null;
        consumer = null;
    }

    protected void ensureConnectionOpened() {
        if (!connection.isConnected()) {
            throw new IllegalStateException("Client is not started.");
        }
    }

    protected void ensureChannelOpened() {
        if (!channel.isConnected()) {
            throw new IllegalStateException("Channel is not started.");
        }
    }

    protected void ensureClientStarted() {
        if (!isStarted()) {
            throw new IllegalStateException("Client is not started.");
        }
    }

    protected void ensureStarted() {
        ensureConnectionOpened();
        ensureChannelOpened();
        ensureClientStarted();
    }

    protected void ensureQueueNotNull() {
        if (queue == null) {
            throw new IllegalStateException("Queue is null.");
        }
    }

    protected void ensureExchangeNotNull() {
        if (exchange == null) {
            throw new IllegalStateException("Exchange is null.");
        }
    }

    protected void ensureRoutingKeyNotNull() {
        if (routingKey == null) {
            throw new IllegalStateException("RoutingKey is null.");
        }
    }

    @Override
    public boolean isInitialized() {
        return state.get() == State.INITIALIZED;
    }

    @Override
    public boolean isStarted() {
        return state.get() == State.STARTED;
    }

    @Override
    public boolean isStopped() {
        return state.get() == State.STOPPED;
    }

    public void throwChannelException() throws ShutdownSignalException {
        logger.warn("This method should only be used in development");

        ShutdownSignalException exception = channel.getException();

        if (exception != null) {
            throw exception;
        }
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        if (!isInitialized()) {
            throw new IllegalStateException("Cannot set queue once started.");
        }

        this.queue = queue;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        if (!isInitialized()) {
            throw new IllegalStateException("Cannot set exchange once started.");
        }

        this.exchange = exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        if (!isInitialized()) {
            throw new IllegalStateException("Cannot set routingKey once started.");
        }

        this.routingKey = routingKey;
    }
}
