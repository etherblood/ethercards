package com.etherblood.a.network.api.serializers.moves;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.etherblood.a.rules.moves.DeclareBlock;

public class BlockSerializer extends Serializer<DeclareBlock> {

    @Override
    public void write(Kryo kryo, Output output, DeclareBlock t) {
        output.writeInt(t.player);
        output.writeInt(t.source);
        output.writeInt(t.target);
    }

    @Override
    public DeclareBlock read(Kryo kryo, Input input, Class<DeclareBlock> type) {
        return new DeclareBlock(input.readInt(), input.readInt(), input.readInt());
    }

}
