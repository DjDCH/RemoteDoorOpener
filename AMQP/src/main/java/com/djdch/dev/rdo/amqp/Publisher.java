package com.djdch.dev.rdo.amqp;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.djdch.dev.rdo.amqp.exception.PassiveDeclareException;

public class Publisher extends Producer {
    private static final Logger logger = LogManager.getLogger();

    public Publisher(Connection connection) {
        super(connection);
    }

    @Override
    public void start() throws IOException {
        ensureConnectionOpened();
        ensureExchangeNotNull();

        if (state.compareAndSet(State.INITIALIZED, State.STARTING)) {
            channel.connect();

            try {
                logger.debug("Declaring exchange passively");
                channel.exchangeDeclarePassive(exchange);
            } catch (IOException e) {
                throw new PassiveDeclareException(e);
            }

            state.set(State.STARTED);
        }
    }

    @Override
    public void stop() throws IOException {
        if (state.compareAndSet(State.STARTED, State.STOPPING)) {
            channel.disconnect();

            state.set(State.STOPPED);
        }
    }

    @Override
    public void publish(String message) throws IOException {
        ensureStarted();
        ensureExchangeNotNull();
        ensureRoutingKeyNotNull();

        logger.debug("Publishing message");
        channel.basicPublishExchange(exchange, routingKey, message);

        logger.debug("Sent message to `{}/{}` with body `{}`", exchange, routingKey,  message);
    }
}
