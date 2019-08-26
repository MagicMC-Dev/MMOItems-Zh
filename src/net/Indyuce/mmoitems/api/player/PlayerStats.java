package net.Indyuce.mmoitems.api.player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.stat.type.AttributeStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class PlayerStats {
	private final PlayerData playerData;

	private Map<String, StatInstance> stats = new HashMap<>();

	/*
	 * This is not a player data class. This class is used to temporarily save
	 * player stats in case of runnable-skills or item effects. This is to
	 * prevent player stats from changing when skills/effects are not INSTANTLY
	 * cast.
	 */
	public PlayerStats(PlayerData playerData) {
		this.playerData = playerData;
	}

	public PlayerData getPlayerData() {
		return playerData;
	}

	public void update() {
		stats.values().forEach(ins -> {
			ins.remove("item");
			ins.remove("fullSetBonus");
		});

		if (playerData.hasSetBonuses())
			for (Entry<ItemStat, Double> entry : playerData.getSetBonuses().getStats())
				getInstance(entry.getKey()).set("fullSetBonus", entry.getValue());

		for (ItemStat stat : MMOItems.plugin.getStats().getDoubleStats()) {
			double t = 0;

			for (MMOItem item : playerData.getMMOItems())
				t += item.getNBTItem().getStat(stat);

			if (t != 0)
				getInstance(stat).set("item", t);
		}

		updateAttributeModifiers();
	}

	public double getStat(ItemStat stat) {
		return getInstance(stat).getTotal();
	}

	public StatInstance getInstance(ItemStat stat) {
		if (stats.containsKey(stat.getId()))
			return stats.get(stat.getId());

		StatInstance ins = new StatInstance(stat);
		stats.put(stat.getId(), ins);
		return ins;
	}

	public void updateAttributeModifiers() {
		for (AttributeStat stat : MMOItems.plugin.getStats().getAttributeStats()) {
			AttributeInstance ins = playerData.getPlayer().getAttribute(stat.getAttribute());
			removeAttributeModifiers(ins);

			double value = getStat(stat);
			if (value != 0)
				ins.addModifier(new AttributeModifier(UUID.randomUUID(), "mmoitems." + stat.getId(), value - stat.getOffset(), Operation.ADD_NUMBER));
		}
	}

	private void removeAttributeModifiers(AttributeInstance ins) {
		for (Iterator<AttributeModifier> iterator = ins.getModifiers().iterator(); iterator.hasNext();) {
			AttributeModifier attribute = iterator.next();
			if (attribute.getName().startsWith("mmoitems."))
				ins.removeModifier(attribute);
		}
	}

	public TemporaryStats newTemporary() {
		return new TemporaryStats();
	}

	private Map<String, Double> mapCurrentStats() {
		Map<String, Double> map = new HashMap<>();
		stats.values().forEach(ins -> map.put(ins.getStat().getId(), ins.getTotal()));
		return map;
	}

	public class TemporaryStats {

		/*
		 * this field is made final so even when the player logs out, the
		 * ability can still be cast without any additional errors. this allows
		 * not to add a safe check in every ability loop.
		 */
		private final Player player;
		private final Map<String, Double> temp;

		public TemporaryStats() {
			temp = mapCurrentStats();
			player = playerData.getPlayer();
		}

		public PlayerData getPlayerData() {
			return playerData;
		}

		public Player getPlayer() {
			return player;
		}

		public double getStat(ItemStat stat) {
			return temp.containsKey(stat.getId()) ? temp.get(stat.getId()) : 0;
		}

		public boolean hasStat(ItemStat stat) {
			return temp.containsKey(stat.getId());
		}

		public void setStat(ItemStat stat, double value) {
			temp.put(stat.getId(), value);
		}
	}

	public class StatInstance {
		private final ItemStat stat;
		private final Map<String, Double> extra = new HashMap<>();

		public StatInstance(ItemStat stat) {
			this.stat = stat;
		}

		public ItemStat getStat() {
			return stat;
		}

		public double getTotal() {
			double t = 0;
			for (double d : extra.values())
				t += d;
			return t;
		}

		public double get(String source) {
			return extra.get(source);
		}

		public void set(String source, double value) {
			extra.put(source, value);
		}

		public void remove(String source) {
			extra.remove(source);
		}

		public boolean has(String source) {
			return extra.containsKey(source);
		}
	}
}
