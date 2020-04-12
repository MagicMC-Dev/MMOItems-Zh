package net.Indyuce.mmoitems.api.player;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.VolatileMMOItem;
import net.Indyuce.mmoitems.stat.type.AttributeStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.player.MMOData;
import net.mmogroup.mmolib.api.stat.StatInstance;
import net.mmogroup.mmolib.api.stat.StatMap;

public class PlayerStats {
	private final PlayerData playerData;
	private final StatMap map;

	public PlayerStats(PlayerData playerData) {
		this.playerData = playerData;

		map = MMOData.get(playerData.getPlayer()).setMMOItems(playerData).getStatMap();
	}

	public PlayerData getPlayerData() {
		return playerData;
	}

	public void update() {
		map.getInstances().forEach(ins -> {
			ins.remove("item");
			ins.remove("fullSetBonus");
		});

		if (playerData.hasSetBonuses())
			playerData.getSetBonuses().getStats().forEach((stat, value) -> getInstance(stat).addModifier("fullSetBonus", value));

		for (ItemStat stat : MMOItems.plugin.getStats().getNumericStats()) {
			double t = 0;

			for (VolatileMMOItem item : playerData.getMMOItems())
				t += item.getItem().getStat(stat.getId());

			if (t != 0)
				getInstance(stat).addModifier("item", t - (stat instanceof AttributeStat ? ((AttributeStat) stat).getOffset() : 0));
		}
	}

	public double getStat(ItemStat stat) {
		return getInstance(stat).getTotal();
	}

	public StatInstance getInstance(ItemStat stat) {
		return map.getInstance(stat.getId());
	}

	public CachedStats newTemporary() {
		return new CachedStats();
	}

	/*
	 * TODO merge this with MMOLib CachedStatMap
	 */
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
			for (StatInstance ins : map.getInstances())
				this.stats.put(ins.getStat(), ins.getTotal());
		}

		public PlayerData getPlayerData() {
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
