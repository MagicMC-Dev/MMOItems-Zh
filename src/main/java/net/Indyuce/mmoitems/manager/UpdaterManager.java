package net.Indyuce.mmoitems.manager;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.UpdaterData;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.VolatileMMOItem;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.wrapper.VersionWrapper;

public class UpdaterManager implements Listener {
	private final Map<String, UpdaterData> map = new HashMap<>();

	public UpdaterManager() {
		FileConfiguration config = new ConfigFile("/dynamic", "updater").getConfig();
		for (String typeFormat : config.getKeys(false))
			try {
				MMOItems.plugin.getLogger().log(Level.INFO, "Checking " + typeFormat);
				Type type = MMOItems.plugin.getTypes().getOrThrow(typeFormat);
				for (String id : config.getConfigurationSection(typeFormat).getKeys(false)) {
					MMOItems.plugin.getLogger().log(Level.INFO, "Loading " + id);
					enable(new UpdaterData(type, id, config.getConfigurationSection(typeFormat + "." + id)));
				}
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING,
						"An issue occured while trying to load dynamic updater data: " + exception.getMessage());
			}
	}

	public UpdaterData getData(MMOItem mmoitem) {
		return getData(mmoitem.getType(), mmoitem.getId());
	}

	public UpdaterData getData(Type type, String id) {
		return map.get(toPath(type, id));
	}

	@Deprecated
	public UpdaterData getData(String path) {
		return map.get(path);
	}

	public boolean hasData(MMOItem mmoitem) {
		return hasData(mmoitem.getType(), mmoitem.getId());
	}

	public boolean hasData(Type type, String id) {
		return map.containsKey(toPath(type, id));
	}

	@Deprecated
	public boolean hasData(String path) {
		return map.containsKey(path);
	}

	public Collection<UpdaterData> getActive() {
		return map.values();
	}

	public void disable(Type type, String id) {
		map.remove(toPath(type, id));
	}

	@Deprecated
	public void disable(String path) {
		map.remove(path);
	}

	public void enable(MMOItem mmoitem) {
		enable(mmoitem.getType(), mmoitem.getId());
	}

	public void enable(Type type, String id) {
		enable(new UpdaterData(type, id, UUID.randomUUID()));
	}

	@Deprecated
	public void enable(String path) {
		String[] split = path.split("\\.");
		Type type = MMOItems.plugin.getTypes().getOrThrow(split[0]);
		enable(type, split[1]);
	}

	public void enable(UpdaterData data) {
		map.put(data.getPath(), data);
	}

	/*
	 * these keys are used to easily save updater data instances
	 */
	private String toPath(Type type, String id) {
		return type.getId() + "." + id;
	}

	/*
	 * updates inventory item when clicked
	 */
	@SuppressWarnings("deprecation")
	@EventHandler
	public void a(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();
		if (item == null || item.getType() == Material.AIR)
			return;

		ItemStack newItem = getUpdated(item);
		if (!MMOUtils.areSimilar(newItem, item))
			event.setCurrentItem(newItem);
	}

	/*
	 * updated inventory item when joining
	 */
	@EventHandler
	public void b(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		player.getEquipment().setHelmet(getUpdated(player.getEquipment().getHelmet()));
		player.getEquipment().setChestplate(getUpdated(player.getEquipment().getChestplate()));
		player.getEquipment().setLeggings(getUpdated(player.getEquipment().getLeggings()));
		player.getEquipment().setBoots(getUpdated(player.getEquipment().getBoots()));

		for (int j = 0; j < 9; j++)
			player.getInventory().setItem(j, getUpdated(player.getInventory().getItem(j)));
		player.getEquipment().setItemInOffHand(getUpdated(player.getEquipment().getItemInOffHand()));
	}

	public ItemStack getUpdated(ItemStack item) {
		return getUpdated(MMOLib.plugin.getNMS().getNBTItem(item));
	}

	public ItemStack getUpdated(NBTItem item) {

		/*
		 * if the item type is null, then it is not an mmoitem and it does not
		 * need to be updated
		 */
		Type type = item.getType();
		if (type == null)
			return item.getItem();

		String id = item.getString("MMOITEMS_ITEM_ID");
		String path = type.getId() + "." + id;
		if (!map.containsKey(path))
			return item.getItem();

		/*
		 * check the internal UUID of the item, if it does not make the one
		 * stored in the item updater map then the item is outdated.
		 */
		UpdaterData did = map.get(path);
		if (did.matches(item))
			return item.getItem();

		MMOItem newItemMMO = MMOItems.plugin.getItems().getMMOItem(type, id);

		/*
		 * apply older gem stones, using a light MMOItem so the item does not
		 * calculate every stat data from the older item.
		 */
		MMOItem itemMMO = new VolatileMMOItem(item);
		if (did.hasOption(KeepOption.KEEP_GEMS) && itemMMO.hasData(ItemStat.GEM_SOCKETS))
			newItemMMO.setData(ItemStat.GEM_SOCKETS, itemMMO.getData(ItemStat.GEM_SOCKETS));

		if (did.hasOption(KeepOption.KEEP_SOULBOUND) && itemMMO.hasData(ItemStat.SOULBOUND))
			newItemMMO.setData(ItemStat.SOULBOUND, itemMMO.getData(ItemStat.SOULBOUND));

		// apply amount
		ItemStack newItem = newItemMMO.newBuilder().build();
		newItem.setAmount(item.getItem().getAmount());

		ItemMeta newItemMeta = newItem.getItemMeta();
		List<String> lore = newItemMeta.getLore();

		/*
		 * add old enchants to the item. warning - if enabled the item will
		 * remember of ANY enchant on the old item, even the enchants that were
		 * removed!
		 */
		if (did.hasOption(KeepOption.KEEP_ENCHANTS)) {
			Map<Enchantment, Integer> enchants = item.getItem().getItemMeta().getEnchants();
			for (Enchantment enchant : enchants.keySet())
				newItemMeta.addEnchant(enchant, enchants.get(enchant), true);
		}

		/*
		 * keepLore is used to save enchants from custom enchants plugins that
		 * only use lore to save enchant data
		 */
		if (did.hasOption(KeepOption.KEEP_LORE)) {
			int n = 0;
			for (String s : item.getItem().getItemMeta().getLore()) {
				if (!s.startsWith(ChatColor.GRAY + ""))
					break;
				lore.add(n++, s);
			}
		}

		/*
		 * keep durability can be used for tools to save their durability so
		 * users do not get extra durability when the item is updated
		 */
		VersionWrapper wrapper = MMOLib.plugin.getVersion().getWrapper();
		if (did.hasOption(KeepOption.KEEP_DURABILITY) && wrapper.isDamageable(item.getItem()) && wrapper.isDamageable(newItem))
			wrapper.applyDurability(newItem, newItemMeta, wrapper.getDurability(item.getItem(), item.getItem().getItemMeta()));

		/*
		 * keep name so players who renamed the item in the anvil does not have
		 * to rename it again
		 */
		if (did.hasOption(KeepOption.KEEP_NAME) && item.getItem().getItemMeta().hasDisplayName())
			newItemMeta.setDisplayName(item.getItem().getItemMeta().getDisplayName());

		newItemMeta.setLore(lore);
		newItem.setItemMeta(newItemMeta);
		return newItem;
	}

	public enum KeepOption {
		KEEP_LORE("Any lore line starting with '&7' will be", "kept when updating your item.", "", "This option is supposed to keep",
				"the item custom enchants.", ChatColor.RED + "May not support every enchant plugin."),
		KEEP_ENCHANTS("The item keeps its old enchantments."),
		KEEP_DURABILITY("The item keeps its durability.", "Don't use this option if you", "are using texture-by-durability!"),
		KEEP_NAME("The item keeps its display name."),
		KEEP_GEMS("The item keeps its empty gem", "sockets and applied gems."),
		KEEP_SOULBOUND("The item keeps its soulbound data."),
		KEEP_SKIN("Keep the item applied skins.");

		private final List<String> lore;

		private KeepOption(String... lore) {
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