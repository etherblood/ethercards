package com.etherblood.a.client;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.etherblood.a.network.api.GameReplayService;
import com.etherblood.a.network.api.NetworkUtil;
import com.etherblood.a.network.api.messages.IdentifyRequest;
import com.etherblood.a.network.api.messages.match.MatchRequest;
import com.etherblood.a.network.api.messages.match.ConnectedToMatchUpdate;
import com.etherblood.a.network.api.messages.match.MoveRequest;
import com.etherblood.a.network.api.messages.match.MoveUpdate;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.templates.api.setup.RawLibraryTemplate;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class GameClient {

    private final Client client;
    private final String version;
    private final AtomicReference<GameReplayView> game = new AtomicReference<>(null);
    private boolean gameRequested = false;

    public GameClient(Function<String, JsonElement> assetLoader, String version) {
        this.version = version;
        client = new Client(1024 * 1024, 1024 * 1024);
        NetworkUtil.init(client.getKryo());
        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof ConnectedToMatchUpdate) {
                    ConnectedToMatchUpdate connectedToMatch = (ConnectedToMatchUpdate) object;
                    boolean success = game.compareAndSet(null, new GameReplayView(new GameReplayService(connectedToMatch.replay, assetLoader), connectedToMatch.playerIndex));
                    if (!success) {
                        throw new IllegalStateException();
                    }
                    gameRequested = false;
                } else if (object instanceof MoveUpdate) {
                    game.get().gameReplay.apply(((MoveUpdate) object).move);
                }
            }

        });
    }

    public boolean isGameRequested() {
        return gameRequested;
    }

    public GameReplayView getGame() {
        return game.get();
    }

    public void resetGame() {
        game.set(null);
        gameRequested = false;
    }

    public void requestGame(RawLibraryTemplate library, int strength, int[] teamHumanCounts, int teamSize) {
        if (gameRequested) {
            throw new IllegalStateException();
        }
        if (game.get() != null) {
            throw new IllegalStateException();
        }
        client.sendTCP(new MatchRequest(library, strength, teamHumanCounts, teamSize));
        gameRequested = true;
    }

    public void identify(String jwt) {
        client.sendTCP(new IdentifyRequest(version, jwt));
    }

    public void requestMove(Move move) {
        client.sendTCP(new MoveRequest(move));
    }

    public void start(String address) throws IOException {
        client.start();
        client.connect(10_000, address, NetworkUtil.TCP_PORT);
    }

    public void stop() {
        client.stop();
    }
}
