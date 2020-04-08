package net.Indyuce.mmoitems.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.wrapper.VersionWrapper;

public class UpdaterManager implements Listener {
	private final Map<String, UpdaterData> map = new HashMap<>();

	public UpdaterManager() {
		FileConfiguration updater = new ConfigFile("/dynamic", "updater").getConfig();
		for (String type : updater.getKeys(false))
			for (String id : updater.getConfigurationSection(type).getKeys(false)) {
				String path = type + "." + id;
				enable(new UpdaterData(path, updater));
			}
	}

	public UpdaterData getData(String path) {
		return map.get(path);
	}

	public boolean hasData(String path) {
		return map.containsKey(path);
	}

	public Collection<UpdaterData> getDatas() {
		return map.values();
	}

	public Set<String> getItemPaths() {
		return map.keySet();
	}

	public void disable(String path) {
		map.remove(path);
	}

	public void enable(String path) {
		enable(new UpdaterData(path, UUID.randomUUID()));
	}

	public void enable(UpdaterData data) {
		map.put(data.path, data);
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
		MMOItem itemMMO = new MMOItem(item, false);
		if (did.keepGems() && itemMMO.hasData(ItemStat.GEM_SOCKETS))
			newItemMMO.setData(ItemStat.GEM_SOCKETS, itemMMO.getData(ItemStat.GEM_SOCKETS));

		if (did.keepSoulbound() && itemMMO.hasData(ItemStat.SOULBOUND))
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
		if (did.keepEnchants()) {
			Map<Enchantment, Integer> enchants = item.getItem().getItemMeta().getEnchants();
			for (Enchantment enchant : enchants.keySet())
				newItemMeta.addEnchant(enchant, enchants.get(enchant), true);
		}

		/*
		 * keepLore is used to save enchants from custom enchants plugins that
		 * only use lore to save enchant data
		 */
		if (did.keepLore()) {
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
		if (did.keepDurability() && wrapper.isDamageable(item.getItem()) && wrapper.isDamageable(newItem))
			wrapper.applyDurability(newItem, newItemMeta, wrapper.getDurability(item.getItem(), item.getItem().getItemMeta()));

		/*
		 * keep name so players who renamed the item in the anvil does not have
		 * to rename it again
		 */
		if (did.keepName() && item.getItem().getItemMeta().hasDisplayName())
			newItemMeta.setDisplayName(item.getItem().getItemMeta().getDisplayName());

		newItemMeta.setLore(lore);
		newItem.setItemMeta(newItemMeta);
		return newItem;
	}

	public UpdaterData newUpdaterData(String path, FileConfiguration config) {
		return new UpdaterData(path, config);
	}

	public UpdaterData newUpdaterData(String path, UUID uuid, boolean keepLore, boolean keepEnchants, boolean keepDurability, boolean keepName,
			boolean keepGems, boolean keepSoulbound) {
		return new UpdaterData(path, uuid, keepLore, keepEnchants, keepDurability, keepName, keepGems, keepSoulbound);
	}

	public class UpdaterData {

		// itemType.name() + "." + itemId
		private final String path;

		/*
		 * two UUIDs can be found : one on the itemStack in the nbttags, and one
		 * in the UpdaterData instance. if the two match, the item is up to
		 * date. if they don't match, the item needs to be updated
		 */
		private final UUID uuid;

		private boolean keepLore, keepDurability, keepEnchants, keepName, keepGems, keepSoulbound;

		public UpdaterData(String path, FileConfiguration config) {
			this(path, UUID.fromString(config.getString(path + ".uuid")), config.getBoolean(path + ".lore"), config.getBoolean(path + ".enchants"),
					config.getBoolean(path + ".durability"), config.getBoolean(path + ".name"), config.getBoolean(path + ".gems"),
					config.getBoolean(path + ".soulbound"));
		}

		public UpdaterData(String path, UUID uuid) {
			this(path, uuid, false, false, false, false, false, false);
		}

		public UpdaterData(String path, UUID uuid, boolean keepLore, boolean keepEnchants, boolean keepDurability, boolean keepName, boolean keepGems,
				boolean keepSoulbound) {
			this.uuid = uuid;
			this.path = path;

			this.keepLore = keepLore;
			this.keepEnchants = keepEnchants;
			this.keepDurability = keepDurability;
			this.keepName = keepName;
			this.keepGems = keepGems;
			this.keepSoulbound = keepSoulbound;
		}

		public void save(FileConfiguration config) {
			config.set(path + ".lore", keepLore);
			config.set(path + ".enchants", keepEnchants);
			config.set(path + ".durability", keepDurability);
			config.set(path + ".name", keepName);
			config.set(path + ".gems", keepGems);
			config.set(path + ".soulbound", keepSoulbound);
			config.set(path + ".uuid", uuid.toString());
		}

		public UUID getUniqueId() {
			return uuid;
		}

		public boolean matches(NBTItem item) {
			return uuid.toString().equals(item.getString("MMOITEMS_ITEM_UUID"));
		}

		public boolean keepLore() {
			return keepLore;
		}

		public boolean keepDurability() {
			return keepDurability;
		}

		public boolean keepEnchants() {
			return keepEnchants;
		}

		public boolean keepName() {
			return keepName;
		}

		public boolean keepGems() {
			return keepGems;
		}

		public boolean keepSoulbound() {
			return keepSoulbound;
		}

		public void setKeepLore(boolean value) {
			keepLore = value;
		}

		public void setKeepDurability(boolean value) {
			keepDurability = value;
		}

		public void setKeepEnchants(boolean value) {
			keepEnchants = value;
		}

		public void setKeepName(boolean value) {
			keepName = value;
		}

		public void setKeepGems(boolean value) {
			keepGems = value;
		}

		public void setKeepSoulbound(boolean value) {
			keepSoulbound = value;
		}
	}
}