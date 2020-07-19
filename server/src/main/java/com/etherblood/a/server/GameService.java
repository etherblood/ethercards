package com.etherblood.a.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.etherblood.a.ai.MoveBotGame;
import com.etherblood.a.ai.bots.Bot;
import com.etherblood.a.ai.bots.RandomMover;
import com.etherblood.a.ai.bots.SkipBot;
import com.etherblood.a.ai.bots.mcts.MctsBot;
import com.etherblood.a.ai.bots.mcts.MctsBotSettings;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.SimpleEntityData;
import com.etherblood.a.network.api.GameReplayService;
import com.etherblood.a.templates.api.setup.RawGameSetup;
import com.etherblood.a.templates.api.setup.RawPlayerSetup;
import com.etherblood.a.network.api.jwt.JwtParser;
import com.etherblood.a.network.api.matchmaking.GameRequest;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.GameDataPrinter;
import com.etherblood.a.rules.HistoryRandom;
import com.etherblood.a.rules.MoveReplay;
import com.etherblood.a.rules.MoveService;
import com.etherblood.a.game.events.api.NoopGameEventListener;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.rules.moves.Start;
import com.etherblood.a.rules.moves.Surrender;
import com.etherblood.a.rules.moves.Update;
import com.etherblood.a.server.matchmaking.BotRequest;
import com.etherblood.a.server.matchmaking.MatchmakeRequest;
import com.etherblood.a.server.matchmaking.MatchmakeResult;
import com.etherblood.a.server.matchmaking.Matchmaker;
import com.etherblood.a.templates.api.setup.RawLibraryTemplate;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameService {

    private static final Logger LOG = LoggerFactory.getLogger(GameService.class);

    private final Server server;
    private final JwtParser jwtParser;
    private final Function<String, JsonElement> assetLoader;
    private final Matchmaker matchmaker;

    private final Map<UUID, GameReplayService> games = new HashMap<>();
    private final List<GamePlayerMapping> players = new ArrayList<>();
    private final List<GameBotMapping> bots = new ArrayList<>();

    private final Map<UUID, Future<Move>> botMoves = new HashMap<>();
    private final ExecutorService executor;

    public GameService(Server server, JwtParser jwtParser, Function<String, JsonElement> assetLoader, Matchmaker matchmaker, ExecutorService executor) {
        this.server = server;
        this.jwtParser = jwtParser;
        this.assetLoader = assetLoader;
        this.matchmaker = matchmaker;
        this.executor = executor;
    }

    public synchronized void onDisconnect(Connection connection) {
        matchmaker.remove(connection.getID());
        for (GamePlayerMapping player : players) {
            if (player.connectionId == connection.getID()) {
                LOG.info("Game_{} surrendering for player {} due to disconnect.", player.gameId, player.playerId);
                GameReplayService game = games.get(player.gameId);
                makeMove(player.gameId, new Surrender(game.getPlayerEntity(player.playerIndex)));
                assert game.isGameOver();
                assert !games.containsKey(player.gameId);
                return;
            }
        }
    }

    public synchronized void onGameRequest(Connection connection, GameRequest request) {
        matchmaker.enqueue(MatchmakeRequest.of(request, connection.getID(), jwtParser));
        matchmake();
    }

    public synchronized void onMoveRequest(Connection connection, Move move) {
        if (move instanceof Update) {
            return;
        }
        GamePlayerMapping mapping = getPlayerByConnectionId(connection.getID());
        UUID gameId = mapping.gameId;
        makeMove(gameId, move);
    }

    private void makeMove(UUID gameId, Move move) {
        GameReplayService game = games.get(gameId);
        Game gameInstance = game.createInstance();
        GameDataPrinter printer = new GameDataPrinter(gameInstance);
        LOG.info("Game_{} make move '{}'.", gameId, printer.toMoveString(move));
        MoveReplay moveReplay = game.apply(move);
        for (GamePlayerMapping player : getPlayersByGameId(gameId)) {
            Connection other = getConnection(player.connectionId);
            if (other != null) {
                other.sendTCP(moveReplay);
            }
        }
        Future<Move> botMove = botMoves.remove(gameId);
        if (botMove != null) {
            botMove.cancel(true);
        }
        if (game.isGameOver()) {
            LOG.info("Game_{} ended.", gameId);
            games.remove(gameId);
            for (GamePlayerMapping player : getPlayersByGameId(gameId)) {
                int playerEntity = game.getPlayerEntity(player.playerIndex);
                LOG.info("{} {}.", game.getPlayerName(player.playerIndex), game.hasPlayerWon(playerEntity) ? "won" : (game.hasPlayerLost(playerEntity) ? "lost" : " has no result"));
                players.remove(player);
            }
            for (GameBotMapping bot : getBotsByGameId(gameId)) {
                int playerEntity = game.getPlayerEntity(bot.playerIndex);
                LOG.info("{} {}.", game.getPlayerName(bot.playerIndex), game.hasPlayerWon(playerEntity) ? "won" : (game.hasPlayerLost(playerEntity) ? "lost" : " has no result"));
                bots.remove(bot);
            }
        }
    }

    public synchronized void botMoves() throws Exception {
        Iterator<Map.Entry<UUID, Future<Move>>> iterator = botMoves.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Future<Move>> entry = iterator.next();
            if (entry.getValue().isDone()) {
                iterator.remove();
                Move move = entry.getValue().get();
                UUID gameId = entry.getKey();
                GameDataPrinter printer = new GameDataPrinter(games.get(gameId).createInstance());
                LOG.debug("Game_{} computed move '{}'.", gameId, printer.toMoveString(move));
                makeMove(gameId, move);
            }
        }
        if (bots.isEmpty()) {
            return;
        }
        LOG.trace("Processing {} bots.", bots.size());
        for (GameBotMapping bot : bots) {
            if (botMoves.containsKey(bot.gameId)) {
                LOG.debug("Game_{} Still computing move.", bot.gameId);
                continue;
            }

            GameReplayService game = games.get(bot.gameId);
            MoveBotGame botGame = bot.game;
            game.updateInstance(botGame.getGame());
            if (botGame.isGameOver()) {
                throw new AssertionError();
            }
            if (botGame.isPlayerIndexActive(bot.playerIndex)) {
                LOG.debug("Game_{} start computing move.", bot.gameId);
                botMoves.put(bot.gameId, executor.submit(() -> bot.bot.findMove(bot.playerIndex)));
            } else {
                LOG.trace("Game_{} skip, it is not the bots turn.", bot.gameId);
            }
        }
    }

    private synchronized void matchmake() {
        MatchmakeResult result;
        while ((result = matchmaker.matchmake()) != null) {
            RawGameSetup setup = result.setup;
            setup.theCoinAlias = "the_coin";
            applyPlayerEasterEggs(setup.players);
            UUID gameId = result.gameId;
            GameReplayService game = new GameReplayService(setup, assetLoader);
            MoveReplay moveReplay = game.apply(new Start());

            LOG.info("Game_{} started.", gameId);
            players.addAll(result.playerMappings);

            for (BotRequest botRequest : result.botRequests) {
                Game gameInstance = game.createInstance();
                MoveBotGame moveBotGame = new MoveBotGame(gameInstance);
                Bot botInstance;
                if (botRequest.strength < 0) {
                    botInstance = new SkipBot(moveBotGame);
                } else if (botRequest.strength == 0) {
                    botInstance = new RandomMover(moveBotGame, new Random());
                } else {
                    MctsBotSettings<Move, MoveBotGame> settings = new MctsBotSettings<>();
                    settings.maxThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
                    settings.strength = botRequest.strength;
                    botInstance = new MctsBot(moveBotGame, () -> new MoveBotGame(simulationGame(gameInstance)), settings);
                }
                bots.add(new GameBotMapping(gameId, botRequest.playerIndex, moveBotGame, botInstance));
            }

            games.put(gameId, game);
            for (GamePlayerMapping playerMapping : result.playerMappings) {
                Connection connection = getConnection(playerMapping.connectionId);
                connection.sendTCP(setup);
                connection.sendTCP(moveReplay);
            }
        }
    }

    private void applyPlayerEasterEggs(RawPlayerSetup[] players) {
        for (RawPlayerSetup player : players) {
            if (player.name.equalsIgnoreCase("yalee")) {
                replaceCards(player.library, "raigeki", "fabi_raigeki");
            }

            if (player.name.equalsIgnoreCase("destroflyer")) {
                for (RawPlayerSetup opponent : players) {
                    if (player == opponent) {
                        continue;
                    }
                    replaceCards(opponent.library, "the_coin", "the_other_coin");
                }
            }
        }
    }

    private void replaceCards(RawLibraryTemplate library, String toRemove, String replacement) {
        if (library.cards.containsKey(toRemove)) {
            int previous = library.cards.getOrDefault(replacement, 0);
            library.cards.put(replacement, previous + library.cards.remove(toRemove));
        }
    }

    private List<GamePlayerMapping> getPlayersByGameId(UUID gameId) {
        return players.stream().filter(x -> x.gameId.equals(gameId)).collect(Collectors.toList());
    }

    private List<GameBotMapping> getBotsByGameId(UUID gameId) {
        return bots.stream().filter(x -> x.gameId.equals(gameId)).collect(Collectors.toList());
    }

    private GamePlayerMapping getPlayerByConnectionId(int connectionId) {
        return players.stream().filter(x -> x.connectionId == connectionId).findAny().get();
    }

    private Connection getConnection(int connectionId) {
        return Arrays.stream(server.getConnections()).filter(x -> x.getID() == connectionId).findAny().orElse(null);
    }

    private Game simulationGame(Game game) {
        EntityData data = new SimpleEntityData(game.getSettings().components);
        MoveService moves = new MoveService(game.getSettings(), data, HistoryRandom.producer(), null, false, false, new NoopGameEventListener());
        return new Game(game.getSettings(), data, moves);
    }
}
