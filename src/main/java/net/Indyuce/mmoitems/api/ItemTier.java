package net.Indyuce.mmoitems.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.droptable.DropTable;
import net.Indyuce.mmoitems.comp.itemglow.TierColor;

public class ItemTier {
	private final String name, id;
	private final UnidentificationInfo unidentificationInfo;

	/*
	 * item glow. color is an object because we cant let this class import the
	 * GlowAPI.Color enum since plugin is not a hard dependency.
	 */
	private TierColor color;
	private boolean hint;

	private DropTable deconstruct;

	private static final Random random = new Random();
	private static final boolean glow = Bukkit.getPluginManager().getPlugin("GlowAPI") != null;

	public ItemTier(ConfigurationSection config) {
		id = config.getName().toUpperCase().replace("-", "_");
		name = ChatColor.translateAlternateColorCodes('&', config.getString("name"));
		if (config.contains("deconstruct-item"))
			deconstruct = new DropTable(config.getConfigurationSection("deconstruct-item"));
		unidentificationInfo = new UnidentificationInfo(config.getConfigurationSection("unidentification"));

		if (config.contains("item-glow"))
			try {
				hint = config.getBoolean("item-glow.hint");
				color = new TierColor(config.getString("item-glow.color"), glow);
			} catch (NoClassDefFoundError | IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load tier color from '" + config.getString("item-glow.color") + "'");
			}
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

	public UnidentificationInfo getUnidentificationInfo() {
		return unidentificationInfo;
	}

	/*
	 * reads a random item in the drop table.
	 */
	public List<ItemStack> generateDeconstructedItem() {
		return hasDropTable() ? deconstruct.read(false) : new ArrayList<>();
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
		return ChatColor.translateAlternateColorCodes('&', str);
	}
}
