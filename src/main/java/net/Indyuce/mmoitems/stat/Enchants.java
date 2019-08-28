package net.Indyuce.mmoitems.stat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class Enchants extends ItemStat {
	public Enchants() {
		super(new ItemStack(Material.ENCHANTED_BOOK), "Enchantments", new String[] { "The item enchants." }, "enchants", new String[] { "all" });
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getItemType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStat.ENCHANTS).enable("Write in the chat the enchant you want to add.", ChatColor.AQUA + "Format: [ENCHANT] [LEVEL]");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (config.getConfig().getConfigurationSection(inv.getItemId()).contains("enchants")) {
				Set<String> set = config.getConfig().getConfigurationSection(inv.getItemId() + ".enchants").getKeys(false);
				String last = Arrays.asList(set.toArray(new String[0])).get(set.size() - 1);
				config.getConfig().set(inv.getItemId() + ".enchants." + last, null);
				if (set.size() <= 1)
					config.getConfig().set(inv.getItemId() + ".enchants", null);
				inv.registerItemEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + last.substring(0, 1).toUpperCase() + last.substring(1).toLowerCase().replace("_", " ") + "§7.");
			}
		}
		return true;
	}

	private String getName(Enchantment enchant) {
		return MMOItems.plugin.getVersion().getVersionWrapper().getName(enchant);
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		String[] split = message.split("\\ ");
		if (split.length != 2) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid [ENCHANT] [LEVEL].");
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Example: 'DAMAGE_ALL 10' stands for Sharpness 10.");
			return false;
		}

		Enchantment enchant = null;
		for (Enchantment enchant1 : Enchantment.values())
			if (getName(enchant1).equalsIgnoreCase(split[0].replace("-", "_"))) {
				enchant = enchant1;
				break;
			}

		if (enchant == null) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + split[0] + " is not a valid enchantment!");
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "All enchants can be found here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/enchantments/Enchantment.html");
			return false;
		}

		int level = 0;
		try {
			level = (int) Double.parseDouble(split[1]);
		} catch (Exception e1) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + split[1] + " is not a valid number!");
			return false;
		}

		config.getConfig().set(inv.getItemId() + ".enchants." + getName(enchant), level);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName(enchant) + " " + MMOUtils.intToRoman(level) + " successfully added.");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		lore.add(ChatColor.GRAY + "Current Value:");
		if (!config.getConfigurationSection(path).contains("enchants"))
			lore.add(ChatColor.RED + "No enchantment.");
		else if (config.getConfigurationSection(path + ".enchants").getKeys(false).isEmpty())
			lore.add(ChatColor.RED + "No enchantment.");
		else
			for (String s1 : config.getConfigurationSection(path + ".enchants").getKeys(false)) {
				String enchant = MMOUtils.caseOnWords(s1.toLowerCase().replace("_", " ").replace("-", " "));
				String level = MMOUtils.intToRoman(config.getInt(path + ".enchants." + s1));
				lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + enchant + " " + level);
			}
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add an enchant.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last enchant.");
	}

	@Override
	public boolean whenLoaded(MMOItem item, ConfigurationSection config) {
		EnchantListData enchants = new EnchantListData();

		for (String format : config.getConfigurationSection("enchants").getKeys(false)) {
			Enchantment enchant = null;
			for (Enchantment enchant1 : Enchantment.values())
				if (getName(enchant1).equalsIgnoreCase(format.replace("-", "_"))) {
					enchant = enchant1;
					break;
				}

			if (enchant == null) {
				item.log(Level.WARNING, "[Enchantments] " + format + " is not a valid enchantment name.");
				continue;
			}

			enchants.addEnchant(enchant, config.getInt("enchants." + format));
		}

		item.setData(ItemStat.ENCHANTS, enchants);
		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		EnchantListData enchants = new EnchantListData();
		item.getItem().getItemMeta().getEnchants().keySet().forEach(enchant -> enchants.addEnchant(enchant, item.getItem().getItemMeta().getEnchantLevel(enchant)));
		if (enchants.getEnchants().size() > 0)
			mmoitem.setData(ItemStat.ENCHANTS, enchants);
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		EnchantListData enchants = (EnchantListData) data;
		for (Enchantment enchant : enchants.getEnchants())
			item.getMeta().addEnchant(enchant, enchants.getLevel(enchant), true);
		return false;
	}

	public class EnchantListData extends StatData {
		private Map<Enchantment, Integer> enchants = new HashMap<>();

		public EnchantListData() {
		}

		public Set<Enchantment> getEnchants() {
			return enchants.keySet();
		}

		public int getLevel(Enchantment enchant) {
			return enchants.get(enchant);
		}

		public void addEnchant(Enchantment enchant, int level) {
			enchants.put(enchant, level);
		}
	}
}
