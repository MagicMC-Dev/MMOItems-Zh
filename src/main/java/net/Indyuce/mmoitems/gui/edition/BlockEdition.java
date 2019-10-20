package net.Indyuce.mmoitems.gui.edition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
import net.Indyuce.mmoitems.api.CustomBlock;
import net.Indyuce.mmoitems.api.edition.BlockChatEdition;
import net.Indyuce.mmoitems.api.item.plugin.ConfigItem;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.BlockBrowser;
import net.Indyuce.mmoitems.gui.PluginInventory;

public class BlockEdition extends PluginInventory {
	public static Map<Integer, ConfigOptions> correspondingSlot = new HashMap<>();
	FileConfiguration config;
	CustomBlock block;
	
	public BlockEdition(Player player, CustomBlock block) {
		super(player);

		this.block = block;
		config = MMOItems.plugin.getCustomBlocks().getConfig();
		
		if (correspondingSlot.isEmpty()) {
			for (ConfigOptions configOptions : ConfigOptions.values()) {
				correspondingSlot.put(configOptions.getSlot(), configOptions);
			}
		}
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Block Edition: " );
		for (ConfigOptions configOptions : ConfigOptions.values()) {
			ItemStack blockItem = new ItemStack(configOptions.getItem());
			ItemMeta meta = blockItem.getItemMeta();
			meta.setDisplayName(ChatColor.GREEN + configOptions.name().replace("_", " "));
			meta.addItemFlags(ItemFlag.values());
			List<String> eventLore = new ArrayList<String>();
			eventLore.add("");
			if(configOptions.path.equals("lore")) {
				eventLore.add(ChatColor.GRAY + "- Current Value:");
				
				List<String> loreList = config.getStringList(block.getId() + ".lore");
				if(loreList.isEmpty()) eventLore.add(ChatColor.RED + "No lore.");
				for(String lore : loreList)
					eventLore.add(ChatColor.GREEN + ChatColor.translateAlternateColorCodes('&', lore));
			}
			else eventLore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GREEN + (configOptions.format.equals("int") ? config.contains(block.getId() + "." + configOptions.path) ? ChatColor.GREEN + config.getString(block.getId() + "." + configOptions.path) : ChatColor.RED + "0" : ChatColor.translateAlternateColorCodes('&', config.getString(block.getId() + "." + configOptions.path, ChatColor.RED + "Default"))));
					
			eventLore.add("");
			eventLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
			eventLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove this value.");
			meta.setLore(eventLore);
			blockItem.setItemMeta(meta);

			inv.setItem(configOptions.slot, blockItem);
		}
		
		ItemStack back = ConfigItem.BACK.getItem();
		inv.setItem(40, back);
		
		return inv;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isPluginItem(item, false))
			return;

		if(event.getSlot() == 40)
			new BlockBrowser(player).open();
		
		if (correspondingSlot.containsKey(event.getSlot())) {
			if (event.getAction() == InventoryAction.PICKUP_ALL) {
				ConfigOptions co = correspondingSlot.get(event.getSlot());
				new BlockChatEdition(this, co, block.getId()).enable("Write in the chat the " + co.getChatFormat());
			}
				
			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				String path = correspondingSlot.get(event.getSlot()).getConfigPath();
				config.set(block.getId() + "." + path, null);
				try { config.save(new File(MMOItems.plugin.getDataFolder(), "custom-blocks.yml"));
				} catch (IOException e) { e.printStackTrace(); }
				MMOItems.plugin.getCustomBlocks().reload();

				new BlockEdition(player, block).open();
				player.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + MMOUtils.caseOnWords(path.replace("-", " ")) + " Value" + ChatColor.GRAY + " successfully removed.");
			}
		}
	}
	
	public enum ConfigOptions {
		DISPLAY_NAME("display-name", Material.WRITABLE_BOOK, 11, "string"),
		LORE("lore", Material.MAP, 13, "line"),
		REQUIRED_PICKAXE_POWER("required-power", Material.IRON_PICKAXE, 15, "int"),
		MIN_XP("min-xp", Material.EXPERIENCE_BOTTLE, 21, "int"),
		MAX_XP("max-xp", Material.EXPERIENCE_BOTTLE, 23, "int"),
		WORLD_GEN_TEMPLATE("gen-template", Material.GRASS_BLOCK, 31, "string"),
		;
		
		private final String path;
		private final String format;
		private final Material item;
		private final int slot;
		
		ConfigOptions(String s1, Material m, int i, String s2) {
			path = s1;
			item = m;
			slot = i;
			format = s2;
		}
		
		public String getConfigPath()
		{ return path; }
		public String getFormat()
		{ return format; }
		public Material getItem()
		{ return item; }
		public int getSlot()
		{ return slot; }

		public String getChatFormat() {
			switch(format) {
			case "int":
				return "desired number for this field.";
			case "line":
				return "new line to add.";
			default:
				return "new value.";
			}
		}
		
		public boolean whenInput(PluginInventory inv, String message, String path) {
			switch(format) {
			case "int":
				int value = 0;
				try {
					value = Integer.parseInt(message);
				} catch (Exception e1) {
					inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid number.");
					return false;
				}
				setConfigValue(value, path);
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + name().replace("_", " ") + " successfully changed to " + value + ".");
				break;
			case "line":
				FileConfiguration config = MMOItems.plugin.getCustomBlocks().getConfig();
				List<String> lore = config.contains(path) ? config.getStringList(path) : new ArrayList<>();
				lore.add(message);
				setConfigValue(lore, path);
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully added to " + message + " to block lore.");
				break;
			default:
				setConfigValue(message, path);
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully changed value to " + message + ".");
				break;
			}
			
			return true;
		};
		
		public void setConfigValue(Object value, String path) {
			FileConfiguration config = MMOItems.plugin.getCustomBlocks().getConfig();
			
			config.set(path, value);
			try { config.save(new File(MMOItems.plugin.getDataFolder(), "custom-blocks.yml"));
			} catch (IOException e) { e.printStackTrace(); }
			MMOItems.plugin.getCustomBlocks().reload();
		}
	}
}