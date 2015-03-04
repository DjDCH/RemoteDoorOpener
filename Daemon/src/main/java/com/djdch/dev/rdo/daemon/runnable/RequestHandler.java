package com.djdch.dev.rdo.daemon.runnable;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.djdch.dev.rdo.amqp.Connection;
import com.djdch.dev.rdo.amqp.Producer;
import com.djdch.dev.rdo.amqp.exception.PassiveDeclareException;

public class RequestHandler implements Runnable {
    private static final Logger logger = LogManager.getLogger();

    private final String message;
    private final Connection connection;

    public RequestHandler(String message, Connection connection) {
        this.message = message;
        this.connection = connection;
    }

    @Override
    public void run() {
        logger.debug("RequestHandler begin");

        String queue = null;

        Producer producer = new Producer(connection);

        try {
            // Step 1: Parse message
            logger.info("Received `{}`", message);
            queue = message;

            // Step 2: Create and start Producer (passive declare client queue)
            producer.setQueue(queue);
            producer.start();

            // Step 3: Process request
            producer.publish("Hello back!");

            try {
                Thread.sleep(1000); // Wait for basic.return
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            producer.throwChannelException();
        } catch (IOException e) {
            logger.error("IOException occurred while processing request, ignoring request", e);
        } catch (PassiveDeclareException e) {
            logger.warn("Client queue `{}` does not exists, ignoring request", queue);
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
