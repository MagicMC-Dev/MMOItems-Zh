package net.Indyuce.mmoitems.gui.edition.recipe;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.gui.edition.recipe.registry.burninglegacy.CraftingType;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import io.lumine.mythic.lib.api.util.AltChar;

public class RecipeListEdition extends EditionInventory {
	public RecipeListEdition(Player player, MMOItemTemplate template) {
		super(player, template);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, "Crafting Recipes: " + template.getId());

		for (CraftingType ctype : CraftingType.values())
			if (ctype.shouldAdd()) {
				ItemStack craftingEvent = ctype.getItem();
				ItemMeta craftingEventItem = craftingEvent.getItemMeta();
				craftingEventItem.addItemFlags(ItemFlag.values());
				craftingEventItem.setDisplayName(ChatColor.GREEN + ctype.getName());
				List<String> eventLore = new ArrayList<>();
				eventLore.add(ChatColor.GRAY + ctype.getLore());
				eventLore.add("");
				eventLore.add(getEditedSection().contains("crafting." + ctype.name().toLowerCase()) ? ChatColor.GREEN + "Found one or more recipe(s)."
						: ChatColor.RED + "No recipes found.");
				eventLore.add("");
				eventLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this recipe.");
				eventLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove recipe.");
				craftingEventItem.setLore(eventLore);
				craftingEvent.setItemMeta(craftingEventItem);

				inv.setItem(ctype.getSlot(), craftingEvent);
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

		CraftingType corresponding = CraftingType.getBySlot(event.getSlot());
		if (corresponding == null)
			return;

		if (event.getAction() == InventoryAction.PICKUP_ALL) {
			if (corresponding == CraftingType.SHAPELESS || corresponding == CraftingType.SHAPED)
				new RecipeEdition(player, template, corresponding == CraftingType.SHAPELESS).open(getPreviousPage());
			else if (corresponding == CraftingType.SMITHING)
				new StatEdition(this, ItemStats.CRAFTING, "smithing").enable("Write in the chat the items required to craft this.",
						"Format: '[ITEM] [ITEM]'", "[ITEM] = '[MATERIAL]' or '[MATERIAL]:[DURABILITY]' or '[TYPE].[ID]'");
			else
				new StatEdition(this, ItemStats.CRAFTING, "item", corresponding.name().toLowerCase()).enable(
						"Write in the chat the item, tickspeed and exp you want.", "Format: '[ITEM] [TICKS] [EXP]'",
						"[ITEM] = '[MATERIAL]' or '[MATERIAL]:[DURABILITY]' or '[TYPE].[ID]'");
		}

		if (event.getAction() == InventoryAction.PICKUP_HALF && getEditedSection().contains("crafting." + corresponding.name().toLowerCase())) {
			getEditedSection().set("crafting." + corresponding.name().toLowerCase(), null);
			player.sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + corresponding.getName() + " recipe.");

			if (getEditedSection().getConfigurationSection("crafting").getKeys(false).size() == 0)
				getEditedSection().set("crafting", null);

			registerTemplateEdition();
		}
	}
}