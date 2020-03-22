package com.etherblood.a.gui;

import com.etherblood.a.templates.DisplayCardTemplate;
import com.etherblood.a.templates.DisplayMinionTemplate;
import com.etherblood.a.templates.LibraryTemplate;
import com.etherblood.a.templates.TemplatesParser;
import com.google.gson.JsonObject;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import java.util.Set;

public class TemplatesLoader {
    private final TemplatesParser parser = new TemplatesParser();
    private final AssetManager assets;
    private final String templatesPath = "templates/";

    public TemplatesLoader(AssetManager assets) {
        this.assets = assets;
    }
    
    public LibraryTemplate loadLibrary(String alias) {
        LibraryTemplate library = parser.getLibrary(alias);
        if(library == null) {
            library = parser.parseLibrary(load(alias));
        }
        Set<String> cards, minions;
        while(!(cards = parser.unresolvedCards()).isEmpty() | !(minions = parser.unresolvedMinions()).isEmpty()) {
            for (String minion : minions) {
                parser.parseMinion(load(minion));
            }
            for (String card : cards) {
                parser.parseCard(load(card));
            }
        }
        return library;
    }
    
    public DisplayCardTemplate getCard(int id) {
        return parser.getCard(id);
    }
    
    public DisplayMinionTemplate getMinion(int id) {
        return parser.getMinion(id);
    }
    
    private JsonObject load(String alias) {
        return assets.loadAsset(new AssetKey<>(aliasToPath(alias)));
    }

    private String aliasToPath(String alias) {
        return templatesPath + alias;
    }
}
