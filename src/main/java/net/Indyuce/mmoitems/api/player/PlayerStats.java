package net.Indyuce.mmoitems.api.player;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.modifier.ModifierSource;
import io.lumine.mythic.lib.api.stat.modifier.ModifierType;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.player.inventory.EquippedPlayerItem;
import net.Indyuce.mmoitems.stat.type.AttributeStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

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
		return getMap().getInstance(stat.getId()).getTotal();
	}

	public StatInstance getInstance(ItemStat stat) {
		return getMap().getInstance(stat.getId());
	}

	/**
	 * Used to cache stats when a player casts a skill so that if the player
	 * swaps items or changes any of his stat value before the end of the
	 * spell duration, the stat value is not updated.
	 *
	 * @ignored Every stat modifier with that modifier source
	 * will be ignored when calculating the total stat value
	 */
	public CachedStats newTemporary(EquipmentSlot castSlot) {
		return new CachedStats(castSlot);
	}

	public void updateStats() {
		getMap().getInstances().forEach(ins -> ins.removeIf(name -> name.startsWith("MMOItem")));

		if (playerData.hasSetBonuses())
			playerData.getSetBonuses().getStats()
					.forEach((stat, value) -> getInstance(stat).addModifier("MMOItemSetBonus", new StatModifier(value, ModifierType.FLAT, EquipmentSlot.OTHER, ModifierSource.OTHER)));

		for (EquippedPlayerItem item : playerData.getInventory().getEquipped()) {
			Type type = item.getItem().getType();
			ModifierSource source = type == null ? ModifierSource.OTHER : type.getItemSet().getModifierSource();
			System.out.println("Inv -> " + source + " " + item.getSlot());
		}

		for (ItemStat stat : MMOItems.plugin.getStats().getNumericStats()) {

			/**
			 * Lets MMOItems first add stat modifiers and then update the stat instance
			 */
			StatInstance.ModifierPacket packet = getInstance(stat).newPacket();

			/**
			 * Some stats including Atk Damage and Speed have stat offsets, when equipping
			 * at least one item which grants this stat the final value must be lowered
			 * by a flat amount
			 */
			boolean mainHand = false;

			/**
			 * The index of the mmoitem stat modifier being added
			 */
			int index = 0;

			for (EquippedPlayerItem item : playerData.getInventory().getEquipped()) {
				double value = item.getItem().getNBT().getStat(stat.getId());

				if (value != 0) {
					Type type = item.getItem().getType();
					ModifierSource source = type == null ? ModifierSource.OTHER : type.getItemSet().getModifierSource();

					packet.addModifier("MMOItem-" + index++, new StatModifier(value, ModifierType.FLAT, item.getSlot(), source));
					if (!mainHand && item.getSlot() == EquipmentSlot.MAIN_HAND)
						mainHand = true;
				}
			}

			if (mainHand && stat instanceof AttributeStat)
				packet.addModifier("MMOItemOffset", new StatModifier(-((AttributeStat) stat).getOffset()));

			/**
			 * Finally run a stat update
			 */
			packet.runUpdate();
		}
	}


	public class CachedStats {
		private final Player player;
		private final Map<String, Double> stats = new HashMap<>();

		/**
		 * Used to cache stats when a player casts a skill so that if the player
		 * swaps items or changes any of his stat value before the end of the
		 * spell duration, the stat value is not updated
		 *
		 * @castSlot The equipment slot of the item the player is casting
		 * a skill/attacking with. Helps determine what stats modifiers needs to be
		 * applied and what modifiers must be filtered
		 */
		public CachedStats(EquipmentSlot castSlot) {
			player = playerData.getPlayer();

			if (castSlot.isHand()) {

				/**
				 * When casting a skill or an attack with a certain hand, stats from the
				 * other hand shouldn't be taken into account
				 */
				EquipmentSlot ignored = castSlot.getOppositeHand();
				for (StatInstance ins : getMap().getInstances())
					this.stats.put(ins.getStat(), ins.getFilteredTotal(mod -> mod.getSlot() != ignored));
			} else

			/**
			 * Not casting the attack with a specific hand so take everything into account
			 */
				for (StatInstance ins : getMap().getInstances())
					this.stats.put(ins.getStat(), ins.getTotal());
		}

		/*public CachedStats() {
			player = playerData.getPlayer();
			for (StatInstance ins : getMap().getInstances())
				this.stats.put(ins.getStat(), ins.getTotal());
		}*/

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
