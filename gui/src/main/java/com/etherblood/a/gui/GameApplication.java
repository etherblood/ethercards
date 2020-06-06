package com.etherblood.a.gui;

import com.destrostudios.cardgui.Board;
import com.destrostudios.cardgui.BoardAppState;
import com.destrostudios.cardgui.BoardObject;
import com.destrostudios.cardgui.BoardSettings;
import com.destrostudios.cardgui.Card;
import com.destrostudios.cardgui.CardZone;
import com.destrostudios.cardgui.Interactivity;
import com.destrostudios.cardgui.TargetSnapMode;
import com.destrostudios.cardgui.boardobjects.TargetArrow;
import com.destrostudios.cardgui.events.MoveCardEvent;
import com.destrostudios.cardgui.interactivities.AimToTargetInteractivity;
import com.destrostudios.cardgui.interactivities.DragToPlayInteractivity;
import com.destrostudios.cardgui.samples.animations.CameraShakeAnimation;
import com.destrostudios.cardgui.samples.animations.SnowAnimation;
import com.destrostudios.cardgui.samples.boardobjects.connectionmarker.ConnectionMarker;
import com.destrostudios.cardgui.samples.boardobjects.connectionmarker.ConnectionMarkerVisualizer;
import com.destrostudios.cardgui.samples.boardobjects.targetarrow.SimpleTargetArrowSettings;
import com.destrostudios.cardgui.samples.boardobjects.targetarrow.SimpleTargetArrowVisualizer;
import com.destrostudios.cardgui.samples.visualization.DebugZoneVisualizer;
import com.destrostudios.cardgui.transformations.SimpleTargetRotationTransformation;
import com.destrostudios.cardgui.zones.CenteredIntervalZone;
import com.destrostudios.cardgui.zones.SimpleIntervalZone;
import com.etherblood.a.ai.MoveBotGame;
import com.etherblood.a.ai.bots.evaluation.RolloutToEvaluation;
import com.etherblood.a.ai.bots.evaluation.SimpleEvaluation;
import com.etherblood.a.ai.bots.mcts.MctsBot;
import com.etherblood.a.ai.bots.mcts.MctsBotSettings;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.gui.soprettyboard.CameraAppState;
import com.etherblood.a.gui.soprettyboard.ForestBoardAppstate;
import com.etherblood.a.gui.soprettyboard.PostFilterAppstate;
import com.etherblood.a.entities.SimpleEntityData;
import com.etherblood.a.gui.prettycards.CardImages;
import com.etherblood.a.gui.prettycards.CardModel;
import com.etherblood.a.gui.prettycards.CardPainterAWT;
import com.etherblood.a.gui.prettycards.CardPainterJME;
import com.etherblood.a.gui.prettycards.CardVisualizer_Card;
import com.etherblood.a.gui.prettycards.CardVisualizer_Minion;
import com.etherblood.a.gui.prettycards.MinionModel;
import com.etherblood.a.network.api.GameReplayService;
import com.etherblood.a.network.api.game.GameSetup;
import com.etherblood.a.network.api.game.PlayerSetup;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.HistoryRandom;
import com.etherblood.a.rules.MoveService;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.moves.Block;
import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.DeclareMulligan;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.rules.moves.EndMulliganPhase;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.rules.moves.Start;
import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.templates.DisplayCardTemplate;
import com.etherblood.a.templates.DisplayMinionTemplate;
import com.etherblood.a.templates.RawLibraryTemplate;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.IntPredicate;

public class GameApplication extends SimpleApplication implements ActionListener {

    private static final float ZONE_HEIGHT = 1.3f;
    private GameReplayService gameReplayService;
    private Game game;
    private Board board;
    private final Map<Integer, PlayerZones> playerZones = new HashMap<>();
    private final Map<Integer, Card<CardModel>> visualCards = new HashMap<>();
    private final Map<Integer, Card<MinionModel>> visualMinions = new HashMap<>();
    private final Map<BoardObject<?>, Integer> objectEntities = new HashMap<>();
    private final Map<Integer, ConnectionMarker> attacks = new HashMap<>();
    private BitmapText hudText;
    private final AtomicBoolean botIsComputing = new AtomicBoolean(false);

