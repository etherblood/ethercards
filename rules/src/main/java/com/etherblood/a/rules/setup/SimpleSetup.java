package com.etherblood.a.rules.setup;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.templates.MinionTemplate;
import java.util.function.IntFunction;

public class SimpleSetup {

    private IntList library0template, library1template;
    private int hero0template, hero1template;

    public void apply(Game game) {
        IntFunction<MinionTemplate> minions = game.getMinions();
        EntityData data = game.getData();

        int player0 = game.getPlayers()[0];

        int hero0 = data.createEntity();
        data.set(hero0, Components.OWNED_BY, player0);
        data.set(hero0, Components.IN_BATTLE_ZONE, 1);
        MinionTemplate hero_0 = minions.apply(hero0template);
        for (int component : hero_0) {
            data.set(hero0, component, hero_0.get(component));
        }

        int player1 = game.getPlayers()[1];

        int hero1 = data.createEntity();
        data.set(hero1, Components.OWNED_BY, player1);
        data.set(hero1, Components.IN_BATTLE_ZONE, 1);
        MinionTemplate hero_1 = minions.apply(hero1template);
        for (int component : hero_1) {
            data.set(hero1, component, hero_1.get(component));
        }

        for (int cardTemplate : library0template) {
            int card = data.createEntity();
            data.set(card, Components.IN_LIBRARY_ZONE, 1);
            data.set(card, Components.OWNED_BY, player0);
            data.set(card, Components.CARD_TEMPLATE, cardTemplate);
        }

        for (int cardTemplate : library1template) {
            int card = data.createEntity();
            data.set(card, Components.IN_LIBRARY_ZONE, 1);
            data.set(card, Components.OWNED_BY, player1);
            data.set(card, Components.CARD_TEMPLATE, cardTemplate);
        }
    }

    public void setLibrary0template(IntList library0template) {
        this.library0template = library0template;
    }

    public void setLibrary1template(IntList library1template) {
        this.library1template = library1template;
    }

    public void setHero0template(int hero0template) {
        this.hero0template = hero0template;
    }

    public void setHero1template(int hero1template) {
        this.hero1template = hero1template;
    }
}
