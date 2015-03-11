package com.djdch.dev.rdo.daemon.serial;

import jssc.SerialPortException;

/**
 * @See org.apache.logging.log4j.core.LifeCycle
 */
public interface SerialCycle {
    public void connect() throws SerialPortException;

    public void disconnect() throws SerialPortException;

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
