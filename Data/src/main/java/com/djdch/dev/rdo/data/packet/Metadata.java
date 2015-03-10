package com.djdch.dev.rdo.data.packet;

import java.util.Date;

import com.djdch.dev.rdo.data.packet.metadata.Auth;
import com.djdch.dev.rdo.data.packet.metadata.Client;

public class Metadata {

    public Date date;

    public Client client;
    public Auth auth;

    public Metadata() {
        date = new Date();
        client = new Client();
        auth = new Auth();
    }
}
