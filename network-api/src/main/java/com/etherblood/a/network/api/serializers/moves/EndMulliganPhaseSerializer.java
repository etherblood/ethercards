package com.etherblood.a.network.api.serializers.moves;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.etherblood.a.rules.moves.EndMulliganPhase;

public class EndMulliganPhaseSerializer extends Serializer<EndMulliganPhase> {

    @Override
    public void write(Kryo kryo, Output output, EndMulliganPhase t) {
        output.writeInt(t.player);
    }

    @Override
    public EndMulliganPhase read(Kryo kryo, Input input, Class<EndMulliganPhase> type) {
        return new EndMulliganPhase(input.readInt());
    }

}
