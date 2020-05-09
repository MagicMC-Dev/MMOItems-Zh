package net.Indyuce.mmoitems.stat;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonParser;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.ReadMMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.itemgen.RandomStatData;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.UpgradingEdition;
import net.Indyuce.mmoitems.stat.data.UpgradeData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.util.AltChar;

public class UpgradeStat extends ItemStat {
	public UpgradeStat() {
		super("UPGRADE", new ItemStack(Material.FLINT), "Item Upgrading",
				new String[] { "Upgrading your item improves its", "current stats. It requires either a", "consumable or a specific crafting ",
						"station. Upgrading may sometimes &cfail&7..." },
				new String[] { "piercing", "slashing", "blunt", "offhand", "range", "tool", "armor", "consumable", "accessory" });
	}

	@Override
	public UpgradeData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		return new UpgradeData((ConfigurationSection) object);
	}

	@Override
	public RandomStatData whenInitializedGeneration(Object object) {
		return whenInitialized(object);
	}

	@Override
	public void whenApplied(MMOItemBuilder item, StatData data) {
		item.addItemTag(new ItemTag("MMOITEMS_UPGRADE", data.toString()));
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new UpgradingEdition(inv.getPlayer(), inv.getEdited()).open(inv.getPage());

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			ConfigFile config = inv.getEdited().getType().getConfigFile();
			if (config.getConfig().getConfigurationSection(inv.getEdited().getId()).contains("upgrade")) {
				config.getConfig().set(inv.getEdited().getId() + ".upgrade", null);
				inv.registerItemEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset the upgrading setup.");
			}
		}
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {

		if (info[0].equals("ref")) {
			config.getConfig().set(inv.getEdited().getId() + ".upgrade.reference", message);
			inv.registerItemEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(
					MMOItems.plugin.getPrefix() + "Upgrading reference successfully changed to " + ChatColor.GOLD + message + ChatColor.GRAY + ".");
			return true;
		}

		if (info[0].equals("max")) {

			int i = 0;
			try {
				i = Integer.parseInt(message);
			} catch (NumberFormatException exception) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid number.");
				return false;
			}

			config.getConfig().set(inv.getEdited().getId() + ".upgrade.max", i);
			inv.registerItemEdition(config);
			inv.open();
			inv.getPlayer()
					.sendMessage(MMOItems.plugin.getPrefix() + "Max upgrades successfully set to " + ChatColor.GOLD + i + ChatColor.GRAY + ".");
			return true;
		}

		if (info[0].equals("rate")) {

			double d = 0;
			try {
				d = Double.parseDouble(message);
			} catch (NumberFormatException exception) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid number.");
				return false;
			}

			config.getConfig().set(inv.getEdited().getId() + ".upgrade.success", d);
			inv.registerItemEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(
					MMOItems.plugin.getPrefix() + "Upgrading rate successfully set to " + ChatColor.GOLD + d + "%" + ChatColor.GRAY + ".");
			return true;
		}

		if (!MMOItems.plugin.getUpgrades().hasTemplate(message)) {
			inv.getPlayer().sendMessage(
					MMOItems.plugin.getPrefix() + "Could not find any upgrade template with ID " + ChatColor.GOLD + message + ChatColor.GRAY + ".");
			return false;
		}

		config.getConfig().set(inv.getEdited().getId() + ".upgrade.template", message);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(
				MMOItems.plugin.getPrefix() + "Upgrading template successfully changed to " + ChatColor.GOLD + message + ChatColor.GRAY + ".");
		return true;
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag("MMOITEMS_UPGRADE"))
			mmoitem.setData(this, new UpgradeData(new JsonParser().parse(mmoitem.getNBT().getString("MMOITEMS_UPGRADE")).getAsJsonObject()));
	}

	@Override
	public void whenDisplayed(List<String> lore, MMOItem mmoitem) {
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Left click to setup upgrading.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to reset.");
	}
}
