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
		Inventory inv = Bukkit.createInventory(this, 54, "配方列表: " + template.getId());

		for (CraftingType ctype : CraftingType.values())
			if (ctype.shouldAdd()) {
				ItemStack craftingEvent = ctype.getItem();
				ItemMeta craftingEventItem = craftingEvent.getItemMeta();
				craftingEventItem.addItemFlags(ItemFlag.values());
				craftingEventItem.setDisplayName(ChatColor.GREEN + ctype.getName());
				List<String> eventLore = new ArrayList<>();
				eventLore.add(ChatColor.GRAY + ctype.getLore());
				eventLore.add("");
				eventLore.add(getEditedSection().contains("crafting." + ctype.name().toLowerCase()) ? ChatColor.GREEN + "找到一个或多个合成配方."
						: ChatColor.RED + "没有找到配方");
				eventLore.add("");
				eventLore.add(ChatColor.YELLOW + AltChar.listDash + "► 左键单击以更改此配方");
				eventLore.add(ChatColor.YELLOW + AltChar.listDash + "► 右键单击以删除配方");
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
				new StatEdition(this, ItemStats.CRAFTING, "smithing").enable("在聊天中输入制作此物品所需的物品.",
						"格式: '[ITEM] [ITEM]'", "[ITEM] = '[MATERIAL]' 或 '[MATERIAL]:[DURABILITY]' 或 '[TYPE].[ID]'");
			else
				new StatEdition(this, ItemStats.CRAFTING, "item", corresponding.name().toLowerCase()).enable(
						"在聊天中输入你想要合成的物品、合成速度和经验值.", "格式: '[ITEM] [TICKS] [EXP]'",
						"[ITEM] = '[MATERIAL]' 或 '[MATERIAL]:[DURABILITY]' 或 '[TYPE].[ID]'");
		}

		if (event.getAction() == InventoryAction.PICKUP_HALF && getEditedSection().contains("crafting." + corresponding.name().toLowerCase())) {
			getEditedSection().set("crafting." + corresponding.name().toLowerCase(), null);
			player.sendMessage(MMOItems.plugin.getPrefix() + "成功移除 " + corresponding.getName() + " 配方.");

			if (getEditedSection().getConfigurationSection("crafting").getKeys(false).size() == 0)
				getEditedSection().set("crafting", null);

			registerTemplateEdition();
		}
	}
}