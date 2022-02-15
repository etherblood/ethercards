package com.etherblood.ethercards.templates.api.model;

import com.etherblood.ethercards.rules.templates.Tribe;
import java.util.EnumSet;
import java.util.Set;

public class RawCardTemplate {

    public int version;
    public String alias;
    public RawCardDisplay display;
    public Set<Tribe> tribes = EnumSet.noneOf(Tribe.class);
    public RawZoneState hand;
    public RawZoneState battle;
    public RawZoneState graveyard;
    public RawZoneState library;
}
