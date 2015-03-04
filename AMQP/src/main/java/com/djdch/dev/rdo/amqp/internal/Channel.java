package com.djdch.dev.rdo.amqp.internal;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.djdch.dev.rdo.amqp.Connection;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ReturnListener;
import com.rabbitmq.client.ShutdownSignalException;

public class Channel implements ConnectionCycle {
    private static final Logger logger = LogManager.getLogger();

    private static final String DEFAULT_EXCHANGE = "";
    private static final String EXCHANGE_TYPE = "direct";
    private static final boolean MANDATORY = true;
    private static final boolean DURABLE = false;
    private static final boolean EXCLUSIVE = false;
    private static final boolean AUTO_ACK = true;
    private static final boolean AUTO_DELETE = true;

    private final AtomicReference<State> state;

    private Connection connection;
    private com.rabbitmq.client.Channel channel;

    public Channel(Connection connection) {
        this.connection = connection;

        state = new AtomicReference<>(ConnectionCycle.State.INITIALIZED);
    }

    public void connect() throws IOException {
        if (state.compareAndSet(State.INITIALIZED, State.CONNECTING)) {
            logger.debug("Creating channel");
            channel = connection.createChannel();

            logger.debug("Binding return listener");
            setupReturnListener();

            state.set(State.CONNECTED);
        }
    }

    public void disconnect() throws IOException {
        if (state.compareAndSet(State.CONNECTED, State.DISCONNECTING)) {
            try {
                if (channel.isOpen()) {
                    logger.debug("Closing channel");
                    channel.close();
                } else {
                    logger.warn("Channel is already closed");
                }
            } finally {
                channel = null;

                state.set(State.DISCONNECTED);
            }
        }
    }

    private void setupReturnListener() {
        // FIXME
        channel.addReturnListener(new ReturnListener() {
            @Override
            public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {
                System.out.println("FUCKFUCKFUCKFUCKFUCKFUCKFUCKFUCKFUCKFUCK");
                logger.error("Last publish was unsuccessful");
                logger.trace("basic.return: {} `{}` to `{}/{}`", replyCode, replyText, exchange, routingKey);
            }
        });
    }

    public void addReturnListener(ReturnListener returnListener) {
        channel.addReturnListener(returnListener);
    }

    public void basicConsume(String queue, Consumer consumer) throws IOException {
        channel.basicConsume(queue, AUTO_ACK, consumer);
    }

    public void basicPublishExchange(String exchange, String routingKey, String body) throws IOException {
        channel.basicPublish(exchange, routingKey, MANDATORY, null, body.getBytes());
    }

    public void basicPublishQueue(String queue, String body) throws IOException {
        channel.basicPublish(DEFAULT_EXCHANGE, queue, MANDATORY, null, body.getBytes());
    }

    public QueueingConsumer createConsumer() {
        return new QueueingConsumer(channel);
    }

    public void exchangeDeclare(String exchange) throws IOException {
        channel.exchangeDeclare(exchange, EXCHANGE_TYPE, DURABLE, AUTO_DELETE, null);
    }

    public void exchangeDeclarePassive(String exchange) throws IOException {
        channel.exchangeDeclarePassive(exchange);
    }

    public void queueBind(String queue, String exchange, String routingKey) throws IOException {
        channel.queueBind(queue, exchange, routingKey);
    }

    public String queueDeclareServerNamedExclusive() throws IOException {
        return channel.queueDeclare().getQueue();
    }

    public String queueDeclareServerNamed() throws IOException {
        return channel.queueDeclare("", DURABLE, EXCLUSIVE, AUTO_DELETE, null).getQueue();
    }

    public void queueDeclarePassive(String queue) throws IOException {
        channel.queueDeclarePassive(queue);
    }

    public ShutdownSignalException getException() {
        return channel.getCloseReason();
    }

    @Override
    public boolean isInitialized() {
        return state.get() == State.INITIALIZED;
    }

    @Override
    public boolean isConnected() {
        return state.get() == State.CONNECTED;
    }

    @Override
    public boolean isDisconnected() {
        return state.get() == State.DISCONNECTED;
    }
}
