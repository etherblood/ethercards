package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.Update;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.updates.EffectiveStatsService;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class KodamaOfTheCenterTreeTest extends AbstractGameTest {

    @Test
    public void attack_and_health_equal_spirit_count() {
        EffectiveStatsService stats = new EffectiveStatsService(data, templates);
        int kodama = createMinion(player(0), "kodama_of_the_center_tree");

        Assertions.assertEquals(1, stats.attack(kodama));
        Assertions.assertEquals(1, stats.health(kodama));

        int spiritCount = 5;
        for (int i = 1; i < spiritCount; i++) {
            createMinion(player(0), "kodama_of_the_center_tree");
        }

        Assertions.assertEquals(spiritCount, stats.attack(kodama));
        Assertions.assertEquals(spiritCount, stats.health(kodama));
    }
    
    @Test
    public void soulshift_equal_spirit_count() {
        int spider = createCard(player(0), "spectral_spider", core.IN_GRAVEYARD_ZONE);
        int kodama = createMinion(player(0), "kodama_of_the_center_tree");
        CardTemplate card = templates.getCard(getCardId("kodama_of_the_center_tree"));
        
        data.set(kodama, core.DEATH_REQUEST, 1);
        game.getMoves().apply(new Update());
        
        Assertions.assertTrue(data.has(spider, core.IN_HAND_ZONE));
    }
}
