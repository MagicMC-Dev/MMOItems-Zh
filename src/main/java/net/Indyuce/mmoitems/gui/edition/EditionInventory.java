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
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.PluginInventory;
import net.mmogroup.mmolib.version.VersionMaterial;

public abstract class EditionInventory extends PluginInventory {
	protected Type type;
	protected String id;

	private int prevPage;
	private ItemStack cached;

	public EditionInventory(Player player, Type type, String id) {
		this(player, type, id, null);
	}

	public EditionInventory(Player player, Type type, String id, ItemStack cached) {
		super(player);
		this.type = type;
		this.id = id == null ? null : id.toUpperCase().replace("-", "_").replace(" ", "_");
		this.cached = player.getOpenInventory() != null && player.getOpenInventory().getTopInventory().getHolder() instanceof EditionInventory ? ((EditionInventory) player.getOpenInventory().getTopInventory().getHolder()).cached : cached;
	}

	public Type getItemType() {
		return type;
	}

	public String getItemId() {
		return id;
	}

	public boolean hasCachedItem() {
		return cached != null;
	}

	public void flushItem() {
		cached = null;
	}

	public void registerItemEdition(ConfigFile config, boolean uuid) {
		flushItem();
		getItemType().registerItemEdition(config, uuid ? id : null);
	}

	public void registerItemEdition(ConfigFile config) {
		registerItemEdition(config, true);
	}

	/*
	 * the item is cached in the inventory class to allow GUIs not to generate
	 * the item each time the user goes to another GUI or page
	 */
	public ItemStack getCachedItem() {
		return cached != null ? cached : (cached = MMOItems.plugin.getItems().getItem(type, id));
	}

	public void addEditionInventoryItems(Inventory inv, boolean backBool) {
		ItemStack get = new ItemStack(VersionMaterial.GUNPOWDER.toMaterial());
		ItemMeta getMeta = get.getItemMeta();
		getMeta.addItemFlags(ItemFlag.values());
		getMeta.setDisplayName(ChatColor.GREEN + AltChar.fourEdgedClub + " Get the Item! " + AltChar.fourEdgedClub);
		List<String> getLore = new ArrayList<>();
		getLore.add(ChatColor.GRAY + "You can also use /mi " + type.getId() + " " + id + " (player) (amount).");
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
