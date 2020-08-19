package com.etherblood.a.gui.prettycards;

import com.destrostudios.cardgui.BoardObjectModel;
import com.destrostudios.cardgui.annotations.IsBoardObjectInspected;
import com.etherblood.a.templates.api.DisplayCardTemplate;
import com.jme3.math.ColorRGBA;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CardModel extends BoardObjectModel {

    private final int entityId;
    private DisplayCardTemplate template;
    private Integer attack, health;
    private boolean damaged, foil;
    private boolean faceUp = true;
    private boolean mulligan = false;
    private ColorRGBA glow;
    private List<String> keywords = new ArrayList<>();
    private boolean inBattleZone;
    @IsBoardObjectInspected
    private boolean isInspected;

    public CardModel(int entityId) {
        this.entityId = entityId;
    }

    public boolean isInspected() {
        return isInspected;
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public boolean isMulligan() {
        return mulligan;
    }

    public void setMulligan(boolean mulliganed) {
        updateIfNotEquals(this.mulligan, mulliganed, () -> this.mulligan = mulliganed);
    }

    public void setFaceUp(boolean faceUp) {
        updateIfNotEquals(this.faceUp, faceUp, () -> this.faceUp = faceUp);
    }

    public boolean isInBattleZone() {
        return inBattleZone;
    }

    public void setInBattleZone(boolean inBattleZone) {
        updateIfNotEquals(this.inBattleZone, inBattleZone, () -> this.inBattleZone = inBattleZone);
    }

    public boolean isFoil() {
        return foil;
    }

    public void setFoil(boolean foil) {
        updateIfNotEquals(this.foil, foil, () -> this.foil = foil);
    }

    public DisplayCardTemplate getTemplate() {
        return template;
    }

    public void setTemplate(DisplayCardTemplate template) {
        updateIfNotEquals(this.template, template, () -> this.template = template);
    }

    public List<String> getKeywords() {
        return Collections.unmodifiableList(keywords);
    }

    public void setKeywords(List<String> keywords) {
        Objects.requireNonNull(keywords);
        updateIfNotEquals(this.keywords, keywords, () -> this.keywords = keywords);
    }

    public int getEntityId() {
        return entityId;
    }

    public Integer getAttack() {
        return attack;
    }

    public void setAttack(Integer attack) {
        updateIfNotEquals(this.attack, attack, () -> this.attack = attack);
    }

    public Integer getHealth() {
        return health;
    }

    public void setHealth(Integer health) {
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
