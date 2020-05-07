package net.Indyuce.mmoitems.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;

public class ElementalAttack {
	private final Map<Element, Double> relative = new HashMap<>();
	private final Map<Element, Double> absolute = new HashMap<>();
	private final ItemAttackResult result;
	private final LivingEntity target;

	private static final Random random = new Random();

	public ElementalAttack(NBTItem item, ItemAttackResult result, LivingEntity target) {
		this.result = result;
		this.target = target;

		for (Element element : Element.values()) {
			double damage = item.getStat(element.name() + "_DAMAGE");
			if (damage > 0) {
				relative.put(element, damage);

				double abs = damage / 100 * result.getDamage();
				result.addDamage(-abs);
				absolute.put(element, abs);
			}
		}
	}

	public void apply(CachedStats stats) {

		// elemental defense
		for (ItemStack equip : target.getEquipment().getArmorContents()) {
			NBTItem nbtEquip = MMOLib.plugin.getNMS().getNBTItem(equip);
			if (nbtEquip.getType() != null)
				for (Element element : absolute.keySet()) {
					double defense = nbtEquip.getStat(element.name() + "_DEFENSE") / 100;
					if (defense > 0) {
						relative.put(element, relative.get(element) * (1 - defense));
						absolute.put(element, absolute.get(element) * (1 - defense));
					}
				}
		}

		// elemental attacks
		double p = 1;
		if (!stats.getData().isOnCooldown(CooldownType.ELEMENTAL_ATTACK))
			for (Element element : relative.keySet()) {
				double damage = relative.get(element);
				if (random.nextDouble() < (damage / 100 / p)) {
					stats.getData().applyCooldown(CooldownType.ELEMENTAL_ATTACK, 2);
					element.getHandler().elementAttack(stats, result, target, damage, absolute.get(element));
					break;
				}
				p -= damage / 100;
			}

		for (Element element : absolute.keySet()) {
			double damage = absolute.get(element);
			if (damage > 0) {
				result.addDamage(damage);
				element.getParticle().displayParticle(target);
			}
		}
	}
}
