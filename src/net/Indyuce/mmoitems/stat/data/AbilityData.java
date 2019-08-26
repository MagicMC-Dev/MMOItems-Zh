package net.Indyuce.mmoitems.stat.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import com.google.gson.JsonObject;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.Ability.CastingMode;
import net.Indyuce.mmoitems.api.item.MMOItem;

public class AbilityData extends StatData {
	private Ability ability;
	private CastingMode castMode;
	private Map<String, Double> modifiers = new HashMap<>();

	public AbilityData() {
	}

	public AbilityData(JsonObject object) {
		ability = MMOItems.plugin.getAbilities().getAbility(object.get("Id").getAsString());
		castMode = CastingMode.valueOf(object.get("CastMode").getAsString());

		JsonObject modifiers = object.getAsJsonObject("Modifiers");
		modifiers.entrySet().forEach(entry -> setModifier(entry.getKey(), entry.getValue().getAsDouble()));
	}

	public AbilityData(MMOItem mmoitem, ConfigurationSection config) {
		setMMOItem(mmoitem);

		if (!config.contains("type") || !config.contains("mode")) {
			throwError("Ability is missing type or mode.");
			return;
		}

		String abilityFormat = config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_");
		if (!MMOItems.plugin.getAbilities().hasAbility(abilityFormat)) {
			throwError(abilityFormat + " is not a valid ability ID.");
			return;
		}

		ability = MMOItems.plugin.getAbilities().getAbility(abilityFormat);

		String modeFormat = config.getString("mode").toUpperCase().replace("-", "_").replace(" ", "_");
		try {
			castMode = CastingMode.valueOf(modeFormat);
		} catch (Exception e) {
			throwError(modeFormat + " is not a valid casting mode.");
			return;
		}

		if (!ability.isAllowedMode(castMode)) {
			throwError(ability.getID() + " does not support " + castMode.name() + ".");
			return;
		}

		for (String key : config.getKeys(false))
			if (!key.equalsIgnoreCase("mode") && !key.equalsIgnoreCase("type") && ability.getModifiers().contains(key))
				modifiers.put(key, config.getDouble(key));
	}

	public AbilityData(Ability ability) {
		this.ability = ability;
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

	public void setCastingMode(CastingMode castMode) {
		this.castMode = castMode;
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