package net.Indyuce.mmoitems.api.player;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.modifier.ModifierType;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type.EquipmentSlot;
import net.Indyuce.mmoitems.api.player.inventory.EquippedPlayerItem;
import net.Indyuce.mmoitems.stat.type.AttributeStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class PlayerStats {
	private final PlayerData playerData;

	public PlayerStats(PlayerData playerData) {
		this.playerData = playerData;
	}

	public PlayerData getData() {
		return playerData;
	}

	public StatMap getMap() {
		return playerData.getMMOPlayerData().getStatMap();
	}

	public double getStat(ItemStat stat) {
		return getInstance(stat).getTotal();
	}

	public StatInstance getInstance(ItemStat stat) {
		return getMap().getInstance(stat.getId());
	}

	public CachedStats newTemporary() {
		return new CachedStats();
	}

	public void updateStats() {
		getMap().getInstances().forEach(ins -> {
			ins.remove("MMOItem");
			ins.remove("MMOItemSetBonus");
		});

		if (playerData.hasSetBonuses())
			playerData.getSetBonuses().getStats()
					.forEach((stat, value) -> getInstance(stat).addModifier("MMOItemSetBonus", new StatModifier(value, ModifierType.FLAT)));

		for (ItemStat stat : MMOItems.plugin.getStats().getNumericStats()) {
			double sum = 0;

			/*
			 * If the player is holding a weapon granting a certain stat, the
			 * final stat value should be applied the attribute stat offset like
			 * 4 for attack speed or 1 for attack damage
			 */
			boolean hasWeapon = false;

			for (EquippedPlayerItem item : playerData.getInventory().getEquipped()) {
				double value = item.getItem().getNBT().getStat(stat.getId());
				if (value != 0) {
					sum += value;
					if (!hasWeapon && item.getSlot() == EquipmentSlot.MAIN_HAND)
						hasWeapon = true;
				}
			}

			if (sum != 0) {
				double offset = hasWeapon && stat instanceof AttributeStat ? ((AttributeStat) stat).getOffset() : 0;
				getInstance(stat).addModifier("MMOItem", new StatModifier(sum - offset, ModifierType.FLAT));
			}
		}
	}

	public class CachedStats {
		private final Player player;
		private final Map<String, Double> stats = new HashMap<>();

		/**
		 * Used to cache stats when a player casts a skill so that if the player
		 * swaps items or changes any of his stat value before the end of the
		 * spell duration, the stat value is not updated
		 */
		public CachedStats() {
			player = playerData.getPlayer();
			for (StatInstance ins : getMap().getInstances())
				this.stats.put(ins.getStat(), ins.getTotal());
		}

		public PlayerData getData() {
			return playerData;
		}

		public Player getPlayer() {
			return player;
		}

		public double getStat(ItemStat stat) {
			return stats.containsKey(stat.getId()) ? stats.get(stat.getId()) : 0;
		}

		public void setStat(ItemStat stat, double value) {
			stats.put(stat.getId(), value);
		}
	}
}
