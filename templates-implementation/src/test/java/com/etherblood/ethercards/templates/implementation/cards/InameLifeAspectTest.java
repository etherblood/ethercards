package com.etherblood.ethercards.templates.implementation.cards;

import com.etherblood.ethercards.entities.ComponentMeta;
import com.etherblood.ethercards.rules.DeathOptions;
import com.etherblood.ethercards.rules.moves.Update;
import com.etherblood.ethercards.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InameLifeAspectTest extends AbstractGameTest {

    @Test
    public void exiledOnDeath() {
        int inameLifeAspect = createMinion(player(0), "iname_life_aspect");
        data.set(inameLifeAspect, core.DEATH_REQUEST, DeathOptions.NORMAL);

        moves.apply(new Update());

        for (ComponentMeta meta : data.getSchema().getMetas()) {
            Assertions.assertFalse(data.has(inameLifeAspect, meta.id));
        }
    }

    @Test
    public void allDeadSpiritsToHand() {
        int notSpirit = createCard(player(0), "ornithopter", core.IN_GRAVEYARD_ZONE);
        int deadOpponentSpirit = createCard(player(1), "iname_life_aspect", core.IN_GRAVEYARD_ZONE);
        int deadSpirit1 = createCard(player(0), "iname_life_aspect", core.IN_GRAVEYARD_ZONE);
        int deadSpirit2 = createCard(player(0), "iname_death_aspect", core.IN_GRAVEYARD_ZONE);

        int inameLifeAspect = createMinion(player(0), "iname_life_aspect");
        data.set(inameLifeAspect, core.DEATH_REQUEST, DeathOptions.NORMAL);

        moves.apply(new Update());

        Assertions.assertTrue(data.has(notSpirit, core.IN_GRAVEYARD_ZONE));
        Assertions.assertTrue(data.has(deadOpponentSpirit, core.IN_GRAVEYARD_ZONE));
        Assertions.assertTrue(data.has(deadSpirit1, core.IN_HAND_ZONE));
        Assertions.assertTrue(data.has(deadSpirit2, core.IN_HAND_ZONE));
    }
}
