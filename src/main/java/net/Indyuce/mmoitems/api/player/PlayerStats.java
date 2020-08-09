package net.Indyuce.mmoitems.api.player;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.stat.type.AttributeStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.stat.StatInstance;
import net.mmogroup.mmolib.api.stat.StatMap;
import net.mmogroup.mmolib.api.stat.modifier.ModifierType;
import net.mmogroup.mmolib.api.stat.modifier.StatModifier;

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
			ins.remove("item");
			ins.remove("fullSetBonus");
		});

		if (playerData.hasSetBonuses())
			playerData.getSetBonuses().getStats()
					.forEach((stat, value) -> getInstance(stat).addModifier("fullSetBonus", new StatModifier(value, ModifierType.FLAT)));

		for (ItemStat stat : MMOItems.plugin.getStats().getNumericStats()) {
			double t = 0;

			for (VolatileMMOItem item : playerData.getMMOItems())
				t += item.getNBT().getStat(stat.getId());

			if (t != 0)
				getInstance(stat).addModifier("item",
						new StatModifier(t - (stat instanceof AttributeStat ? ((AttributeStat) stat).getOffset() : 0), ModifierType.FLAT));
		}
	}

	public class CachedStats {

		/*
		 * this field is made final so even when the player logs out, the
		 * ability can still be cast without any additional errors. this allows
		 * not to add a safe check in every ability loop.
		 */
		private final Player player;

		private final Map<String, Double> stats = new HashMap<>();

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
