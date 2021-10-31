package net.Indyuce.mmoitems.stat.data.random;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.lumine.mythic.lib.skill.trigger.TriggerType;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ability.Ability;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public class RandomAbilityData {
	private final Ability ability;
	private final TriggerType triggerType;
	private final Map<String, NumericStatFormula> modifiers = new HashMap<>();

	public RandomAbilityData(ConfigurationSection config) {
		Validate.isTrue(config.contains("type") && config.contains("mode"), "Ability is missing type or mode");

		String abilityFormat = config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_");
		Validate.isTrue(MMOItems.plugin.getAbilities().hasAbility(abilityFormat), "Could not find ability called '" + abilityFormat + "'");
		ability = MMOItems.plugin.getAbilities().getAbility(abilityFormat);

		String modeFormat = config.getString("mode").toUpperCase().replace("-", "_").replace(" ", "_");
		triggerType = TriggerType.valueOf(modeFormat);

		for (String key : config.getKeys(false))
			if (!key.equalsIgnoreCase("mode") && !key.equalsIgnoreCase("type") && ability.getModifiers().contains(key))
				modifiers.put(key, new NumericStatFormula(config.get(key)));
	}

	public RandomAbilityData(Ability ability, TriggerType triggerType) {
		this.ability = ability;
		this.triggerType = triggerType;
	}

	public Ability getAbility() {
		return ability;
	}

	public TriggerType getTriggerType() {
		return triggerType;
	}

	public Set<String> getModifiers() {
		return modifiers.keySet();
	}

	public void setModifier(String path, NumericStatFormula value) {
		modifiers.put(path, value);
	}

	public boolean hasModifier(String path) {
		return modifiers.containsKey(path);
	}

	public NumericStatFormula getModifier(String path) {
		return modifiers.get(path);
	}

	public AbilityData randomize(MMOItemBuilder builder) {
		AbilityData data = new AbilityData(ability, triggerType);
		modifiers.forEach((key, formula) -> data.setModifier(key, formula.calculate(builder.getLevel())));
		return data;
	}
}