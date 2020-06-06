package com.etherblood.a.network.api;

import com.esotericsoftware.kryo.Kryo;
import com.etherblood.a.network.api.game.GameSetup;
import com.etherblood.a.network.api.game.PlayerSetup;
import com.etherblood.a.rules.MoveReplay;
import com.etherblood.a.rules.moves.Block;
import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.DeclareMulligan;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.rules.moves.EndMulliganPhase;
import com.etherblood.a.rules.moves.Start;
import com.etherblood.a.rules.moves.Surrender;
import com.etherblood.a.templates.RawLibraryTemplate;
import java.util.HashMap;

public class NetworkUtil {

    public static final int TCP_PORT = 17239;

    public static void init(Kryo kryo) {
        kryo.register(Identify.class);
//        kryo.register(UUID.class, new UuidSerializer());
        
        kryo.register(GameSetup.class);
        kryo.register(PlayerSetup.class);
        kryo.register(RawLibraryTemplate.class);
        kryo.register(MoveReplay.class);
        kryo.register(int[].class);
        kryo.register(HashMap.class);

        kryo.register(Start.class);
        kryo.register(Block.class);
        kryo.register(Cast.class);
        kryo.register(DeclareAttack.class);
        kryo.register(DeclareMulligan.class);
        kryo.register(EndAttackPhase.class);
        kryo.register(EndBlockPhase.class);
        kryo.register(EndMulliganPhase.class);
        kryo.register(Surrender.class);
    }
}
