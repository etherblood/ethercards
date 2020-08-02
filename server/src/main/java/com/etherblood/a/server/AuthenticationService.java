package com.etherblood.a.server;

import com.esotericsoftware.kryonet.Connection;
import com.etherblood.a.network.api.jwt.JwtAuthentication;
import com.etherblood.a.network.api.jwt.JwtParser;
import com.etherblood.a.network.api.messages.IdentifyRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

    private final JwtParser jwtParser;
    private final Map<Integer, JwtAuthentication> connectionIdToAuthentication = new ConcurrentHashMap<>();
    private final ThreadLocal<JwtAuthentication> activeAuthentication = new ThreadLocal<>();

    public AuthenticationService(JwtParser jwtParser) {
        this.jwtParser = jwtParser;
    }

    public void onIdentify(Connection connection, IdentifyRequest identify) {
        JwtAuthentication authentication = jwtParser.verify(identify.jwt);
        LOG.info("Connection {} identified as {} (id={})", connection.getID(), authentication.user.login, authentication.user.id);
        connectionIdToAuthentication.put(connection.getID(), authentication);
    }

    public AutoCloseable setContext(Connection connection) {
        JwtAuthentication authentication = connectionIdToAuthentication.get(connection.getID());
        activeAuthentication.set(authentication);
        return activeAuthentication::remove;
    }

    public JwtAuthentication getAuthentication() {
        return activeAuthentication.get();
    }

    public void onDisconnect(Connection connection) {
        connectionIdToAuthentication.remove(connection.getID());
    }
}
