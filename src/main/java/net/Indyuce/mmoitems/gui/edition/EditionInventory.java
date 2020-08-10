package net.Indyuce.mmoitems.gui.edition;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.item.template.TemplateModifier;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.gui.PluginInventory;
import net.mmogroup.mmolib.api.util.AltChar;

public abstract class EditionInventory extends PluginInventory {

	/**
	 * Item template currently being edited. This field is not final as it is
	 * refreshed every time the item is edited (after applying a config change,
	 * MMOItems updates the registered template and removes the old one)
	 */
	protected MMOItemTemplate template;

	/**
	 * Config file being edited. It is cached when the edition inventory is
	 * opened and can only be accessed through the getEditedSection() method
	 */
	private final ConfigFile configFile;

	/**
	 * Template modifier being edited, if it is null then the player is directly
	 * base item data
	 */
	private TemplateModifier editedModifier;

	private ItemStack cachedItem;
	private int previousPage;

	public EditionInventory(Player player, MMOItemTemplate template) {
		super(player);

		this.template = template;
		configFile = template.getType().getConfigFile();
		if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory().getHolder() instanceof EditionInventory)
			this.cachedItem = ((EditionInventory) player.getOpenInventory().getTopInventory().getHolder()).cachedItem;
	}

	public MMOItemTemplate getEdited() {
		return template;
	}

	/**
	 * @return The currently edited configuration section. It depends if the
	 *         player is editing the base item data or editing a modifier. This
	 *         config section contains item data (either the 'base' config
	 *         section or the 'stats' section for modifiers).
	 */
	public ConfigurationSection getEditedSection() {
		return configFile.getConfig()
				.getConfigurationSection(template.getId() + (editedModifier == null ? ".base" : ".modifiers." + editedModifier.getId() + ".stats"));
	}

	public void registerTemplateEdition() {

		configFile.registerTemplateEdition(template);

		/*
		 * update edited mmoitem after registering the item edition and
		 * refreshes the displayed item.
		 */
		template = MMOItems.plugin.getItems().getTemplate(template.getType(), template.getId());
		updateCachedItem();

		open();
	}

	/**
	 * Method used when the player gets the item using the chest item so that he
	 * can reroll the stats.
	 */
	public void updateCachedItem() {
		cachedItem = template.newBuilder(PlayerData.get(getPlayer()).getRPG()).build().newBuilder().build();
	}

	public ItemStack getCachedItem() {
		if (cachedItem != null)
			return cachedItem;

		updateCachedItem();
		return cachedItem;
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
		previousPage = page;
		open();
	}

	public int getPreviousPage() {
		return previousPage;
	}
}
