package com.etherblood.a.gui;

import com.destrostudios.cardgui.BoardObjectVisualizer;
import com.destrostudios.cardgui.BoardSettings;
import com.destrostudios.cardgui.Card;
import com.destrostudios.cardgui.CardZone;
import com.destrostudios.cardgui.samples.tools.deckbuilder.DeckBuilderAppState;
import com.destrostudios.cardgui.samples.tools.deckbuilder.DeckBuilderDeckCardModel;
import com.destrostudios.cardgui.samples.tools.deckbuilder.DeckBuilderSettings;
import com.destrostudios.cardgui.samples.visualization.DebugZoneVisualizer;
import com.destrostudios.cardgui.zones.SimpleIntervalZone;
import com.etherblood.a.entities.Components;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.SimpleEntityData;
import com.etherblood.a.gui.prettycards.CardImages;
import com.etherblood.a.gui.prettycards.CardPainterAWT;
import com.etherblood.a.gui.prettycards.CardPainterJME;
import com.etherblood.a.gui.prettycards.MyCardVisualizer;
import com.etherblood.a.gui.prettycards.CardModel;
import com.etherblood.a.gui.soprettyboard.CameraAppState;
import com.etherblood.a.gui.soprettyboard.ForestBoardAppstate;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.updates.SystemsUtil;
import com.etherblood.a.templates.api.DisplayCardTemplate;
import com.etherblood.a.templates.api.RawLibraryTemplate;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MyDeckBuilderAppstate extends AbstractAppState {

    private static final int CARD_COPIES_LIMIT = 100;
    private RawLibraryTemplate result = null;
    private final RawLibraryTemplate presetLibrary;

    private final DeckBuilderAppState<CardModel> deckBuilderAppState;
    private final ActionListener saveLibraryListener = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {
            if (isPressed) {
                completeResult();
            }
        }
    };
    private final ActionListener nextPage = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {
            if (isPressed && deckBuilderAppState.getCollectionPage() + 1 < deckBuilderAppState.getCollectionPagesCount()) {
                deckBuilderAppState.goToNextColletionPage();
            }
        }
    };
    private final ActionListener previousPage = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {
            if (isPressed && deckBuilderAppState.getCollectionPage() > 0) {
                deckBuilderAppState.goToPreviousCollectionPage();
            }
        }
    };
    private final Geometry saveLibraryButton;
    private final Node rootNode;

    public MyDeckBuilderAppstate(List<DisplayCardTemplate> cards, CardImages cardImages, Node rootNode, RawLibraryTemplate presetLibrary, Components components) {
        this.presetLibrary = presetLibrary;
        this.rootNode = rootNode;

        Comparator<CardModel> cardOrder = Comparator.comparingInt(this::getManaCost);
        cardOrder = cardOrder.thenComparing(x -> x.getTemplate().getName());
        cardOrder = cardOrder.thenComparingInt(x -> x.getTemplate().getId());

        EntityData data = new SimpleEntityData(components);
        GameTemplates gameTemplates = new GameTemplates(cards.stream().collect(Collectors.toMap(x -> x.getId(), x -> x)));

        List<CardModel> allCardModels = new LinkedList<>();
        for (DisplayCardTemplate card : cards) {
            CardModel cardModel = new CardModel(SystemsUtil.createCard(data, card.getId(), 0), card);
            cardModel.updateFrom(data, gameTemplates);
            allCardModels.add(cardModel);
        }
        allCardModels.sort(cardOrder);
        CardZone collectionZone = new SimpleIntervalZone(new Vector3f(-0.5f, 10f, 1), new Vector3f(0.9f, 1, 1.2f));
        CardZone deckZone = new SimpleIntervalZone(new Vector3f(8, 1, -4.715f), new Vector3f(1, 1, 0.57f));
        BoardObjectVisualizer<CardZone> collectionZoneVisualizer = new DebugZoneVisualizer() {

            @Override
            protected Geometry createVisualizationObject(AssetManager assetManager) {
                Geometry geometry = super.createVisualizationObject(assetManager);
                geometry.setCullHint(Spatial.CullHint.Always);
                return geometry;
            }

            @Override
            protected Vector2f getSize(CardZone zone) {
                return new Vector2f(16.5f, 10);
            }
        };
        BoardObjectVisualizer<CardZone> deckZoneVisualizer = new DebugZoneVisualizer() {

            @Override
            protected Geometry createVisualizationObject(AssetManager assetManager) {
                Geometry visualizationObject = super.createVisualizationObject(assetManager);
                visualizationObject.move(0, 0, 4.715f);
                visualizationObject.setCullHint(Spatial.CullHint.Always);
                return visualizationObject;
            }

            @Override
            protected Vector2f getSize(CardZone zone) {
                return new Vector2f(4, 10);
            }
        };
        CardPainterJME cardPainterJME = new CardPainterJME(new CardPainterAWT(cardImages));
        BoardObjectVisualizer<Card<CardModel>> collectionCardVisualizer = new MyCardVisualizer(cardPainterJME);
        BoardObjectVisualizer<Card<DeckBuilderDeckCardModel<CardModel>>> deckCardVisualizer = new MyDeckBuilderDeckCardVisualizer(cardImages);
        DeckBuilderSettings<CardModel> settings = DeckBuilderSettings.<CardModel>builder()
                .allCardModels(allCardModels)
                .collectionZone(collectionZone)
                .deckZone(deckZone)
                .collectionZoneVisualizer(collectionZoneVisualizer)
                .deckZoneVisualizer(deckZoneVisualizer)
                .collectionCardVisualizer(collectionCardVisualizer)
                .deckCardVisualizer(deckCardVisualizer)
                .deckCardOrder(cardOrder)
                .collectionCardsPerRow(5)
                .collectionRowsPerPage(3)
                .boardSettings(BoardSettings.builder()
                        .dragProjectionZ(0.9975f)
                        .hoverInspectionDelay(0.5f)
                        .isInspectable(x -> collectionZone.getCards().contains(x))
                        .build())
                .build();

        HashMap<CardModel, Integer> deck = new HashMap<>();
        for (Map.Entry<String, Integer> entry : presetLibrary.cards.entrySet()) {
            CardModel model = allCardModels.stream().filter(x -> x.getTemplate().getAlias().equals(entry.getKey())).findAny().get();
            deck.put(model, entry.getValue());
        }
        deckBuilderAppState = new DeckBuilderAppState<>(rootNode, settings);
        deckBuilderAppState.setDeck(deck);

        Quad quad = new Quad(1, 1);
        saveLibraryButton = new Geometry("saveLibraryButton", quad);
    }

    private int getManaCost(CardModel card) {
        Integer manaCost = card.getTemplate().getManaCost();
        if (manaCost == null) {
            return 0;
        }
        return manaCost;
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
        stateManager.attach(deckBuilderAppState);
        InputManager inputManager = stateManager.getApplication().getInputManager();
        inputManager.addListener(saveLibraryListener, "space");
        inputManager.addListener(nextPage, "right");
        inputManager.addListener(previousPage, "left");
        stateManager.getState(CameraAppState.class).moveTo(new Vector3f(-0.25036395f, 15.04817f, 1), new Quaternion(2.0723649E-8f, 0.71482813f, -0.6993001f, 1.8577744E-7f));
        stateManager.attach(new ForestBoardAppstate(0));

        Material mat = new Material(stateManager.getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", stateManager.getApplication().getAssetManager().loadTexture("textures/buttons/save.png"));
        saveLibraryButton.setMaterial(mat);
        rootNode.attachChild(saveLibraryButton);
        saveLibraryButton.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
        saveLibraryButton.setLocalTranslation(6, 2, 5.7f);
        saveLibraryButton.setLocalScale(2, 1, 1);
        stateManager.getState(ButtonAppstate.class).registerButton(saveLibraryButton, this::completeResult, ColorRGBA.Gray, ColorRGBA.LightGray, ColorRGBA.White);
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        stateManager.detach(deckBuilderAppState);
        InputManager inputManager = stateManager.getApplication().getInputManager();
        inputManager.removeListener(saveLibraryListener);
        inputManager.removeListener(nextPage);
        inputManager.removeListener(previousPage);
        stateManager.detach(stateManager.getState(ForestBoardAppstate.class));

        stateManager.getState(ButtonAppstate.class).unregisterButton(saveLibraryButton);
        rootNode.detachChild(saveLibraryButton);
    }

    private void completeResult() {
        RawLibraryTemplate resultLibrary = new RawLibraryTemplate();
        resultLibrary.hero = presetLibrary.hero;
        resultLibrary.cards = new HashMap<>();
        for (Map.Entry<CardModel, Integer> entry : deckBuilderAppState.getDeck().entrySet()) {
            resultLibrary.cards.put(entry.getKey().getTemplate().getAlias(), Math.min(entry.getValue(), CARD_COPIES_LIMIT));
        }
        result = resultLibrary;
    }

    public RawLibraryTemplate getResult() {
        return result;
    }

}
