package com.djdch.dev.rdo.data.packet;

import com.djdch.dev.rdo.data.packet.payload.Request;
import com.djdch.dev.rdo.data.packet.payload.Response;

public class Payload {

    public Request request;
    public Response response;

    public Payload() {
        request = new Request();
        response = new Response();
    }
}
