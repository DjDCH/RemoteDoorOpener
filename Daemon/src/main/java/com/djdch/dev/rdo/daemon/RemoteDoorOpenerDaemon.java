package com.djdch.dev.rdo.daemon;

public class RemoteDoorOpenerDaemon {

    // XXX: Set custom ShutdownRegistrationStrategy
    static {
        System.setProperty("log4j.shutdownCallbackRegistry", "com.djdch.dev.rdo.daemon.log4j.StaticShutdownCallbackRegistry");
    }

    public static void main(String[] args) {
        ApplicationLauncher launcher = new ApplicationLauncher();
        launcher.launch(args);
    }
}
