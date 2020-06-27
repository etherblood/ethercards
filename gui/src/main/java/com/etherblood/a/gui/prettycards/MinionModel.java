package com.etherblood.a.gui.prettycards;

import com.destrostudios.cardgui.BoardObjectModel;
import com.destrostudios.cardgui.annotations.IsBoardObjectInspected;
import com.etherblood.a.templates.DisplayMinionTemplate;
import com.jme3.math.ColorRGBA;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MinionModel extends BoardObjectModel {

    private DisplayMinionTemplate template;
    private int entityId, attack, health;
    private boolean faceUp, damaged, hero;
    private ColorRGBA glow;
    private Set<String> keywords = new HashSet<>();
    @IsBoardObjectInspected
    private boolean isInspected;

    public boolean isInspected() {
        return isInspected;
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public void setFaceUp(boolean faceUp) {
        updateIfNotEquals(this.faceUp, faceUp, () -> this.faceUp = faceUp);
    }

    public boolean isHero() {
        return hero;
    }

    public void setHero(boolean hero) {
        updateIfNotEquals(this.hero, hero, () -> this.hero = hero);
    }

    public DisplayMinionTemplate getTemplate() {
        return template;
    }

    public void setTemplate(DisplayMinionTemplate template) {
        Objects.requireNonNull(template);
        updateIfNotEquals(this.template, template, () -> this.template = template);
    }

    public Set<String> getKeywords() {
        return Collections.unmodifiableSet(keywords);
    }

    public void setKeywords(Set<String> keywords) {
        Objects.requireNonNull(keywords);
        updateIfNotEquals(this.keywords, keywords, () -> this.keywords = keywords);
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
