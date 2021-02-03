package net.Indyuce.mmoitems.stat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.EnchantListData;
import net.Indyuce.mmoitems.stat.data.random.RandomEnchantListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import io.lumine.mythic.lib.api.util.AltChar;

public class Enchants extends ItemStat {
	public Enchants() {
		super("ENCHANTS", Material.ENCHANTED_BOOK, "Enchantments", new String[] { "The item enchants." }, new String[] { "all" });
	}

	@Override
	public RandomStatData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		return new RandomEnchantListData((ConfigurationSection) object);
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStats.ENCHANTS).enable("Write in the chat the enchant you want to add.",
					ChatColor.AQUA + "Format: {Enchant Name} {Enchant Level Numeric Formula}");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (inv.getEditedSection().contains("enchants")) {
				Set<String> set = inv.getEditedSection().getConfigurationSection("enchants").getKeys(false);
				String last = Arrays.asList(set.toArray(new String[0])).get(set.size() - 1);
				inv.getEditedSection().set("enchants." + last, null);
				if (set.size() <= 1)
					inv.getEditedSection().set("enchants", null);
				inv.registerTemplateEdition();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + last.substring(0, 1).toUpperCase()
						+ last.substring(1).toLowerCase().replace("_", " ") + ".");
			}
		}
	}

	/*
	 * getByName is deprecated, but it's safe to
	 * use and will make the user experience better
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {
		String[] split = message.split(" ");
		Validate.isTrue(split.length >= 2, "Use this format: {Enchant Name} {Enchant Level Numeric Formula}. Example: 'sharpness 5 0.3' "
				+ "stands for Sharpness 5, plus 0.3 level per item level (rounded up to lower integer)");

		Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(split[0].toLowerCase().replace("-", "_")));		
		if(enchant == null) enchant = Enchantment.getByName(split[0].toUpperCase());
		Validate.notNull(enchant, split[0]
				+ " is not a valid enchantment! All enchants can be found here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/enchantments/Enchantment.html");

		NumericStatFormula formula = new NumericStatFormula(message.substring(message.indexOf(" ") + 1));
		formula.fillConfigurationSection(inv.getEditedSection(), "enchants." + enchant.getKey().getKey());
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + enchant.getKey().getKey() + " " + formula.toString() + " successfully added.");
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> statData) {

		if (statData.isPresent()) {
			lore.add(ChatColor.GRAY + "Current Value:");
			RandomEnchantListData data = (RandomEnchantListData) statData.get();
			data.getEnchants().forEach(enchant -> lore.add(ChatColor.GRAY + "* " + MMOUtils.caseOnWords(enchant.getKey().getKey().replace("_", " "))
					+ " " + data.getLevel(enchant).toString()));

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
			mmoitem.setData(ItemStats.ENCHANTS, enchants);
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		EnchantListData enchants = (EnchantListData) data;
		for (Enchantment enchant : enchants.getEnchants())
			item.getMeta().addEnchant(enchant, enchants.getLevel(enchant), true);
	}
}
