package com.etherblood.a.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.etherblood.a.network.api.NetworkUtil;
import com.etherblood.a.network.api.jwt.JwtParser;
import com.etherblood.a.network.api.messages.IdentifyRequest;
import com.etherblood.a.network.api.messages.match.MatchRequest;
import com.etherblood.a.network.api.messages.match.MoveRequest;
import com.etherblood.a.server.matchmaking.Matchmaker;
import com.etherblood.a.templates.api.setup.RawLibraryTemplate;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameServer {

    static final String BOT_NAME = "Bot";
    private static final Logger LOG = LoggerFactory.getLogger(GameServer.class);

    private final Server server;

    public GameServer(JwtParser jwtParser, Function<String, JsonElement> assetLoader, RawLibraryTemplate botLibrary, String version) {
        server = new Server(1024 * 1024, 1024 * 1024);
        NetworkUtil.init(server.getKryo());
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        Matchmaker matchmaker = new Matchmaker(-1, BOT_NAME, botLibrary);
        AuthenticationService authenticationService = new AuthenticationService(jwtParser, version);
        GameService gameService = new GameService(server, authenticationService, assetLoader, matchmaker, scheduledThreadPoolExecutor);
        server.addListener(new Listener() {

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof IdentifyRequest) {
                    authenticationService.onIdentify(connection, (IdentifyRequest) object);
                }
                try (AutoCloseable authentication = authenticationService.setContext(connection)) {
                    if (object instanceof MoveRequest) {
                        gameService.onMoveRequest(connection, (MoveRequest) object);
                    } else if (object instanceof MatchRequest) {
                        gameService.onGameRequest(connection, (MatchRequest) object);
                    } else if (object instanceof IdentifyRequest) {
                        gameService.onIdentify(connection, (IdentifyRequest) object);
                    }
                } catch (Throwable t) {
                    LOG.error("Error when handling message {} for connection {}.", object, connection.getID(), t);
                }
            }

            @Override
            public void disconnected(Connection connection) {
                try {
                    gameService.onDisconnect(connection);
                    authenticationService.onDisconnect(connection);
                } catch (Throwable t) {
                    LOG.error("Error when handling disconnect for connection {}.", connection.getID(), t);
                }
            }

            @Override
            public void connected(Connection connection) {
            }

        });
        scheduledThreadPoolExecutor.scheduleAtFixedRate(() -> {
            try {
                gameService.botMoves();
            } catch (Exception ex) {
                LOG.error("Error when handling bot moves.", ex);
            }
        }, 10_000, 100, TimeUnit.MILLISECONDS);
    }

    public void start() throws IOException {
        server.start();
        server.bind(NetworkUtil.TCP_PORT);
    }

}
