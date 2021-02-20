package net.Indyuce.mmoitems.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.LegacyComponent;
import io.lumine.mythic.utils.adventure.text.Component;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdaterManager implements Listener {
	/*public UpdaterManager() {
		FileConfiguration config = new ConfigFile("/dynamic", "updater").getConfig();
		for (String typeFormat : config.getKeys(false))
			try {
				Type type = MMOItems.plugin.getTypes().getOrThrow(typeFormat);
				for (String id : config.getConfigurationSection(typeFormat).getKeys(false)) {
					MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplateOrThrow(type, id);
					//enable(new UpdaterData(template, config.getConfigurationSection(typeFormat + "." + id)));
				}
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING,
						"An issue occurred while trying to load dynamic updater data: " + exception.getMessage());
			}
	}*/

	/**
	 * Updates inventory item when an item is clicked in a player's inventory
	 */
	@EventHandler
	public void updateOnClick(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();
		if (item == null || item.getType() == Material.AIR) return;

		ItemStack newItem = getUpdated(item, (Player) event.getWhoClicked());
		if (!newItem.equals(item)) event.setCurrentItem(newItem);
	}

	/**
	 * Updates a player inventory when joining
	 */
	@EventHandler
	public void updateOnJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		player.getEquipment().setHelmet(getUpdated(player.getEquipment().getHelmet(), player));
		player.getEquipment().setChestplate(getUpdated(player.getEquipment().getChestplate(), player));
		player.getEquipment().setLeggings(getUpdated(player.getEquipment().getLeggings(), player));
		player.getEquipment().setBoots(getUpdated(player.getEquipment().getBoots(), player));

		for (int j = 0; j < 9; j++)
			player.getInventory().setItem(j, getUpdated(player.getInventory().getItem(j), player));
		player.getEquipment().setItemInOffHand(getUpdated(player.getEquipment().getItemInOffHand(), player));
	}

	public ItemStack getUpdated(ItemStack item, Player target) {
		return getUpdated(MythicLib.plugin.getVersion().getWrapper().getNBTItem(item), target);
	}

	public ItemStack getUpdated(NBTItem item, Player target) {

		/*
		 * If the item type is null, then it is not an mmoitem and it does not
		 * need to be updated
		 */
		Type type = Type.get(item.getType());
		if (type == null) return item.getItem();

		/*
		 * check the internal UUID of the item, if it does not make the one
		 * stored in the item updater data then the item is outdated.

		UpdaterData did = data.getValue(type, item.getString("MMOITEMS_ITEM_ID"));
		if (did.matches(item))
			return item.getItem();

		 */

		MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(type, item.getString("MMOITEMS_ITEM_ID"));
		MMOItem newMMOItem = template.newBuilder(PlayerData.get(target).getRPG()).build();

		/*
		 * apply older gem stones, using a light MMOItem so the item does not
		 * calculate every stat data from the older item.
		 */
		//MMOItem volatileItem = new VolatileMMOItem(item);
		/*if (did.hasOption(KeepOption.KEEP_GEMS) && volatileItem.hasData(ItemStats.GEM_SOCKETS))
			newMMOItem.replaceData(ItemStats.GEM_SOCKETS, volatileItem.getData(ItemStats.GEM_SOCKETS));

		if (did.hasOption(KeepOption.KEEP_SOULBOUND) && volatileItem.hasData(ItemStats.SOULBOUND))
			newMMOItem.replaceData(ItemStats.SOULBOUND, volatileItem.getData(ItemStats.SOULBOUND));*/

		// if (did.hasOption(KeepOption.KEEP_SKIN) && itemMMO.hasData(stat))

		// apply amount
		ItemStack newItem = newMMOItem.newBuilder().build();
		newItem.setAmount(item.getItem().getAmount());

		ItemMeta newItemMeta = newItem.getItemMeta();
		List<String> lore = newItemMeta.getLore();

		/*
		 * add old enchants to the item. warning - if enabled the item will
		 * remember of ANY enchant on the old item, even the enchants that were
		 * removed!
		 */
		//if (did.hasOption(KeepOption.KEEP_ENCHANTS))
		item.getItem().getItemMeta().getEnchants().forEach((enchant, level) -> newItemMeta.addEnchant(enchant, level, true));

		/*
		 * keepLore is used to save enchants from custom enchants plugins that
		 * only use lore to save enchant data
		 */
		//if (did.hasOption(KeepOption.KEEP_LORE)) {
		int n = 0;
		for (String s : item.getItem().getItemMeta().getLore()) {
			if (!s.startsWith(ChatColor.GRAY + "")) break;
			lore.add(n++, s);
		}
		//}

		/*
		 * keep durability can be used for tools to save their durability so
		 * users do not get extra durability when the item is updated
		 */
		//if (did.hasOption(KeepOption.KEEP_DURABILITY) && item.getItem().getItemMeta() instanceof Damageable && newItemMeta instanceof Damageable)
		((Damageable) newItemMeta).setDamage(((Damageable) item.getItem().getItemMeta()).getDamage());

		newItem.setItemMeta(newItemMeta);
		NBTItem nbtItem = NBTItem.get(newItem);
		/*
		 * keep name so players who renamed the item in the anvil does not have
		 * to rename it again
		 */
		//if (did.hasOption(KeepOption.KEEP_NAME) && item.getItem().getItemMeta().hasDisplayName())
		nbtItem.setDisplayNameComponent(LegacyComponent.parse(item.getItem().getItemMeta().getDisplayName()));

		List<Component> componentLore = new ArrayList<>();
		lore.forEach(line -> componentLore.add(LegacyComponent.parse(line)));
		nbtItem.setLoreComponents(componentLore);

		return nbtItem.toItem();
	}

	public enum KeepOption {
		KEEP_LORE("Any lore line starting with '&7' will be", "kept when updating your item.", "", "This option is supposed to keep", "the item custom enchants.", ChatColor.RED + "May not support every enchant plugin."),
		KEEP_ENCHANTS("The item keeps its old enchantments."),
		KEEP_DURABILITY("The item keeps its durability.", "Don't use this option if you", "are using texture-by-durability!"),
		KEEP_NAME("The item keeps its display name."),
		KEEP_GEMS("The item keeps its empty gem", "sockets and applied gems."),
		KEEP_SOULBOUND("The item keeps its soulbound data."),
		// KEEP_SKIN("Keep the item applied skins."),
		;

		private final List<String> lore;

		KeepOption(String... lore) {
			this.lore = Arrays.asList(lore);
		}

		public List<String> getLore() {
			return lore;
		}

		public String getPath() {
			return name().toLowerCase().replace("_", "-").substring(5);
		}
	}
}