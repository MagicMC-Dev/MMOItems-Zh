package net.Indyuce.mmoitems.stat;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.EnchantListData;
import net.Indyuce.mmoitems.stat.data.random.RandomEnchantListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.util.AltChar;

public class Enchants extends ItemStat {
	public Enchants() {
		super("ENCHANTS", new ItemStack(Material.ENCHANTED_BOOK), "Enchantments", new String[] { "The item enchants." }, new String[] { "all" });
	}

	@Override
	public StatData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		ConfigurationSection config = (ConfigurationSection) object;

		EnchantListData enchants = new EnchantListData();

		for (String format : config.getKeys(false)) {
			Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(format.toLowerCase().replace("-", "_")));
			Validate.notNull(enchant, "Could not find enchant with key '" + format + "'");
			enchants.addEnchant(enchant, config.getInt(format));
		}

		return enchants;
	}

	@Override
	public RandomStatData whenInitializedGeneration(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		return new RandomEnchantListData((ConfigurationSection) object);
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getEdited().getType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStat.ENCHANTS).enable("Write in the chat the enchant you want to add.",
					ChatColor.AQUA + "Format: [ENCHANT] [LEVEL]");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (config.getConfig().getConfigurationSection(inv.getEdited().getId()).contains("enchants")) {
				Set<String> set = config.getConfig().getConfigurationSection(inv.getEdited().getId() + ".enchants").getKeys(false);
				String last = Arrays.asList(set.toArray(new String[0])).get(set.size() - 1);
				config.getConfig().set(inv.getEdited().getId() + ".enchants." + last, null);
				if (set.size() <= 1)
					config.getConfig().set(inv.getEdited().getId() + ".enchants", null);
				inv.registerTemplateEdition(config, true);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + last.substring(0, 1).toUpperCase()
						+ last.substring(1).toLowerCase().replace("_", " ") + ".");
			}
		}
	}

	private String getName(Enchantment enchant) {
		return MMOLib.plugin.getVersion().getWrapper().getName(enchant);
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
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix()
					+ "All enchants can be found here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/enchantments/Enchantment.html");
			return false;
		}

		int level = 0;
		try {
			level = (int) Double.parseDouble(split[1]);
		} catch (Exception e1) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + split[1] + " is not a valid number!");
			return false;
		}

		config.getConfig().set(inv.getEdited().getId() + ".enchants." + getName(enchant), level);
		inv.registerTemplateEdition(config, true);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName(enchant) + " " + MMOUtils.intToRoman(level) + " successfully added.");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, MMOItem mmoitem) {

		if (mmoitem.hasData(this)) {
			lore.add(ChatColor.GRAY + "Current Value:");
			EnchantListData data = (EnchantListData) mmoitem.getData(this);
			data.getEnchants().forEach(enchant -> lore.add(ChatColor.GRAY + "* " + MMOUtils.caseOnWords(enchant.getKey().getKey().replace("_", " "))
					+ " " + MMOUtils.intToRoman(data.getLevel(enchant))));

		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "None");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add an enchant.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last enchant.");
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		EnchantListData enchants = new EnchantListData();
		mmoitem.getNBT().getItem().getItemMeta().getEnchants().keySet()
				.forEach(enchant -> enchants.addEnchant(enchant, mmoitem.getNBT().getItem().getItemMeta().getEnchantLevel(enchant)));
		if (enchants.getEnchants().size() > 0)
			mmoitem.setData(ItemStat.ENCHANTS, enchants);
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		EnchantListData enchants = (EnchantListData) data;
		for (Enchantment enchant : enchants.getEnchants())
			item.getMeta().addEnchant(enchant, enchants.getLevel(enchant), true);
	}
}
