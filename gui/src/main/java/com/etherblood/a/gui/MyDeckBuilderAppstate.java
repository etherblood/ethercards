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
import com.etherblood.a.gui.prettycards.CardImages;
import com.etherblood.a.gui.prettycards.CardPainterAWT;
import com.etherblood.a.gui.prettycards.CardPainterJME;
import com.etherblood.a.gui.prettycards.MyCardVisualizer;
import com.etherblood.a.gui.prettycards.CardModel;
import com.etherblood.a.gui.prettycards.CardModelUpdater;
import com.etherblood.a.gui.soprettyboard.CameraAppState;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.templates.Tribe;
import com.etherblood.a.templates.api.CardColor;
import com.etherblood.a.templates.api.DisplayCardTemplate;
import com.etherblood.a.templates.api.setup.RawLibraryTemplate;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.TextField;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MyDeckBuilderAppstate extends AbstractAppState {

    private static final int LIBRARY_SIZE_LIMIT = 100;
    private RawLibraryTemplate result = null;
    private final RawLibraryTemplate presetLibrary;
    private final DeckBuilderAppState<CardModel> deckBuilderAppState;
    private final Node guiNode;
    private final Button selectButton;
    private final TextField searchField;
    private long searchVersion;
    private final ActionListener saveLibraryListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (isPressed) {
                completeResult();
            }
        }
    };
    private final ActionListener nextPage = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (isPressed && deckBuilderAppState.getCollectionPage() + 1 < deckBuilderAppState.getCollectionPagesCount()) {
                deckBuilderAppState.goToNextColletionPage();
            }
        }
    };
    private final ActionListener previousPage = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (isPressed && deckBuilderAppState.getCollectionPage() > 0) {
                deckBuilderAppState.goToPreviousCollectionPage();
            }
        }
    };

    public MyDeckBuilderAppstate(List<DisplayCardTemplate> cards, CardImages cardImages, Node rootNode, Node guiNode, RawLibraryTemplate presetLibrary, Components components) {
        this.presetLibrary = presetLibrary;
        this.guiNode = guiNode;

        Comparator<CardModel> cardOrder = Comparator.comparingInt(this::getManaCost);
        cardOrder = cardOrder.thenComparing(x -> x.getTemplate().getName());
        cardOrder = cardOrder.thenComparingInt(x -> x.getTemplate().getId());

        CoreComponents core = components.getModule(CoreComponents.class);

        List<CardModel> allCardModels = new LinkedList<>();
        for (DisplayCardTemplate card : cards) {
            CardModel cardModel = new CardModel(-1);
            new CardModelUpdater().updateFromTemplate(cardModel, card, core);
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
        BoardObjectVisualizer<Card<CardModel>> collectionCardVisualizer = new MyCardVisualizer(cardPainterJME, false);
        BoardObjectVisualizer<Card<DeckBuilderDeckCardModel<CardModel>>> deckCardVisualizer = new MyDeckBuilderDeckCardVisualizer(cardImages);
        Map<CardModel, Integer> maxCounts = new HashMap<>();
        for (CardModel card : allCardModels) {
            if (card.getTemplate().getAlias().equals("blue_eyes_white_dragon")) {
                maxCounts.put(card, 3);
            } else {
                maxCounts.put(card, 2);
            }
        }
        DeckBuilderSettings<CardModel> settings = DeckBuilderSettings.<CardModel>builder()
                .allCardModels(allCardModels)
                .deckCardsMaximumUnique(maxCounts)
                .deckCardsMaximumTotal(LIBRARY_SIZE_LIMIT)
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

        selectButton = new Button("Select");
        selectButton.setFontSize(50);
        selectButton.addClickCommands(x -> completeResult());
        selectButton.setLocalTranslation(100, 100, 0);

        searchField = new TextField("search");
        searchField.setFontSize(40);
        searchField.setLocalTranslation(50, 700, 0);
        searchField.setPreferredWidth(200);
        searchVersion = searchField.getDocumentModel().getVersion();
    }

    private int getManaCost(CardModel card) {
        Integer manaCost = card.getTemplate().getHand().getCast().getManaCost();
        if (manaCost == null) {
            return 0;
        }
        return manaCost;
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
        guiNode.attachChild(searchField);
        guiNode.attachChild(selectButton);
        stateManager.attach(deckBuilderAppState);
        InputManager inputManager = stateManager.getApplication().getInputManager();
        inputManager.addListener(saveLibraryListener, "space");
        inputManager.addListener(nextPage, "right");
        inputManager.addListener(previousPage, "left");
        stateManager.getState(CameraAppState.class).moveTo(new Vector3f(-0.25036395f, 15.04817f, 1), new Quaternion(2.0723649E-8f, 0.71482813f, -0.6993001f, 1.8577744E-7f));
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        stateManager.detach(deckBuilderAppState);
        InputManager inputManager = stateManager.getApplication().getInputManager();
        inputManager.removeListener(saveLibraryListener);
        inputManager.removeListener(nextPage);
        inputManager.removeListener(previousPage);

        guiNode.detachChild(selectButton);
        guiNode.detachChild(searchField);
    }

    private void completeResult() {
        RawLibraryTemplate resultLibrary = new RawLibraryTemplate();
        resultLibrary.hero = presetLibrary.hero;
        resultLibrary.cards = new HashMap<>();
        for (Map.Entry<CardModel, Integer> entry : deckBuilderAppState.getDeck().entrySet()) {
            resultLibrary.cards.put(entry.getKey().getTemplate().getAlias(), entry.getValue());
        }
        result = resultLibrary;
    }

    public RawLibraryTemplate getResult() {
        return result;
    }

    @Override
    public void update(float tpf) {
        if (searchField.getDocumentModel().getVersion() != searchVersion) {
            deckBuilderAppState.setCollectionCardFilter(card -> {
                String search = searchField.getText().toLowerCase();
                if (search.isEmpty()) {
                    return true;
                }
                DisplayCardTemplate template = card.getTemplate();
                if (template.getName().toLowerCase().contains(search)) {
                    return true;
                }
                if (template.getDescription() != null && template.getDescription().toLowerCase().contains(search)) {
                    return true;
                }
                if (template.getFlavourText() != null && template.getFlavourText().toLowerCase().contains(search)) {
                    return true;
                }
                for (String keyword : card.getKeywords()) {
                    if (keyword.toLowerCase().contains(search)) {
                        return true;
                    }
                }
                for (CardColor color : template.getColors()) {
                    if (color.name().toLowerCase().contains(search)) {
                        return true;
                    }
                }
                for (Tribe tribe : template.getTribes()) {
                    if (tribe.name().toLowerCase().contains(search)) {
                        return true;
                    }
                }
                if (isInteger(search, 10)) {
                    Integer searchInt = Integer.parseInt(search);
                    if (searchInt.equals(template.getHand().getCast().getManaCost())) {
                        return true;
                    }
                }
                return false;
            });
            searchVersion = searchField.getDocumentModel().getVersion();
        }
    }

    // https://stackoverflow.com/a/5439547
    private static boolean isInteger(String s, int radix) {
        try (Scanner sc = new Scanner(s.trim())) {
            if (!sc.hasNextInt(radix)) {
                return false;
            }
            // we know it starts with a valid int, now make sure
            // there's nothing left!
            sc.nextInt(radix);
            return !sc.hasNext();
        }
    }

}
