package com.djdch.dev.rdo.amqp;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ReturnListener;

public class Publisher extends Client {
    private static final Logger logger = LogManager.getLogger();

    public Publisher() {
        super();
    }

    public void connect() throws IOException {
        clientConnect();

        // FIXME
        logger.debug("Binding return listener");
        channel.addReturnListener(new ReturnListener() {
            @Override
            public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {
                logger.error("Last publish was unsuccessful");
                logger.trace(String.format("basic.return: %s `%s` %s %s", replyCode, replyText, exchange, routingKey));
            }
        });
    }

    public void disconnect() throws IOException {
        clientDisconnect();
    }

    public void publish(String message) throws IOException {
        logger.debug("Publishing message");
        channel.basicPublish(exchange, "", true, null, message.getBytes());

        logger.debug(String.format("Sent message with body `%s`", message));
    }
}
