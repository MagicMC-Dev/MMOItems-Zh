package net.Indyuce.mmoitems.comp.rpg.damage;

import org.bukkit.entity.Entity;

public interface DamageHandler {
	public DamageInfo getDamage(Entity entity);

	public boolean hasDamage(Entity entity);
}
