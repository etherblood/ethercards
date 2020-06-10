package com.etherblood.a.network.api.serializers.moves;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.etherblood.a.rules.moves.Cast;

public class CastSerializer extends Serializer<Cast> {

    @Override
    public void write(Kryo kryo, Output output, Cast t) {
        output.writeInt(t.player);
        output.writeInt(t.source);
        output.writeInt(t.target);
    }

    @Override
    public Cast read(Kryo kryo, Input input, Class<Cast> type) {
        return new Cast(input.readInt(), input.readInt(), input.readInt());
    }

}
