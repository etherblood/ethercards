package com.etherblood.a.rules.templates.casteffects;

public class SummonEffect extends CastEffect {

    @MinionId
    public final int minionId;

    public SummonEffect(int minionId) {
        this.minionId = minionId;
    }
}
