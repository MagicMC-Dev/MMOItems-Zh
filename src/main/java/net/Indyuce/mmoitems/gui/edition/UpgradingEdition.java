package net.Indyuce.mmoitems.gui.edition;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.internal.NamedItemStack;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.util.AltChar;
import net.mmogroup.mmolib.version.VersionMaterial;

public class UpgradingEdition extends EditionInventory {
	private static final ItemStack notAvailable = new NamedItemStack(VersionMaterial.RED_STAINED_GLASS_PANE.toMaterial(), "&cNot Available");

	public UpgradingEdition(Player player, MMOItemTemplate template) {
		super(player, template);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Upgrade Setup: " + template.getId());
		ConfigFile config = template.getType().getConfigFile();

		if (!config.getConfig().contains(template.getId() + ".upgrade")) {
			config.getConfig().createSection(template.getId() + ".upgrade");
			config.save();
		}

		boolean workbench = config.getConfig().getBoolean(template.getId() + ".upgrade.workbench");
		if (!template.getType().corresponds(Type.CONSUMABLE)) {

			ItemStack workbenchItem = new ItemStack(VersionMaterial.CRAFTING_TABLE.toMaterial());
			ItemMeta workbenchItemMeta = workbenchItem.getItemMeta();
			workbenchItemMeta.setDisplayName(ChatColor.GREEN + "Workbench Upgrade Only?");
			List<String> workbenchItemLore = new ArrayList<>();
			workbenchItemLore.add(ChatColor.GRAY + "When toggled on, players must");
			workbenchItemLore.add(ChatColor.GRAY + "use a crafting station recipe in");
			workbenchItemLore.add(ChatColor.GRAY + "order to upgrade their weapon.");
			workbenchItemLore.add("");
			workbenchItemLore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GOLD + workbench);
			workbenchItemLore.add("");
			workbenchItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
			workbenchItemMeta.setLore(workbenchItemLore);
			workbenchItem.setItemMeta(workbenchItemMeta);
			inv.setItem(20, workbenchItem);

			String upgradeTemplate = config.getConfig().getString(template.getId() + ".upgrade.template");
			ItemStack templateItem = new ItemStack(VersionMaterial.OAK_SIGN.toMaterial());
			ItemMeta templateItemMeta = templateItem.getItemMeta();
			templateItemMeta.setDisplayName(ChatColor.GREEN + "Upgrade Template");
			List<String> templateItemLore = new ArrayList<>();
			templateItemLore.add(ChatColor.GRAY + "This option dictates what stats are improved");
			templateItemLore.add(ChatColor.GRAY + "when your item is upgraded. More info on the wiki.");
			templateItemLore.add("");
			templateItemLore.add(ChatColor.GRAY + "Current Value: "
					+ (upgradeTemplate == null ? ChatColor.RED + "No template" : ChatColor.GOLD + upgradeTemplate));
			templateItemLore.add("");
			templateItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to input the template.");
			templateItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to reset.");
			templateItemMeta.setLore(templateItemLore);
			templateItem.setItemMeta(templateItemMeta);
			inv.setItem(22, templateItem);

			int max = config.getConfig().getInt(template.getId() + ".upgrade.max");
			ItemStack maxItem = new ItemStack(Material.BARRIER);
			ItemMeta maxItemMeta = maxItem.getItemMeta();
			maxItemMeta.setDisplayName(ChatColor.GREEN + "Max Upgrades");
			List<String> maxItemLore = new ArrayList<>();
			maxItemLore.add(ChatColor.GRAY + "The maximum amount of upgrades your");
			maxItemLore.add(ChatColor.GRAY + "item may receive (recipe or consumable).");
			maxItemLore.add("");
			maxItemLore.add(ChatColor.GRAY + "Current Value: " + (max == 0 ? ChatColor.RED + "No limit" : ChatColor.GOLD + "" + max));
			maxItemLore.add("");
			maxItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to chance this value.");
			maxItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to reset.");
			maxItemMeta.setLore(maxItemLore);
			maxItem.setItemMeta(maxItemMeta);
			inv.setItem(40, maxItem);
		} else {
			inv.setItem(20, notAvailable);
			inv.setItem(22, notAvailable);
		}

		if (!workbench || template.getType().corresponds(Type.CONSUMABLE)) {

			String reference = config.getConfig().getString(template.getId() + ".upgrade.reference");
			ItemStack referenceItem = new ItemStack(Material.PAPER);
			ItemMeta referenceItemMeta = referenceItem.getItemMeta();
			referenceItemMeta.setDisplayName(ChatColor.GREEN + "Upgrade Reference");
			List<String> referenceItemLore = new ArrayList<>();
			referenceItemLore.add(ChatColor.GRAY + "This option dictates what consumables can");
			referenceItemLore.add(ChatColor.GRAY + "upgrade your item. " + ChatColor.AQUA + "The consumable upgrade");
			referenceItemLore.add(ChatColor.AQUA + "reference must match your item's reference" + ChatColor.GRAY + ",");
			referenceItemLore.add(ChatColor.GRAY + "otherwise it can't upgrade it. Leave this blank");
			referenceItemLore.add(ChatColor.GRAY + "so any consumable can upgrade this item.");
			referenceItemLore.add("");
			referenceItemLore
					.add(ChatColor.GRAY + "Current Value: " + (reference == null ? ChatColor.RED + "No reference" : ChatColor.GOLD + reference));
			referenceItemLore.add("");
			referenceItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to input the reference.");
			referenceItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to reset.");
			referenceItemMeta.setLore(referenceItemLore);
			referenceItem.setItemMeta(referenceItemMeta);
			inv.setItem(38, referenceItem);
		} else
			inv.setItem(38, notAvailable);

