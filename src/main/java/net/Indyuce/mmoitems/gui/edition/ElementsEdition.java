package net.Indyuce.mmoitems.gui.edition;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Element;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.stat.data.random.RandomElementListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import io.lumine.mythic.lib.api.util.AltChar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ElementsEdition extends EditionInventory {
	private static final int[] slots = { 19, 25, 20, 24, 28, 34, 29, 33, 30, 32, 37, 43, 38, 42, 39, 41 };

	public ElementsEdition(Player player, MMOItemTemplate template) {
		super(player, template);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, "Elements E.: " + template.getId());
		int n = 0;

		for (Element element : Element.values()) {
			ItemStack attack = element.getItem().clone();
			ItemMeta attackMeta = attack.getItemMeta();
			attackMeta.setDisplayName(ChatColor.GREEN + element.getName() + " Damage");
			List<String> attackLore = new ArrayList<>();
			Optional<RandomStatData> statData = getEventualStatData(ItemStats.ELEMENTS);
			attackLore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GREEN
					+ (statData.isPresent() && ((RandomElementListData) statData.get()).hasDamage(element)
							? ((RandomElementListData) statData.get()).getDamage(element) + " (%)"
							: "---"));
			attackLore.add("");
			attackLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
			attackLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove this value.");
			attackMeta.setLore(attackLore);
			attack.setItemMeta(attackMeta);

			ItemStack defense = element.getItem().clone();
			ItemMeta defenseMeta = defense.getItemMeta();
			defenseMeta.setDisplayName(ChatColor.GREEN + element.getName() + " Defense");
			List<String> defenseLore = new ArrayList<>();
			defenseLore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GREEN
					+ (statData.isPresent() && ((RandomElementListData) statData.get()).hasDefense(element)
							? ((RandomElementListData) statData.get()).getDefense(element) + " (%)"
							: "---"));
			defenseLore.add("");
			defenseLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
			defenseLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove this value.");
			defenseMeta.setLore(defenseLore);
			defense.setItemMeta(defenseMeta);

			inv.setItem(slots[n], attack);
			inv.setItem(slots[n + 1], defense);
			n += 2;
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

		String elementPath = getElementPath(event.getSlot());
		if (elementPath == null)
			return;

		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(this, ItemStats.ELEMENTS, elementPath).enable("Write in the value you want.");

		else if (event.getAction() == InventoryAction.PICKUP_HALF) {
			getEditedSection().set("element." + elementPath, null);

			// clear element config section
			String elementName = elementPath.split("\\.")[0];
			if (getEditedSection().contains("element." + elementName)
					&& getEditedSection().getConfigurationSection("element." + elementName).getKeys(false).isEmpty()) {
				getEditedSection().set("element." + elementName, null);
				if (getEditedSection().getConfigurationSection("element").getKeys(false).isEmpty())
					getEditedSection().set("element", null);
			}

			registerTemplateEdition();
			new ElementsEdition(player, template).open(getPreviousPage());
			player.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + MMOUtils.caseOnWords(elementPath.replace(".", " ")) + ChatColor.GRAY
					+ " successfully removed.");
		}
	}

	public String getElementPath(int guiSlot) {
		for (Element element : Element.values())
			if (element.getDamageGuiSlot() == guiSlot)
				return element.name().toLowerCase() + ".damage";
			else if (element.getDefenseGuiSlot() == guiSlot)
				return element.name().toLowerCase() + ".defense";
		return null;
	}
}