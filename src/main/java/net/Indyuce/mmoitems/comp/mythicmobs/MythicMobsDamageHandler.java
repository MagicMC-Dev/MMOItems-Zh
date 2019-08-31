package net.Indyuce.mmoitems.comp.mythicmobs;

import org.bukkit.entity.Entity;

import net.Indyuce.mmoitems.api.AttackResult.DamageType;
import net.Indyuce.mmoitems.comp.rpg.damage.DamageHandler;
import net.Indyuce.mmoitems.comp.rpg.damage.DamageInfo;

public class MythicMobsDamageHandler implements DamageHandler {

	@Override
	public DamageInfo getDamage(Entity entity) {
		return new DamageInfo(0, DamageType.MAGICAL);
	}

	@Override
	public boolean hasDamage(Entity entity) {
		return entity.hasMetadata("skill-damage");
	}
}
