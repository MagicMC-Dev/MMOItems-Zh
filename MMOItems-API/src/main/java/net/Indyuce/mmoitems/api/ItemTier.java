package net.Indyuce.mmoitems.api;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.droptable.DropTable;
import net.Indyuce.mmoitems.tooltip.TooltipTexture;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemTier {
    private final String id;
    private final String name;
    private final String unparsedName;
    private final UnidentificationInfo unidentificationInfo;

    @Nullable
    private final TooltipTexture tooltip;

    // Deconstruction
    @Nullable
    private final DropTable deconstructTable;

    // Item glow options
    @Nullable
    private final ChatColor glowColor;
    private final boolean itemHint;

    // Item generation
    @Nullable
    private final NumericStatFormula capacity;
    private final double chance;

    @NotNull
    private static final Random RANDOM = new Random();

    /**
     * Load an ItemTier from the YML Configuration Itself
     *
     * @param config Configuration section to get all values from
     */
    public ItemTier(@NotNull ConfigurationSection config) {
        // The name and ID, crucial parts.
        this.id = config.getName().toUpperCase().replace("-", "_");
        this.unparsedName = config.getString("name");
        this.name = MythicLib.plugin.parseColors(unparsedName);

        // Deconstruct and Unidentification
        deconstructTable = config.contains("deconstruct-item") ? new DropTable(config.getConfigurationSection("deconstruct-item")) : null;

        final ConfigurationSection unidentificationSection = config.getConfigurationSection("unidentification");
        unidentificationInfo = unidentificationSection == null ? UnidentificationInfo.DEFAULT : new UnidentificationInfo(unidentificationSection);

        if (config.contains("item-glow")) {
            itemHint = config.getBoolean("item-glow.hint");
            glowColor = ChatColor.valueOf(UtilityMethods.enumName(config.getString("item-glow.color", "WHITE")));
        } else {
            itemHint = false;
            glowColor = null;
        }

        tooltip = config.isConfigurationSection("tooltip") ? new TooltipTexture(config.getConfigurationSection("tooltip")) : null;

        // What are the chances?
        chance = config.getDouble("generation.chance");
        capacity = config.contains("generation.capacity") ? new NumericStatFormula(config.get("generation.capacity")) : null;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public boolean hasDropTable() {
        return deconstructTable != null;
    }

    @Nullable
    public DropTable getDropTable() {
        return deconstructTable;
    }

    @Nullable
    public TooltipTexture getTooltip() {
        return tooltip;
    }

    /**
     * @return Reads the deconstruction drop table. This may return a list
     * containing multiple items and they should all be added to the
     * player's inventory
     */
    public List<ItemStack> getDeconstructedLoot(@NotNull PlayerData player) {
        //noinspection ConstantConditions
        return hasDropTable() ? deconstructTable.read(player, false) : new ArrayList<>();
    }

    public boolean hasColor() {
        return glowColor != null;
    }

    @Nullable
    public ChatColor getColor() {
        return glowColor;
    }

    public boolean isHintEnabled() {
        return itemHint;
    }

    /**
     * @return The chance of the tier being chosen when generating a random item
     */
    public double getGenerationChance() {
        return chance;
    }

    /**
     * @return If the item tier has a modifier capacity ie if this tier let
     * generated items have modifiers
     */
    public boolean hasCapacity() {
        return capacity != null;
    }

    /**
     * @return The formula for modifier capacity which can be then rolled to
     * generate a random amount of modifier capacity when generating a
     * random item
     */
    @Nullable
    public NumericStatFormula getModifierCapacity() {
        return capacity;
    }

    @NotNull
    public UnidentificationInfo getUnidentificationInfo() {
        return unidentificationInfo;
    }

    public @NotNull String getUnparsedName() {
        return unparsedName;
    }

    public static class UnidentificationInfo {
        @NotNull
        private final String name, prefix;
        private final int range;

        public static final String DEFAULT_NAME = "Unidentified Item";
        public static final String DEFAULT_PREFIX = "Unknown";
        public static final UnidentificationInfo DEFAULT = new UnidentificationInfo(UnidentificationInfo.DEFAULT_NAME, UnidentificationInfo.DEFAULT_PREFIX, 0);

        public UnidentificationInfo(@NotNull ConfigurationSection config) {
            this(config.getString("name", DEFAULT_NAME), config.getString("prefix", DEFAULT_PREFIX), config.getInt("range"));
        }

        public UnidentificationInfo(@NotNull String name, @NotNull String prefix, int range) {
            this.name = name;
            this.prefix = prefix;
            this.range = range;
        }

        @NotNull
        public String getPrefix() {
            return prefix;
        }

        @NotNull
        public String getDisplayName() {
            return name;
        }

        public int[] calculateRange(int level) {
            int min = (int) Math.max(1, (level - (double) range * RANDOM.nextDouble()));
            return new int[]{min, min + range};
        }
    }

    @Nullable
    public static ItemTier ofItem(NBTItem item) {
        final @Nullable String format = item.getString("MMOITEMS_TIER");
        return format == null ? null : MMOItems.plugin.getTiers().get(format);
    }
}
