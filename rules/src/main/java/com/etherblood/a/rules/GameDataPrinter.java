package com.etherblood.a.rules;

import com.etherblood.a.rules.moves.DeclareBlock;
import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.DeclareMulligan;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.rules.moves.EndMulliganPhase;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.rules.moves.Start;
import com.etherblood.a.rules.moves.Surrender;
import com.etherblood.a.rules.moves.Update;
import com.etherblood.a.rules.templates.CardTemplate;

public class GameDataPrinter {

    private final Game game;
    private final CoreComponents core;

    public GameDataPrinter(Game game) {
        this.game = game;
        this.core = game.getData().getComponents().getModule(CoreComponents.class);
    }

    public String toMoveString(Move move) {
        if (move instanceof EndBlockPhase) {
            return "End BlockPhase";
        }
        if (move instanceof EndAttackPhase) {
            return "End AttackPhase";
        }
        if (move instanceof EndMulliganPhase) {
            return "End MulliganPhase";
        }
        if (move instanceof DeclareMulligan) {
            DeclareMulligan mulligan = (DeclareMulligan) move;
            return "DeclareMulligan " + toCardString(mulligan.card);
        }
        if (move instanceof Cast) {
            Cast cast = (Cast) move;
            return "Cast " + toCardString(cast.source) + " -> " + toMinionString(cast.target);
        }
        if (move instanceof DeclareBlock) {
            DeclareBlock block = (DeclareBlock) move;
            return "DeclareBlock " + toMinionString(block.source) + " -> " + toMinionString(block.target);
        }
        if (move instanceof DeclareAttack) {
            DeclareAttack attack = (DeclareAttack) move;
            return "DeclareAttack " + toMinionString(attack.source) + " -> " + toMinionString(attack.target);
        }
        if (move instanceof Start) {
            return "Start";
        }
        if (move instanceof Update) {
            return "Update";
        }
        if (move instanceof Surrender) {
            return "Surrender";
        }
        return String.valueOf(move);
    }

    public String toMinionString(int minion) {
        if (!game.getData().has(minion, core.CARD_TEMPLATE)) {
            return "Null";
        }
        int templateId = game.getData().get(minion, core.CARD_TEMPLATE);
        CardTemplate template = game.getTemplates().getCard(templateId);
        return "#" + minion + " " + template.getTemplateName() + " (" + game.getData().getOptional(minion, core.ATTACK).orElse(0) + ", " + game.getData().getOptional(minion, core.HEALTH).orElse(0) + ")";
    }

    public String toCardString(int card) {
        int templateId = game.getData().get(card, core.CARD_TEMPLATE);
        CardTemplate template = game.getTemplates().getCard(templateId);
        return "#" + card + " " + template.getTemplateName();
    }
}
