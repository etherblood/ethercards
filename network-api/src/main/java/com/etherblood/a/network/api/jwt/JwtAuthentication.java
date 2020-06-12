package com.etherblood.a.network.api.jwt;

import java.time.Instant;

public class JwtAuthentication {

    public String rawJwt;
    public Instant iat;
    public JwtAuthenticationUser user;
}
