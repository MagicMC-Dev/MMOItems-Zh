package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.BlockStateMeta;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.ShieldPatternData;
import io.lumine.mythic.lib.api.util.AltChar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShieldPatternStat extends ItemStat<ShieldPatternData, ShieldPatternData> {
	public ShieldPatternStat() {
		super("SHIELD_PATTERN", Material.SHIELD, "Shield Pattern", new String[] { "The color & patterns", "of your shield." },
				new String[] { "all" }, Material.SHIELD);
	}

	@Override
	public ShieldPatternData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		ConfigurationSection config = (ConfigurationSection) object;

		ShieldPatternData shieldPattern = new ShieldPatternData(
				config.contains("color") ? DyeColor.valueOf(config.getString("color").toUpperCase().replace("-", "_").replace(" ", "_")) : null);

		// Apply patterns
		for (String key : config.getKeys(false))
			if (!key.equalsIgnoreCase("color")) {
				String format = config.getString(key + ".pattern").toUpperCase().replace("-", "_").replace(" ", "_");
				PatternType type = PatternType.valueOf(format);

				format = config.getString(key + ".color").toUpperCase().replace("-", "_").replace(" ", "_");
				DyeColor color = DyeColor.valueOf(format);

				shieldPattern.add(new Pattern(color, type));
			}

		return shieldPattern;
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull ShieldPatternData data) {
		BlockStateMeta meta = (BlockStateMeta) item.getMeta();
		Banner banner = (Banner) meta.getBlockState();
		banner.setBaseColor(data.getBaseColor());
		banner.setPatterns(data.getPatterns());
		((BlockStateMeta) item.getMeta()).setBlockState(banner);
		item.getMeta().addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
	}

	/**
	 * This stat is saved not as a custom tag, but as the vanilla pattenrs themselves.
	 * Alas this is an empty array
	 */
	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull ShieldPatternData data) { return new ArrayList<>(); }

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStats.SHIELD_PATTERN, 0).enable("Write in the chat the color of your shield.");

		if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
			inv.getEditedSection().set("shield-pattern.color", null);

			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset the shield color.");
		}

		if (event.getAction() == InventoryAction.PICKUP_HALF)
			new StatEdition(inv, ItemStats.SHIELD_PATTERN, 1).enable("Write in the chat the pattern you want to add.",
					ChatColor.AQUA + "Format: [PATTERN_TYPE] [DYE_COLOR]");

		if (event.getAction() == InventoryAction.DROP_ONE_SLOT && inv.getEditedSection().contains("shield-pattern")) {
			Set<String> set = inv.getEditedSection().getConfigurationSection("shield-pattern").getKeys(false);
			String last = new ArrayList<>(set).get(set.size() - 1);
			if (last.equalsIgnoreCase("color"))
				return;

			inv.getEditedSection().set("shield-pattern." + last, null);
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed the last pattern.");
		}
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		int editedStatData = (int) info[0];

		if (editedStatData == 1) {
			String[] split = message.split(" ");
			Validate.isTrue(split.length == 2, message + " is not a valid [PATTERN_TYPE] [DYE_COLOR].");

			PatternType patternType = PatternType.valueOf(split[0].toUpperCase().replace("-", "_").replace(" ", "_"));
			DyeColor dyeColor = DyeColor.valueOf(split[1].toUpperCase().replace("-", "_").replace(" ", "_"));

			int availableKey = getNextAvailableKey(inv.getEditedSection().getConfigurationSection("shield-pattern"));
			Validate.isTrue(availableKey >= 0, "You can have more than 100 shield patterns on a single item.");

			inv.getEditedSection().set("shield-pattern." + availableKey + ".pattern", patternType.name());
			inv.getEditedSection().set("shield-pattern." + availableKey + ".color", dyeColor.name());
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(
					MMOItems.plugin.getPrefix() + MMOUtils.caseOnWords(patternType.name().toLowerCase().replace("_", " ")) + " successfully added.");
			return;
		}

		DyeColor color = DyeColor.valueOf(message.toUpperCase().replace("-", "_").replace(" ", "_"));
		inv.getEditedSection().set("shield-pattern.color", color.name());
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Shield color successfully changed.");
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<ShieldPatternData> statData) {

		if (statData.isPresent()) {
			lore.add(ChatColor.GRAY + "Current Value:");
			ShieldPatternData data =  statData.get();
			lore.add(ChatColor.GRAY + "* Base Color: "
					+ (data.getBaseColor() != null
							? ChatColor.GREEN + MMOUtils.caseOnWords(data.getBaseColor().name().toLowerCase().replace("_", " "))
							: ChatColor.RED + "None"));
			data.getPatterns().forEach(pattern -> lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + pattern.getPattern().name() + ChatColor.GRAY
					+ " - " + ChatColor.GREEN + pattern.getColor().name()));

		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "None");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Left Click to change the shield color.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Shift Left Click to reset the shield color.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right Click to add a pattern.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Drop to remove the last pattern.");
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().getItem().getItemMeta() instanceof BlockStateMeta
				&& ((BlockStateMeta) mmoitem.getNBT().getItem().getItemMeta()).hasBlockState()
				&& ((BlockStateMeta) mmoitem.getNBT().getItem().getItemMeta()).getBlockState() instanceof Banner) {
			Banner banner = (Banner) ((BlockStateMeta) mmoitem.getNBT().getItem().getItemMeta()).getBlockState();

			ShieldPatternData shieldPattern = new ShieldPatternData(banner.getBaseColor());
			shieldPattern.addAll(banner.getPatterns());
			mmoitem.setData(ItemStats.SHIELD_PATTERN, shieldPattern);
		}
	}

	@NotNull
	@Override
	public ShieldPatternData getClearStatData() {
		return new ShieldPatternData(DyeColor.YELLOW);
	}

	/**
	 * This stat is saved not as a custom tag, but as the vanilla Pattern itself.
	 * Alas this method returns null.
	 */
	@Nullable
	@Override
	public ShieldPatternData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) { return null; }

	private int getNextAvailableKey(ConfigurationSection section) {
		for (int j = 0; j < 100; j++)
			if (!section.contains("" + j))
				return j;
		return -1;
	}
}
