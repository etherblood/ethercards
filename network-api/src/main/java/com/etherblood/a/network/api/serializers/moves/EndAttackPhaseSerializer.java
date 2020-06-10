package com.etherblood.a.network.api.serializers.moves;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.etherblood.a.rules.moves.EndAttackPhase;

public class EndAttackPhaseSerializer extends Serializer<EndAttackPhase> {

    @Override
    public void write(Kryo kryo, Output output, EndAttackPhase t) {
        output.writeInt(t.player);
    }

    @Override
    public EndAttackPhase read(Kryo kryo, Input input, Class<EndAttackPhase> type) {
        return new EndAttackPhase(input.readInt());
    }

}
