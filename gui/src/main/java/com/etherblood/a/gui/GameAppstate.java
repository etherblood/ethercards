package com.etherblood.a.gui;

import com.destrostudios.cardgui.Board;
import com.destrostudios.cardgui.BoardAppState;
import com.destrostudios.cardgui.BoardObject;
import com.destrostudios.cardgui.BoardSettings;
import com.destrostudios.cardgui.Card;
import com.destrostudios.cardgui.CardZone;
import com.destrostudios.cardgui.Interactivity;
import com.destrostudios.cardgui.TargetSnapMode;
import com.destrostudios.cardgui.TransformedBoardObject;
import com.destrostudios.cardgui.boardobjects.TargetArrow;
import com.destrostudios.cardgui.events.MoveCardEvent;
import com.destrostudios.cardgui.interactivities.AimToTargetInteractivity;
import com.destrostudios.cardgui.interactivities.DragToPlayInteractivity;
import com.destrostudios.cardgui.samples.boardobjects.connectionmarker.ConnectionMarker;
import com.destrostudios.cardgui.samples.boardobjects.connectionmarker.ConnectionMarkerVisualizer;
import com.destrostudios.cardgui.samples.boardobjects.targetarrow.SimpleTargetArrowSettings;
import com.destrostudios.cardgui.samples.boardobjects.targetarrow.SimpleTargetArrowVisualizer;
import com.destrostudios.cardgui.samples.visualization.DebugZoneVisualizer;
import com.destrostudios.cardgui.transformations.LinearTargetPositionTransformation3f;
import com.destrostudios.cardgui.transformations.LinearTargetRotationTransformation;
import com.destrostudios.cardgui.zones.CenteredIntervalZone;
import com.destrostudios.cardgui.zones.SimpleIntervalZone;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.gui.prettycards.CardImages;
import com.etherblood.a.gui.prettycards.CardModel;
import com.etherblood.a.gui.prettycards.CardPainterAWT;
import com.etherblood.a.gui.prettycards.CardPainterJME;
import com.etherblood.a.gui.prettycards.CardVisualizer_Card;
import com.etherblood.a.gui.prettycards.CardVisualizer_Minion;
import com.etherblood.a.gui.prettycards.MinionModel;
import com.etherblood.a.gui.soprettyboard.CameraAppState;
import com.etherblood.a.network.api.GameReplayService;
import com.etherblood.a.network.api.jwt.JwtAuthentication;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.moves.Block;
import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.DeclareMulligan;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.rules.moves.EndMulliganPhase;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.templates.DisplayCardTemplate;
import com.etherblood.a.templates.DisplayMinionTemplate;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameAppstate extends AbstractAppState implements ActionListener {

    private static final Logger LOG = LoggerFactory.getLogger(GameAppstate.class);
    private static final float ZONE_HEIGHT = 1.3f;
    private final Consumer<Move> moveRequester;
    private final GameReplayService gameReplayService;
    private Game game;
    private Board board;
    private final Map<Integer, PlayerZones> playerZones = new HashMap<>();
    private final Map<Integer, Card<CardModel>> visualCards = new HashMap<>();
    private final Map<Integer, Card<MinionModel>> visualMinions = new HashMap<>();
    private final Map<BoardObject<?>, Integer> objectEntities = new HashMap<>();
    private final Map<Integer, ConnectionMarker> arrows = new HashMap<>();
    private final int userControlledPlayer;
    private final CardImages cardImages;

    private final Node rootNode;

    private CameraAppState cameraAppstate;
    private HudTextAppstate hudAppstate;
    private AssetManager assetManager;
    private Geometry endPhaseButton;
    private final Node endPhaseButtonNode = new Node();

    public GameAppstate(Consumer<Move> moveRequester, GameReplayService gameReplayService, JwtAuthentication authentication, CardImages cardImages, Node rootNode) {
        this.moveRequester = moveRequester;
        this.gameReplayService = gameReplayService;
        this.game = gameReplayService.createInstance();
        this.userControlledPlayer = game.findPlayerByIndex(gameReplayService.getPlayerIndex(authentication.user.id));
        this.cardImages = cardImages;
        this.rootNode = rootNode;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        app.getInputManager().addMapping("space", new KeyTrigger(KeyInput.KEY_SPACE));
        app.getInputManager().addListener(this, "space");
    }

    @Override
    public void update(float tpf) {
        gameReplayService.updateInstance(game);
        updateBoard();
        updateCamera();
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
        cameraAppstate = stateManager.getState(CameraAppState.class);
        hudAppstate = stateManager.getState(HudTextAppstate.class);
        board = new Board();
        stateManager.attach(initBoardGui());

        assetManager = stateManager.getApplication().getAssetManager();
        endPhaseButton = createButton(assetManager);

        if (game.findPlayerByIndex(0) != userControlledPlayer) {
            endPhaseButtonNode.setLocalRotation(new Quaternion().fromAngles(0, FastMath.PI, 0));
            endPhaseButtonNode.setLocalTranslation(1, 0.8f, 1);
        }
        endPhaseButtonNode.attachChild(endPhaseButton);
        rootNode.attachChild(endPhaseButtonNode);

        ButtonAppstate buttonAppstate = stateManager.getState(ButtonAppstate.class);
        buttonAppstate.registerButton(endPhaseButton, () -> onAction("space", true, 0), ColorRGBA.Gray, ColorRGBA.LightGray, ColorRGBA.White);
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        stateManager.detach(stateManager.getState(BoardAppState.class));

        playerZones.clear();
        visualCards.clear();
        visualMinions.clear();
        objectEntities.clear();
        arrows.clear();
        board = null;
        game = null;

        ButtonAppstate buttonAppstate = stateManager.getState(ButtonAppstate.class);
        buttonAppstate.unregisterButton(endPhaseButton);
        rootNode.detachChild(endPhaseButtonNode);
        endPhaseButtonNode.detachChild(endPhaseButton);
        endPhaseButton = null;
        assetManager = null;
    }

    private void updateBoard() {
        EntityData data = game.getData();
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        StringBuilder builder = new StringBuilder();
        IntList players = data.list(core.PLAYER_INDEX);
        for (int player : players) {
            int playerIndex = data.get(player, core.PLAYER_INDEX);
            builder.append(gameReplayService.getPlayerName(playerIndex));
            builder.append(System.lineSeparator());
            builder.append(" mana: ");
            builder.append(data.getOptional(player, core.MANA).orElse(0));
            builder.append(System.lineSeparator());
            if (game.hasPlayerLost(player)) {
                builder.append(" LOST");
            } else if (game.hasPlayerWon(player)) {
                builder.append(" WON");
            } else if (data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK)) {
                builder.append(" ATTACK_PHASE");
            } else if (data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK)) {
                builder.append(" BLOCK_PHASE");
            } else if (data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.MULLIGAN)) {
                builder.append(" MULLIGAN_PHASE");
            }
            builder.append(System.lineSeparator());
            builder.append(System.lineSeparator());
        }

        hudAppstate.setText(builder.toString());
        OptionalInt optionalPhase = data.getOptional(userControlledPlayer, core.ACTIVE_PLAYER_PHASE);
        if (optionalPhase.isPresent()) {
            int phase = optionalPhase.getAsInt();
            endPhaseButton.setCullHint(Spatial.CullHint.Dynamic);
            switch (phase) {
                case PlayerPhase.ATTACK:
                    endPhaseButton.getMaterial().setTexture("ColorMap", assetManager.loadTexture("textures/buttons/end_turn.png"));
                    break;
                case PlayerPhase.BLOCK:
                    endPhaseButton.getMaterial().setTexture("ColorMap", assetManager.loadTexture("textures/buttons/end_phase.png"));
                    break;
                case PlayerPhase.MULLIGAN:
                    endPhaseButton.getMaterial().setTexture("ColorMap", assetManager.loadTexture("textures/buttons/end_mulligan.png"));
                    break;
                default:
                    throw new AssertionError();
            }
        } else {
            endPhaseButton.setCullHint(Spatial.CullHint.Always);
        }

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
            if (!arrows.containsKey(attacker)) {
                int target = data.get(attacker, core.ATTACKS_TARGET);
                if (visualMinions.containsKey(target)) {
                    ConnectionMarker arrow = new ConnectionMarker();
                    arrow.getModel().setSourceBoardObject(visualMinions.get(attacker));
                    arrows.put(attacker, arrow);
                    board.register(arrow);
                }
            }
        }

        for (int blocker : data.list(core.BLOCKS_ATTACKER)) {
            if (!arrows.containsKey(blocker)) {
                int target = data.get(blocker, core.BLOCKS_ATTACKER);
                if (visualMinions.containsKey(target)) {
                    ConnectionMarker arrow = new ConnectionMarker();
                    arrow.getModel().setSourceBoardObject(visualMinions.get(blocker));
                    arrows.put(blocker, arrow);
                    board.register(arrow);
                }
            }
        }

        for (Map.Entry<Integer, ConnectionMarker> entry : new ArrayList<>(arrows.entrySet())) {
            ConnectionMarker arrow = entry.getValue();
            int minion = entry.getKey();
            OptionalInt target = data.getOptional(minion, core.ATTACKS_TARGET);
            if (target.isPresent()) {
                Card<MinionModel> targetObject = visualMinions.get(target.getAsInt());
                if (targetObject != null) {
                    arrow.getModel().setTargetBoardObject(targetObject);
                    continue;
                }
            }
            OptionalInt attacker = data.getOptional(minion, core.BLOCKS_ATTACKER);
            if (attacker.isPresent()) {
                Card<MinionModel> targetObject = visualMinions.get(attacker.getAsInt());
                if (targetObject != null) {
                    arrow.getModel().setTargetBoardObject(targetObject);
                    continue;
                }
            }
            arrows.remove(minion);
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
        List<Move> moves = game.getMoves().generate(false, false);
        int index = 0;
        for (int cardEntity : cards) {
            if (data.has(cardEntity, core.CARD_TEMPLATE)) {
                Card<CardModel> card = getOrCreateCard(cardEntity);
                CardModel cardModel = card.getModel();
                cardModel.setEntityId(cardEntity);
                cardModel.setFaceUp(!data.has(cardEntity, core.IN_LIBRARY_ZONE));
                cardModel.setTemplate((DisplayCardTemplate) game.getTemplates().getCard(data.get(cardEntity, core.CARD_TEMPLATE)));

                if (moves.stream().filter(Cast.class::isInstance).map(Cast.class::cast)
                        .anyMatch(cast -> cast.player == userControlledPlayer && cast.source == cardEntity)) {
//                if (game.getMoves().canCast(userControlledPlayer, cardEntity)) {
                    card.setInteractivity(castInteractivity(userControlledPlayer, cardEntity));
                    cardModel.setGlow(ColorRGBA.Yellow);
                } else if (moves.stream().filter(DeclareMulligan.class::isInstance).map(DeclareMulligan.class::cast)
                        .anyMatch(mulligan -> mulligan.player == userControlledPlayer && mulligan.card == cardEntity)) {
//                } else if (game.getMoves().canDeclareMulligan(userControlledPlayer, cardEntity)) {
                    card.setInteractivity(mulliganInteractivity(userControlledPlayer, cardEntity));
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
                Set<String> keywords = new HashSet<>();
                if (data.has(cardEntity, core.TRAMPLE)) {
                    keywords.add("Trample");
                }
                if (data.has(cardEntity, core.LIFELINK)) {
                    keywords.add("Lifelink");
                }
                if (data.has(cardEntity, core.VENOM)) {
                    keywords.add("Venom_" + data.get(cardEntity, core.VENOM));
                }
                if (data.has(cardEntity, core.POISONED)) {
                    keywords.add("Poisoned_" + data.get(cardEntity, core.POISONED));
                }
                if (data.has(cardEntity, core.MANA_POOL)) {
                    keywords.add("Mana_Pool_" + data.get(cardEntity, core.MANA_POOL));
                }
                if (data.has(cardEntity, core.MANA_GROWTH)) {
                    keywords.add("Mana_Growth_" + data.get(cardEntity, core.MANA_GROWTH));
                }
                if (data.has(cardEntity, core.DRAWS_PER_TURN)) {
                    keywords.add("Draws_per_Turn " + data.get(cardEntity, core.DRAWS_PER_TURN));
                }
                if (data.has(cardEntity, core.DRAWS_ON_ATTACK)) {
                    keywords.add("Draws_on_Attack " + data.get(cardEntity, core.DRAWS_ON_ATTACK));
                }
                if (data.has(cardEntity, core.DRAWS_ON_BLOCK)) {
                    keywords.add("Draws_on_Block " + data.get(cardEntity, core.DRAWS_ON_BLOCK));
                }
                if (data.has(cardEntity, core.CANNOT_ATTACK)) {
                    keywords.add("Cannot_attack");
                }
                if (data.has(cardEntity, core.CANNOT_BLOCK)) {
                    keywords.add("Cannot_block");
                }
                if (data.has(cardEntity, core.CANNOT_BE_BLOCKED)) {
                    keywords.add("Cannot_be_blocked");
                }
                minionModel.setKeywords(keywords);

                if (moves.stream().filter(DeclareAttack.class::isInstance).map(DeclareAttack.class::cast)
                        .anyMatch(attack -> attack.player == userControlledPlayer && attack.source == cardEntity)) {
//                if (game.getMoves().canDeclareAttack(userControlledPlayer, cardEntity)) {
                    card.setInteractivity(attackInteractivity(userControlledPlayer, cardEntity));
                    minionModel.setGlow(ColorRGBA.Red);
                } else if (moves.stream().filter(Block.class::isInstance).map(Block.class::cast)
                        .anyMatch(block -> block.player == userControlledPlayer && block.source == cardEntity)) {
//                } else if (game.getMoves().canBlock(userControlledPlayer, cardEntity)) {
                    card.setInteractivity(blockInteractivity(userControlledPlayer, cardEntity));
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
                    List<Move> moves = game.getMoves().generate(false, false);
                    return moves.stream().anyMatch(new DeclareAttack(player, attacker, targetId)::equals);
                }
                return false;
            }

            @Override
            public void trigger(BoardObject source, BoardObject target) {
                int actor = objectEntities.get(source);
                int dest = objectEntities.get(target);
                requestMove(new DeclareAttack(player, actor, dest));
            }
        };
    }

    private Interactivity blockInteractivity(int player, int blocker) {
        return new AimToTargetInteractivity(TargetSnapMode.VALID) {
            @Override
            public boolean isValid(BoardObject target) {
                if (target instanceof Card) {
                    int targetId = objectEntities.get(target);
                    List<Move> moves = game.getMoves().generate(false, false);
                    return moves.stream().anyMatch(new Block(player, blocker, targetId)::equals);
                }
                return false;
            }

            @Override
            public void trigger(BoardObject source, BoardObject target) {
                int actor = objectEntities.get(source);
                int dest = objectEntities.get(target);
                requestMove(new Block(player, actor, dest));
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
                        List<Move> moves = game.getMoves().generate(false, false);
                        return moves.stream().anyMatch(new Cast(player, castable, targetId)::equals);
                    }
                    return false;
                }

                @Override
                public void trigger(BoardObject source, BoardObject target) {
                    int targetId = objectEntities.get(target);
                    requestMove(new Cast(player, castable, targetId));
                }
            };
        }
        return new DragToPlayInteractivity() {

            @Override
            public void trigger(BoardObject boardObject, BoardObject target) {
                requestMove(new Cast(player, castable, ~0));
            }
        };
    }

    private Interactivity mulliganInteractivity(int player, int card) {
        return new DragToPlayInteractivity() {

            @Override
            public void trigger(BoardObject boardObject, BoardObject target) {
                requestMove(new DeclareMulligan(player, card));
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

            card.rotation().addRelativeTransformation(new LinearTargetRotationTransformation(new Quaternion().fromAngles(0, 0, -FastMath.PI)), () -> !inner.getModel().isFaceUp());
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
            card.rotation().addRelativeTransformation(new LinearTargetRotationTransformation(new Quaternion().fromAngles(0, -FastMath.PI / 6, 0)), () -> data.has(myCard, core.TIRED));
            card.rotation().addRelativeTransformation(new LinearTargetRotationTransformation(new Quaternion().fromAngles(0, 0, -FastMath.PI)), () -> !inner.getModel().isFaceUp());
        }
        return card;
    }

    private void updateCamera() {
        if (game.isGameOver()) {
            return;
        }

        Vector3f position = new Vector3f();
        Quaternion rotation = new Quaternion();
        boolean isPlayer1 = userControlledPlayer == game.findPlayerByIndex(1);
        position.set(0, 3.8661501f, 6.470482f);
        if (isPlayer1) {
            position.addLocal(0, 0, -10.339f);
        }
        rotation.lookAt(new Vector3f(0, -0.7237764f, -0.6900346f), Vector3f.UNIT_Y);
        if (isPlayer1) {
            rotation = new Quaternion().fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y).multLocal(rotation);
        }
        cameraAppstate.moveTo(position, rotation, 0.3f);
    }

    private BoardAppState initBoardGui() {
        CoreComponents core = game.getData().getComponents().getModule(CoreComponents.class);
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
        CardPainterJME cardPainterJME = new CardPainterJME(new CardPainterAWT(cardImages));
        board.registerVisualizer(card -> card.getModel() instanceof CardModel, new CardVisualizer_Card(cardPainterJME));
        board.registerVisualizer(card -> card.getModel() instanceof MinionModel, new CardVisualizer_Minion(cardPainterJME));
        board.registerVisualizer_Class(TargetArrow.class, new SimpleTargetArrowVisualizer(SimpleTargetArrowSettings.builder()
                .color(new ColorRGBA(1, 1, 1, 0.8f))
                .width(0.5f)
                .build()));
        board.registerVisualizer_Class(ConnectionMarker.class, new ConnectionMarkerVisualizer(SimpleTargetArrowSettings.builder()
                .color(new ColorRGBA(1, 1, 1, 0.5f))
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
            x += 3.9f;
            SimpleIntervalZone boardZone = new SimpleIntervalZone(offset.add(directionX * x, 0, directionZ * z), zoneRotation, new Vector3f(-0.9f * directionX, 1, 1));

            x = 0;
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
        return new BoardAppState(board, rootNode, BoardSettings.builder()
                .hoverInspectionDelay(1f)
                .isInspectable(this::isInBattleZone)
                .dragProjectionZ(0.9975f)
                .build());
    }

    private boolean isInBattleZone(TransformedBoardObject<?> transformedBoardObject) {
        if (transformedBoardObject instanceof Card) {
            Card<?> card = (Card<?>) transformedBoardObject;
            CardZone cardZone = card.getZonePosition().getZone();
            for (PlayerZones playerZones : playerZones.values()) {
                if ((cardZone == playerZones.getBoardZone())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onAction(String name, boolean isPressed, float lastTimePerFrame) {
        if (!isEnabled()) {
            return;
        }
        if ("space".equals(name) && isPressed) {
            EntityData data = game.getData();
            CoreComponents core = data.getComponents().getModule(CoreComponents.class);
            data.getOptional(userControlledPlayer, core.ACTIVE_PLAYER_PHASE).ifPresent(phase -> {
                switch (phase) {
                    case PlayerPhase.BLOCK:
                        requestMove(new EndBlockPhase(userControlledPlayer));
                        break;
                    case PlayerPhase.ATTACK:
                        requestMove(new EndAttackPhase(userControlledPlayer));
                        break;
                    case PlayerPhase.MULLIGAN:
                        requestMove(new EndMulliganPhase(userControlledPlayer));
                        break;
                }
            });
        }
    }

    private void requestMove(Move move) {
        if (game.isGameOver()) {
            LOG.error("Game is already over, discarded {}.", move);
            return;
        }
        moveRequester.accept(move);
    }

    private Geometry createButton(AssetManager assetManager) {
        Quad quad = new Quad(2, 1);
        Geometry button = new Geometry("button", quad);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        button.setMaterial(mat);

        button.setLocalScale(0.3f);
        button.setLocalTranslation(2.6f, 0.5f, 1);
        button.setLocalRotation(new Quaternion().fromAngles(-FastMath.QUARTER_PI, 0, 0));
        return button;
    }

}
