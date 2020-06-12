package com.etherblood.a.client;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.etherblood.a.network.api.GameReplayService;
import com.etherblood.a.network.api.NetworkUtil;
import com.etherblood.a.network.api.game.GameSetup;
import com.etherblood.a.network.api.matchmaking.GameRequest;
import com.etherblood.a.network.api.matchmaking.OpponentType;
import com.etherblood.a.rules.MoveReplay;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.templates.RawLibraryTemplate;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class GameClient {

    private final Client client;
    private final AtomicReference<CompletableFuture<GameReplayService>> gameRequest = new AtomicReference<>(null);

    public GameClient(Function<String, JsonElement> assetLoader) {
        client = new Client();
        NetworkUtil.init(client.getKryo());
        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                CompletableFuture<GameReplayService> future = gameRequest.get();
                if (object instanceof GameSetup) {
                    future.complete(new GameReplayService((GameSetup) object, assetLoader));
                } else if (object instanceof MoveReplay) {
                    try {
                        future.get().apply((MoveReplay) object);
                    } catch (InterruptedException | ExecutionException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }

        });
    }

    public void reset() {
        gameRequest.set(null);
    }

    public Future<GameReplayService> requestGame(String jwt, RawLibraryTemplate library) {
        CompletableFuture<GameReplayService> future = new CompletableFuture<>();
        if (gameRequest.compareAndSet(null, future)) {
            client.sendTCP(new GameRequest(jwt, library, OpponentType.HUMAN, 0));
            return future;
        }
        throw new IllegalStateException();
    }

    public Future<GameReplayService> requestBotgame(String jwt, RawLibraryTemplate library, int strength) {
        CompletableFuture<GameReplayService> future = new CompletableFuture<>();
        if (gameRequest.compareAndSet(null, future)) {
            client.sendTCP(new GameRequest(jwt, library, OpponentType.BOT, strength));
            return future;
        }
        throw new IllegalStateException();
    }

    public void requestMove(Move move) {
        client.sendTCP(move);
    }

    public void start(String address) throws IOException {
        client.start();
        client.connect(10_000, address, NetworkUtil.TCP_PORT);
    }
}
