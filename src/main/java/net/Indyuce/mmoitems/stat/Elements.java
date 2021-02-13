package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmoitems.ItemStats;
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
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Elements extends ItemStat {
	public Elements() {
		super("ELEMENT", Material.SLIME_BALL, "Elements", new String[] { "The elements of your item." },
				new String[] { "slashing", "piercing", "blunt", "offhand", "range", "tool", "armor", "gem_stone" });

		// Initialize paths
		if (defenseNBTpaths == null) {

			// Initialize
			defenseNBTpaths = new HashMap<>();
			damageNBTpaths = new HashMap<>();

			// Initialize
			for (Element element : Element.values()) {

				// Add I guess
				defenseNBTpaths.put(element, "MMOITEMS_" + element.name() + "_DEFENSE");
				damageNBTpaths.put(element, "MMOITEMS_" + element.name() + "_DAMAGE");
			}

		}
	}

	@Override
	public RandomStatData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		return new RandomElementListData((ConfigurationSection) object);
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
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
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
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
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> statData) {

		if (statData.isPresent()) {
			lore.add(ChatColor.GRAY + "Current Value:");
			RandomElementListData data = (RandomElementListData) statData.get();
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

	@NotNull
	@Override
	public StatData getClearStatData() { return new ElementListData(); }

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) {

		// Write Lore
		ElementListData elements = (ElementListData) data;
		for (Element element : elements.getDamageElements()) {
			String path = element.name().toLowerCase() + "-damage";
			double value = elements.getDamage(element);
			item.getLore().insert(path, ItemStat.translate(path).replace("#", new StatFormat("##").format(value))); }
		for (Element element : elements.getDefenseElements()) {
			String path = element.name().toLowerCase() + "-defense";
			double value = elements.getDefense(element);
			item.getLore().insert(path, ItemStat.translate(path).replace("#", new StatFormat("##").format(value))); }

		// Addtags
		item.addItemTag(getAppliedNBT(data));
	}

	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull StatData data) {

		// Must be element list data
		ElementListData elements = (ElementListData) data;

		// Create Array
		ArrayList<ItemTag> ret = new ArrayList<>();

		// Add damages
		for (Element element : elements.getDamageElements()) {

			// Obtain damage
			String path = element.name().toLowerCase() + "-damage";
			double value = elements.getDamage(element);

			// Create Tag
			ret.add(new ItemTag(damageNBTpaths.get(element), value));
		}

		// Add defenses
		for (Element element : elements.getDefenseElements()) {

			// Obtain defense
			String path = element.name().toLowerCase() + "-defense";
			double value = elements.getDefense(element);

			// Create Tag
			ret.add(new ItemTag(defenseNBTpaths.get(element), value));
		}

		// Thats it
		return ret;
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

		// Seek the relevant tags
		ArrayList<ItemTag> relevantTags = new ArrayList<>();
		for (Element element : Element.values()) {

			// Add I guess
			if (mmoitem.getNBT().hasTag(damageNBTpaths.get(element)))
				relevantTags.add(ItemTag.getTagAtPath(damageNBTpaths.get(element), mmoitem.getNBT(), SupportedNBTTagValues.DOUBLE));


			if (mmoitem.getNBT().hasTag(defenseNBTpaths.get(element)))
				relevantTags.add(ItemTag.getTagAtPath(defenseNBTpaths.get(element), mmoitem.getNBT(), SupportedNBTTagValues.DOUBLE));
		}

		// Generate Data
		StatData data = getLoadedNBT(relevantTags);

		// Found?
		if (data != null) { mmoitem.setData(this, data); }
	}

	@Nullable
	@Override
	public StatData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

		// Create new
		ElementListData elements = new ElementListData();
		boolean success = false;

		// Try to find every existing element
		for (Element element : Element.values()) {

			// Find Damage and Defense Tags
			ItemTag damTag = ItemTag.getTagAtPath(damageNBTpaths.get(element), storedTags);
			ItemTag defTag = ItemTag.getTagAtPath(defenseNBTpaths.get(element), storedTags);

			// Found?
			if (damTag != null) { elements.setDamage(element, (double) damTag.getValue()); success = true; }
			if (defTag != null) { elements.setDefense(element, (double) defTag.getValue()); success = true; }
		}

		if (success) { return elements; }
		return null;
	}

	static HashMap<Element, String> defenseNBTpaths = null;
	static HashMap<Element, String> damageNBTpaths = null;
}
