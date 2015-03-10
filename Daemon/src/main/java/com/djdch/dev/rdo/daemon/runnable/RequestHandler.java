package com.djdch.dev.rdo.daemon.runnable;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.djdch.dev.rdo.amqp.Connection;
import com.djdch.dev.rdo.amqp.Producer;
import com.djdch.dev.rdo.amqp.exception.PassiveDeclareException;
import com.djdch.dev.rdo.daemon.exception.InvalidRequestException;
import com.djdch.dev.rdo.data.Packet;
import com.djdch.dev.rdo.data.packet.metadata.Client;
import com.djdch.dev.rdo.data.packet.payload.Request;
import com.djdch.dev.rdo.data.packet.payload.Response;
import com.djdch.dev.rdo.data.packet.payload.response.Reply;
import com.google.gson.JsonSyntaxException;

public class RequestHandler implements Runnable {
    private static final Logger logger = LogManager.getLogger();

    private final String message;
    private final Connection connection;
    private final Client broker;

    public RequestHandler(String message, Connection connection, Client broker) {
        this.message = message;
        this.connection = connection;
        this.broker = broker;
    }

    @Override
    public void run() {
        logger.debug("RequestHandler begin");

        String queue = null;

        Producer producer = new Producer(connection);

        try {
            // Step 1: Parse message
            logger.debug("Parsing request");
            Packet requestPacket = Packet.decode(message);

            Client client = requestPacket.metadata.client;
            Request request = requestPacket.payload.request;
            queue = request.returnQueue;

            // Queue cannot be empty
            if (queue.isEmpty()) {
                throw new InvalidRequestException("Return queue is empty.");
            }

            if (!request.target.equals(broker.fndq)) {
                throw new InvalidRequestException(String.format("Target `%s` does not match current broker `%s`.", request.target, broker.name));
            }

            // Step 2: Create and start Producer (passive declare client queue)
            logger.debug("Starting producer");
            producer.setQueue(queue);
            producer.start();

            // Step 3: Process request
            logger.debug("Processing request");
            Response response = new Response();
            response.target = client.fndq;

            switch (request.query) {
                case IS_CONNECTED:
                    response.reply = Reply.OK;
                    break;
                case DO_OPEN:
                    response.reply = Reply.ERROR;
                    break;
                default:
                    response.reply = Reply.UNKNOWN;
            }

            response.status = response.reply.getCode();

            logger.debug("Sending response");
            Packet responsePacket = Packet.createResponsePacket(broker, request, response);

            producer.publish(responsePacket.encode());

            try {
                Thread.sleep(1000); // Wait for basic.return
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            producer.throwChannelException(); // FIXME: For debugging only
        } catch (IOException e) {
            logger.error("IOException occurred while processing request, ignoring request", e);
        } catch (JsonSyntaxException e) {
            logger.warn("Message contain invalid json, ignoring request", e);
        } catch (InvalidRequestException | NullPointerException e) {
            logger.warn("Error occurred while processing request, ignoring request", e);
        } catch (PassiveDeclareException e) {
            logger.warn("Return queue `{}` does not exists, ignoring request", queue);
        } catch (Exception e) {
            logger.fatal("Unexpected exception occurred while processing request", e);
            throw e;
        } finally {
            try {
                producer.stop();
            } catch (IOException e) {
                logger.error("IOException occurred while stopping producer", e);
            }
        }

        logger.debug("RequestHandler ended");
    }
}
