package com.etherblood.a.gui.prettycards;

import com.destrostudios.cardgui.BoardObjectModel;
import com.etherblood.a.templates.DisplayCardTemplate;
import com.jme3.math.ColorRGBA;
import java.util.Objects;

public class CardModel extends BoardObjectModel {

    private DisplayCardTemplate template;
    private int entityId;
    private boolean faceUp;
    private ColorRGBA glow;

    public boolean isFaceUp() {
        return faceUp;
    }

    public void setFaceUp(boolean faceUp) {
        updateIfNotEquals(this.faceUp, faceUp, () -> this.faceUp = faceUp);
    }

    public DisplayCardTemplate getTemplate() {
        return template;
    }

    public void setTemplate(DisplayCardTemplate template) {
        Objects.requireNonNull(template);
        updateIfNotEquals(this.template, template, () -> this.template = template);
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        updateIfNotEquals(this.entityId, entityId, () -> this.entityId = entityId);
    }

    public ColorRGBA getGlow() {
        return glow;
    }

    public void setGlow(ColorRGBA glow) {
        updateIfNotEquals(this.glow, glow, () -> this.glow = glow);
    }

}
