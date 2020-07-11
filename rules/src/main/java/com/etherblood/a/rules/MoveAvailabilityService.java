package com.etherblood.a.rules;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.targeting.TargetUtil;
import com.etherblood.a.rules.updates.EffectiveStatsService;
import java.util.OptionalInt;

public class MoveAvailabilityService {

    private final EntityData data;
    private final CoreComponents core;
    private final GameTemplates templates;
    private final EffectiveStatsService effectiveStats;

    public MoveAvailabilityService(EntityData data, GameTemplates templates) {
        this.data = data;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.templates = templates;
        this.effectiveStats = new EffectiveStatsService(data, templates);
    }

    public boolean canStart(boolean throwOnFail) {
        if (!data.list(core.ACTIVE_PLAYER_PHASE).isEmpty()) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to start game, there are already active players.");
            }
            return false;
        }
        if (!data.list(core.PLAYER_RESULT).isEmpty()) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to start game, at least one player already won or lost.");
            }
            return false;
        }
        return true;
    }

    public boolean canEndAttackPhase(int player, boolean throwOnFail) {
        if (!data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to end attack phase, player #" + player + " is not in attack phase.");
            }
            return false;
        }
        return true;
    }

    public boolean canEndBlockPhase(int player, boolean throwOnFail) {
        if (!data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to end block phase, player #" + player + " is not in block phase.");
            }
            return false;
        }
        return true;
    }

    public boolean canEndMulliganPhase(int player, boolean throwOnFail) {
        if (!data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.MULLIGAN)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to end mulligan phase, player #" + player + " is not in mulligan phase.");
            }
            return false;
        }
        return true;
    }

    public boolean canDeclareAttack(int player, int attacker, int target, boolean throwOnFail) {
        if (!data.has(target, core.IN_BATTLE_ZONE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, target #" + target + " is not in battle zone.");
            }
            return false;
        }
        if (data.has(target, core.FLYING) && !data.has(attacker, core.FLYING) && !data.has(attacker, core.REACH)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, target #" + target + " is flying.");
            }
            return false;
        }
        if (data.hasValue(target, core.OWNED_BY, player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, target #" + target + " is owned by attacker.");
            }
            return false;
        }
        if (data.has(target, core.CANNOT_BE_ATTACKED)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, target #" + target + " cannot be attacked.");
            }
            return false;
        }
        return canDeclareAttack(player, attacker, throwOnFail);
    }

    public boolean canDeclareAttack(int player, int attacker, boolean throwOnFail) {
        if (!data.hasValue(attacker, core.OWNED_BY, player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, player #" + player + " does not own attacker #" + attacker + ".");
            }
            return false;
        }
        if (!data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, player #" + player + " is not in attack phase.");
            }
            return false;
        }
        if (!data.has(attacker, core.IN_BATTLE_ZONE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, attacker #" + attacker + " is not in battle zone.");
            }
            return false;
        }
        if (data.has(attacker, core.CANNOT_ATTACK)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, attacker #" + attacker + " can not attack.");
            }
            return false;
        }
        if (data.has(attacker, core.SUMMONING_SICKNESS) && !effectiveStats.isFastToAttack(attacker)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, attacker #" + attacker + " has summoning sickness.");
            }
            return false;
        }
        if (data.has(attacker, core.TIRED)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, attacker #" + attacker + " is tired.");
            }
            return false;
        }
        if (data.has(attacker, core.ATTACKS_TARGET)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, attacker #" + attacker + " is already attacking.");
            }
            return false;
        }
        return true;
    }

    public boolean canDeclareBlock(int player, int blocker, int attacker, boolean throwOnFail) {
        if (!data.has(attacker, core.ATTACKS_TARGET)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, attacker #" + attacker + " is not attacking.");
            }
            return false;
        }
        if (data.has(attacker, core.CANNOT_BE_BLOCKED)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, attacker #" + attacker + " can not be blocked.");
            }
            return false;
        }
        if (data.has(attacker, core.FLYING) && !data.has(blocker, core.FLYING) && !data.has(blocker, core.REACH)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, target #" + attacker + " is flying.");
            }
            return false;
        }
        return canDeclareBlock(player, blocker, throwOnFail);
    }

    public boolean canDeclareBlock(int player, int blocker, boolean throwOnFail) {
        if (!data.hasValue(blocker, core.OWNED_BY, player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, player #" + player + " does not own blocker #" + blocker + ".");
            }
            return false;
        }
        if (!data.has(blocker, core.IN_BATTLE_ZONE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " is not in battle zone.");
            }
            return false;
        }
        if (!data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, player #" + player + " is not in block phase.");
            }
            return false;
        }
        if (data.has(blocker, core.SUMMONING_SICKNESS) && !effectiveStats.isFastToDefend(blocker)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " has summoning sickness.");
            }
            return false;
        }
        if (data.has(blocker, core.TIRED)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " is tired.");
            }
            return false;
        }
        if (data.has(blocker, core.BLOCKS_ATTACKER)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " is already blocking.");
            }
            return false;
        }
        if (data.has(blocker, core.CANNOT_BLOCK)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " can not block.");
            }
            return false;
        }
        boolean allAttackersUnblockable = true;
        for (int attacker : data.list(core.ATTACKS_TARGET)) {
            int target = data.get(attacker, core.ATTACKS_TARGET);
            if (target == blocker) {
                if (throwOnFail) {
                    throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " is being attacked.");
                }
                return false;
            }
            if (allAttackersUnblockable && !data.has(attacker, core.CANNOT_BE_BLOCKED)) {
                allAttackersUnblockable = false;
            }
        }
        if (allAttackersUnblockable) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, nobody is attacking.");
            }
            return false;
        }
        return true;
    }

    public boolean canCast(int player, int castable, boolean throwOnFail) {
        if (!data.hasValue(castable, core.OWNED_BY, player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to cast, player #" + player + " does not own castable #" + castable + ".");
            }
            return false;
        }
        if (!data.has(castable, core.IN_HAND_ZONE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to cast, castable #" + castable + " is not in hand zone.");
            }
            return false;
        }
        CardTemplate template = templates.getCard(data.get(castable, core.CARD_TEMPLATE));
        CardCast cast;
        OptionalInt phase = data.getOptional(player, core.ACTIVE_PLAYER_PHASE);
        if (phase.isPresent()) {
            switch (phase.getAsInt()) {
                case PlayerPhase.ATTACK:
                    cast = template.getAttackPhaseCast();
                    if (cast == null) {
                        if (throwOnFail) {
                            throw new IllegalArgumentException("Failed to cast, castable #" + castable + " cannot be cast in attack phase.");
                        }
                        return false;
                    }
                    break;
                case PlayerPhase.BLOCK:
                    cast = template.getBlockPhaseCast();
                    if (cast == null) {
                        if (throwOnFail) {
                            throw new IllegalArgumentException("Failed to cast, castable #" + castable + " cannot be cast in block phase.");
                        }
                        return false;
                    }
                    break;
                default:
                    if (throwOnFail) {
                        throw new IllegalArgumentException("Failed to cast, casting is not possible during current phase.");
                    }
                    return false;
            }
        } else {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to cast, player #" + player + " is not the active player.");
            }
            return false;
        }
        if (template.getManaCost() != null && template.getManaCost() > data.getOptional(player, core.MANA).orElse(0)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to cast, castable #" + castable + ", player #" + player + " does not have enough mana.");
            }
            return false;
        }
        return true;
    }

    public boolean canCast(int player, int castable, int target, boolean throwOnFail) {
        if (data.has(target, core.CANNOT_BE_CAST_TARGETED)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to cast, target #" + target + " cannot be targeted.");
            }
            return false;
        }
        CardTemplate template = templates.getCard(data.get(castable, core.CARD_TEMPLATE));
        OptionalInt phase = data.getOptional(player, core.ACTIVE_PLAYER_PHASE);
        if (phase.isPresent()) {
            CardCast cast;
            if (phase.getAsInt() == PlayerPhase.ATTACK) {
                cast = template.getAttackPhaseCast();
                if (cast != null && cast.isTargeted() && !TargetUtil.isValidTarget(data, castable, target, cast.getTargets())) {
                    if (throwOnFail) {
                        throw new IllegalArgumentException("Failed to cast, target #" + target + " is not valid.");
                    }
                    return false;
                }
            } else if (phase.getAsInt() == PlayerPhase.BLOCK) {
                cast = template.getBlockPhaseCast();
                if (cast != null && cast.isTargeted() && !TargetUtil.isValidTarget(data, castable, target, cast.getTargets())) {
                    if (throwOnFail) {
                        throw new IllegalArgumentException("Failed to cast, target #" + target + " is not valid.");
                    }
                    return false;
                }
            }
        }
        return canCast(player, castable, throwOnFail);
    }

    public boolean canDeclareMulligan(int player, int card, boolean throwOnFail) {
        if (!data.hasValue(card, core.OWNED_BY, player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare mulligan, player #" + player + " does not own card #" + card + ".");
            }
            return false;
        }
        if (!data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.MULLIGAN)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare mulligan, player #" + player + " is not in mulligan phase.");
            }
            return false;
        }
        if (!data.has(card, core.IN_HAND_ZONE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare mulligan, card #" + card + " is not in hand zone.");
            }
            return false;
        }
        if (data.has(card, core.MULLIGAN)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare mulligan, card #" + card + " is already mulliganed.");
            }
            return false;
        }
        int requiredCards = 1;
        for (int mulliganedCard : data.list(core.MULLIGAN)) {
            if (data.hasValue(mulliganedCard, core.OWNED_BY, player)) {
                requiredCards++;
            }
        }
        for (int availableCard : data.list(core.IN_LIBRARY_ZONE)) {
            if (data.hasValue(availableCard, core.OWNED_BY, player)) {
                requiredCards--;
            }
        }
        if (requiredCards > 0) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare mulligan, player #" + player + " does not have enough remaining cards in their library.");
            }
            return false;
        }

        return true;
    }

    public boolean canSurrender(int player, boolean throwOnFail) {
        if (hasPlayerLost(player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to surrender, player #" + player + " already lost.");
            }
            return false;
        }
        if (hasPlayerWon(player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to surrender, player #" + player + " already won.");
            }
            return false;
        }
        return true;
    }

    private boolean hasPlayerWon(int player) {
        return data.hasValue(player, core.PLAYER_RESULT, PlayerResult.WIN);
    }

    private boolean hasPlayerLost(int player) {
        return data.hasValue(player, core.PLAYER_RESULT, PlayerResult.LOSS);
    }
}