		double success = config.getConfig().getDouble(template.getId() + ".upgrade.success");
		ItemStack successItem = new ItemStack(VersionMaterial.EXPERIENCE_BOTTLE.toMaterial());
		ItemMeta successItemMeta = successItem.getItemMeta();
		successItemMeta.setDisplayName(ChatColor.GREEN + "Success Chance");
		List<String> successItemLore = new ArrayList<>();
		successItemLore.add(ChatColor.GRAY + "The chance of successfully upgrading");
		successItemLore.add(ChatColor.GRAY + "when using a consumable or when using");
		successItemLore.add(ChatColor.GRAY + "a station upgrading recipe.");
		successItemLore.add("");
		successItemLore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GOLD + (success == 0 ? "100" : "" + success) + "%");
		successItemLore.add("");
		successItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Left click to change this value.");
		successItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to reset.");
		successItemMeta.setLore(successItemLore);
		successItem.setItemMeta(successItemMeta);
		inv.setItem(24, successItem);

		if (success > 0 && !template.getType().corresponds(Type.CONSUMABLE)) {
			ItemStack destroyOnFail = MMOLib.plugin.getVersion().getWrapper().generate(Material.FISHING_ROD, 30);
			ItemMeta destroyOnFailMeta = destroyOnFail.getItemMeta();
			destroyOnFailMeta.setDisplayName(ChatColor.GREEN + "Destroy on fail?");
			List<String> destroyOnFailLore = new ArrayList<>();
			destroyOnFailLore.add(ChatColor.GRAY + "When toggled on, the item will be");
			destroyOnFailLore.add(ChatColor.GRAY + "destroyed when failing at upgrading it.");
			destroyOnFailLore.add("");
			destroyOnFailLore
					.add(ChatColor.GRAY + "Current Value: " + ChatColor.GOLD + config.getConfig().getBoolean(template.getId() + ".upgrade.destroy"));
			destroyOnFailLore.add("");
			destroyOnFailLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
			destroyOnFailMeta.setLore(destroyOnFailLore);
			destroyOnFail.setItemMeta(destroyOnFailMeta);
			inv.setItem(42, destroyOnFail);
		}

		addEditionInventoryItems(inv, true);

		return inv;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isMetaItem(item, false))
			return;

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Success Chance")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStat.UPGRADE, "rate").enable("Write in the chat the success rate you want.");

			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				ConfigFile config = template.getType().getConfigFile();
				if (config.getConfig().getConfigurationSection(template.getId()).contains("upgrade")
						&& config.getConfig().getConfigurationSection(template.getId() + ".upgrade").contains("success")) {
					config.getConfig().set(template.getId() + ".upgrade.success", null);
					registerTemplateEdition(config);
					open();
					player.sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset success chance.");
				}
			}
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Max Upgrades")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStat.UPGRADE, "max").enable("Write in the chat the number you want.");

			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				ConfigFile config = template.getType().getConfigFile();
				if (config.getConfig().getConfigurationSection(template.getId()).contains("upgrade")
						&& config.getConfig().getConfigurationSection(template.getId() + ".upgrade").contains("max")) {
					config.getConfig().set(template.getId() + ".upgrade.max", null);
					registerTemplateEdition(config);
					open();
					player.sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset the number of max upgrades.");
				}
			}
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Upgrade Template")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStat.UPGRADE, "template").enable("Write in the chat the upgrade template ID you want.");

			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				ConfigFile config = template.getType().getConfigFile();
				if (config.getConfig().getConfigurationSection(template.getId()).contains("upgrade")
						&& config.getConfig().getConfigurationSection(template.getId() + ".upgrade").contains("template")) {
					config.getConfig().set(template.getId() + ".upgrade.template", null);
					registerTemplateEdition(config);
					open();
					player.sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset upgrade template.");
				}
			}
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Upgrade Reference")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStat.UPGRADE, "ref").enable("Write in the chat the upgrade reference (text) you want.");

			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				ConfigFile config = template.getType().getConfigFile();
				if (config.getConfig().getConfigurationSection(template.getId()).contains("upgrade")
						&& config.getConfig().getConfigurationSection(template.getId() + ".upgrade").contains("reference")) {
					config.getConfig().set(template.getId() + ".upgrade.reference", null);
					registerTemplateEdition(config);
					open();
					player.sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset upgrade reference.");
				}
			}
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Workbench Upgrade Only?")) {
			ConfigFile config = template.getType().getConfigFile();
			boolean bool = !config.getConfig().getBoolean(template.getId() + ".upgrade.workbench");
			config.getConfig().set(template.getId() + ".upgrade.workbench", bool);
			registerTemplateEdition(config);
			open();
			player.sendMessage(MMOItems.plugin.getPrefix()
					+ (bool ? "Your item must now be upgraded via recipes." : "Your item can now be upgraded using consumables."));
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Destroy on fail?")) {
			ConfigFile config = template.getType().getConfigFile();
			boolean bool = !config.getConfig().getBoolean(template.getId() + ".upgrade.destroy");
			config.getConfig().set(template.getId() + ".upgrade.destroy", bool);
			registerTemplateEdition(config);
			open();
			player.sendMessage(MMOItems.plugin.getPrefix()
					+ (bool ? "Your item will be destroyed upon failing upgrade." : "Your item will not be destroyed upon failing upgrade."));
		}
	}
}