package com.etherblood.a.gui;

import com.destrostudios.cardgui.CardZone;


/**
 *
 * @author Carl
 */
public class PlayerZones {

    public PlayerZones(CardZone deckZone, CardZone graveyardZone, CardZone handZone, CardZone boardZone) {
        this.deckZone = deckZone;
        this.graveyardZone = graveyardZone;
        this.handZone = handZone;
        this.boardZone = boardZone;
    }
    private CardZone deckZone;
    private CardZone graveyardZone;
    private CardZone handZone;
    private CardZone boardZone;

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