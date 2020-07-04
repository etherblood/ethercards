package com.etherblood.a.gui.prettycards;

import com.destrostudios.cardgui.BoardObjectModel;
import com.destrostudios.cardgui.annotations.IsBoardObjectInspected;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.updates.EffectiveStatsService;
import com.etherblood.a.templates.DisplayCardTemplate;
import com.jme3.math.ColorRGBA;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CardModel extends BoardObjectModel {

    private final int entityId;
    private final DisplayCardTemplate template;
    private Integer attack, health;
    private boolean damaged, foil;
    private ColorRGBA glow;
    private Set<String> keywords = new HashSet<>();
    private BoardZone zone;
    @IsBoardObjectInspected
    private boolean isInspected;

    public CardModel(int entityId, DisplayCardTemplate template) {
        this.entityId = entityId;
        this.template = Objects.requireNonNull(template);
    }

    public void updateFrom(EntityData data) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        if (data.has(entityId, core.IN_BATTLE_ZONE)) {
            setZone(BoardZone.BATTLE);
        } else if (data.has(entityId, core.IN_HAND_ZONE)) {
            setZone(BoardZone.HAND);
        } else if (data.has(entityId, core.IN_LIBRARY_ZONE)) {
            setZone(BoardZone.LIBRARY);
        } else if (data.has(entityId, core.IN_GRAVEYARD_ZONE)) {
            setZone(BoardZone.GRAVEYARD);
        } else {
            setZone(BoardZone.NONE);
        }

        EffectiveStatsService stats = new EffectiveStatsService(data);
        setFoil(data.has(entityId, core.HERO));

        if (data.has(entityId, core.ATTACK)) {
            setAttack(data.get(entityId, core.ATTACK));
        } else {
            setAttack(null);
        }
        if (data.has(entityId, core.HEALTH)) {
            int health = stats.health(entityId);
            setHealth(health);
            setDamaged(health < template.get(core.HEALTH));
        } else {
            setHealth(null);
            setDamaged(false);
        }
        Set<String> keywords = new HashSet<>();
        if (data.has(entityId, core.TRAMPLE)) {
            keywords.add("Trample");
        }
        if (data.has(entityId, core.LIFELINK)) {
            keywords.add("Lifelink");
        }
        if (data.has(entityId, core.VIGILANCE)) {
            keywords.add("Vigilance");
        }
        if (data.has(entityId, core.FLYING)) {
            keywords.add("Flying");
        }
        if (data.has(entityId, core.REACH)) {
            keywords.add("Reach");
        }
        if (stats.venom(entityId) != 0) {
            keywords.add("Venom_" + stats.venom(entityId));
        }
        if (data.has(entityId, core.POISONED)) {
            keywords.add("Poisoned_" + data.get(entityId, core.POISONED));
        }
        if (data.has(entityId, core.MANA_POOL)) {
            keywords.add("Mana_Pool_" + data.get(entityId, core.MANA_POOL));
        }
        if (data.has(entityId, core.MANA_GROWTH)) {
            keywords.add("Mana_Growth_" + data.get(entityId, core.MANA_GROWTH));
        }
        if (data.has(entityId, core.DRAWS_PER_TURN)) {
            keywords.add("Draws_per_Turn_" + data.get(entityId, core.DRAWS_PER_TURN));
        }
        if (data.has(entityId, core.DRAWS_ON_ATTACK)) {
            keywords.add("Draws_on_Attack_" + data.get(entityId, core.DRAWS_ON_ATTACK));
        }
        if (data.has(entityId, core.GIVE_DRAWS_ON_ATTACK)) {
            keywords.add("Opponent_Draws_on_Attack_" + data.get(entityId, core.GIVE_DRAWS_ON_ATTACK));
        }
        if (data.has(entityId, core.DRAWS_ON_BLOCK)) {
            keywords.add("Draws_on_Block_" + data.get(entityId, core.DRAWS_ON_BLOCK));
        }
        if (data.has(entityId, core.CANNOT_ATTACK)) {
            keywords.add("Cannot_attack");
        }
        if (data.has(entityId, core.CANNOT_BLOCK)) {
            keywords.add("Cannot_block");
        }
        if (data.has(entityId, core.CANNOT_BE_BLOCKED)) {
            keywords.add("Cannot_be_blocked");
        }
        if (data.has(entityId, core.OWN_MINIONS_HASTE_AURA)) {
            keywords.add("Haste_Aura");
        }
        if (stats.isFastToAttack(entityId) && stats.isFastToDefend(entityId)) {
            keywords.add("Haste");
        } else if (stats.isFastToAttack(entityId)) {
            keywords.add("Fast_Attacker");
        } else if (stats.isFastToDefend(entityId)) {
            keywords.add("Fast_Blocker");
        }
        if (data.has(entityId, core.FATIGUE)) {
            keywords.add("Fatigue_" + data.get(entityId, core.FATIGUE));
        }
        if (data.has(entityId, core.OWN_MINIONS_HEALTH_AURA)) {
            keywords.add("Health_Aura_" + data.get(entityId, core.OWN_MINIONS_HEALTH_AURA));
        }
        if (data.has(entityId, core.OWN_MINIONS_VENOM_AURA)) {
            keywords.add("Venom_Aura_" + data.get(entityId, core.OWN_MINIONS_VENOM_AURA));
        }
        setKeywords(keywords);
    }

    public boolean isInspected() {
        return isInspected;
    }

    public boolean isFaceUp() {
        return zone != BoardZone.LIBRARY;
    }

    public BoardZone getZone() {
        return zone;
    }

    private void setZone(BoardZone zone) {
        updateIfNotEquals(this.zone, zone, () -> this.zone = zone);
    }

    public boolean isFoil() {
        return foil;
    }

    private void setFoil(boolean foil) {
        updateIfNotEquals(this.foil, foil, () -> this.foil = foil);
    }

    public DisplayCardTemplate getTemplate() {
        return template;
    }

    public Set<String> getKeywords() {
        return Collections.unmodifiableSet(keywords);
    }

    private void setKeywords(Set<String> keywords) {
        Objects.requireNonNull(keywords);
        updateIfNotEquals(this.keywords, keywords, () -> this.keywords = keywords);
    }

    public int getEntityId() {
        return entityId;
    }

    public Integer getAttack() {
        return attack;
    }

    private void setAttack(Integer attack) {
        updateIfNotEquals(this.attack, attack, () -> this.attack = attack);
    }

    public Integer getHealth() {
        return health;
    }

    private void setHealth(Integer health) {
        updateIfNotEquals(this.health, health, () -> this.health = health);
    }

    private void setDamaged(boolean damaged) {
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
