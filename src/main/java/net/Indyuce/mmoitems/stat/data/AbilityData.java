package net.Indyuce.mmoitems.stat.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import com.google.gson.JsonObject;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.Ability.CastingMode;

public class AbilityData {
	private final Ability ability;
	private final CastingMode castMode;
	private final Map<String, Double> modifiers = new HashMap<>();

	public AbilityData(JsonObject object) {
		ability = MMOItems.plugin.getAbilities().getAbility(object.get("Id").getAsString());
		castMode = CastingMode.valueOf(object.get("CastMode").getAsString());

		JsonObject modifiers = object.getAsJsonObject("Modifiers");
		modifiers.entrySet().forEach(entry -> setModifier(entry.getKey(), entry.getValue().getAsDouble()));
	}

	public AbilityData(ConfigurationSection config) {
		Validate.isTrue(config.contains("type") && config.contains("mode"), "Ability is missing type or mode");

		String abilityFormat = config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_");
		Validate.isTrue(MMOItems.plugin.getAbilities().hasAbility(abilityFormat), "Could not find ability called '" + abilityFormat + "'");
		ability = MMOItems.plugin.getAbilities().getAbility(abilityFormat);

		String modeFormat = config.getString("mode").toUpperCase().replace("-", "_").replace(" ", "_");
		castMode = CastingMode.valueOf(modeFormat);

		Validate.isTrue(ability.isAllowedMode(castMode), "Ability " + ability.getID() + " does not support cast mode " + castMode.name());

		for (String key : config.getKeys(false))
			if (!key.equalsIgnoreCase("mode") && !key.equalsIgnoreCase("type") && ability.getModifiers().contains(key))
				modifiers.put(key, config.getDouble(key));
	}

	public AbilityData(Ability ability, CastingMode castMode) {
		this.ability = ability;
		this.castMode = castMode;
	}

	public Ability getAbility() {
		return ability;
	}

	public CastingMode getCastingMode() {
		return castMode;
	}

	public Set<String> getModifiers() {
		return modifiers.keySet();
	}

	public void setModifier(String path, double value) {
		modifiers.put(path, value);
	}

	public boolean hasModifier(String path) {
		return modifiers.containsKey(path);
	}

	public double getModifier(String path) {
		return modifiers.containsKey(path) ? modifiers.get(path) : ability.getDefaultValue(path);
	}

	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("Id", ability.getID());
		object.addProperty("CastMode", castMode.name());

		JsonObject modifiers = new JsonObject();
		this.modifiers.keySet().forEach(modifier -> modifiers.addProperty(modifier, getModifier(modifier)));
		object.add("Modifiers", modifiers);

		return object;
	}
}