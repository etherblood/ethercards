package com.etherblood.ethercards.network.api;

import com.etherblood.ethercards.rules.MoveReplay;
import com.etherblood.ethercards.templates.api.setup.RawGameSetup;
import java.util.ArrayList;
import java.util.List;

public record GameReplay(
        RawGameSetup setup,
        List<MoveReplay> moves
) {

    public GameReplay(GameReplay other) {
        this(new RawGameSetup(other.setup), new ArrayList<>(other.moves));
    }
}
