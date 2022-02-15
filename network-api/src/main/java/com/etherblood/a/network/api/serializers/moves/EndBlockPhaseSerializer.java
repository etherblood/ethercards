package com.etherblood.ethercards.network.api.serializers.moves;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.etherblood.ethercards.rules.moves.EndBlockPhase;

public class EndBlockPhaseSerializer extends Serializer<EndBlockPhase> {

    @Override
    public void write(Kryo kryo, Output output, EndBlockPhase t) {
        output.writeInt(t.player);
    }

    @Override
    public EndBlockPhase read(Kryo kryo, Input input, Class<EndBlockPhase> type) {
        return new EndBlockPhase(input.readInt());
    }

}
