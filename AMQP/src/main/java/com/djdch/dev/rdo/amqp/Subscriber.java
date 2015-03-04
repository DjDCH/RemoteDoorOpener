package com.djdch.dev.rdo.amqp;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rabbitmq.client.QueueingConsumer;

public class Subscriber extends Consumer {
    private static final Logger logger = LogManager.getLogger();

    public Subscriber(Connection connection) {
        super(connection);
    }

    @Override
    public void start() throws IOException {
        ensureConnectionOpened();
        ensureExchangeNotNull();
        ensureRoutingKeyNotNull();

        if (state.compareAndSet(State.INITIALIZED, State.STARTING)) {
            channel.connect();

            logger.debug("Declaring exchange");
            channel.exchangeDeclare(exchange);

            logger.debug("Declaring exclusive server-named queue");
            queue = channel.queueDeclareServerNamedExclusive();

            logger.debug("Binding server-named queue to exchange");
            channel.queueBind(queue, exchange, routingKey);

            logger.trace("Server-named queue `{}` was bound to exchange `{}`", queue, exchange);

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

    @Override
    public QueueingConsumer.Delivery getDelivery() throws InterruptedException {
        ensureStarted();

        QueueingConsumer.Delivery delivery = consumer.nextDelivery();

        if (delivery != null) {
            logger.debug("Received message from `{}/{}` with body `{}`", exchange, routingKey, new String(delivery.getBody()));
        } else {
            logger.error("Delivery is null");
        }

        return delivery;
    }
}
