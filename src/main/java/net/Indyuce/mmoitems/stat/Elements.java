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

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Element;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.ElementsEdition;
import net.Indyuce.mmoitems.stat.data.ElementListData;
import net.Indyuce.mmoitems.stat.data.random.RandomElementListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.util.AltChar;

public class Elements extends ItemStat {
	public Elements() {
		super("ELEMENT", new ItemStack(Material.SLIME_BALL), "Elements", new String[] { "The elements of your item." },
				new String[] { "slashing", "piercing", "blunt", "offhand", "range", "tool", "armor" });
	}

	@Override
	public RandomStatData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		return new RandomElementListData((ConfigurationSection) object);
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new ElementsEdition(inv.getPlayer(), inv.getEdited()).open(inv.getPage());

		if (event.getAction() == InventoryAction.PICKUP_HALF)
			if (inv.getEditedSection().contains("element")) {
				inv.getEditedSection().set("element", null);
				inv.registerTemplateEdition();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Elements successfully removed.");
			}
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {
		String elementPath = info[0].toString();

		NumericStatFormula formula = new NumericStatFormula(message);
		formula.fillConfigurationSection(inv.getEditedSection(), "element." + elementPath);

		// clear element config section
		String elementName = elementPath.split("\\.")[0];
		if (inv.getEditedSection().contains("element")) {
			if (inv.getEditedSection().getConfigurationSection("element").contains(elementName)
					&& inv.getEditedSection().getConfigurationSection("element." + elementName).getKeys(false).isEmpty())
				inv.getEditedSection().set("element." + elementName, null);
			if (inv.getEditedSection().getConfigurationSection("element").getKeys(false).isEmpty())
				inv.getEditedSection().set("element", null);
		}

		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + MMOUtils.caseOnWords(elementPath.replace(".", " ")) + ChatColor.GRAY
				+ " successfully changed to " + ChatColor.GOLD + formula.toString() + ChatColor.GRAY + ".");
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> optional) {

		if (optional.isPresent()) {
			lore.add(ChatColor.GRAY + "Current Value:");
			RandomElementListData data = (RandomElementListData) optional.get();
			data.getDamageElements().forEach(
					element -> lore.add(ChatColor.GRAY + "* " + element.getName() + " Damage: " + ChatColor.RED + data.getDamage(element) + " (%)"));
			data.getDefenseElements().forEach(
					element -> lore.add(ChatColor.GRAY + "* " + element.getName() + " Damage: " + ChatColor.RED + data.getDefense(element) + " (%)"));

		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "None");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to access the elements edition menu.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove all the elements.");
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
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
	public void whenLoaded(ReadMMOItem mmoitem) {
		ElementListData elements = new ElementListData();

		for (Element element : Element.values()) {
			elements.setDefense(element, mmoitem.getNBT().getDouble("MMOITEMS_" + element.name() + "_DEFENSE"));
			elements.setDamage(element, mmoitem.getNBT().getDouble("MMOITEMS_" + element.name() + "_DAMAGE"));
		}

		if (elements.total() > 0)
			mmoitem.setData(ItemStat.ELEMENTS, elements);
	}
}
