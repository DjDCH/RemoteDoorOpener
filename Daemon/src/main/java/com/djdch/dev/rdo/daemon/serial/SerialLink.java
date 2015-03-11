package com.djdch.dev.rdo.daemon.serial;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jssc.SerialPort;
import jssc.SerialPortException;

public class SerialLink implements SerialCycle {
    private static final Logger logger = LogManager.getLogger();

    private final AtomicReference<State> state;

    private String portName;
    private SerialPort serialPort;
    private ByteArrayOutputStream buffer;

    public SerialLink() {
        state = new AtomicReference<>(State.INITIALIZED);

        portName = null;
        serialPort = null;

        buffer = new ByteArrayOutputStream();
    }

    @Override
    public void connect() throws SerialPortException {
        if (state.compareAndSet(State.INITIALIZED, State.CONNECTING)) {
            if (portName == null) {
                throw new IllegalArgumentException("PortName is null.");
            }

            logger.debug("Creating SerialPort");
            serialPort = new SerialPort(portName);

            logger.debug("Opening SerialPort");
            serialPort.openPort();

            logger.debug("Configuring SerialPort");
            serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            state.set(State.CONNECTED);
        }
    }

    @Override
    public void disconnect() throws SerialPortException {
        if (state.compareAndSet(State.CONNECTED, State.DISCONNECTING)) {
            try {
                if (serialPort.isOpened()) {
                    logger.debug("Closing SerialPort");
                    serialPort.closePort();
                } else {
                    logger.warn("SerialPort is already closed");
                }
            } finally {
                serialPort = null;

                state.set(State.DISCONNECTED);
            }
        }
    }

    public void write(byte val) {
        buffer.write(val);
    }

    public void flush() throws SerialPortException {
        serialPort.writeBytes(buffer.toByteArray());
        reset();
    }

    public void reset() {
        buffer.reset();
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

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        if (!isInitialized()) {
            throw new IllegalStateException("Cannot set portName while connected.");
        }

        this.portName = portName;
    }
}
