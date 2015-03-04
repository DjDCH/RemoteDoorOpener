package com.djdch.dev.rdo.amqp;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rabbitmq.client.QueueingConsumer;

public class Subscriber extends Client {
    private static final Logger logger = LogManager.getLogger();

    private String queue;
    private QueueingConsumer consumer;

    public Subscriber() {
        super();

        queue = null;
        consumer = null;
    }

    public void connect() throws IOException {
        clientConnect();

        logger.debug("Declaring server-named queue");
        queue = channel.queueDeclare().getQueue();

        logger.debug("Binding server-named queue to exchange");
        channel.queueBind(queue, exchange, "");

        logger.trace(String.format("Server-named queue `%s` was bound to exchange `%s`", queue, exchange));

        logger.debug("Ready for subscriber");
    }

    public void disconnect() throws IOException {
        queue = null;
        consumer = null;

        clientDisconnect();
    }

    public void subscribe() throws IOException {
        logger.debug("Creating queueing consumer");
        consumer = new QueueingConsumer(channel);

        logger.debug("Binding queueing consumer to queue");
        channel.basicConsume(queue, true, consumer);

        logger.debug("Ready for deliveries");
    }

    public QueueingConsumer.Delivery getDelivery() throws InterruptedException {
        QueueingConsumer.Delivery delivery = consumer.nextDelivery();

        if (delivery != null) {
            logger.debug(String.format("Received message with body `%s`", new String(delivery.getBody())));
        } else {
            logger.error("Delivery was null");
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
