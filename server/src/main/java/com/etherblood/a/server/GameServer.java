package com.etherblood.a.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.etherblood.a.network.api.NetworkUtil;
import com.etherblood.a.network.api.matchmaking.GameRequest;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.templates.RawLibraryTemplate;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameServer {

    private static final Logger LOG = LoggerFactory.getLogger(GameServer.class);

    private final Server server;

    public GameServer(Function<String, JsonObject> assetLoader) {
        RawLibraryTemplate botLibrary = new Gson().fromJson(assetLoader.apply("libraries/default.json"), RawLibraryTemplate.class);

        server = new Server();
        NetworkUtil.init(server.getKryo());
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        GameService gameService = new GameService(server, assetLoader, -1, botLibrary, scheduledThreadPoolExecutor);
        server.addListener(new Listener() {

            @Override
            public void received(Connection connection, Object object) {
                try {
                    if (object instanceof Move) {
                        gameService.onMoveRequest(connection, (Move) object);
                    } else if (object instanceof GameRequest) {
                        gameService.onGameRequest(connection, (GameRequest) object);
                    }
                } catch (Throwable t) {
                    LOG.error("Error when handling message {} for connection {}.", object, connection.getID(), t);
                }
            }

            @Override
            public void disconnected(Connection connection) {
                LOG.info("Connection {} disconnected.", connection.getID());
                gameService.onDisconnect(connection);
            }

            @Override
            public void connected(Connection connection) {
                LOG.info("Connection {} connected.", connection.getID());
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
