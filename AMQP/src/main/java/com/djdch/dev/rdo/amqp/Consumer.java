package com.djdch.dev.rdo.amqp;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.djdch.dev.rdo.amqp.internal.Client;
import com.rabbitmq.client.QueueingConsumer;

public class Consumer extends Client {
    private static final Logger logger = LogManager.getLogger();

    public Consumer(Connection connection) {
        super(connection);
    }

    @Override
    public void start() throws IOException {
        ensureConnectionOpened();

        if (state.compareAndSet(State.INITIALIZED, State.STARTING)) {
            channel.connect();

            logger.debug("Declaring server-named queue");
            queue = channel.queueDeclareServerNamed();

            logger.debug("Creating queueing consumer");
            consumer = channel.createConsumer();

            logger.debug("Binding queueing consumer to queue");
            channel.basicConsume(queue, consumer);

            logger.debug("Ready for deliveries");

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

    public QueueingConsumer.Delivery getDelivery() throws InterruptedException {
        ensureStarted();

        QueueingConsumer.Delivery delivery = consumer.nextDelivery();

        if (delivery != null) {
            logger.debug("Received message from `{}` with body `{}`", queue, new String(delivery.getBody()));
        } else {
            logger.error("Delivery is null");
        }

        return delivery;
    }

    public String getDeliveryBody() throws InterruptedException {
        QueueingConsumer.Delivery delivery = getDelivery();
        String message = null;

        if (delivery != null) {
            message = new String(delivery.getBody());
        }

        return message;
    }
}
