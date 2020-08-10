package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.ShieldPatternData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.mmogroup.mmolib.api.util.AltChar;

public class ShieldPatternStat extends StringStat {
	public ShieldPatternStat() {
		super("SHIELD_PATTERN", new ItemStack(Material.SHIELD), "Shield Pattern", new String[] { "The color & patterns", "of your shield." },
				new String[] { "all" }, Material.SHIELD);
	}

	@Override
	public RandomStatData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		ConfigurationSection config = (ConfigurationSection) object;

		ShieldPatternData shieldPattern = new ShieldPatternData(
				config.contains("color") ? DyeColor.valueOf(config.getString("color").toUpperCase().replace("-", "_").replace(" ", "_")) : null);

		// apply patterns
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
	public void whenApplied(ItemStackBuilder item, StatData data) {
		BlockStateMeta meta = (BlockStateMeta) item.getMeta();
		Banner banner = (Banner) meta.getBlockState();
		ShieldPatternData pattern = (ShieldPatternData) data;
		banner.setBaseColor(pattern.getBaseColor());
		banner.setPatterns(pattern.getPatterns());
		((BlockStateMeta) item.getMeta()).setBlockState(banner);
		item.getMeta().addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getEdited().getType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStat.SHIELD_PATTERN, 0).enable("Write in the chat the color of your shield.");

		if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
			config.getConfig().set("shield-pattern.color", null);

			inv.registerTemplateEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset the shield color.");
		}
		if (event.getAction() == InventoryAction.PICKUP_HALF)
			new StatEdition(inv, ItemStat.SHIELD_PATTERN, 1).enable("Write in the chat the pattern you want to add.",
					ChatColor.AQUA + "Format: [PATTERN_TYPE] [DYE_COLOR]");

		if (event.getAction() == InventoryAction.DROP_ONE_SLOT) {
			if (!config.getConfig().contains("shield-pattern"))
				return;

			Set<String> set = config.getConfig().getConfigurationSection("shield-pattern").getKeys(false);
			String last = new ArrayList<String>(set).get(set.size() - 1);
			if (last.equalsIgnoreCase("color"))
				return;

			config.getConfig().set("shield-pattern." + last, null);
			inv.registerTemplateEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed the last pattern.");
		}
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		int editedStatData = (int) info[0];

		if (editedStatData == 1) {
			String[] split = message.split("\\ ");
			if (split.length != 2) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid [PATTERN_TYPE] [DYE_COLOR].");
				return false;
			}

			String patternFormat = split[0].toUpperCase().replace("-", "_").replace(" ", "_");
			PatternType patternType;
			try {
				patternType = PatternType.valueOf(patternFormat);
			} catch (Exception e1) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + patternFormat + " is not a valid pattern type!");
				return false;
			}

			String colorFormat = split[1].toUpperCase().replace("-", "_").replace(" ", "_");
			DyeColor dyeColor;
			try {
				dyeColor = DyeColor.valueOf(colorFormat);
			} catch (Exception e1) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + colorFormat + " is not a valid dye color!");
				return false;
			}

			int availableKey = getNextAvailableKey(config.getConfig().getConfigurationSection("shield-pattern"));
			if (availableKey < 0) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "You can have more than 100 shield patterns on a single item.");
				return false;
			}

			config.getConfig().set("shield-pattern." + availableKey + ".pattern", patternType.name());
			config.getConfig().set("shield-pattern." + availableKey + ".color", dyeColor.name());
			inv.registerTemplateEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(
					MMOItems.plugin.getPrefix() + MMOUtils.caseOnWords(patternType.name().toLowerCase().replace("_", " ")) + " successfully added.");
			return true;
		}

		DyeColor color;
		try {
			color = DyeColor.valueOf(message.toUpperCase().replace("-", "_").replace(" ", "_"));
		} catch (Exception e) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid color!");
			return false;
		}

		config.getConfig().set("shield-pattern.color", color.name());
		inv.registerTemplateEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Shield color successfully changed.");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> optional) {

		if (optional.isPresent()) {
			lore.add(ChatColor.GRAY + "Current Value:");
			ShieldPatternData data = (ShieldPatternData) optional.get();
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

	@EventHandler
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().getItem().getItemMeta() instanceof BlockStateMeta
				&& ((BlockStateMeta) mmoitem.getNBT().getItem().getItemMeta()).hasBlockState()
				&& ((BlockStateMeta) mmoitem.getNBT().getItem().getItemMeta()).getBlockState() instanceof Banner) {
			Banner banner = (Banner) ((BlockStateMeta) mmoitem.getNBT().getItem().getItemMeta()).getBlockState();

			ShieldPatternData shieldPattern = new ShieldPatternData(banner.getBaseColor());
			shieldPattern.addAll(banner.getPatterns());
			mmoitem.setData(ItemStat.SHIELD_PATTERN, shieldPattern);
		}
	}

	private int getNextAvailableKey(ConfigurationSection section) {
		for (int j = 0; j < 100; j++)
			if (!section.contains("" + j))
				return j;
		return -1;
	}
}