    @Override
    public void simpleInitApp() {
        assetManager.registerLocator("../assets/", FileLocator.class);
        assetManager.registerLoader(GsonLoader.class, "json");

        stateManager.attach(new ForestBoardAppstate(0));
        stateManager.attach(new PostFilterAppstate());

        hudText = new BitmapText(guiFont, false);
        hudText.setSize(guiFont.getCharSet().getRenderedSize());
        hudText.setColor(ColorRGBA.White);
        hudText.setLocalTranslation(0, getCamera().getHeight(), 0);
        guiNode.attachChild(hudText);

        initGame();
        initListeners();
        initBoardGui();
        initCamera();
    }

    @Override
    public void simpleUpdate(float tpf) {
        gameReplayService.updateInstance(game);
        applyAI();
        updateBoard();
        updateCamera();
    }

    private void initCamera() {
        stateManager.attach(new CameraAppState());
    }

    private void updateCamera() {
        if (game.isGameOver()) {
            return;
        }

        CameraAppState cameraAppState = stateManager.getState(CameraAppState.class);
        Vector3f position = new Vector3f();
        Quaternion rotation = new Quaternion();
        boolean isPlayer1 = game.isPlayerActive(game.findPlayerByIndex(1));
        position.set(0, 3.8661501f, 6.470482f);
        if (isPlayer1) {
            position.addLocal(0, 0, -10.339f);
        }
        rotation.lookAt(new Vector3f(0, -0.7237764f, -0.6900346f), Vector3f.UNIT_Y);
        if (isPlayer1) {
            rotation = new Quaternion().fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y).multLocal(rotation);
        }
        cameraAppState.moveTo(position, rotation, 0.3f);
    }

    private void initListeners() {
        inputManager.addMapping("space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("1", new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping("2", new KeyTrigger(KeyInput.KEY_2));
        inputManager.addMapping("3", new KeyTrigger(KeyInput.KEY_3));
        inputManager.addListener(this, "space", "1", "2", "3");
    }

    private void initGame() {
        Function<String, JsonObject> assetLoader = x -> assetManager.loadAsset(new AssetKey<>("templates/" + x));
        GameSetup setup = new GameSetup();
        PlayerSetup player0 = new PlayerSetup();
        player0.id = 0;
        player0.library = new Gson().fromJson(assetLoader.apply("libraries/default.json"), RawLibraryTemplate.class);
        PlayerSetup player1 = new PlayerSetup();
        player1.id = 1;
        player1.library = new Gson().fromJson(assetLoader.apply("libraries/default.json"), RawLibraryTemplate.class);
        setup.players = new PlayerSetup[]{player0, player1};
        gameReplayService = new GameReplayService(setup, assetLoader);

        game = gameReplayService.createInstance();
        gameReplayService.apply(new Start());
        gameReplayService.updateInstance(game);
    }

    private void initBoardGui() {
        CoreComponents core = game.getData().getComponents().getModule(CoreComponents.class);
        board = new Board();
        board.registerVisualizer_Class(CardZone.class, new DebugZoneVisualizer() {

            @Override
            protected Geometry createVisualizationObject(AssetManager assetManager) {
                Geometry geometry = super.createVisualizationObject(assetManager);
                geometry.setCullHint(Spatial.CullHint.Always);
                return geometry;
            }

            @Override
            protected Vector2f getSize(CardZone zone) {
                for (PlayerZones playerZones : playerZones.values()) {
                    if (zone == playerZones.getDeckZone()) {
                        return new Vector2f(1, ZONE_HEIGHT);
                    } else if (zone == playerZones.getHandZone()) {
                        return new Vector2f(5, ZONE_HEIGHT - 0.1f);
                    } else if (zone == playerZones.getBoardZone()) {
                        return new Vector2f(5, ZONE_HEIGHT);
                    }
                }
                return super.getSize(zone);
            }
        });
        CardPainterJME cardPainterJME = new CardPainterJME(new CardPainterAWT(new CardImages(assetManager)));
        board.registerVisualizer(card -> card.getModel() instanceof CardModel, new CardVisualizer_Card(cardPainterJME));
        board.registerVisualizer(card -> card.getModel() instanceof MinionModel, new CardVisualizer_Minion(cardPainterJME));
        board.registerVisualizer_Class(TargetArrow.class, new SimpleTargetArrowVisualizer(SimpleTargetArrowSettings.builder()
                .color(ColorRGBA.White)
                .width(0.5f)
                .build()));
        board.registerVisualizer_Class(ConnectionMarker.class, new ConnectionMarkerVisualizer(SimpleTargetArrowSettings.builder()
                .arcHeight(0.1f)
                .width(0.25f)
                .build()));
        IntList players = game.getData().list(core.PLAYER_INDEX);

        Vector3f offset = new Vector3f(0, 0, ZONE_HEIGHT);
        float directionX = 1;
        float directionZ = 1;
        Quaternion zoneRotation = Quaternion.IDENTITY;

        for (int player : players) {
            if (game.getData().hasValue(player, core.PLAYER_INDEX, 1)) {
                directionX *= -1;
                directionZ *= -1;
                zoneRotation = new Quaternion().fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y);
            }

            float x = -1.25f;
            float z = 2 * (ZONE_HEIGHT / 2);
            x += 3.25f;
            SimpleIntervalZone boardZone = new SimpleIntervalZone(offset.add(directionX * x, 0, directionZ * z), zoneRotation, new Vector3f(-directionX, 1, 1));
            x += 1.25f;

            x = -0.5f;
            x += 3.75f;
            SimpleIntervalZone deckZone = new SimpleIntervalZone(offset.add(directionX * x, 0, directionZ * z), zoneRotation, new Vector3f(0, 0.02f, 0));
            z += ZONE_HEIGHT / 2;

            x = 0;
            z += (ZONE_HEIGHT - 0.25f);
            Quaternion handRotation = zoneRotation.mult(new Quaternion().fromAngleAxis(FastMath.QUARTER_PI, Vector3f.UNIT_X));
            CenteredIntervalZone handZone = new CenteredIntervalZone(offset.add(directionX * x, 0, directionZ * z), handRotation, new Vector3f(0.85f, 1, 1));

            board.addZone(deckZone);
            board.addZone(handZone);
            board.addZone(boardZone);
            playerZones.put(player, new PlayerZones(deckZone, handZone, boardZone));
        }

        stateManager.attach(new BoardAppState(board, rootNode, BoardSettings.builder()
                .draggedCardProjectionZ(0.9975f)
                .build()));
    }

    private void updateBoard() {
        EntityData data = game.getData();
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        StringBuilder builder = new StringBuilder();
        IntList players = data.list(core.PLAYER_INDEX);
        for (int player : players) {
            int playerIndex = data.get(player, core.PLAYER_INDEX);
            builder.append("Player ");
            builder.append(playerIndex + 1);
            builder.append(" mana: ");
            builder.append(data.getOptional(player, core.MANA).orElse(0));
            if (game.hasPlayerLost(player)) {
                builder.append(" - LOST");
            } else if (game.hasPlayerWon(player)) {
                builder.append(" - WON");
            } else if (data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK_PHASE)) {
                builder.append(" - ATTACK_PHASE");
            } else if (data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK_PHASE)) {
                builder.append(" - BLOCK_PHASE");
            }
            builder.append(System.lineSeparator());
        }
        hudText.setText(builder.toString());

        IntList handCards = data.list(core.IN_HAND_ZONE);
        IntList battleCards = data.list(core.IN_BATTLE_ZONE);
        IntList libraryCards = data.list(core.IN_LIBRARY_ZONE);
        for (int player : players) {
            PlayerZones zones = playerZones.get(player);
            IntPredicate playerFilter = x -> data.hasValue(x, core.OWNED_BY, player);
            updateZone(libraryCards.stream().filter(playerFilter).toArray(), zones.getDeckZone(), Vector3f.UNIT_Y);
            updateZone(handCards.stream().filter(playerFilter).toArray(), zones.getHandZone(), Vector3f.UNIT_X);
            updateZone(battleCards.stream().filter(playerFilter).toArray(), zones.getBoardZone(), Vector3f.UNIT_X);
        }

        for (int attacker : data.list(core.ATTACKS_TARGET)) {
            if (!attacks.containsKey(attacker)) {
                int target = data.get(attacker, core.ATTACKS_TARGET);
                if (visualMinions.containsKey(target)) {
                    ConnectionMarker arrow = new ConnectionMarker() {
                        @Override
                        public void update(float lastTimePerFrame) {
                            getModel().updateIfNotEquals(true, false, () -> {
                            });
                        }

                    };
                    arrow.getModel().setSourceBoardObject(visualMinions.get(attacker));
                    attacks.put(attacker, arrow);
                    board.register(arrow);
                }
            }
        }

        for (Map.Entry<Integer, ConnectionMarker> entry : new ArrayList<>(attacks.entrySet())) {
            ConnectionMarker arrow = entry.getValue();
            int attacker = entry.getKey();
            OptionalInt target = data.getOptional(attacker, core.ATTACKS_TARGET);
            if (target.isPresent()) {
                Card<MinionModel> targetObject = visualMinions.get(target.getAsInt());
                if (targetObject != null) {
                    arrow.getModel().setTargetBoardObject(targetObject);
                    continue;
                }
            }
            attacks.remove(attacker);
            board.unregister(arrow);
        }
    }

    private void updateZone(int[] cards, CardZone cardZone, Vector3f interval) {
        EntityData data = game.getData();
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        for (Card card : new ArrayList<>(cardZone.getCards())) {
            int entity = objectEntities.get(card);
            if (!data.has(entity, core.IN_LIBRARY_ZONE) && !data.has(entity, core.IN_HAND_ZONE) && !data.has(entity, core.IN_BATTLE_ZONE)) {
                cardZone.removeCard(card);
                board.unregister(card);
                objectEntities.remove(card);
                visualCards.remove(entity);
                visualMinions.remove(entity);
            }
        }
        if (game.isGameOver()) {
            return;
        }
        int player = game.findPlayerByIndex(0);
        int index = 0;
        for (int cardEntity : cards) {
            if (data.has(cardEntity, core.CARD_TEMPLATE)) {
                Card<CardModel> card = getOrCreateCard(cardEntity);
                CardModel cardModel = card.getModel();
                cardModel.setEntityId(cardEntity);
                cardModel.setFaceUp(!data.has(cardEntity, core.IN_LIBRARY_ZONE));
                cardModel.setTemplate((DisplayCardTemplate) game.getTemplates().getCard(data.get(cardEntity, core.CARD_TEMPLATE)));

                if (game.getMoves().canCast(player, cardEntity)) {
                    card.setInteractivity(castInteractivity(player, cardEntity));
                    cardModel.setGlow(ColorRGBA.Yellow);
                } else if (game.getMoves().canDeclareMulligan(player, cardEntity)) {
                    card.setInteractivity(mulliganInteractivity(player, cardEntity));
                    cardModel.setGlow(ColorRGBA.Red);
                } else {
                    card.clearInteractivity();
                    cardModel.setGlow(null);
                }
                board.triggerEvent(new MoveCardEvent(card, cardZone, interval.mult(index)));
            } else if (data.has(cardEntity, core.MINION_TEMPLATE)) {
                Card<MinionModel> card = getOrCreateMinion(cardEntity);
                MinionModel minionModel = card.getModel();
                minionModel.setEntityId(cardEntity);
                minionModel.setFaceUp(true);
                minionModel.setAttack(data.getOptional(cardEntity, core.ATTACK).orElse(0));
                minionModel.setHealth(data.getOptional(cardEntity, core.HEALTH).orElse(0));
                DisplayMinionTemplate template = (DisplayMinionTemplate) game.getTemplates().getMinion(data.get(cardEntity, core.MINION_TEMPLATE));
                minionModel.setTemplate(template);
                minionModel.setDamaged(minionModel.getHealth() < template.get(core.HEALTH));

                if (game.getMoves().canDeclareAttack(player, cardEntity)) {
                    card.setInteractivity(attackInteractivity(player, cardEntity));
                    minionModel.setGlow(ColorRGBA.Red);
                } else if (game.getMoves().canBlock(player, cardEntity)) {
                    card.setInteractivity(blockInteractivity(player, cardEntity));
                    minionModel.setGlow(ColorRGBA.Blue);
                } else {
                    card.clearInteractivity();
                    minionModel.setGlow(null);
                }
                board.triggerEvent(new MoveCardEvent(card, cardZone, interval.mult(index)));
            }
            index++;
        }
    }

    private Interactivity attackInteractivity(int player, int attacker) {
        return new AimToTargetInteractivity(TargetSnapMode.VALID) {
            @Override
            public boolean isValid(BoardObject target) {
                if (target instanceof Card) {
                    int targetId = objectEntities.get(target);
                    return game.getMoves().canDeclareAttack(player, attacker, targetId);
                }
                return false;
            }

            @Override
            public void trigger(BoardObject source, BoardObject target) {
                int actor = objectEntities.get(source);
                int dest = objectEntities.get(target);
                applyMove(new DeclareAttack(player, actor, dest));
            }
        };
    }

    private Interactivity blockInteractivity(int player, int blocker) {
        return new AimToTargetInteractivity(TargetSnapMode.VALID) {
            @Override
            public boolean isValid(BoardObject target) {
                if (target instanceof Card) {
                    int targetId = objectEntities.get(target);
                    return game.getMoves().canBlock(player, blocker, targetId);
                }
                return false;
            }

            @Override
            public void trigger(BoardObject source, BoardObject target) {
                int actor = objectEntities.get(source);
                int dest = objectEntities.get(target);
                applyMove(new Block(player, actor, dest));
            }
        };
    }

    private Interactivity castInteractivity(int player, int castable) {
        CoreComponents core = game.getData().getComponents().getModule(CoreComponents.class);
        int cardTemplate = game.getData().get(castable, core.CARD_TEMPLATE);
        CardTemplate template = game.getTemplates().getCard(cardTemplate);
        CardCast cast = template.getAttackPhaseCast() != null ? template.getAttackPhaseCast() : template.getBlockPhaseCast();
        if (cast.isTargeted()) {
            return new AimToTargetInteractivity(TargetSnapMode.VALID) {
                @Override
                public boolean isValid(BoardObject target) {
                    if (target instanceof Card) {
                        int targetId = objectEntities.get(target);
                        return game.getMoves().canCast(player, castable, targetId);
                    }
                    return false;
                }

                @Override
                public void trigger(BoardObject source, BoardObject target) {
                    int targetId = objectEntities.get(target);
                    applyMove(new Cast(player, castable, targetId));
                }
            };
        }
        return new DragToPlayInteractivity() {

            @Override
            public void trigger(BoardObject boardObject, BoardObject target) {
                applyMove(new Cast(player, castable, ~0));
            }
        };
    }

    private Interactivity mulliganInteractivity(int player, int card) {
        return new DragToPlayInteractivity() {

            @Override
            public void trigger(BoardObject boardObject, BoardObject target) {
                applyMove(new DeclareMulligan(player, card));
            }
        };
    }

    private Card<CardModel> getOrCreateCard(int myCard) {
        Card<CardModel> card = visualCards.get(myCard);
        if (card == null) {
            Card<CardModel> inner = new Card<>(new CardModel());
            card = inner;
            visualCards.put(myCard, card);
            objectEntities.put(card, myCard);

            card.rotation().addRelativeTransformation(new SimpleTargetRotationTransformation(new Quaternion().fromAngles(0, 0, -FastMath.PI)), () -> !inner.getModel().isFaceUp());
        }
        return card;
    }

    private Card<MinionModel> getOrCreateMinion(int myCard) {
        Card<MinionModel> card = visualMinions.get(myCard);
        if (card == null) {
            Card<MinionModel> inner = new Card<>(new MinionModel());
            card = inner;
            visualMinions.put(myCard, card);
            objectEntities.put(card, myCard);

            EntityData data = game.getData();
            CoreComponents core = data.getComponents().getModule(CoreComponents.class);
            card.rotation().addRelativeTransformation(new SimpleTargetRotationTransformation(new Quaternion().fromAngles(0, -FastMath.PI / 6, 0)), () -> data.has(myCard, core.TIRED));
            card.rotation().addRelativeTransformation(new SimpleTargetRotationTransformation(new Quaternion().fromAngles(0, 0, -FastMath.PI)), () -> !inner.getModel().isFaceUp());
        }
        return card;
    }

    @Override
    public void onAction(String name, boolean isPressed, float lastTimePerFrame) {
        if ("space".equals(name) && isPressed) {
            EntityData data = game.getData();
            CoreComponents core = data.getComponents().getModule(CoreComponents.class);
            IntList list = data.list(core.ACTIVE_PLAYER_PHASE);
            if (!list.isEmpty()) {
                int player = list.get(0);
                int phase = data.get(player, core.ACTIVE_PLAYER_PHASE);
                switch (phase) {
                    case PlayerPhase.BLOCK_PHASE:
                        applyMove(new EndBlockPhase(player));
                        break;
                    case PlayerPhase.ATTACK_PHASE:
                        applyMove(new EndAttackPhase(player));
                        break;
                    case PlayerPhase.MULLIGAN_PHASE:
                        applyMove(new EndMulliganPhase(player));
                        break;
                }
            }
//        } else if ("1".equals(name) && isPressed) {
//            List<Card> cards = playerZones[0].getDeckZone().getCards();
//            board.playAnimation(new ShuffleAnimation(cards, this));
        } else if ("2".equals(name) && isPressed) {
            board.playAnimation(new CameraShakeAnimation(cam, 1, 0.01f));
        } else if ("3".equals(name) && isPressed) {
            board.playAnimation(new SnowAnimation(assetManager, cam, rootNode));
        }
    }

    private void applyMove(Move move) {
        if (botIsComputing.get()) {
            System.out.println("User action discarded, bot is still working: " + move);
            return;
        }
        gameReplayService.apply(move);
    }

    private void applyAI() {
        if (!botIsComputing.compareAndSet(false, true)) {
            return;
        }
        Game game = gameReplayService.createInstance();
        int botPlayerIndex = 1;
        int botPlayer = game.findPlayerByIndex(botPlayerIndex);
        if (game.isPlayerActive(botPlayer)) {
            Thread botThread = new Thread(() -> {
                System.out.println("computing...");
                Function<MoveBotGame, float[]> evaluation = new SimpleEvaluation<Move, MoveBotGame>()::evaluate;
                Function<MoveBotGame, float[]> rolloutEvaluation = new RolloutToEvaluation<>(new Random(), 10, evaluation)::evaluate;
                MctsBotSettings<Move, MoveBotGame> botSettings = new MctsBotSettings<>();
                botSettings.verbose = true;
                botSettings.evaluation = rolloutEvaluation;
                botSettings.strength = 10_000;
                MctsBot<Move, MoveBotGame> bot = new MctsBot<>(new MoveBotGame(game), new MoveBotGame(simulationGame(game)), botSettings);
                Move move = bot.findBestMove(botPlayerIndex);
                game.getMoves().move(move);
                System.out.println("Eval: " + Arrays.toString(evaluation.apply(new MoveBotGame(game))));
                botIsComputing.set(false);
                applyMove(move);
                System.out.println("Bot is done.");
            });
            botThread.start();
        } else {
            botIsComputing.set(false);
        }
    }

    private Game simulationGame(Game game) {
        EntityData data = new SimpleEntityData(game.getSettings().components);
        MoveService moves = new MoveService(game.getSettings(), data, HistoryRandom.producer(), null, false, false);
        return new Game(game.getSettings(), data, moves);
    }
}
