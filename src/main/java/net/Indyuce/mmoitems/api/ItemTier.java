package net.Indyuce.mmoitems.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.droptable.DropTable;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.comp.itemglow.TierColor;
import net.mmogroup.mmolib.MMOLib;

public class ItemTier {
	private final String name, id;

	// unidentification
	private final UnidentificationInfo unidentificationInfo;

	// deconstruction
	private final DropTable deconstruct;

	// item glow options
	private final TierColor color;
	private final boolean hint;

	// item generation
	private final double chance;
	private final NumericStatFormula capacity;

	private static final Random random = new Random();
	private static final boolean glow = Bukkit.getPluginManager().getPlugin("GlowAPI") != null;

	public ItemTier(ConfigurationSection config) {
		id = config.getName().toUpperCase().replace("-", "_");
		name = MMOLib.plugin.parseColors(config.getString("name"));
		deconstruct = config.contains("deconstruct-item") ? new DropTable(config.getConfigurationSection("deconstruct-item")) : null;
		unidentificationInfo = new UnidentificationInfo(config.getConfigurationSection("unidentification"));

		try {
			hint = config.contains("item-glow") && config.getBoolean("item-glow.hint");
			color = config.contains("item-glow") ? new TierColor(config.getString("item-glow.color"), glow) : null;
		} catch (NoClassDefFoundError | IllegalAccessException | NoSuchFieldException | SecurityException exception) {
			throw new IllegalArgumentException("Could not load tier color: " + exception.getMessage());
		}

		chance = config.getDouble("generation.chance");
		capacity = config.contains("generation.capacity") ? new NumericStatFormula(config.getConfigurationSection("")) : null;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean hasDropTable() {
		return deconstruct != null;
	}

	public DropTable getDropTable() {
		return deconstruct;
	}

	public boolean hasColor() {
		return color != null;
	}

	public TierColor getColor() {
		return color;
	}

	public boolean isHintEnabled() {
		return hint;
	}

	/**
	 * @return The chance of being chosen when a random tier is selected while
	 *         calling an mmoitem template to generate an item.
	 */
	public double getGenerationChance() {
		return chance;
	}

	/**
	 * @return If the item tier has capacity ie if this tier can be applied onto
	 *         item templates.
	 */
	public boolean hasCapacity() {
		return capacity != null;
	}

	public NumericStatFormula getCapacity() {
		return capacity;
	}

	public UnidentificationInfo getUnidentificationInfo() {
		return unidentificationInfo;
	}

	/**
	 * @return Reads the deconstruction drop table. This may return a list
	 *         containing multiple items and they should all be added to the
	 *         player's inventory
	 */
	public List<ItemStack> getDeconstructedLoot(PlayerData player) {
		return hasDropTable() ? deconstruct.read(player, false) : new ArrayList<>();
	}

	public class UnidentificationInfo {
		private final String name, prefix;
		private final int range;

		public UnidentificationInfo(ConfigurationSection config) {
			this(color(config.getString("name")), color(config.getString("prefix")), config.getInt("range"));
		}

		public UnidentificationInfo(String name, String prefix, int range) {
			this.name = name;
			this.prefix = prefix;
			this.range = range;
		}

		public String getPrefix() {
			return prefix;
		}

		public String getDisplayName() {
			return name;
		}

		public int[] calculateRange(int level) {
			int min = (int) Math.max(1, (level - (double) range * random.nextDouble()));
			return new int[] { min, min + range };
		}
	}

	private String color(String str) {
		return MMOLib.plugin.parseColors(str);
	}
}
