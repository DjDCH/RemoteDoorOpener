package com.djdch.dev.rdo.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.djdch.dev.rdo.amqp.Connection;
import com.djdch.dev.rdo.amqp.Consumer;
import com.djdch.dev.rdo.amqp.Publisher;
import com.djdch.dev.rdo.amqp.exception.PassiveDeclareException;
import com.djdch.dev.rdo.data.Packet;
import com.djdch.dev.rdo.data.packet.metadata.Client;
import com.djdch.dev.rdo.data.packet.payload.Request;
import com.djdch.dev.rdo.data.packet.payload.Response;
import com.djdch.dev.rdo.data.packet.payload.request.Query;
import com.djdch.dev.rdo.data.packet.payload.response.Reply;
import com.google.gson.JsonSyntaxException;
import com.rabbitmq.client.ShutdownSignalException;

public class RemoteDoorOpenerClient {
    private static final Logger logger = LogManager.getLogger();

    private static final String HOSTNAME = "localhost";
    private static final String EXCHANGE_NAME = "world";
    private static final String ROUTING_KEY = "country.state.city.address";

    public static void main(String[] args) {
        try {
            Client client = Client.createRandomClient();

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

                        Packet requestPacket = Packet.decode(message);

                        Request request = requestPacket.payload.request;
                        Response response = requestPacket.payload.response;

                        // TODO: Ensure that reply match sent request (add request parameter to runnable)

                        switch (request.query) {
                            case IS_CONNECTED:
                                if (response.reply == Reply.OK) {
                                    logger.info("Server is connected");
                                }
                                break;
                            case DO_OPEN:
                                if (response.reply == Reply.OK) {
                                    logger.info("Door was opened");
                                } else {
                                    logger.info("Could not open the door");
                                }
                                break;
                            default:
                                logger.warn("Invalid request");
                        }

                    } catch (JsonSyntaxException e) {
                        logger.error("Message contain invalid json, cannot parse reply", e);
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

            Request request = new Request();
            request.target = ROUTING_KEY;
            request.query = Query.IS_CONNECTED;
            request.returnQueue = consumer.getQueue();

            Packet requestPacket = Packet.createRequestPacket(client, request);

            publisher.publish(requestPacket.encode());

            Thread.sleep(1000);

            publisher.throwChannelException(); // FIXME: For debugging only

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
