package net.Indyuce.mmoitems.api;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.droptable.DropTable;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.comp.itemglow.TierColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemTier {
	@NotNull private final String name, id;

	// Unidentification
	@NotNull private final UnidentificationInfo unidentificationInfo;

	// deconstruction
	@Nullable private final DropTable deconstruct;

	// item glow options
	@Nullable private TierColor color = null;
	private boolean hint = false;

	// item generation
	private final double chance;
	@Nullable private final NumericStatFormula capacity;

	@NotNull private static final Random RANDOM = new Random();
	private static final boolean GLOW = Bukkit.getPluginManager().getPlugin("GlowAPI") != null;

	/**
	 * Load an ItemTier from the YML Configuration Itself
	 *
	 * @param config Configuration section to get all values from
	 */
	public ItemTier(@NotNull ConfigurationSection config) {

		// The name and ID, crucial parts.
		id = config.getName().toUpperCase().replace("-", "_");
		name = MythicLib.plugin.parseColors(config.getString("name"));

		// Deconstruct and Unidentification
		deconstruct = config.contains("deconstruct-item") ? new DropTable(config.getConfigurationSection("deconstruct-item")) : null;

		ConfigurationSection unidentificationSection = config.getConfigurationSection("unidentification");
		if (unidentificationSection == null) { unidentificationInfo = getDefaultUnident(); }
		else { unidentificationInfo = new UnidentificationInfo(unidentificationSection); }

		//noinspection ErrorNotRethrown
		try {

			// Is it defined?
			ConfigurationSection glowSection = config.getConfigurationSection("item-glow");

			// Alr then lets read it
			if (glowSection != null) {

				// Does it hint?
				hint = glowSection.getBoolean("hint");

				// Does it color?
				color = new TierColor(config.getString("color", "WHITE"), GLOW);
			}

		} catch (NoClassDefFoundError | IllegalAccessException | NoSuchFieldException | SecurityException exception) {

			// No hints
			hint = false;
			color = null;

			// Grrr but GlowAPI crashing shall not crash MMOItems tiers wtf
			MMOItems.print(null, "Could not load glow color for tier $r{0}$b;$f {1}", "Tier Hints", id, exception.getMessage());
		}

		// What are the chances?
		chance = config.getDouble("generation.chance");
		capacity = config.contains("generation.capacity") ? new NumericStatFormula(config.getConfigurationSection("generation.capacity")) : null;
	}

	@NotNull public String getId() { return id; }

	@NotNull public String getName() { return name; }

	public boolean hasDropTable() { return deconstruct != null; }

	@Nullable public DropTable getDropTable() { return deconstruct; }

	public boolean hasColor() { return color != null; }

	@Nullable public TierColor getColor() { return color; }

	public boolean isHintEnabled() { return hint; }

	/**
	 * @return The chance of the tier being chosen when generating a random item
	 */
	public double getGenerationChance() { return chance; }

	/**
	 * @return If the item tier has a modifier capacity ie if this tier let
	 *         generated items have modifiers
	 */
	public boolean hasCapacity() { return capacity != null; }

	/**
	 * @return The formula for modifier capacity which can be then rolled to
	 *         generate a random amount of modifier capacity when generating a
	 *         random item
	 */
	@Nullable public NumericStatFormula getModifierCapacity() { return capacity; }

	@NotNull public UnidentificationInfo getUnidentificationInfo() { return unidentificationInfo; }

	/**
	 * @return Reads the deconstruction drop table. This may return a list
	 *         containing multiple items and they should all be added to the
	 *         player's inventory
	 */
	public List<ItemStack> getDeconstructedLoot(@NotNull PlayerData player) {
		//noinspection ConstantConditions
		return hasDropTable() ? deconstruct.read(player, false) : new ArrayList<>();
	}

	/**
	 * @return Default unidentification info, if it is missing in the config.
	 */
	@NotNull private UnidentificationInfo getDefaultUnident() { return new UnidentificationInfo(UnidentificationInfo.UNIDENT_NAME, UnidentificationInfo.UNIDENT_PREFIX, 0); }

	public class UnidentificationInfo {
		@NotNull private final String unidentificationName, prefix;
		private final int range;

		public static final String UNIDENT_NAME = "Unidentified Item";
		public static final String UNIDENT_PREFIX = "Unknown";

		public UnidentificationInfo(@NotNull ConfigurationSection config) {
			this(color(config.getString("name", UNIDENT_NAME)), color(config.getString("prefix", UNIDENT_PREFIX)), config.getInt("range"));
		}

		public UnidentificationInfo(@NotNull String name, @NotNull String prefix, int range) {
			unidentificationName = name;
			this.prefix = prefix;
			this.range = range;
		}

		@NotNull public String getPrefix() { return prefix; }

		@NotNull public String getDisplayName() {
			return unidentificationName;
		}

		public int[] calculateRange(int level) {
			int min = (int) Math.max(1, (level - (double) range * RANDOM.nextDouble()));
			return new int[] { min, min + range };
		}

	}

	private String color(@Nullable String str) {
		return MythicLib.plugin.parseColors(str);
	}
}
