package com.etherblood.a.rules.moves;

public class DeclareMulligan implements Move {

    public final int player, card;

    public DeclareMulligan(int player, int card) {
        this.player = player;
        this.card = card;
    }

    @Override
    public int hashCode() {
        return 131 * player + card;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DeclareMulligan)) {
            return false;
        }
        DeclareMulligan other = (DeclareMulligan) obj;
        return player == other.player && card == other.card;
    }

}
