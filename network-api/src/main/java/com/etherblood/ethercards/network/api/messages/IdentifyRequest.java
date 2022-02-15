package com.etherblood.ethercards.network.api.messages;

public class IdentifyRequest {

    public String version;
    public String jwt;

    public IdentifyRequest(String version, String jwt) {
        this.version = version;
        this.jwt = jwt;
    }

    IdentifyRequest() {
    }
}
