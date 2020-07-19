package com.etherblood.a.gui;

import com.destroflyer.jme3.effekseer.model.ParticleEffect;
import com.destroflyer.jme3.effekseer.model.ParticleEffectSettings;
import com.destroflyer.jme3.effekseer.reader.EffekseerReader;
import com.destroflyer.jme3.effekseer.renderer.EffekseerControl;
import com.destrostudios.cardgui.Animation;
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
import com.destrostudios.cardgui.samples.animations.CameraShakeAnimation;
import com.destrostudios.cardgui.samples.animations.EffekseerAnimation;
import com.destrostudios.cardgui.samples.animations.TargetedArcAnimation;
import com.destrostudios.cardgui.samples.boardobjects.staticspatial.StaticSpatial;
import com.destrostudios.cardgui.samples.boardobjects.staticspatial.StaticSpatialVisualizer;
import com.destrostudios.cardgui.samples.boardobjects.targetarrow.SimpleTargetArrowSettings;
import com.destrostudios.cardgui.samples.boardobjects.targetarrow.SimpleTargetArrowVisualizer;
import com.destrostudios.cardgui.samples.transformations.relative.HoveringTransformation;
import com.destrostudios.cardgui.samples.visualization.DebugZoneVisualizer;
import com.destrostudios.cardgui.transformations.LinearTargetRotationTransformation;
import com.destrostudios.cardgui.zones.CenteredIntervalZone;
import com.destrostudios.cardgui.zones.SimpleIntervalZone;
import com.destrostudios.cardgui.zones.SimpleScalingIntervalZone;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.game.events.api.events.ParticleEvent;
import com.etherblood.a.gui.arrows.ColoredConnectionArrow;
import com.etherblood.a.gui.arrows.ColoredConnectionArrowVisualizer;
import com.etherblood.a.gui.particles.ColorModel;
import com.etherblood.a.gui.particles.ColoredSphere;
import com.etherblood.a.gui.particles.ColoredSphereVisualizer;
import com.etherblood.a.gui.prettycards.CardImages;
import com.etherblood.a.gui.prettycards.CardPainterAWT;
import com.etherblood.a.gui.prettycards.CardPainterJME;
import com.etherblood.a.gui.prettycards.MyCardVisualizer;
import com.etherblood.a.gui.prettycards.CardModel;
import com.etherblood.a.gui.soprettyboard.CameraAppState;
import com.etherblood.a.gui.soprettyboard.ScalingGridCardZone;
import com.etherblood.a.network.api.GameReplayService;
import com.etherblood.a.network.api.jwt.JwtAuthentication;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.EntityUtil;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.moves.DeclareBlock;
import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.DeclareMulligan;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.rules.moves.EndMulliganPhase;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
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
    private final QueueEventListener events;
    private final Map<Integer, PlayerZones> playerZones = new HashMap<>();
    private final Map<Integer, Card<CardModel>> visualCards = new HashMap<>();
    private final Map<BoardObject<?>, Integer> objectEntities = new HashMap<>();
    private final Map<Integer, ColoredConnectionArrow> arrows = new HashMap<>();
    private final int userControlledPlayer;
    private final CardImages cardImages;
    private final Node rootNode;
    private final String assetsPath;
    private final boolean battleFullArt;

    private CameraAppState cameraAppstate;
    private HudTextAppstate hudAppstate;
    private BoardAppState boardAppState;
    private AssetManager assetManager;
    private Geometry endPhaseButton;
    private final Node endPhaseButtonNode = new Node();

    private final Map<Animation, BoardObject> particleMapBoardObjects = new HashMap<>();

    public GameAppstate(Consumer<Move> moveRequester, GameReplayService gameReplayService, JwtAuthentication authentication, CardImages cardImages, Node rootNode, String assetsPath, boolean battleFullArt) {
        this.moveRequester = moveRequester;
        this.gameReplayService = gameReplayService;
        events = new QueueEventListener();
        this.game = gameReplayService.createInstance(events);
        this.userControlledPlayer = game.findPlayerByIndex(gameReplayService.getPlayerIndex(authentication.user.id));
        this.cardImages = cardImages;
        this.rootNode = rootNode;
        this.assetsPath = assetsPath;
        this.battleFullArt = battleFullArt;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        app.getInputManager().addListener(this, "space");
    }

    @Override
    public void update(float tpf) {
        removeFinishedAnimationObjects();
        gameReplayService.updateInstance(game);
        Object event;
        while ((event = events.getQueue().poll()) != null) {
            playAnimation(event);
        }
        updateBoard();
        updateCamera();
    }

    private void removeFinishedAnimationObjects() {
        for (Map.Entry<Animation, BoardObject> entry : new ArrayList<>(particleMapBoardObjects.entrySet())) {
            if (entry.getKey().isFinished()) {
                board.unregister(entry.getValue());
                particleMapBoardObjects.remove(entry.getKey());
            }
        }
    }

    private void playAnimation(Object event) {
        if (event instanceof ParticleEvent) {
            ParticleEvent particle = (ParticleEvent) event;
            switch (particle.alias) {
                case "raigeki": {
                    board.playAnimation(new CameraShakeAnimation(cameraAppstate.getCamera(), 0.3f, 0.2f));
                    break;
                }
                case "deathwing": {
                    board.playAnimation(new CameraShakeAnimation(cameraAppstate.getCamera(), 0.3f, 0.2f));
                    playParticleEffect("tktk01/Fire7", 1, 1);
                    break;
                }
                case "dark_hole": {
                    playParticleEffect("Pierre02/Benediction", 0.3f, 2);
                    break;
                }
                case "flamestrike": {
                    playParticleEffect("TouhouStrategy/patch_stElmo_area", 0.3f, 1);
                    break;
                }
                case "shock": {
                    playParticleEffect(particle.target, "Pierre01/LightningStrike", 0.08f, 2);
                    break;
                }
                case "fireball": {
                    shootColorSphere(particle.source, particle.target, ColorRGBA.Red);
                    break;
                }
                case "boombot":
                case "stingerfling_spider":
                case "flesh_to_dust": {
                    shootColorSphere(particle.source, particle.target, ColorRGBA.Black);
                    break;
                }
                case "arcane_flight": {
                    shootColorSphere(particle.source, particle.target, ColorRGBA.Blue);
                    break;
                }
                case "antidote": {
                    shootParticleEffect(particle.source, particle.target, "MAGICALxSPIRAL/AquaPoint", 0.4f, 2);
                    break;
                }
                case "blessing_of_wisdom": {
                    playParticleEffect(particle.target, "MAGICALxSPIRAL/Lance3", 0.5f, 2);
                    break;
                }
                case "armadillo_cloak":
                case "slagwurm_armor": {
                    shootColorSphere(particle.source, particle.target, ColorRGBA.White);
                    break;
                }
            }
        }
    }

    private void shootColorSphere(int source, int target, ColorRGBA color) {
        ColoredSphere sphere = new ColoredSphere(new ColorModel());
        sphere.getModel().setColor(color);
        shootBoardObject(source, target, sphere);
    }

    private void shootParticleEffect(int source, int target, String particleEffectName, float scale, float speed) {
        ParticleEffect particleEffect = new EffekseerReader().read(assetsPath, getParticleEffectPath(particleEffectName));
        Node node = createParticleEffectNode(scale);
        node.addControl(new EffekseerControl(particleEffect, getParticleEffectSettings(speed), assetManager));
        shootSpatial(source, target, node);
    }

    private void shootSpatial(int source, int target, Spatial spatial) {
        StaticSpatial staticSpatial = new StaticSpatial();
        staticSpatial.getModel().setSpatial(spatial);
        staticSpatial.setVisibleToMouse(false);
        shootBoardObject(source, target, staticSpatial);
    }

    private void shootBoardObject(int source, int target, TransformedBoardObject transformedBoardObject) {
        transformedBoardObject.resetTransformations();
        transformedBoardObject.position().setCurrentValue(visualCards.get(source).position().getCurrentValue());
        board.register(transformedBoardObject);
        TargetedArcAnimation animation = new TargetedArcAnimation(transformedBoardObject, visualCards.get(target), 1, 0.6f);
        board.playAnimation(animation);
        particleMapBoardObjects.put(animation, transformedBoardObject);
    }

    private void playParticleEffect(String particleEffectName, float scale, float speed) {
        Node node = createParticleEffectNode(scale);
        node.setLocalTranslation(0, 0, 1);
        playParticleEffect(node, particleEffectName, speed);
    }

    private void playParticleEffect(int target, String particleEffectName, float scale, float speed) {
        Node node = createParticleEffectNode(scale);
        StaticSpatial staticSpatial = playParticleEffect(node, particleEffectName, speed);
        staticSpatial.getModel().setFollowTarget(visualCards.get(target));
    }

    private StaticSpatial playParticleEffect(Node node, String particleEffectName, float speed) {
        StaticSpatial staticSpatial = new StaticSpatial();
        staticSpatial.getModel().setSpatial(node);
        board.register(staticSpatial);
        EffekseerAnimation animation = new EffekseerAnimation(
                node,
                assetsPath,
                getParticleEffectPath(particleEffectName),
                getParticleEffectSettings(speed),
                assetManager
        );
        board.playAnimation(animation);
        particleMapBoardObjects.put(animation, staticSpatial);
        return staticSpatial;
    }

    private Node createParticleEffectNode(float scale) {
        Node node = new Node();
        node.setLocalScale(scale);
        node.setShadowMode(RenderQueue.ShadowMode.Off);
        return node;
    }

    private ParticleEffectSettings getParticleEffectSettings(float speed) {
        return ParticleEffectSettings.builder()
                .loop(false)
                .frameLength((1f / 24) / speed)
                .build();
    }

    private String getParticleEffectPath(String particleEffectName) {
        return assetsPath + "effekseer/" + particleEffectName + ".efkproj";
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
        cameraAppstate = stateManager.getState(CameraAppState.class);
        hudAppstate = stateManager.getState(HudTextAppstate.class);
        board = new Board();
        boardAppState = initBoardGui();
        stateManager.attach(boardAppState);

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
        stateManager.getApplication().getInputManager().addListener(this, "space", "f1");
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        stateManager.getApplication().getInputManager().removeListener(this);
        stateManager.detach(stateManager.getState(BoardAppState.class));

        playerZones.clear();
        visualCards.clear();
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
        IntList battleCards = data.listInValueOrder(core.IN_BATTLE_ZONE);
        IntList libraryCards = data.list(core.IN_LIBRARY_ZONE);
        IntList graveyardCards = data.listInValueOrder(core.IN_GRAVEYARD_ZONE);
        for (int player : players) {
            PlayerZones zones = playerZones.get(player);
            IntPredicate playerFilter = x -> data.hasValue(x, core.OWNER, player);
            updateZone(libraryCards.stream().filter(playerFilter).toArray(), zones.getDeckZone(), Vector3f.UNIT_Y);
            updateZone(handCards.stream().filter(playerFilter).toArray(), zones.getHandZone(), Vector3f.UNIT_X);
            updateZone(battleCards.stream().filter(playerFilter).toArray(), zones.getBoardZone(), Vector3f.UNIT_X);
            updateZone(graveyardCards.stream().filter(playerFilter).toArray(), zones.getGraveyardZone(), Vector3f.UNIT_Y);
        }

        for (Map.Entry<Integer, ColoredConnectionArrow> entry : new ArrayList<>(arrows.entrySet())) {
            ColoredConnectionArrow arrow = entry.getValue();
            int minion = entry.getKey();
            OptionalInt attackTarget = data.getOptional(minion, core.ATTACK_TARGET);
            if (attackTarget.isPresent() && arrow.getModel().getTarget() == visualCards.get(attackTarget.getAsInt())) {
                continue;
            }
            OptionalInt blockTarget = data.getOptional(minion, core.BLOCK_TARGET);
            if (blockTarget.isPresent() && arrow.getModel().getTarget() == visualCards.get(blockTarget.getAsInt())) {
                continue;
            }
            arrows.remove(minion);
            board.unregister(arrow);
        }

        IntList blocked = new IntList();
        for (int blocker : data.list(core.BLOCK_TARGET)) {
            int target = data.get(blocker, core.BLOCK_TARGET);
            blocked.add(target);
            ColoredConnectionArrow arrow = arrows.get(blocker);
            if (arrow == null) {
                ColorRGBA color = new ColorRGBA(0.25f, 0.25f, 1, 0.75f);
                arrow = new ColoredConnectionArrow(visualCards.get(blocker), visualCards.get(target), color);
                arrows.put(blocker, arrow);
                board.register(arrow);
            }
        }
        for (int attacker : data.list(core.ATTACK_TARGET)) {
            int target = data.get(attacker, core.ATTACK_TARGET);
            ColoredConnectionArrow arrow = arrows.get(attacker);
            ColorRGBA color;
            if (blocked.contains(attacker)) {
                color = new ColorRGBA(0.5f, 0, 0, 0.25f);
            } else {
                color = new ColorRGBA(1, 0.25f, 0.25f, 0.75f);
            }
            if (arrow == null) {
                arrow = new ColoredConnectionArrow(visualCards.get(attacker), visualCards.get(target), color);
                arrows.put(attacker, arrow);
                board.register(arrow);
            } else {
                arrow.getModel().setColor(color);
            }
        }
    }

    private void updateZone(int[] cards, CardZone cardZone, Vector3f interval) {
        EntityData data = game.getData();
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        for (Card card : new ArrayList<>(cardZone.getCards())) {
            int entity = objectEntities.get(card);
            if (!data.has(entity, core.IN_LIBRARY_ZONE) && !data.has(entity, core.IN_HAND_ZONE) && !data.has(entity, core.IN_BATTLE_ZONE) && !data.has(entity, core.IN_GRAVEYARD_ZONE)) {
                cardZone.removeCard(card);
                board.unregister(card);
                objectEntities.remove(card);
                visualCards.remove(entity);
            }
        }
        List<Move> moves = game.getMoves().generate(false, userControlledPlayer);
        int index = 0;
        for (int cardEntity : cards) {
            Card<CardModel> card = getOrCreateMinion(cardEntity);
            CardModel minionModel = card.getModel();
            minionModel.updateFrom(data, game.getTemplates());
            if (moves.stream().filter(Cast.class::isInstance).map(Cast.class::cast)
                    .anyMatch(cast -> cast.source == cardEntity)) {
                card.setInteractivity(castInteractivity(userControlledPlayer, cardEntity));
                minionModel.setGlow(ColorRGBA.Yellow);
            } else if (moves.stream().filter(DeclareMulligan.class::isInstance).map(DeclareMulligan.class::cast)
                    .anyMatch(mulligan -> mulligan.card == cardEntity)) {
                card.setInteractivity(mulliganInteractivity(userControlledPlayer, cardEntity));
                minionModel.setGlow(ColorRGBA.Red);
            } else if (moves.stream().filter(DeclareAttack.class::isInstance).map(DeclareAttack.class::cast)
                    .anyMatch(attack -> attack.source == cardEntity)) {
                card.setInteractivity(attackInteractivity(userControlledPlayer, cardEntity));
                minionModel.setGlow(ColorRGBA.Red);
            } else if (moves.stream().filter(DeclareBlock.class::isInstance).map(DeclareBlock.class::cast)
                    .anyMatch(block -> block.source == cardEntity)) {
                card.setInteractivity(blockInteractivity(userControlledPlayer, cardEntity));
                minionModel.setGlow(ColorRGBA.Blue);
            } else {
                card.clearInteractivity();
                minionModel.setGlow(null);
            }
            board.triggerEvent(new MoveCardEvent(card, cardZone, interval.mult(index++)));
        }
    }

    private Interactivity attackInteractivity(int player, int attacker) {
        return new AimToTargetInteractivity(TargetSnapMode.VALID) {
            @Override
            public boolean isValid(BoardObject target) {
                if (target instanceof Card) {
                    int targetId = objectEntities.get(target);
                    List<Move> moves = game.getMoves().generate(false);
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
                    List<Move> moves = game.getMoves().generate(false);
                    return moves.stream().anyMatch(new DeclareBlock(player, blocker, targetId)::equals);
                }
                return false;
            }

            @Override
            public void trigger(BoardObject source, BoardObject target) {
                int actor = objectEntities.get(source);
                int dest = objectEntities.get(target);
                requestMove(new DeclareBlock(player, actor, dest));
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
                        List<Move> moves = game.getMoves().generate(false);
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
                requestMove(new Cast(player, castable, null));
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

    private Card<CardModel> getOrCreateMinion(int myCard) {
        Card<CardModel> card = visualCards.get(myCard);
        if (card == null) {
            EntityData data = game.getData();
            CoreComponents core = data.getComponents().getModule(CoreComponents.class);
            Card<CardModel> inner = new Card<>(new CardModel(myCard));
            card = inner;
            visualCards.put(myCard, card);
            objectEntities.put(card, myCard);

            card.position().addRelativeTransformation(new HoveringTransformation(0.1f, 4), () -> data.has(myCard, core.IN_BATTLE_ZONE) && data.has(myCard, core.FLYING));
            card.rotation().addRelativeTransformation(new LinearTargetRotationTransformation(new Quaternion().fromAngles(0, -FastMath.PI / 6, 0)), () -> data.has(myCard, core.TIRED));
            card.rotation().addRelativeTransformation(new LinearTargetRotationTransformation(new Quaternion().fromAngles(0, 0, -FastMath.PI)), () -> !inner.getModel().isFaceUp());
        }
        return card;
    }

    private void updateCamera() {
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
        board.registerVisualizer_Class(ColoredSphere.class, new ColoredSphereVisualizer());
        board.registerVisualizer_Class(StaticSpatial.class, new StaticSpatialVisualizer());
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
        board.registerVisualizer(card -> card.getModel() instanceof CardModel, new MyCardVisualizer(cardPainterJME, battleFullArt));
        board.registerVisualizer_Class(TargetArrow.class, new SimpleTargetArrowVisualizer(SimpleTargetArrowSettings.builder()
                .color(new ColorRGBA(1, 1, 1, 0.8f))
                .width(0.5f)
                .build()));
        board.registerVisualizer_Class(ColoredConnectionArrow.class, new ColoredConnectionArrowVisualizer(SimpleTargetArrowSettings.builder()
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

            float x = -2.65f;
            float z = 1.3f;
            ScalingGridCardZone boardZone = new ScalingGridCardZone(offset.add(x, 0, directionZ * z).subtract(0.5f, 0, 0.7f), zoneRotation, new Vector4f(0, 0, 6, 1.5f), new Vector2f(1, 1.4f));

            x = -3.75f;
            SimpleIntervalZone deckZone = new SimpleIntervalZone(offset.add(x, 0, directionZ * z), zoneRotation, new Vector3f(0, 0.01f, 0));

            z = 0.35f;
            float graveyardScale = 0.5f;
            SimpleIntervalZone graveyardZone = new SimpleIntervalZone(offset.add(x, 0, directionZ * z), zoneRotation, new Vector3f(graveyardScale, graveyardScale, graveyardScale), new Vector3f(0, 0.01f, 0));

            x = 0;
            z = 3;
            Quaternion handRotation = zoneRotation.mult(new Quaternion().fromAngleAxis(FastMath.QUARTER_PI, Vector3f.UNIT_X));
            CenteredIntervalZone handZone = new CenteredIntervalZone(offset.add(directionX * x, 0, directionZ * z), handRotation, new Vector3f(0.85f, 1, 1));

            board.addZone(deckZone);
            board.addZone(graveyardZone);
            board.addZone(handZone);
            board.addZone(boardZone);
            playerZones.put(player, new PlayerZones(deckZone, graveyardZone, handZone, boardZone));
        }
        return new BoardAppState(board, rootNode, BoardSettings.builder()
                .hoverInspectionDelay(1f)
                .isInspectable(this::isInspectable)
                .dragProjectionZ(0.9975f)
                .build());
    }

    private boolean isInspectable(TransformedBoardObject<?> transformedBoardObject) {
        if (transformedBoardObject instanceof Card) {
            Card<?> card = (Card<?>) transformedBoardObject;
            CardZone cardZone = card.getZonePosition().getZone();
            for (PlayerZones playerZones : playerZones.values()) {
                if ((cardZone == playerZones.getBoardZone())) {
                    return true;
                }
                if ((cardZone == playerZones.getGraveyardZone())) {
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
        } else if ("f1".equals(name) && isPressed) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            System.out.println(gson.toJson(EntityUtil.toMap(game.getData())));
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
        button.setLocalTranslation(3.3f, 0.5f, 1);
        button.setLocalRotation(new Quaternion().fromAngles(-FastMath.QUARTER_PI, 0, 0));
        return button;
    }

}
