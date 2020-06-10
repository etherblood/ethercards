package com.etherblood.a.network.api.serializers.moves;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.etherblood.a.rules.moves.Surrender;

public class SurrenderSerializer extends Serializer<Surrender> {

    @Override
    public void write(Kryo kryo, Output output, Surrender t) {
        output.writeInt(t.player);
    }

    @Override
    public Surrender read(Kryo kryo, Input input, Class<Surrender> type) {
        return new Surrender(input.readInt());
    }

}
