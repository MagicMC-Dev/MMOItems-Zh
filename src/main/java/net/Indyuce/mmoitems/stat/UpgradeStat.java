package net.Indyuce.mmoitems.stat;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonParser;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.UpgradingEdition;
import net.Indyuce.mmoitems.stat.data.UpgradeData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
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
	public void whenApplied(ItemStackBuilder item, StatData data) {
		item.addItemTag(new ItemTag("MMOITEMS_UPGRADE", data.toString()));
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new UpgradingEdition(inv.getPlayer(), inv.getEdited()).open(inv.getPage());

		if (event.getAction() == InventoryAction.PICKUP_HALF && inv.getEditedSection().contains("upgrade")) {
			inv.getEditedSection().set("upgrade", null);
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset the upgrading setup.");
		}
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {

		if (info[0].equals("ref")) {
			inv.getEditedSection().set("upgrade.reference", message);
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(
					MMOItems.plugin.getPrefix() + "Upgrading reference successfully changed to " + ChatColor.GOLD + message + ChatColor.GRAY + ".");
			return;
		}

		if (info[0].equals("max")) {
			int i = Integer.parseInt(message);
			inv.getEditedSection().set("upgrade.max", i);
			inv.registerTemplateEdition();
			inv.getPlayer()
					.sendMessage(MMOItems.plugin.getPrefix() + "Max upgrades successfully set to " + ChatColor.GOLD + i + ChatColor.GRAY + ".");
			return;
		}

		if (info[0].equals("rate")) {
			double d = Double.parseDouble(message);
			inv.getEditedSection().set("upgrade.success", d);
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(
					MMOItems.plugin.getPrefix() + "Upgrading rate successfully set to " + ChatColor.GOLD + d + "%" + ChatColor.GRAY + ".");
			return;
		}

		Validate.isTrue(MMOItems.plugin.getUpgrades().hasTemplate(message), "Could not find any upgrade template with ID '" + message + "'.");
		inv.getEditedSection().set("upgrade.template", message);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(
				MMOItems.plugin.getPrefix() + "Upgrading template successfully changed to " + ChatColor.GOLD + message + ChatColor.GRAY + ".");
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag("MMOITEMS_UPGRADE"))
			mmoitem.setData(this, new UpgradeData(new JsonParser().parse(mmoitem.getNBT().getString("MMOITEMS_UPGRADE")).getAsJsonObject()));
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> optional) {
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Left click to setup upgrading.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to reset.");
	}
}
