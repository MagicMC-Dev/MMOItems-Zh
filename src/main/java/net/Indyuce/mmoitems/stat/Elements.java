package net.Indyuce.mmoitems.stat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import net.Indyuce.mmoitems.api.Element.StatType;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.ElementsEdition;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class Elements extends ItemStat {
	public Elements() {
		super(new ItemStack(Material.SLIME_BALL), "Elements", new String[] { "The elements of your item." }, "element", new String[] { "slashing", "piercing", "blunt", "offhand", "range", "tool", "armor" });
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getItemType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new ElementsEdition(inv.getPlayer(), inv.getItemType(), inv.getItemId()).open();

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
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + MMOUtils.caseOnWords(elementPath.replace(".", " ")) + ChatColor.GRAY + " successfully changed to " + value + ".");
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
				lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + element + ChatColor.GRAY + ": " + ChatColor.RED + "" + ChatColor.BOLD + config.getDouble(path + ".element." + s1 + ".damage") + "%" + ChatColor.GRAY + " | " + ChatColor.WHITE + "" + ChatColor.BOLD + config.getDouble(path + ".element." + s1 + ".defense") + "%");
			}
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to access the elements edition menu.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove all the elements.");
	}

	@Override
	public boolean whenLoaded(MMOItem item, ConfigurationSection config) {
		ElementListData elements = new ElementListData();

		for (Element element : Element.values()) {
			String path = element.name().toLowerCase();
			if (!config.getConfigurationSection("element").contains(path))
				continue;

			for (Element.StatType type : Element.StatType.values()) {
				String statTypePath = type.name().toLowerCase();
				double value = config.getDouble("element." + path + "." + statTypePath);
				if (value != 0)
					elements.set(element, type, value);
			}
		}

		item.setData(ItemStat.ELEMENTS, elements);
		return true;
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		ElementListData elements = (ElementListData) data;

		elements.getElements().forEach(element -> {
			for (StatType type : elements.getStatTypes(element)) {
				String path = element.name().toLowerCase() + "-" + type.name().toLowerCase();
				double value = elements.get(element, type);

				item.addItemTag(new ItemTag("MMOITEMS_" + element.name() + "_" + type.name(), value));
				item.getLore().insert(path, ItemStat.translate(path).replace("#", new StatFormat("##").format(value)));
			}
		});

		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		ElementListData elements = new ElementListData();

		double value;
		for (Element element : Element.values())
			for (StatType type : StatType.values())
				if ((value = item.getDouble("MMOITEMS_" + element.name() + "_" + type.name())) != 0)
					elements.set(element, type, value);

		if (elements.total() > 0)
			mmoitem.setData(ItemStat.ELEMENTS, elements);
	}

	public class ElementListData extends StatData {
		private Map<Element, Map<StatType, Double>> stats = new HashMap<>();

		public ElementListData() {
		}

		public Set<Element> getElements() {
			return stats.keySet();
		}

		public Set<StatType> getStatTypes(Element element) {
			return stats.get(element).keySet();
		}

		public double get(Element element, StatType type) {
			return stats.get(element).get(type);
		}

		public void set(Element element, StatType type, double value) {
			if (!this.stats.containsKey(element))
				this.stats.put(element, new HashMap<>());
			this.stats.get(element).put(type, value);
		}

		public int total() {
			int t = 0;
			for (Element element : stats.keySet())
				t += stats.get(element).size();
			return t;
		}
	}
}
