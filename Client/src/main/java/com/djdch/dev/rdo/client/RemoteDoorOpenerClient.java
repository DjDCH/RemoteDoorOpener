package com.djdch.dev.rdo.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.djdch.dev.rdo.amqp.Connection;
import com.djdch.dev.rdo.amqp.Consumer;
import com.djdch.dev.rdo.amqp.Publisher;
import com.djdch.dev.rdo.amqp.exception.PassiveDeclareException;
import com.rabbitmq.client.ShutdownSignalException;

public class RemoteDoorOpenerClient {
    private static final Logger logger = LogManager.getLogger();

    private static final String HOSTNAME = "localhost";
    private static final String EXCHANGE_NAME = "world";
    private static final String ROUTING_KEY = "country.state.city.address";

    public static void main(String[] args) {
        try {
            final Connection connection = new Connection();
            final Publisher publisher = new Publisher(connection);
            final Consumer consumer = new Consumer(connection);

            connection.setHostname(HOSTNAME);
            connection.connect();

            consumer.start();

            Thread iWaitHere = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        logger.debug("Waiting for reply");
                        String message = consumer.getDeliveryBody();

                        logger.info(String.format("Received `%s`", message));
                    } catch (InterruptedException e) {
                        logger.fatal("Exception occurred while waiting for delivery", e);
                    } catch (ShutdownSignalException e) {
                        if (connection.isConnected()) {
                            logger.fatal("Shutdown signal received while waiting for delivery", e);
                        }
                    } catch (Exception e) {
                        logger.fatal("Unexpected exception occurred while waiting for delivery", e);
                        throw e;
                    }
                }
            });
            iWaitHere.start();

            publisher.setExchange(EXCHANGE_NAME);
            publisher.setRoutingKey(ROUTING_KEY);

            try {
                publisher.start();
            } catch (PassiveDeclareException e) {
                logger.error("Exchange is not declared");
                consumer.stop();
                publisher.stop();
                connection.disconnect();

                return;
            }

            publisher.publish(consumer.getQueue());

            Thread.sleep(1000);

            publisher.throwChannelException();

            Thread.sleep(1000);

            iWaitHere.join(); // Wait till reply

            consumer.stop();
            publisher.stop();
            connection.disconnect();
        } catch (Exception e) {
            logger.fatal("Unexpected exception occurred", e);
        }
    }
}
