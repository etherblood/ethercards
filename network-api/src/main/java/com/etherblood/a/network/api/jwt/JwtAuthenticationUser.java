package com.etherblood.a.network.api.jwt;

public class JwtAuthenticationUser {

    public long id;
    public String login;

    @Override
    public int hashCode() {
        return (int) id;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JwtAuthenticationUser)) {
            return false;
        }
        JwtAuthenticationUser other = (JwtAuthenticationUser) obj;
        return id == other.id;
    }
}
