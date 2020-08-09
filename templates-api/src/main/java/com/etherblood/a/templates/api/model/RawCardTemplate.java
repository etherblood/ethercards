package com.etherblood.a.templates.api.model;

import com.etherblood.a.rules.templates.ZoneState;
import com.etherblood.a.rules.templates.Tribe;
import java.util.ArrayList;
import java.util.List;

public class RawCardTemplate {

    public int version;
    public String alias;
    public RawCardDisplay display;
    public List<Tribe> tribes = new ArrayList<>();
    public ZoneState hand;
    public ZoneState battle;
    public ZoneState graveyard;
    public ZoneState library;
}
