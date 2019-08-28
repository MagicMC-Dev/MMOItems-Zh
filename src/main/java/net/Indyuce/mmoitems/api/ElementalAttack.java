package net.Indyuce.mmoitems.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;

public class ElementalAttack {
	private Map<Element, Double> relative = new HashMap<>();
	private Map<Element, Double> absolute = new HashMap<>();
	private AttackResult result;
	private LivingEntity target;

	private static final Random random = new Random();

	public ElementalAttack(NBTItem item, AttackResult result) {
		this.result = result;

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

	public ElementalAttack applyElementalArmor(LivingEntity target) {
		this.target = target;

		for (ItemStack equip : target.getEquipment().getArmorContents()) {
			NBTItem nbtEquip = MMOItems.plugin.getNMS().getNBTItem(equip);
			if (nbtEquip.getType() != null)
				for (Element element : absolute.keySet()) {
					double defense = nbtEquip.getStat(element.name() + "_DEFENSE") / 100;
					if (defense > 0) {
						relative.put(element, relative.get(element) * (1 - defense));
						absolute.put(element, absolute.get(element) * (1 - defense));
					}
				}
		}

		return this;
	}

	public void apply(TemporaryStats stats) {

		// elemental attacks
		double p = 1;
		if (!stats.getPlayerData().isOnCooldown(CooldownType.ELEMENTAL_ATTACK))
			for (Element element : relative.keySet()) {
				double damage = relative.get(element);
				if (random.nextDouble() < (damage / 100 / p)) {
					stats.getPlayerData().applyCooldown(CooldownType.ELEMENTAL_ATTACK, 2);
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
