package com.etherblood.a.network.api;

import com.esotericsoftware.kryo.Kryo;
import com.etherblood.a.network.api.messages.IdentifyRequest;
import com.etherblood.a.network.api.serializers.UuidSerializer;
import com.etherblood.a.templates.api.setup.RawGameSetup;
import com.etherblood.a.templates.api.setup.RawPlayerSetup;
import com.etherblood.a.network.api.messages.match.MatchRequest;
import com.etherblood.a.network.api.messages.match.ConnectedToMatchUpdate;
import com.etherblood.a.network.api.messages.match.MoveRequest;
import com.etherblood.a.network.api.messages.match.MoveUpdate;
import com.etherblood.a.network.api.serializers.moves.DeclareBlockSerializer;
import com.etherblood.a.network.api.serializers.moves.CastSerializer;
import com.etherblood.a.network.api.serializers.moves.DeclareAttackSerializer;
import com.etherblood.a.network.api.serializers.moves.DeclareMulliganSerializer;
import com.etherblood.a.network.api.serializers.moves.EndAttackPhaseSerializer;
import com.etherblood.a.network.api.serializers.moves.EndBlockPhaseSerializer;
import com.etherblood.a.network.api.serializers.moves.EndMulliganPhaseSerializer;
import com.etherblood.a.network.api.serializers.moves.SurrenderSerializer;
import com.etherblood.a.rules.MoveReplay;
import com.etherblood.a.rules.moves.DeclareBlock;
import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.DeclareMulligan;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.rules.moves.EndMulliganPhase;
import com.etherblood.a.rules.moves.Start;
import com.etherblood.a.rules.moves.Surrender;
import com.etherblood.a.templates.api.setup.RawLibraryTemplate;
import com.google.gson.internal.LinkedTreeMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

public class NetworkUtil {

    public static final int TCP_PORT = 17239;

    public static void init(Kryo kryo) {
        kryo.register(UUID.class, new UuidSerializer());
        kryo.register(IdentifyRequest.class);
        kryo.register(GameReplay.class);
        kryo.register(MatchRequest.class);
        kryo.register(MoveRequest.class);
        kryo.register(MoveUpdate.class);
        kryo.register(ConnectedToMatchUpdate.class);
        kryo.register(RawGameSetup.class);
        kryo.register(RawPlayerSetup[].class);
        kryo.register(RawPlayerSetup.class);
        kryo.register(RawLibraryTemplate.class);
        kryo.register(MoveReplay.class);
        kryo.register(int[].class);
        kryo.register(HashMap.class);
        kryo.register(LinkedTreeMap.class);
        kryo.register(LinkedHashMap.class);
        kryo.register(ArrayList.class);

        kryo.register(Start.class);
        kryo.register(Cast.class, new CastSerializer());
        kryo.register(DeclareBlock.class, new DeclareBlockSerializer());
        kryo.register(DeclareAttack.class, new DeclareAttackSerializer());
        kryo.register(DeclareMulligan.class, new DeclareMulliganSerializer());
        kryo.register(EndAttackPhase.class, new EndAttackPhaseSerializer());
        kryo.register(EndBlockPhase.class, new EndBlockPhaseSerializer());
        kryo.register(EndMulliganPhase.class, new EndMulliganPhaseSerializer());
        kryo.register(Surrender.class, new SurrenderSerializer());
    }
}
