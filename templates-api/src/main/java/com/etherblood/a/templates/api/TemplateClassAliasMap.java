package com.etherblood.ethercards.templates.api;

import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.rules.templates.StatModifier;
import com.etherblood.ethercards.rules.templates.TargetSelection;
import java.util.Map;


public interface TemplateClassAliasMap {

    Map<String, Class<? extends Effect>> getEffects();

    Map<String, Class<? extends TargetPredicate>> getTargetPredicates();

    Map<String, Class<? extends StatModifier>> getStatModifiers();

    Map<String, Class<? extends TargetSelection>> getTargetSelections();

}
