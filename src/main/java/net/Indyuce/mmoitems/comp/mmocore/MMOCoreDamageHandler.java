package net.Indyuce.mmoitems.comp.mmocore;

import org.bukkit.entity.Entity;

import net.Indyuce.mmocore.comp.rpg.damage.DamageHandler;
import net.Indyuce.mmocore.comp.rpg.damage.DamageInfo;
import net.Indyuce.mmocore.comp.rpg.damage.DamageInfo.DamageType;
import net.Indyuce.mmoitems.MMOItems;

public class MMOCoreDamageHandler implements DamageHandler {

	@Override
	public DamageInfo getDamage(Entity entity) {
		net.Indyuce.mmoitems.manager.DamageManager.DamageInfo miInfo = MMOItems.plugin.getDamage().getDamage(entity);
		return new DamageInfo(DamageType.valueOf(miInfo.getType().name()), miInfo.getValue());
	}

	@Override
	public boolean hasDamage(Entity entity) {
		return MMOItems.plugin.getDamage().isDamaged(entity);
	}
}
