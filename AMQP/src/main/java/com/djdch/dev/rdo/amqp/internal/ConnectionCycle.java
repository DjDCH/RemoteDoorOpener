package com.djdch.dev.rdo.amqp.internal;

import java.io.IOException;

/**
 * @See org.apache.logging.log4j.core.LifeCycle
 */
public interface ConnectionCycle {
    public void connect() throws IOException;

    public void disconnect() throws IOException;

    public boolean isInitialized();

    public boolean isConnected();

    public boolean isDisconnected();

    public static enum State {
        INITIALIZED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED;

        private State() {
        }
    }
}
