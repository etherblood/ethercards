package com.etherblood.ethercards.rules.templates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.CoreComponents;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class CardTemplate {

    private final int id;
    private final boolean isMinion;
    private final Set<Tribe> tribes;
    private final ZoneState hand;
    private final ZoneState battle;
    private final ZoneState graveyard;
    private final ZoneState library;

    protected CardTemplate(int id, boolean isMinion, Set<Tribe> tribes, ZoneState hand, ZoneState battle, ZoneState graveyard, ZoneState library) {
        this.id = id;
        this.isMinion = isMinion;
        Set<Tribe> tribesCopy = EnumSet.noneOf(Tribe.class);
        tribesCopy.addAll(tribes);
        this.tribes = Collections.unmodifiableSet(tribesCopy);
        this.hand = hand;
        this.battle = battle;
        this.graveyard = graveyard;
        this.library = library;
    }

    public int getId() {
        return id;
    }

    public String getTemplateName() {
        return "#" + id;
    }

    public Set<Tribe> getTribes() {
        return tribes;
    }

    public boolean isMinion() {
        return isMinion;
    }

    public ZoneState getActiveZone(int entity, EntityData data) {
        //This method does not belong here, move it into a utility class or a service
        CoreComponents core = data.getSchema().getModule(CoreComponents.class);
        if (data.has(entity, core.IN_HAND_ZONE)) {
            return getHand();
        } else if (data.has(entity, core.IN_BATTLE_ZONE)) {
            return getBattle();
        } else if (data.has(entity, core.IN_GRAVEYARD_ZONE)) {
            return getGraveyard();
        } else if (data.has(entity, core.IN_LIBRARY_ZONE)) {
            return getLibrary();
        } else {
            throw new AssertionError();
        }
    }

    public ZoneState getHand() {
        return hand;
    }

    public ZoneState getBattle() {
        return battle;
    }

    public ZoneState getGraveyard() {
        return graveyard;
    }

    public ZoneState getLibrary() {
        return library;
    }
}
