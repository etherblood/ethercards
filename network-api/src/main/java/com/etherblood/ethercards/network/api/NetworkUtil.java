package com.etherblood.ethercards.network.api;

import com.destrostudios.gametools.network.shared.serializers.RecordSerializer;
import com.destrostudios.gametools.network.shared.serializers.UuidSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import com.etherblood.ethercards.network.api.messages.IdentifyRequest;
import com.etherblood.ethercards.network.api.messages.match.ConnectedToMatchUpdate;
import com.etherblood.ethercards.network.api.messages.match.MatchRequest;
import com.etherblood.ethercards.network.api.messages.match.MoveRequest;
import com.etherblood.ethercards.network.api.messages.match.MoveUpdate;
import com.etherblood.ethercards.rules.MoveReplay;
import com.etherblood.ethercards.rules.moves.Cast;
import com.etherblood.ethercards.rules.moves.DeclareAttack;
import com.etherblood.ethercards.rules.moves.DeclareBlock;
import com.etherblood.ethercards.rules.moves.DeclareMulligan;
import com.etherblood.ethercards.rules.moves.EndAttackPhase;
import com.etherblood.ethercards.rules.moves.EndBlockPhase;
import com.etherblood.ethercards.rules.moves.EndMulliganPhase;
import com.etherblood.ethercards.rules.moves.Start;
import com.etherblood.ethercards.rules.moves.Surrender;
import com.etherblood.ethercards.rules.moves.UseAbility;
import com.etherblood.ethercards.templates.api.setup.RawGameSetup;
import com.etherblood.ethercards.templates.api.setup.RawLibraryTemplate;
import com.etherblood.ethercards.templates.api.setup.RawPlayerSetup;
import com.google.gson.internal.LinkedTreeMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

public class NetworkUtil {

    public static final int TCP_PORT = 17239;

    public static void init(Kryo kryo) {
        kryo.register(UUID.class, new UuidSerializer());
        kryo.register(IdentifyRequest.class, new RecordSerializer<>());
        kryo.register(GameReplay.class, new RecordSerializer<>());
        kryo.register(MatchRequest.class, new RecordSerializer<>());
        kryo.register(MoveRequest.class, new RecordSerializer<>());
        kryo.register(MoveUpdate.class, new RecordSerializer<>());
        kryo.register(ConnectedToMatchUpdate.class, new RecordSerializer<>());
        kryo.register(RawGameSetup.class, new RecordSerializer<>());
        kryo.register(RawPlayerSetup[].class, new DefaultArraySerializers.ObjectArraySerializer(kryo, RawPlayerSetup[].class));
        kryo.register(RawPlayerSetup.class, new RecordSerializer<>());
        kryo.register(RawLibraryTemplate.class, new RecordSerializer<>());
        kryo.register(MoveReplay.class, new RecordSerializer<>());
        kryo.register(int[].class, new DefaultArraySerializers.IntArraySerializer());
        kryo.register(HashMap.class, new MapSerializer());
        kryo.register(LinkedTreeMap.class, new MapSerializer());
        kryo.register(LinkedHashMap.class, new MapSerializer());
        kryo.register(ArrayList.class, new CollectionSerializer());

        kryo.register(Start.class, new RecordSerializer<>());
        kryo.register(Cast.class, new RecordSerializer<>());
        kryo.register(DeclareBlock.class, new RecordSerializer<>());
        kryo.register(DeclareAttack.class, new RecordSerializer<>());
        kryo.register(DeclareMulligan.class, new RecordSerializer<>());
        kryo.register(UseAbility.class, new RecordSerializer<>());
        kryo.register(EndAttackPhase.class, new RecordSerializer<>());
        kryo.register(EndBlockPhase.class, new RecordSerializer<>());
        kryo.register(EndMulliganPhase.class, new RecordSerializer<>());
        kryo.register(Surrender.class, new RecordSerializer<>());
    }
}
