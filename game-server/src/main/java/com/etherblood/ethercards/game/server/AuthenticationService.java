package com.etherblood.ethercards.game.server;

import com.destrostudios.authtoken.JwtAuthentication;
import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.destrostudios.authtoken.JwtService;
import com.esotericsoftware.kryonet.Connection;
import com.etherblood.ethercards.network.api.messages.IdentifyRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

    private final JwtService jwtService;
    private final String version;
    private final Map<Integer, JwtAuthentication> connectionIdToAuthentication = new ConcurrentHashMap<>();
    private final ThreadLocal<JwtAuthentication> activeAuthentication = new ThreadLocal<>();

    public AuthenticationService(JwtService jwtService, String version) {
        this.jwtService = jwtService;
        this.version = version;
    }

    public void onIdentify(Connection connection, IdentifyRequest identify) {
        if (!version.equals(identify.version)) {
            throw new IllegalArgumentException("Tried to identify with version " + identify.version + " when expected version is " + version + ".");
        }
        JwtAuthentication authentication = jwtService.decode(identify.jwt);
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

    public JwtAuthenticationUser getUser() {
        return getAuthentication().user;
    }

    public void onDisconnect(Connection connection) {
        connectionIdToAuthentication.remove(connection.getID());
    }
}
