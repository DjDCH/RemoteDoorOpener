package com.djdch.dev.rdo.data;

import com.djdch.dev.rdo.data.packet.Metadata;
import com.djdch.dev.rdo.data.packet.Payload;
import com.djdch.dev.rdo.data.packet.metadata.Client;
import com.djdch.dev.rdo.data.packet.payload.Request;
import com.djdch.dev.rdo.data.packet.payload.Response;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class Packet {

    public static final String PROTOCOL_VERSION = "1.0";

    public final String protocolVersion;

    public final Metadata metadata;
    public final Payload payload;

    public Packet() {
        protocolVersion = PROTOCOL_VERSION;

        metadata = new Metadata();
        payload = new Payload();
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(this);
    }

    public String encode() {
        return Packet.encode(this);
    }

    public static String encode(Packet packet) {
        Gson gson = new Gson();

        return gson.toJson(packet);
    }

    public static Packet decode(String json) throws JsonSyntaxException {
        Gson gson = new Gson();

        return gson.fromJson(json, Packet.class);
    }

    public static Packet createRequestPacket(Client client, Request request) {
        Packet packet = new Packet();

        packet.metadata.client = client;
        packet.payload.request = request;

        return packet;
    }

    public static Packet createResponsePacket(Client client, Request request, Response response) {
        Packet packet = new Packet();

        packet.metadata.client = client;
        packet.payload.request = request;
        packet.payload.response = response;

        return packet;
    }
}
