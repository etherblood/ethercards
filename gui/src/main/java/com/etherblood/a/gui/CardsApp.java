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
import com.destrostudios.cardgui.samples.visualisation.DebugZoneVisualizer;
import com.destrostudios.cardgui.zones.CenteredIntervalZone;
import com.destrostudios.cardgui.zones.SimpleIntervalZone;
import com.etherblood.a.gui.prettycards.*;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.gui.soprettyboard.CameraAppState;
import com.etherblood.a.gui.soprettyboard.ForestBoardAppstate;
import com.etherblood.a.gui.soprettyboard.PostFilterAppstate;
import com.etherblood.a.ai.bots.RandomMover;
import com.etherblood.a.ai.moves.Move;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.setup.SimpleSetup;
import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.templates.DisplayCardTemplate;
import com.etherblood.a.templates.DisplayMinionTemplate;
import com.etherblood.a.templates.LibraryTemplate;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;

public class CardsApp extends SimpleApplication implements ActionListener {

    private static final float ZONE_HEIGHT = 1.3f;
    private Game game;
    private Board board;
    private final Map<Integer, PlayerZones> playerZones = new HashMap<>();
    private final Map<Integer, Card<CardModel>> visualCards = new HashMap<>();
    private final Map<Integer, Card<MinionModel>> visualMinions = new HashMap<>();
    private final Map<BoardObject<?>, Integer> objectEntities = new HashMap<>();
    private final Map<Integer, ConnectionMarker> attacks = new HashMap<>();
    private BitmapText hudText;

    @Override
    public void simpleInitApp() {
        assetManager.registerLocator("assets/", ClasspathLocator.class);
        assetManager.registerLoader(GsonLoader.class, "json");

        stateManager.attach(new ForestBoardAppstate(0));
        stateManager.attach(new PostFilterAppstate());

        hudText = new BitmapText(guiFont, false);
        hudText.setSize(guiFont.getCharSet().getRenderedSize());
        hudText.setColor(ColorRGBA.White);
        hudText.setLocalTranslation(0, getCamera().getHeight(), 0);
        guiNode.attachChild(hudText);

        initListeners();
        initGame();
        initBoardGui();
        initCamera();

//        updateBoard();
    }

    private void initCamera() {
        stateManager.attach(new CameraAppState() {

            @Override
            public void initialize(AppStateManager stateManager, Application application) {
                super.initialize(stateManager, application);
//                updateCamera();
//dirty workaround (:
                applyAI();
            }
        });
    }

    private void updateCamera() {
        int activePlayer = game.getActivePlayer();
        int player0 = game.getPlayers()[0];
        int player1 = game.getPlayers()[1];

        CameraAppState cameraAppState = stateManager.getState(CameraAppState.class);
        Vector3f position = new Vector3f();
        Quaternion rotation = new Quaternion();
        boolean isPlayer1 = activePlayer == player1;
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
        TemplatesLoader loader = new TemplatesLoader(assetManager);
        LibraryTemplate lib0 = loader.loadLibrary("libraries/default.json");
        LibraryTemplate lib1 = loader.loadLibrary("libraries/default.json");
        game = new Game(new SecureRandom(), loader::getCard, loader::getMinion);
        SimpleSetup setup = new SimpleSetup();
        setup.setHero0template(lib0.hero);
        setup.setHero1template(lib1.hero);

        IntList library0 = new IntList();
        for (int card : lib0.cards) {
            library0.add(card);
        }
        IntList library1 = new IntList();
        for (int card : lib1.cards) {
            library1.add(card);
        }

        setup.setLibrary0template(library0);
        setup.setLibrary1template(library1);

        setup.apply(game);
        game.start();
    }

