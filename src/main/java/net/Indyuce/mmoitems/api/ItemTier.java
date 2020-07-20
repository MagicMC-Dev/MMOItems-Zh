package net.Indyuce.mmoitems.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.droptable.DropTable;
import net.Indyuce.mmoitems.comp.itemglow.TierColor;
import net.asangarin.hexcolors.ColorParse;

public class ItemTier {
	private final String name, id;
	private final UnidentificationInfo unidentificationInfo;
	private final DropTable deconstruct;

	/*
	 * item glow. color is an object because we cant let this class import the
	 * GlowAPI.Color enum since plugin is not a hard dependency.
	 */
	private final TierColor color;
	private final boolean hint;

	private static final Random random = new Random();
	private static final boolean glow = Bukkit.getPluginManager().getPlugin("GlowAPI") != null;

	public ItemTier(ConfigurationSection config) {
		id = config.getName().toUpperCase().replace("-", "_");
		name = new ColorParse('&', config.getString("name")).toChatColor();
		deconstruct = config.contains("deconstruct-item") ? new DropTable(config.getConfigurationSection("deconstruct-item")) : null;
		unidentificationInfo = new UnidentificationInfo(config.getConfigurationSection("unidentification"));

		try {
			hint = config.contains("item-glow") && config.getBoolean("item-glow.hint");
			color = config.contains("item-glow") ? new TierColor(config.getString("item-glow.color"), glow) : null;
		} catch (NoClassDefFoundError | IllegalAccessException | NoSuchFieldException | SecurityException exception) {
			throw new IllegalArgumentException("Could not load tier color: " + exception.getMessage());
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
	
//	public RolledTier roll() {
//		return new RolledTier(info, itemLevel)
//	}

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
		return new ColorParse('&', str).toChatColor();
	}
}
