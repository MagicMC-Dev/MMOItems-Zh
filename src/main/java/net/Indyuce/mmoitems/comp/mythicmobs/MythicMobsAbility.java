package net.Indyuce.mmoitems.comp.mythicmobs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.skills.Skill;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public class MythicMobsAbility extends Ability {
	private final Skill skill;
	private final boolean selfOnly;

	public MythicMobsAbility(String id, FileConfiguration config) {
		super(id, config.getString("name"), CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		String skillName = config.getString("mythicmobs-skill-id");
		Validate.notNull(skillName, "Could not find MM skill name");

		Optional<io.lumine.xikage.mythicmobs.skills.Skill> opt = MythicMobs.inst().getSkillManager().getSkill(skillName);
		Validate.isTrue(opt.isPresent(), "Could not find MM skill " + skillName);
		skill = opt.get();

		selfOnly = config.getBoolean("self-only");

		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);

		for (String mod : config.getKeys(false))
			if (!mod.equals("name") && !mod.equals("mythicmobs-skill-id") && !mod.equals("self-only"))
				addModifier(mod.toLowerCase().replace("_", "-").replace(" ", "-"), config.getInt(mod));
	}

	public String getInternalName() {
		return skill.getInternalName();
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		List<Entity> targets = new ArrayList<>();
		targets.add(target == null || selfOnly ? stats.getPlayer() : target);

		/*
		 * cache placeholders so they can be retrieved later by MythicMobs math
		 * formulas
		 */
		stats.getPlayerData().getAbilityData().cacheModifiers(this, data);

		if (!MythicMobs.inst().getAPIHelper().castSkill(stats.getPlayer(), skill.getInternalName(), stats.getPlayer(), stats.getPlayer().getEyeLocation(), targets, null, 1))
			result.setSuccessful(false);
	}
}
