package com.djdch.dev.rdo.data.packet.payload;

import com.djdch.dev.rdo.data.packet.payload.request.Query;

public class Request {

    public String target;
    public Query query;
    public String returnQueue;

    public Request() {
    }
}
