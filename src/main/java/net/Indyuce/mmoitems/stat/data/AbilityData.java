package net.Indyuce.mmoitems.stat.data;

import com.google.gson.JsonObject;
import io.lumine.mythic.lib.comp.mythicmobs.MythicSkillInfo;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.skill.trigger.TriggeredSkill;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.Ability;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AbilityData implements MythicSkillInfo, TriggeredSkill {
	private final Ability ability;
	private final TriggerType triggerType;
	private final Map<String, Double> modifiers = new HashMap<>();

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AbilityData)) { return false; }

		// Compare casitng mode
		if (((AbilityData) obj).getTriggerType() != getTriggerType()) { return false; }

		// Not same ability
		if (!((AbilityData) obj).getAbility().equals(getAbility())) { return false; }

		// Check modifiers
		for (String mod : ((AbilityData) obj).getModifiers()) {

			// Any difference?
			double objMod = ((AbilityData) obj).getModifier(mod);
			double thisMod = getModifier(mod);
			if (objMod != thisMod) { return false; } }

		// Success
		return true;
	}

	@Override
	public void execute(@Nullable TriggerMetadata triggerMetadata) {
		PlayerData playerData = PlayerData.get(triggerMetadata.getAttack().getPlayer().getUniqueId());
		playerData.cast(triggerMetadata.getAttack(), triggerMetadata.getTarget() instanceof LivingEntity ? (LivingEntity) triggerMetadata.getTarget() : null, this);
	}

	public AbilityData(JsonObject object) {
		ability = MMOItems.plugin.getAbilities().getAbility(object.get("Id").getAsString());
		triggerType = MMOUtils.backwardsCompatibleTriggerType(object.get("CastMode").getAsString());

		JsonObject modifiers = object.getAsJsonObject("Modifiers");
		modifiers.entrySet().forEach(entry -> setModifier(entry.getKey(), entry.getValue().getAsDouble()));
	}

	public AbilityData(ConfigurationSection config) {
		Validate.isTrue(config.contains("type") && config.contains("mode"), "Ability is missing type or mode");

		String abilityFormat = config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_");
		Validate.isTrue(MMOItems.plugin.getAbilities().hasAbility(abilityFormat), "Could not find ability called '" + abilityFormat + "'");
		ability = MMOItems.plugin.getAbilities().getAbility(abilityFormat);

		String modeFormat = config.getString("mode").toUpperCase().replace("-", "_").replace(" ", "_");
		triggerType = MMOUtils.backwardsCompatibleTriggerType(modeFormat);

		for (String key : config.getKeys(false))
			if (!key.equalsIgnoreCase("mode") && !key.equalsIgnoreCase("type") && ability.getModifiers().contains(key))
				modifiers.put(key, config.getDouble(key));
	}

	public AbilityData(Ability ability, TriggerType triggerType) {
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

	public void setModifier(String path, double value) {
		modifiers.put(path, value);
	}

	public boolean hasModifier(String path) {
		return modifiers.containsKey(path);
	}

	@Override
	public double getModifier(String path) {
		return modifiers.getOrDefault(path, ability.getDefaultValue(path));
	}

	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("Id", ability.getID());
		object.addProperty("CastMode", triggerType.name());

		JsonObject modifiers = new JsonObject();
		this.modifiers.keySet().forEach(modifier -> modifiers.addProperty(modifier, getModifier(modifier)));
		object.add("Modifiers", modifiers);

		return object;
	}
}