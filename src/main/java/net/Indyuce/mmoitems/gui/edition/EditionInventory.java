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
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.gui.PluginInventory;
import net.mmogroup.mmolib.api.util.AltChar;

public abstract class EditionInventory extends PluginInventory {
	protected MMOItem mmoitem;
	private ItemStack cached;
	private int prevPage;

	public EditionInventory(Player player, MMOItem mmoitem) {
		this(player, mmoitem, null);
	}

	public EditionInventory(Player player, MMOItem mmoitem, ItemStack cached) {
		super(player);

		this.mmoitem = mmoitem;
		this.cached = player.getOpenInventory() != null && player.getOpenInventory().getTopInventory().getHolder() instanceof EditionInventory
				? ((EditionInventory) player.getOpenInventory().getTopInventory().getHolder()).cached
				: cached;
	}

	public MMOItem getEdited() {
		return mmoitem;
	}

	public ItemStack getCachedItem() {
		return cached != null ? cached : (cached = mmoitem.newBuilder().build());
	}

	public void registerItemEdition(ConfigFile config) {
		registerItemEdition(config, true);
	}

	public void registerItemEdition(ConfigFile config, boolean uuid) {

		/*
		 * cached item needs to be flushed otherwise modifications applied
		 * cannot display on the edition GUI
		 */
		config.registerItemEdition(mmoitem.getType(), uuid ? mmoitem.getId() : null);

		/*
		 * update edited mmoitem after registering the item edition and
		 * refreshes the displayed item.
		 */
		mmoitem = MMOItems.plugin.getItems().getMMOItem(mmoitem.getType(), mmoitem.getId());
		updateCachedItem();
	}

	/*
	 * method made public so that when generating the item using the chest item,
	 * the player can reroll the item stats if needed
	 */
	public void updateCachedItem() {
		cached = mmoitem.newBuilder().build();
	}

	public void addEditionInventoryItems(Inventory inv, boolean backBool) {
		ItemStack get = new ItemStack(Material.CHEST);
		ItemMeta getMeta = get.getItemMeta();
		getMeta.addItemFlags(ItemFlag.values());
		getMeta.setDisplayName(ChatColor.GREEN + AltChar.fourEdgedClub + " Get the Item! " + AltChar.fourEdgedClub);
		List<String> getLore = new ArrayList<>();
		getLore.add(ChatColor.GRAY + "");
		getLore.add(ChatColor.GRAY + "You may also use /mi " + mmoitem.getType().getId() + " " + mmoitem.getId());
		getLore.add(ChatColor.GRAY + "");
		getLore.add(ChatColor.YELLOW + AltChar.smallListDash + " Left click to get the item.");
		getLore.add(ChatColor.YELLOW + AltChar.smallListDash + " Right click to get it & reroll its stats.");
		getMeta.setLore(getLore);
		get.setItemMeta(getMeta);

		if (backBool) {
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
