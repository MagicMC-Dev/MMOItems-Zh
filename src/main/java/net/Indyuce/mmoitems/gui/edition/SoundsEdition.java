package net.Indyuce.mmoitems.gui.edition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.CustomSound;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.util.AltChar;

public class SoundsEdition extends EditionInventory {
	public static Map<Integer, String> correspondingSlot = new HashMap<>();

	static {
		for (CustomSound sound : CustomSound.values())
			correspondingSlot.put(sound.getSlot(), sound.getName().replace(" ", "-").toLowerCase());
	}

	public SoundsEdition(Player player, MMOItem mmoitem) {
		super(player, mmoitem);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Custom Sounds: " + mmoitem.getId());
		int[] slots = { 19, 22, 25, 28, 31, 34, 37, 40, 43 };
		int n = 0;

		FileConfiguration config = mmoitem.getType().getConfigFile().getConfig();
		for (CustomSound sound : CustomSound.values()) {
			ItemStack soundEvent = sound.getItem().clone();
			ItemMeta soundEventMeta = soundEvent.getItemMeta();
			soundEventMeta.addItemFlags(ItemFlag.values());
			soundEventMeta.setDisplayName(ChatColor.GREEN + sound.getName());
			List<String> eventLore = new ArrayList<String>();
			for (String lore : sound.getLore())
				eventLore.add(ChatColor.GRAY + lore);
			eventLore.add("");
			String configSoundName = sound.getName().replace(" ", "-").toLowerCase();
			String value = config.getString(mmoitem.getId() + ".sounds." + configSoundName + ".sound");
			if (value != null) {
				eventLore.add(ChatColor.GRAY + "Current Values:");
				eventLore.add(ChatColor.GRAY + " - Sound Name: '" + ChatColor.GREEN
						+ config.getString(mmoitem.getId() + ".sounds." + configSoundName + ".sound") + ChatColor.GRAY + "'");
				eventLore.add(ChatColor.GRAY + " - Volume: " + ChatColor.GREEN
						+ config.getDouble(mmoitem.getId() + ".sounds." + configSoundName + ".volume"));
				eventLore.add(ChatColor.GRAY + " - Pitch: " + ChatColor.GREEN
						+ config.getDouble(mmoitem.getId() + ".sounds." + configSoundName + ".pitch"));
			} else
				eventLore.add(ChatColor.GRAY + "Current Values: " + ChatColor.RED + "None");
			eventLore.add("");
			eventLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
			eventLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove this value.");
			soundEventMeta.setLore(eventLore);
			soundEvent.setItemMeta(soundEventMeta);

			inv.setItem(slots[n], soundEvent);
			n += 1;
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

		if (correspondingSlot.containsKey(event.getSlot())) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStat.CUSTOM_SOUNDS, event.getSlot()).enable("Write in the chat the custom sound you want to add.",
						ChatColor.AQUA + "Format: [SOUND NAME] [VOLUME] [PITCH]");

			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				ConfigFile config = mmoitem.getType().getConfigFile();
				String soundPath = correspondingSlot.get(event.getSlot());
				config.getConfig().set(mmoitem.getId() + ".sounds." + soundPath, null);

				// clear sound config section
				if (config.getConfig().getConfigurationSection(mmoitem.getId()).contains("sounds")) {
					if (config.getConfig().getConfigurationSection(mmoitem.getId() + ".sounds").contains(soundPath))
						if (config.getConfig().getConfigurationSection(mmoitem.getId() + ".sounds." + soundPath).getKeys(false).isEmpty())
							config.getConfig().set(mmoitem.getId() + ".sounds." + soundPath, null);
					if (config.getConfig().getConfigurationSection(mmoitem.getId() + ".sounds").getKeys(false).isEmpty())
						config.getConfig().set(mmoitem.getId() + ".sounds", null);
				}

				registerItemEdition(config);
				new SoundsEdition(player, mmoitem).open(getPreviousPage());
				player.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + MMOUtils.caseOnWords(soundPath.replace("-", " ")) + " Sound"
						+ ChatColor.GRAY + " successfully removed.");
			}
		}
	}
}