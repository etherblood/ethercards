package com.etherblood.a.game.tests;

import com.etherblood.a.entities.Components;
import com.etherblood.a.entities.ComponentsBuilder;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.MinionTemplate;
import com.etherblood.a.rules.templates.effects.Effect;
import com.etherblood.a.rules.templates.effects.SummonEffect;
import com.etherblood.a.templates.DisplayCardTemplate;
import com.etherblood.a.templates.DisplayStats;
import com.etherblood.a.templates.TemplatesLoader;
import com.etherblood.a.templates.TemplatesParser;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValidateDisplayStatsTest {

    @Test
    public void validate() {

        Function<String, JsonElement> assetLoader = x -> {
            try ( Reader reader = Files.newBufferedReader(Paths.get("../assets/templates/" + x))) {
                return new Gson().fromJson(reader, JsonElement.class);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        };
        ComponentsBuilder componentsBuilder = new ComponentsBuilder();
        componentsBuilder.registerModule(CoreComponents::new);
        Components components = componentsBuilder.build();
        TemplatesLoader loader = new TemplatesLoader(assetLoader, new TemplatesParser(components));
        String[] cardPool = new Gson().fromJson(assetLoader.apply("card_pool.json"), String[].class);
        for (String card : cardPool) {
            loader.registerCardAlias(card);
        }
        GameTemplates templates = loader.buildGameTemplates();
        for (String card : cardPool) {
            DisplayCardTemplate cardTemplate = (DisplayCardTemplate) templates.getCard(loader.registerCardAlias(card));
            DisplayStats stats = cardTemplate.getDisplayStats();
            if (stats != null) {
                for (CardCast cast : cardTemplate.getCasts()) {
                    Assertions.assertTrue(hasSummonWithStats(components.getModule(CoreComponents.class), templates, cast, stats), "All casts of " + card + " have a summon with stats " + stats.getAttack() + "/" + stats.getHealth());
                }
            }
        }
    }

    private boolean hasSummonWithStats(CoreComponents core, GameTemplates templates, CardCast cast, DisplayStats stats) {
        for (Effect effect : cast.getEffects()) {
            if (effect instanceof SummonEffect) {
                SummonEffect summon = (SummonEffect) effect;
                MinionTemplate minionTemplate = templates.getMinion(summon.minionId);
                if (minionTemplate.has(core.ATTACK, stats.getAttack()) && minionTemplate.has(core.HEALTH, stats.getHealth())) {
                    return true;
                }
            }
        }
        return false;
    }
}
