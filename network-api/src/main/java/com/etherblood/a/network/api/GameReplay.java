package com.etherblood.a.network.api;

import com.etherblood.a.rules.MoveReplay;
import com.etherblood.a.templates.api.setup.RawGameSetup;
import java.util.List;

public class GameReplay {

    public RawGameSetup setup;
    public List<MoveReplay> moves;
}
