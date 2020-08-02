package com.etherblood.a.network.api.messages;

public class IdentifyRequest {

    public String jwt;

    public IdentifyRequest(String jwt) {
        this.jwt = jwt;
    }

    IdentifyRequest() {
    }
}
