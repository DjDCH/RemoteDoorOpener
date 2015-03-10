package com.djdch.dev.rdo.data.packet.metadata;

import java.util.Random;
import java.util.UUID;

import com.djdch.dev.rdo.data.Packet;

public class Client {

    public final String protocolVersion;

    public UUID uuid;
    public String fndq;
    public String name;

    public Client() {
        protocolVersion = Packet.PROTOCOL_VERSION;
    }

    public static Client createRandomClient() {
        int value = (new Random()).nextInt(Integer.MAX_VALUE) + 1;

        Client client = new Client();
        client.uuid = UUID.randomUUID();
        client.fndq = "world.c" + value;
        client.name = "client-" + value;

        return client;
    }

    public static Client createNamedClient(String fndq) {
        int index = fndq.lastIndexOf('.') + 1;

        Client client = new Client();
        client.uuid = UUID.nameUUIDFromBytes(fndq.getBytes());
        client.fndq = fndq;
        client.name = "door-" + fndq.substring(index);

        return client;
    }
}
