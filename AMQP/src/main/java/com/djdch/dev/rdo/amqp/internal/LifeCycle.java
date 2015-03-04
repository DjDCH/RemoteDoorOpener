package com.djdch.dev.rdo.amqp.internal;

import java.io.IOException;

/**
 * @See org.apache.logging.log4j.core.LifeCycle
 */
public interface LifeCycle {
    public void start() throws IOException;

    public void stop() throws IOException;

    public boolean isInitialized();

    public boolean isStarted();

    public boolean isStopped();

    public static enum State {
        INITIALIZED,
        STARTING,
        STARTED,
        STOPPING,
        STOPPED;

        private State() {
        }
    }
}
