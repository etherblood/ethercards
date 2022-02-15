package com.etherblood.ethercards.gui;

import com.destrostudios.cardgui.CardZone;

public class PlayerZones {

    private final CardZone deckZone;
    private final CardZone graveyardZone;
    private final CardZone handZone;
    private final CardZone boardZone;

    public PlayerZones(CardZone deckZone, CardZone graveyardZone, CardZone handZone, CardZone boardZone) {
        this.deckZone = deckZone;
        this.graveyardZone = graveyardZone;
        this.handZone = handZone;
        this.boardZone = boardZone;
    }

    public CardZone getDeckZone() {
        return deckZone;
    }

    public CardZone getGraveyardZone() {
        return graveyardZone;
    }

    public CardZone getHandZone() {
        return handZone;
    }

    public CardZone getBoardZone() {
        return boardZone;
    }
}
