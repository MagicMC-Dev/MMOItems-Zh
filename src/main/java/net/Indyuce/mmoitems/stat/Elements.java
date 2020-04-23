package net.Indyuce.mmoitems.stat;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Element;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.itemgen.RandomStatData;
import net.mmogroup.mmolib.api.util.AltChar;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.ElementsEdition;
import net.Indyuce.mmoitems.stat.data.ElementListData;
import net.Indyuce.mmoitems.stat.data.random.RandomElementListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class Elements extends ItemStat {
	public Elements() {
		super("ELEMENT", new ItemStack(Material.SLIME_BALL), "Elements", new String[] { "The elements of your item." },
				new String[] { "slashing", "piercing", "blunt", "offhand", "range", "tool", "armor" });
	}

	@Override
	public StatData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		ConfigurationSection config = (ConfigurationSection) object;

		ElementListData elements = new ElementListData();

		for (Element element : Element.values()) {
			elements.setDamage(element, config.getDouble(element.name().toLowerCase() + ".damage"));
			elements.setDefense(element, config.getDouble(element.name().toLowerCase() + ".defense"));
		}

		return elements;
	}

	@Override
	public RandomStatData whenInitializedGeneration(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		return new RandomElementListData((ConfigurationSection) object);
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getItemType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new ElementsEdition(inv.getPlayer(), inv.getItemType(), inv.getItemId()).open(inv.getPage());

		if (event.getAction() == InventoryAction.PICKUP_HALF)
			if (config.getConfig().getConfigurationSection(inv.getItemId()).contains("element")) {
				config.getConfig().set(inv.getItemId() + ".element", null);
				inv.registerItemEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Elements successfully removed.");
			}
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		String elementPath = ElementsEdition.correspondingSlot.get(info[0]);
		double value = 0;
		try {
			value = Double.parseDouble(message);
		} catch (Exception e1) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid number.");
			return false;
		}
		config.getConfig().set(inv.getItemId() + ".element." + elementPath, value);
		if (value == 0)
			config.getConfig().set(inv.getItemId() + ".element." + elementPath, null);

		// clear element config section
		String elementName = elementPath.split("\\.")[0];
		if (config.getConfig().getConfigurationSection(inv.getItemId()).contains("element")) {
			if (config.getConfig().getConfigurationSection(inv.getItemId() + ".element").contains(elementName))
				if (config.getConfig().getConfigurationSection(inv.getItemId() + ".element." + elementName).getKeys(false).isEmpty())
					config.getConfig().set(inv.getItemId() + ".element." + elementName, null);
			if (config.getConfig().getConfigurationSection(inv.getItemId() + ".element").getKeys(false).isEmpty())
				config.getConfig().set(inv.getItemId() + ".element", null);
		}

		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + MMOUtils.caseOnWords(elementPath.replace(".", " ")) + ChatColor.GRAY
				+ " successfully changed to " + value + ".");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		lore.add(ChatColor.GRAY + "Current Value:");
		if (!config.getConfigurationSection(path).contains("element"))
			lore.add(ChatColor.RED + "No element.");
		else if (config.getConfigurationSection(path + ".element").getKeys(false).isEmpty())
			lore.add(ChatColor.RED + "No element.");
		else
			for (String s1 : config.getConfigurationSection(path + ".element").getKeys(false)) {
				String element = s1.substring(0, 1).toUpperCase() + s1.substring(1);
				lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + element + ChatColor.GRAY + ": " + ChatColor.RED + "" + ChatColor.BOLD
						+ config.getDouble(path + ".element." + s1 + ".damage") + "%" + ChatColor.GRAY + " | " + ChatColor.WHITE + "" + ChatColor.BOLD
						+ config.getDouble(path + ".element." + s1 + ".defense") + "%");
			}
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to access the elements edition menu.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove all the elements.");
	}

	@Override
	public void whenApplied(MMOItemBuilder item, StatData data) {
		ElementListData elements = (ElementListData) data;

		for (Element element : elements.getDamageElements()) {
			String path = element.name().toLowerCase() + "-damage";
			double value = elements.getDamage(element);

			item.addItemTag(new ItemTag("MMOITEMS_" + element.name() + "_DAMAGE", value));
			item.getLore().insert(path, ItemStat.translate(path).replace("#", new StatFormat("##").format(value)));
		}

		for (Element element : elements.getDefenseElements()) {
			String path = element.name().toLowerCase() + "-defense";
			double value = elements.getDefense(element);

			item.addItemTag(new ItemTag("MMOITEMS_" + element.name() + "_DEFENSE", value));
			item.getLore().insert(path, ItemStat.translate(path).replace("#", new StatFormat("##").format(value)));
		}
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		ElementListData elements = new ElementListData();

		for (Element element : Element.values()) {
			elements.setDefense(element, item.getDouble("MMOITEMS_" + element.name() + "_DEFENSE"));
			elements.setDamage(element, item.getDouble("MMOITEMS_" + element.name() + "_DAMAGE"));
		}

		if (elements.total() > 0)
			mmoitem.setData(ItemStat.ELEMENTS, elements);
	}
}
