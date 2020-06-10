package com.etherblood.a.network.api.serializers.moves;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.etherblood.a.rules.moves.Block;

public class BlockSerializer extends Serializer<Block> {

    @Override
    public void write(Kryo kryo, Output output, Block t) {
        output.writeInt(t.player);
        output.writeInt(t.source);
        output.writeInt(t.target);
    }

    @Override
    public Block read(Kryo kryo, Input input, Class<Block> type) {
        return new Block(input.readInt(), input.readInt(), input.readInt());
    }

}
