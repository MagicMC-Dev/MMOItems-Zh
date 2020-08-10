package net.Indyuce.mmoitems.gui.edition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Element;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.util.AltChar;

public class ElementsEdition extends EditionInventory {
	public static Map<Integer, String> correspondingSlot = new HashMap<>();
	private static final int[] slots = { 19, 25, 20, 24, 28, 34, 29, 33, 30, 32, 37, 43, 38, 42, 39, 41 };

	static {
		correspondingSlot.put(19, "fire.damage");
		correspondingSlot.put(25, "fire.defense");
		correspondingSlot.put(20, "ice.damage");
		correspondingSlot.put(24, "ice.defense");
		correspondingSlot.put(28, "wind.damage");
		correspondingSlot.put(34, "wind.defense");
		correspondingSlot.put(29, "earth.damage");
		correspondingSlot.put(33, "earth.defense");
		correspondingSlot.put(30, "thunder.damage");
		correspondingSlot.put(32, "thunder.defense");
		correspondingSlot.put(37, "water.damage");
		correspondingSlot.put(43, "water.defense");
	}

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
			List<String> attackLore = new ArrayList<String>();
			attackLore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GREEN
					+ getEditedSection().getDouble("element." + element.getName().toLowerCase() + ".damage"));
			attackLore.add("");
			attackLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
			attackLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove this value.");
			attackMeta.setLore(attackLore);
			attack.setItemMeta(attackMeta);

			ItemStack defense = element.getItem().clone();
			ItemMeta defenseMeta = defense.getItemMeta();
			defenseMeta.setDisplayName(ChatColor.GREEN + element.getName() + " Defense");
			List<String> defenseLore = new ArrayList<String>();
			defenseLore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GREEN
					+ getEditedSection().getDouble("element." + element.getName().toLowerCase() + ".defense"));
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

		if (correspondingSlot.containsKey(event.getSlot())) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStat.ELEMENTS, event.getSlot()).enable("Write in the value you want.");

			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				String elementPath = correspondingSlot.get(event.getSlot());
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
	}
}