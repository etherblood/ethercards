package com.etherblood.a.rules.setup;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.systems.util.SystemsUtil;
import com.etherblood.a.rules.templates.MinionTemplate;

public class SimpleSetup {

    private final IntList[] libraries;
    private final int[] heroes;

    public SimpleSetup(int playerCount) {
        libraries = new IntList[playerCount];
        heroes = new int[playerCount];
    }

    public void apply(Game game) {
        EntityData data = game.getData();
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);

        for (int i = 0; i < heroes.length; i++) {
            int player = data.createEntity();
            data.set(player, core.PLAYER_INDEX, i);
            if (i == 0) {
                data.set(player, core.DRAW_CARDS, 3);
            } else {
                data.set(player, core.DRAW_CARDS, 4);
            }
            
            int hero = data.createEntity();
            data.set(hero, core.OWNED_BY, player);
            data.set(hero, core.HERO, 1);
            data.set(hero, core.IN_BATTLE_ZONE, 1);
            //TODO: mana growth & draws should be somehow added to the player, not their hero
            SystemsUtil.increase(data, hero, core.MANA_GROWTH, 1);
            SystemsUtil.increase(data, hero, core.DRAWS_PER_TURN, 1);
            MinionTemplate heroTemplate = game.getTemplates().getMinion(heroes[i]);
            for (int component : heroTemplate) {
                data.set(hero, component, heroTemplate.get(component));
            }

            for (int cardTemplate : libraries[i]) {
                int card = data.createEntity();
                data.set(card, core.IN_LIBRARY_ZONE, 1);
                data.set(card, core.OWNED_BY, player);
                data.set(card, core.CARD_TEMPLATE, cardTemplate);
            }
        }
    }

    public void setLibrary(int playerIndex, IntList library) {
        libraries[playerIndex] = library;
    }

    public void setHero(int playerIndex, int hero) {
        heroes[playerIndex] = hero;
    }
}
