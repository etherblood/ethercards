package com.etherblood.a.gui.prettycards;

import com.destrostudios.cardgui.BoardObjectModel;
import com.etherblood.a.templates.DisplayMinionTemplate;
import com.jme3.math.ColorRGBA;
import java.util.Objects;

public class MinionModel extends BoardObjectModel {

    private DisplayMinionTemplate template;
    private int entityId, attack, health;
    private boolean faceUp, damaged;
    private ColorRGBA glow;

    public boolean isFaceUp() {
        return faceUp;
    }

    public void setFaceUp(boolean faceUp) {
        updateIfNotEquals(this.faceUp, faceUp, () -> this.faceUp = faceUp);
    }

    public DisplayMinionTemplate getTemplate() {
        return template;
    }

    public void setTemplate(DisplayMinionTemplate template) {
        Objects.requireNonNull(template);
        updateIfNotEquals(this.template, template, () -> this.template = template);
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        updateIfNotEquals(this.entityId, entityId, () -> this.entityId = entityId);
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        updateIfNotEquals(this.attack, attack, () -> this.attack = attack);
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        updateIfNotEquals(this.health, health, () -> this.health = health);
    }

    public void setDamaged(boolean damaged) {
        updateIfNotEquals(this.damaged, damaged, () -> this.damaged = damaged);
    }

    public boolean isDamaged() {
        return damaged;
    }

    public ColorRGBA getGlow() {
        return glow;
    }

    public void setGlow(ColorRGBA glow) {
        updateIfNotEquals(this.glow, glow, () -> this.glow = glow);
    }

}
