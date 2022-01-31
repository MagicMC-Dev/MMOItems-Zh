package net.Indyuce.mmoitems.stat.data;

import com.google.gson.JsonObject;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.skill.RegisteredSkill;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class AbilityData extends Skill {
	private final RegisteredSkill ability;
	private final TriggerType triggerType;
	private final Map<String, Double> modifiers = new HashMap<>();

	public AbilityData(JsonObject object) {
		ability = MMOItems.plugin.getSkills().getSkill(object.get("Id").getAsString());
		triggerType = MMOUtils.backwardsCompatibleTriggerType(object.get("CastMode").getAsString());

		JsonObject modifiers = object.getAsJsonObject("Modifiers");
		modifiers.entrySet().forEach(entry -> setModifier(entry.getKey(), entry.getValue().getAsDouble()));
	}

	public AbilityData(ConfigurationSection config) {
		Validate.isTrue(config.contains("type") && config.contains("mode"), "Ability is missing type or mode");

		String abilityFormat = config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_");
		Validate.isTrue(MMOItems.plugin.getSkills().hasSkill(abilityFormat), "Could not find ability called '" + abilityFormat + "'");
		ability = MMOItems.plugin.getSkills().getSkill(abilityFormat);

		String modeFormat = config.getString("mode").toUpperCase().replace("-", "_").replace(" ", "_");
		triggerType = MMOUtils.backwardsCompatibleTriggerType(modeFormat);

		for (String key : config.getKeys(false))
			if (!key.equalsIgnoreCase("mode") && !key.equalsIgnoreCase("type") && ability.getHandler().getModifiers().contains(key))
				modifiers.put(key, config.getDouble(key));
	}

	public AbilityData(RegisteredSkill ability, TriggerType triggerType) {
		this.ability = ability;
		this.triggerType = triggerType;
	}

	public RegisteredSkill getAbility() {
		return ability;
	}

	public TriggerType getTriggerType() {
		return triggerType;
	}

	public Set<String> getModifiers() {
		return modifiers.keySet();
	}

	public void setModifier(String path, double value) {
		// Validate.isTrue(getHandler().getModifiers().contains(path), "Could not find modifier called '" + path + "'");
		modifiers.put(path, value);
	}

	public boolean hasModifier(String path) {
		return modifiers.containsKey(path);
	}

	@NotNull
	@Override
	public boolean getResult(SkillMetadata meta) {

		PlayerData playerData = PlayerData.get(meta.getCaster().getData().getUniqueId());
		RPGPlayer rpgPlayer = playerData.getRPG();
		Player player = meta.getCaster().getPlayer();

		// Check for cooldown
		if (meta.getCaster().getData().getCooldownMap().isOnCooldown(this)) {
			CooldownInfo info = playerData.getMMOPlayerData().getCooldownMap().getInfo(this);
			if (!triggerType.isSilent()) {
				StringBuilder progressBar = new StringBuilder(ChatColor.YELLOW + "");
				double progress = (double) (info.getInitialCooldown() - info.getRemaining()) / info.getInitialCooldown() * 10;
				String barChar = MMOItems.plugin.getConfig().getString("cooldown-progress-bar-char");
				for (int j = 0; j < 10; j++)
					progressBar.append(progress >= j ? ChatColor.GREEN : ChatColor.WHITE).append(barChar);
				Message.SPELL_ON_COOLDOWN.format(ChatColor.RED, "#left#", "" + new DecimalFormat("0.#").format(info.getRemaining() / 1000d), "#progress#",
						progressBar.toString(), "#s#", (info.getRemaining() > 1999 ? "s" : "")).send(player);
			}
			return false;
		}

		// Check for permission
		if (MMOItems.plugin.getConfig().getBoolean("permissions.abilities")
				&& !player.hasPermission("mmoitems.ability." + getHandler().getLowerCaseId())
				&& !player.hasPermission("mmoitems.bypass.ability"))
			return false;

		// Check for mana cost
		if (hasModifier("mana") && rpgPlayer.getMana() < getModifier("mana")) {
			Message.NOT_ENOUGH_MANA.format(ChatColor.RED).send(player);
			return false;
		}

		// Check for stamina cost
		if (hasModifier("stamina") && rpgPlayer.getStamina() < getModifier("stamina")) {
			Message.NOT_ENOUGH_STAMINA.format(ChatColor.RED).send(player);
			return false;
		}

		return true;
	}

	@Override
	public void whenCast(SkillMetadata meta) {
		PlayerData playerData = PlayerData.get(meta.getCaster().getData().getUniqueId());
		RPGPlayer rpgPlayer = playerData.getRPG();

		// Apply mana cost
		if (hasModifier("mana"))
			rpgPlayer.giveMana(-meta.getModifier("mana"));

		// Apply stamina cost
		if (hasModifier("stamina"))
			rpgPlayer.giveStamina(-meta.getModifier("stamina"));

		// Apply cooldown
		double cooldown = meta.getModifier("cooldown") * (1 - Math.min(.8, meta.getCaster().getStat("COOLDOWN_REDUCTION") / 100));
		if (cooldown > 0)
			meta.getCaster().getData().getCooldownMap().applyCooldown(this, cooldown);
	}

	@Nullable
	@Override
	public SkillHandler getHandler() {
		if (ability == null) { return null; }
		return ability.getHandler();
	}

	@Override
	public double getModifier(String path) {
		return modifiers.getOrDefault(path, ability.getDefaultModifier(path));
	}

	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("Id", ability.getHandler().getId());
		object.addProperty("CastMode", triggerType.name());

		JsonObject modifiers = new JsonObject();
		this.modifiers.keySet().forEach(modifier -> modifiers.addProperty(modifier, getModifier(modifier)));
		object.add("Modifiers", modifiers);

		return object;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AbilityData that = (AbilityData) o;
		return ability.equals(that.ability) && triggerType == that.triggerType && modifiers.equals(that.modifiers);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ability, triggerType, modifiers);
	}
}