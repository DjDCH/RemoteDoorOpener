package com.djdch.dev.rdo.amqp;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.djdch.dev.rdo.amqp.exception.PassiveDeclareException;
import com.djdch.dev.rdo.amqp.internal.Client;

public class Producer extends Client {
    private static final Logger logger = LogManager.getLogger();

    public Producer(Connection connection) {
        super(connection);
    }

    @Override
    public void start() throws IOException {
        ensureConnectionOpened();
        ensureQueueNotNull();

        if (state.compareAndSet(State.INITIALIZED, State.STARTING)) {
            channel.connect();

            try {
                logger.debug("Declaring queue passively");
                channel.queueDeclarePassive(queue);
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

    public void publish(String message) throws IOException {
        ensureStarted();
        ensureQueueNotNull();

        logger.debug("Publishing message");
        channel.basicPublishQueue(queue, message);

        logger.debug("Sent message to `{}` with body `{}`", queue, message);
    }
}
