package net.Indyuce.mmoitems.gui.edition;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.CustomSound;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import io.lumine.mythic.lib.api.util.AltChar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoundsEdition extends EditionInventory {
	public static final Map<Integer, String> CORRESPONDING_SLOT = new HashMap<>();

	static {
		for (CustomSound sound : CustomSound.values())
			CORRESPONDING_SLOT.put(sound.getSlot(), sound.name().replace("_", "-").toLowerCase());
	}

	public SoundsEdition(Player player, MMOItemTemplate template) {
		super(player, template);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, "Custom Sounds: " + template.getId());
		int n = 0;

		for (CustomSound sound : CustomSound.values()) {
			ItemStack soundEvent = sound.getItem().clone();
			ItemMeta soundEventMeta = soundEvent.getItemMeta();
			soundEventMeta.addItemFlags(ItemFlag.values());
			soundEventMeta.setDisplayName(ChatColor.GREEN + sound.getName());
			List<String> eventLore = new ArrayList<>();
			for (String lore : sound.getLore())
				eventLore.add(ChatColor.GRAY + lore);
			eventLore.add("");
			String configSoundName = sound.getName().replace(" ", "-").toLowerCase();
			String value = getEditedSection().getString("sounds." + configSoundName + ".sound");
			if (value != null) {
				eventLore.add(ChatColor.GRAY + "Current Values:");
				eventLore.add(ChatColor.GRAY + " - Sound Name: '" + ChatColor.GREEN
						+ getEditedSection().getString("sounds." + configSoundName + ".sound") + ChatColor.GRAY + "'");
				eventLore.add(
						ChatColor.GRAY + " - Volume: " + ChatColor.GREEN + getEditedSection().getDouble("sounds." + configSoundName + ".volume"));
				eventLore.add(ChatColor.GRAY + " - Pitch: " + ChatColor.GREEN + getEditedSection().getDouble("sounds." + configSoundName + ".pitch"));
			} else
				eventLore.add(ChatColor.GRAY + "Current Values: " + ChatColor.RED + "None");
			eventLore.add("");
			eventLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
			eventLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove this value.");
			soundEventMeta.setLore(eventLore);
			soundEvent.setItemMeta(soundEventMeta);

			inv.setItem(sound.getSlot(), soundEvent);
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

		if (CORRESPONDING_SLOT.containsKey(event.getSlot())) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStats.CUSTOM_SOUNDS, CORRESPONDING_SLOT.get(event.getSlot())).enable("Write in the chat the custom sound you want to add.",
						ChatColor.AQUA + "Format: [SOUND NAME] [VOLUME] [PITCH]",
						ChatColor.AQUA + "Example: entity.generic.drink 1 1");

			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				String soundPath = CORRESPONDING_SLOT.get(event.getSlot());
				getEditedSection().set("sounds." + soundPath, null);

				// clear sound config section
				if (getEditedSection().contains("sounds." + soundPath)
						&& getEditedSection().getConfigurationSection("sounds." + soundPath).getKeys(false).isEmpty()) {
					getEditedSection().set("sounds." + soundPath, null);
					if (getEditedSection().getConfigurationSection("sounds").getKeys(false).isEmpty())
						getEditedSection().set("sounds", null);
				}

				registerTemplateEdition();
				player.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + MMOUtils.caseOnWords(soundPath.replace("-", " ")) + " Sound"
						+ ChatColor.GRAY + " successfully removed.");
			}
		}
	}
}