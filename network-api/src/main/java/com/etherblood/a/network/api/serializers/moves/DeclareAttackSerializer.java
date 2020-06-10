package com.etherblood.a.network.api.serializers.moves;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.etherblood.a.rules.moves.DeclareAttack;

public class DeclareAttackSerializer extends Serializer<DeclareAttack> {

    @Override
    public void write(Kryo kryo, Output output, DeclareAttack t) {
        output.writeInt(t.player);
        output.writeInt(t.source);
        output.writeInt(t.target);
    }

    @Override
    public DeclareAttack read(Kryo kryo, Input input, Class<DeclareAttack> type) {
        return new DeclareAttack(input.readInt(), input.readInt(), input.readInt());
    }

}
