package com.etherblood.a.client;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.etherblood.a.network.api.GameReplayService;
import com.etherblood.a.network.api.NetworkUtil;
import com.etherblood.a.network.api.messages.IdentifyRequest;
import com.etherblood.a.network.api.messages.matchmaking.GameRequest;
import com.etherblood.a.network.api.messages.matchmaking.GameStarted;
import com.etherblood.a.rules.MoveReplay;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.templates.api.setup.RawLibraryTemplate;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class GameClient {

    private final Client client;
    private final AtomicReference<CompletableFuture<GameReplayView>> gameRequest = new AtomicReference<>(null);

    public GameClient(Function<String, JsonElement> assetLoader) {
        client = new Client(1024 * 1024, 1024 * 1024);
        NetworkUtil.init(client.getKryo());
        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                CompletableFuture<GameReplayView> future = gameRequest.get();
                if (object instanceof GameStarted) {
                    GameStarted gameStarted = (GameStarted) object;
                    future.complete(new GameReplayView(new GameReplayService(gameStarted.setup, assetLoader), gameStarted.playerIndex));
                } else if (object instanceof MoveReplay) {
                    try {
                        future.get().gameReplay.apply((MoveReplay) object);
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

    public Future<GameReplayView> requestGame(RawLibraryTemplate library, int strength, int[] teamHumanCounts, int teamSize) {
        CompletableFuture<GameReplayView> future = new CompletableFuture<>();
        if (gameRequest.compareAndSet(null, future)) {
            client.sendTCP(new GameRequest(library, strength, teamHumanCounts, teamSize));
            return future;
        }
        throw new IllegalStateException();
    }
    
    public void identify(String jwt) {
        client.sendTCP(new IdentifyRequest(jwt));
    }

    public void requestMove(Move move) {
        client.sendTCP(move);
    }

    public void start(String address) throws IOException {
        client.start();
        client.connect(10_000, address, NetworkUtil.TCP_PORT);
    }
    
    public void stop() {
        client.stop();
    }
}
