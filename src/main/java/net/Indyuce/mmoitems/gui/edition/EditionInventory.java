package net.Indyuce.mmoitems.gui.edition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.Validate;
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
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
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
		this.configFile = template.getType().getConfigFile();
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
		ConfigurationSection config = configFile.getConfig().getConfigurationSection(template.getId());
		Validate.notNull(config, "Could not find config section associated to the template '" + template.getType().getId() + "." + template.getId()
				+ "': make sure the config section name is in capital letters");
		return config.getConfigurationSection(editedModifier == null ? ".base" : ".modifiers." + editedModifier.getId() + ".stats");
	}

	/**
	 * Used in edition GUIs to display the current stat data of the edited
	 * template.
	 * 
	 * @param  stat The stat which data we are looking for
	 * @return      Optional which contains the corresponding random stat data
	 */
	public Optional<RandomStatData> getEventualStatData(ItemStat stat) {

		/*
		 * The item data map used to display what the player is currently
		 * editing. If he is editing a stat modifier, use the modifier item data
		 * map. Otherwise, use the base item data map
		 */
		Map<ItemStat, RandomStatData> map = editedModifier != null ? editedModifier.getItemData() : template.getBaseItemData();
		return map.containsKey(stat) ? Optional.of(map.get(stat)) : Optional.empty();
	}

	public void registerTemplateEdition() {
		configFile.registerTemplateEdition(template);

		/*
		 * update edited mmoitem after registering the item edition and
		 * refreshes the displayed item.
		 */
		template = MMOItems.plugin.getTemplates().getTemplate(template.getType(), template.getId());
		editedModifier = editedModifier != null ? template.getModifier(editedModifier.getId()) : null;
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