    private void initBoardGui() {
        board = new Board();
        board.registerVisualizer_Class(CardZone.class, new DebugZoneVisualizer() {

            @Override
            public void createVisualisation(Node node, AssetManager assetManager) {
                super.createVisualisation(node, assetManager);
                node.setCullHint(Spatial.CullHint.Always);
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
        board.registerVisualizer(card -> card.getModel() instanceof CardModel, new IngameCardVisualizer(cardPainterJME));
        board.registerVisualizer(card -> card.getModel() instanceof MinionModel, new IngameMinionVisualizer(cardPainterJME));
        board.registerVisualizer_Class(TargetArrow.class, new SimpleTargetArrowVisualizer(SimpleTargetArrowSettings.builder()
                .color(ColorRGBA.White)
                .width(0.5f)
                .build()));
        board.registerVisualizer_Class(ConnectionMarker.class, new ConnectionMarkerVisualizer(SimpleTargetArrowSettings.builder()
                .arcHeight(0.1f)
                .width(0.25f)
                .build()));
        int[] players = game.getPlayers();

        Vector3f offset = new Vector3f(0, 0, ZONE_HEIGHT);
        float directionX = 1;
        float directionZ = 1;
        Quaternion zoneRotation = Quaternion.IDENTITY;

        for (int player : players) {
            if (player == players[1]) {
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
            SimpleIntervalZone deckZone = new SimpleIntervalZone(offset.add(directionX * x, 0, directionZ * z), zoneRotation, new Vector3f(0.02f, 0, 0)) {

                // TODO: Cleanup
                @Override
                public Vector3f getLocalPosition(Vector3f zonePosition) {
                    Vector3f localPosition = super.getLocalPosition(zonePosition);
                    return new Vector3f(localPosition.y, localPosition.x, localPosition.z);
                }
            };
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
        int[] players = game.getPlayers();

        StringBuilder builder = new StringBuilder();
        builder.append("Player 1 mana: ");
        builder.append(data.getOptional(players[0], Components.MANA).orElse(0));
        if (game.hasPlayerLost(players[0])) {
            builder.append(" - LOST");
        } else if (game.hasPlayerWon(players[0])) {
            builder.append(" - WON");
        }
        builder.append(System.lineSeparator());
        builder.append("Player 2 mana: ");
        builder.append(data.getOptional(players[1], Components.MANA).orElse(0));
        if (game.hasPlayerLost(players[1])) {
            builder.append(" - LOST");
        } else if (game.hasPlayerWon(players[1])) {
            builder.append(" - WON");
        }
        builder.append(System.lineSeparator());
        IntList list = data.list(Components.IN_BLOCK_PHASE);
        if (!list.isEmpty()) {
            builder.append("BLOCK PHASE: ").append(list.stream().mapToObj(Integer::toUnsignedString).collect(Collectors.joining(", ")));
        } else {
            list = data.list(Components.IN_ATTACK_PHASE);
            builder.append("ATTACK PHASE: ").append(list.stream().mapToObj(Integer::toUnsignedString).collect(Collectors.joining(", ")));
        }
        hudText.setText(builder.toString());

        IntList handCards = data.list(Components.IN_HAND_ZONE);
        IntList battleCards = data.list(Components.IN_BATTLE_ZONE);
        IntList libraryCards = data.list(Components.IN_LIBRARY_ZONE);
        for (int player : players) {
            PlayerZones zones = playerZones.get(player);
            IntPredicate playerFilter = x -> data.hasValue(x, Components.OWNED_BY, player);
            updateZone(libraryCards.stream().filter(playerFilter).toArray(), zones.getDeckZone(), Vector3f.UNIT_Y);
            updateZone(handCards.stream().filter(playerFilter).toArray(), zones.getHandZone(), Vector3f.UNIT_X);
            updateZone(battleCards.stream().filter(playerFilter).toArray(), zones.getBoardZone(), Vector3f.UNIT_X);
        }

        for (int attacker : data.list(Components.ATTACKS_TARGET)) {
            if (!attacks.containsKey(attacker)) {
                int target = data.get(attacker, Components.ATTACKS_TARGET);
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
            OptionalInt target = data.getOptional(attacker, Components.ATTACKS_TARGET);
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
        for (Card card : new ArrayList<>(cardZone.getCards())) {
            int entity = objectEntities.get(card);
            if (!data.has(entity, Components.IN_LIBRARY_ZONE) && !data.has(entity, Components.IN_HAND_ZONE) && !data.has(entity, Components.IN_BATTLE_ZONE)) {
                cardZone.removeCard(card);
                board.unregister(card);
                objectEntities.remove(card);
                visualCards.remove(entity);
                visualMinions.remove(entity);
            }
        }
        int player = game.getActivePlayer();
        int index = 0;
        for (int cardEntity : cards) {
            if (data.has(cardEntity, Components.CARD_TEMPLATE)) {
                Card<CardModel> card = getOrCreateCard(cardEntity);
                CardModel cardModel = card.getModel();
                cardModel.setEntityId(cardEntity);
                cardModel.setFaceUp(!data.has(cardEntity, Components.IN_LIBRARY_ZONE));
                cardModel.setTemplate((DisplayCardTemplate) game.getCards().apply(data.get(cardEntity, Components.CARD_TEMPLATE)));

                if (game.canCast(player, cardEntity)) {
                    card.setInteractivity(castInteractivity(player, cardEntity));
                    cardModel.setGlow(ColorRGBA.Yellow);
                } else {
                    card.clearInteractivity();
                    cardModel.setGlow(null);
                }
                board.triggerEvent(new MoveCardEvent(card, cardZone, interval.mult(index)));
            } else if (data.has(cardEntity, Components.MINION_TEMPLATE)) {
                Card<MinionModel> card = getOrCreateMinion(cardEntity);
                MinionModel minionModel = card.getModel();
                minionModel.setEntityId(cardEntity);
                minionModel.setFaceUp(true);
                minionModel.setAttack(data.getOptional(cardEntity, Components.ATTACK).orElse(0));
                minionModel.setHealth(data.getOptional(cardEntity, Components.HEALTH).orElse(0));
                minionModel.setTemplate((DisplayMinionTemplate) game.getMinions().apply(data.get(cardEntity, Components.MINION_TEMPLATE)));

                if (game.canDeclareAttack(player, cardEntity)) {
                    card.setInteractivity(attackInteractivity(player, cardEntity));
                    minionModel.setGlow(ColorRGBA.Red);
                } else if (game.canBlock(player, cardEntity)) {
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
                    return game.canDeclareAttack(player, attacker, targetId);
                }
                return false;
            }

            @Override
            public void trigger(BoardObject source, BoardObject target) {
                int actor = objectEntities.get(source);
                int dest = objectEntities.get(target);
                try {
                    game.declareAttack(player, actor, dest);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace(System.err);
                }
                applyAI();
            }
        };
    }

    private Interactivity blockInteractivity(int player, int blocker) {
        return new AimToTargetInteractivity(TargetSnapMode.VALID) {
            @Override
            public boolean isValid(BoardObject target) {
                if (target instanceof Card) {
                    int targetId = objectEntities.get(target);
                    return game.canBlock(player, blocker, targetId);
                }
                return false;
            }

            @Override
            public void trigger(BoardObject source, BoardObject target) {
                int actor = objectEntities.get(source);
                int dest = objectEntities.get(target);
                try {
                    game.block(player, actor, dest);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace(System.err);
                }
                applyAI();
            }
        };
    }

    private Interactivity castInteractivity(int player, int castable) {
        int cardTemplate = game.getData().get(castable, Components.CARD_TEMPLATE);
        CardTemplate template = game.getCards().apply(cardTemplate);
        CardCast cast = template.getAttackPhaseCast() != null ? template.getAttackPhaseCast() : template.getBlockPhaseCast();
        if (cast.isTargeted()) {
            return new AimToTargetInteractivity(TargetSnapMode.VALID) {
                @Override
                public boolean isValid(BoardObject target) {
                    if (target instanceof Card) {
                        int targetId = objectEntities.get(target);
                        return game.canCast(player, castable, targetId);
                    }
                    return false;
                }

                @Override
                public void trigger(BoardObject source, BoardObject target) {
                    try {
                        int targetId = objectEntities.get(target);
                        game.cast(player, castable, targetId);
                    } catch (IllegalArgumentException e) {
                        // should not happen, but game has been rolled back to valid state and can be continued.
                        e.printStackTrace(System.err);
                    }
                    applyAI();
                }
            };
        }
        return new DragToPlayInteractivity() {

            @Override
            public void trigger(BoardObject boardObject, BoardObject target) {
                try {
                    game.cast(player, castable, null);
                } catch (IllegalArgumentException e) {
                    // should not happen, but game has been rolled back to valid state and can be continued.
                    e.printStackTrace(System.err);
                }
                applyAI();
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

            // card.rotation().addRelativeTransformation(new SimpleTargetRotationTransformation(new Quaternion().fromAngles(0, 0, -FastMath.PI)), () -> !inner.getModel().isFaceUp());
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

            // card.rotation().addRelativeTransformation(new SimpleTargetRotationTransformation(new Quaternion().fromAngles(0, -FastMath.PI / 6, 0)), () -> game.getData().has(myCard, Components.TIRED));
            // card.rotation().addRelativeTransformation(new SimpleTargetRotationTransformation(new Quaternion().fromAngles(0, 0, -FastMath.PI)), () -> !inner.getModel().isFaceUp());
        }
        return card;
    }

    @Override
    public void onAction(String name, boolean isPressed, float lastTimePerFrame) {
        if ("space".equals(name) && isPressed) {
            EntityData data = game.getData();
            try {
                IntList list = data.list(Components.IN_BLOCK_PHASE);
                if (!list.isEmpty()) {
                    game.endBlockPhase(list.get(0));
                } else {
                    list = data.list(Components.IN_ATTACK_PHASE);
                    game.endAttackPhase(list.get(0));
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace(System.err);
            }
            applyAI();
//        } else if ("1".equals(name) && isPressed) {
//            List<Card> cards = playerZones[0].getDeckZone().getCards();
//            board.playAnimation(new ShuffleAnimation(cards, this));
        } else if ("2".equals(name) && isPressed) {
            board.playAnimation(new CameraShakeAnimation(cam, 1, 0.01f));
        } else if ("3".equals(name) && isPressed) {
            board.playAnimation(new SnowAnimation(assetManager, cam, rootNode));
        }
    }

    private void applyAI() {
        int botPlayer = game.getPlayers()[0];
        RandomMover randomMover = new RandomMover(game);
        while (!game.isGameOver() && game.getActivePlayer() == botPlayer) {
            Move move = randomMover.nextMove();
            move.apply(game, botPlayer);
        }
        updateBoard();
        updateCamera();
    }
}
