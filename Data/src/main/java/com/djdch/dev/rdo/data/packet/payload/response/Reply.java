package com.djdch.dev.rdo.data.packet.payload.response;

public enum Reply {
    OK(200),
    UNKNOWN(400),
    ERROR(500);

    private final int code;

    Reply(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
