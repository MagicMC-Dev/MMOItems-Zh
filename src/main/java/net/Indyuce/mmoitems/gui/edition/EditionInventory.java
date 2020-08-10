package net.Indyuce.mmoitems.gui.edition;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.gui.PluginInventory;
import net.mmogroup.mmolib.api.util.AltChar;

public abstract class EditionInventory extends PluginInventory {

	protected MMOItemTemplate template;

	private ItemStack cachedItem;
	private int prevPage;

	public EditionInventory(Player player, MMOItemTemplate template) {
		this(player, template, null);
	}

	public EditionInventory(Player player, MMOItemTemplate template, ItemStack cached) {
		super(player);

		this.template = template;
		if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory().getHolder() instanceof EditionInventory)
			this.cachedItem = ((EditionInventory) player.getOpenInventory().getTopInventory().getHolder()).cachedItem;
	}

	public MMOItemTemplate getEdited() {
		return template;
	}

	public ItemStack getCachedItem() {
		if (cachedItem != null)
			return cachedItem;

		updateCachedItem();
		return cachedItem;
	}

	public void registerTemplateEdition(ConfigFile config) {

		config.registerTemplateEdition(template);

		/*
		 * update edited mmoitem after registering the item edition and
		 * refreshes the displayed item.
		 */
		template = MMOItems.plugin.getItems().getTemplate(template.getType(), template.getId());
		updateCachedItem();
	}

	/**
	 * Method used when the player gets the item using the chest item so that he
	 * can reroll the stats.
	 */
	public void updateCachedItem() {
		cachedItem = template.newBuilder(PlayerData.get(getPlayer()).getRPG()).build().newBuilder().build();
	}

	public void addEditionInventoryItems(Inventory inv, boolean displayBack) {
		ItemStack get = new ItemStack(Material.CHEST);
		ItemMeta getMeta = get.getItemMeta();
		getMeta.addItemFlags(ItemFlag.values());
		getMeta.setDisplayName(ChatColor.GREEN + AltChar.fourEdgedClub + " Get the Item! " + AltChar.fourEdgedClub);
		List<String> getLore = new ArrayList<>();
		getLore.add(ChatColor.GRAY + "");
		getLore.add(ChatColor.GRAY + "You may also use /mi " + template.getType().getId() + " " + template.getId());
		getLore.add(ChatColor.GRAY + "");
		getLore.add(ChatColor.YELLOW + AltChar.smallListDash + " Left click to get the item.");
		getLore.add(ChatColor.YELLOW + AltChar.smallListDash + " Right click to get it & reroll its stats.");
		getMeta.setLore(getLore);
		get.setItemMeta(getMeta);

		if (displayBack) {
			ItemStack back = new ItemStack(Material.BARRIER);
			ItemMeta backMeta = back.getItemMeta();
			backMeta.setDisplayName(ChatColor.GREEN + AltChar.rightArrow + " Back");
			back.setItemMeta(backMeta);

			inv.setItem(6, back);
		}

		inv.setItem(2, get);
		inv.setItem(4, getCachedItem());
	}

	public void open(int page) {
		prevPage = page;
		open();
	}

	public int getPreviousPage() {
		return prevPage;
	}
}
