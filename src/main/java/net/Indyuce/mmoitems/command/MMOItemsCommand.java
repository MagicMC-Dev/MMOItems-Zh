package net.Indyuce.mmoitems.command;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.PluginUpdate;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.drop.DropItem;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.plugin.identify.IdentifiedItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.AmountReader;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.comp.flags.FlagPlugin.CustomFlag;
import net.Indyuce.mmoitems.gui.CraftingStationView;
import net.Indyuce.mmoitems.gui.ItemBrowser;
import net.Indyuce.mmoitems.gui.edition.ItemEdition;
import net.Indyuce.mmoitems.manager.UpdaterManager.UpdaterData;
import net.Indyuce.mmoitems.stat.Lute_Attack_Effect.LuteAttackEffect;
import net.Indyuce.mmoitems.stat.Staff_Spirit.StaffSpirit;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class MMOItemsCommand implements CommandExecutor {
	private static final Random random = new Random();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("mmoitems.admin")) {
			Message.NOT_ENOUGH_PERMS_COMMAND.format(ChatColor.RED).send(sender);
			return true;
		}

		// ==================================================================================================================================
		if (args.length < 1) {
			new PluginHelp(sender).open(1);
			return true;
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("help")) {
			if (args.length < 2) {
				new PluginHelp(sender).open(1);
				return true;
			}

			int page = 0;
			try {
				page = Integer.parseInt(args[1]);
			} catch (Exception e) {
				sender.sendMessage(ChatColor.RED + args[1] + " is not a valid number.");
			}

			new PluginHelp(sender).open(page);
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("browse")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command is only for players.");
				return true;
			}

			if (args.length < 2) {
				new ItemBrowser((Player) sender).open();
				return true;
			}
			if (!Type.isValid(args[1])) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Please specify a valid item type.");
				return true;
			}

			new ItemBrowser((Player) sender, Type.get(args[1])).open();
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("update")) {
			if (args.length < 2) {
				sender.sendMessage("");
				sender.sendMessage(ChatColor.GRAY + "Sometimes updates happen to break config files due to a change in data storage. Applying a plugin config update using /mi update allows to instantly fix these issues.");
				sender.sendMessage("");
				sender.sendMessage(ChatColor.RED + "Make sure you only apply required updates! You may also consider backing up your data before applying any update.");
				sender.sendMessage("");
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "/mi update list" + ChatColor.WHITE + " lists available config updates.");
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "/mi update info <id>" + ChatColor.WHITE + " displays info about an update.");
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "/mi update apply <id> <id>" + ChatColor.WHITE + " applies a config update.");
				return true;
			}

			if (args[1].equals("info")) {
				if (args.length < 3) {
					sender.sendMessage(ChatColor.RED + "Usage: /mi update apply <id>");
					return true;
				}

				int id;
				try {
					id = Integer.parseInt(args[2]);
				} catch (NumberFormatException exception) {
					sender.sendMessage(ChatColor.RED + "Please specify a valid number.");
					return true;
				}

				if (!MMOItems.plugin.getUpdates().has(id)) {
					sender.sendMessage(ChatColor.RED + "Could not find any config update with ID " + id);
					return true;
				}

				PluginUpdate update = MMOItems.plugin.getUpdates().get(id);

				sender.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Config Update n" + update.getId());
				if (update.hasDescription()) {
					sender.sendMessage("");
					sender.sendMessage(ChatColor.DARK_GRAY + "Description:");
					for (String line : update.getDescription())
						sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.translateAlternateColorCodes('&', line));
				}

				sender.sendMessage("");
				sender.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.GOLD + "/mi update " + update.getId() + ChatColor.YELLOW + " to apply this config update.");
			}

			if (args[1].equalsIgnoreCase("list")) {
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "Available Config Updates");
				for (PluginUpdate update : MMOItems.plugin.getUpdates().getAll())
					sender.sendMessage(ChatColor.DARK_GRAY + "- Update " + update.getId());
			}

			if (args[1].equalsIgnoreCase("apply")) {
				if (args.length < 4) {
					sender.sendMessage(ChatColor.RED + "Usage: /mi update apply <id> <id>");
					return true;
				}

				int id;
				try {
					id = Integer.parseInt(args[2]);
				} catch (NumberFormatException exception) {
					sender.sendMessage(ChatColor.RED + "Please specify a valid number.");
					return true;
				}

				try {
					if (id != Integer.parseInt(args[3]))
						throw new NumberFormatException();
				} catch (NumberFormatException exception) {
					sender.sendMessage(ChatColor.RED + "Specified IDs do not match.");
					return true;
				}

				if (!MMOItems.plugin.getUpdates().has(id)) {
					sender.sendMessage(ChatColor.RED + "Could not find any config update with ID " + id);
					return true;
				}

				PluginUpdate update = MMOItems.plugin.getUpdates().get(id);
				sender.sendMessage(ChatColor.YELLOW + "Applying config update " + id + "...");
				update.apply(sender);
				sender.sendMessage(ChatColor.YELLOW + "Config update " + id + " was successfully applied.");
			}
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("checkstat")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command is only for players.");
				return true;
			}

			ItemStat stat = MMOItems.plugin.getStats().get(args[1].toUpperCase().replace("-", "_"));
			if (stat == null) {
				sender.sendMessage(ChatColor.RED + "Couldn't find the stat called " + args[1].toUpperCase().replace("-", "_") + ".");
				return true;
			}

			Player player = (Player) sender;
			player.sendMessage(ChatColor.AQUA + stat.getId() + " = " + PlayerData.get((Player) sender).getStats().getStat(stat));
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("checkflag")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command is only for players.");
				return true;
			}

			Player player = (Player) sender;
			sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------------------------------------");
			player.sendMessage(ChatColor.AQUA + "PvP = " + ChatColor.RESET + MMOItems.plugin.getFlags().isPvpAllowed(player.getLocation()));
			player.sendMessage(ChatColor.AQUA + "Abilities = " + ChatColor.RESET + MMOItems.plugin.getFlags().isFlagAllowed(player, CustomFlag.MI_ABILITIES));
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("checkattribute")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command is only for players.");
				return true;
			}

			if (args.length < 2)
				return true;

			Player player = (Player) sender;
			try {
				AttributeInstance att = player.getAttribute(Attribute.valueOf(args[1].toUpperCase().replace("-", "_")));
				sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------------------------------------");
				sender.sendMessage(ChatColor.AQUA + "Default Value = " + ChatColor.RESET + att.getDefaultValue());
				sender.sendMessage(ChatColor.AQUA + "Base Value = " + ChatColor.RESET + att.getBaseValue());
				sender.sendMessage(ChatColor.AQUA + "Value = " + ChatColor.RESET + att.getValue());
				for (AttributeModifier mod : att.getModifiers())
					sender.sendMessage(mod.getName() + " " + new DecimalFormat("0.####").format(mod.getAmount()) + " " + mod.getOperation() + " " + mod.getSlot());
			} catch (IllegalArgumentException exception) {
				player.sendMessage("Couldn't find attribute.");
			} catch (NoSuchMethodError error) {
				player.sendMessage("This command is not supported by your server version.");
			}
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("checkstats")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command is only for players.");
				return true;
			}
			
			Player player = (Player) sender;
			try {
				for(ItemStat stat : MMOItems.plugin.getStats().getAll()) {
					player.sendMessage("Stat: " + stat.getId() + "|" + stat.getName() + " | Value: " + PlayerData.get(player).getStats().getStat(stat));
				}
			} catch (IllegalArgumentException exception) {
				player.sendMessage("Couldn't find stats.");
			} catch (NoSuchMethodError error) {
				player.sendMessage("This command is not supported by your server version.");
			}
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("checkupdater")) {
			if (args.length < 2) {
				sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------------------------------------");
				for (String s : MMOItems.plugin.getUpdater().getItemPaths())
					sender.sendMessage(ChatColor.RED + s + ChatColor.WHITE + " - " + ChatColor.RED + MMOItems.plugin.getUpdater().getData(s).getUniqueId().toString());
				return true;
			}
			try {
				UpdaterData data = MMOItems.plugin.getUpdater().getData(args[1].toUpperCase().replace("-", "_"));
				sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------------------------------------");
				sender.sendMessage(ChatColor.AQUA + "UUID = " + ChatColor.RESET + data.getUniqueId().toString());
				sender.sendMessage(ChatColor.AQUA + "Keep Enchants = " + ChatColor.RESET + data.keepEnchants());
				sender.sendMessage(ChatColor.AQUA + "Keep Lore = " + ChatColor.RESET + data.keepLore());
				sender.sendMessage(ChatColor.AQUA + "Keep DefaultDurability = " + ChatColor.RESET + data.keepDurability());
				sender.sendMessage(ChatColor.AQUA + "Keep Name = " + ChatColor.RESET + data.keepName());
			} catch (Exception e) {
				sender.sendMessage("Couldn't find updater data.");
			}
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("checktags")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command is only for players.");
				return true;
			}

			Player player = (Player) sender;
			player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------------------------------------");
			for (String s : MMOItems.plugin.getNMS().getNBTItem(player.getInventory().getItemInMainHand()).getTags())
				player.sendMessage("- " + s);
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("checktag")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command is only for players.");
				return true;
			}

			Player player = (Player) sender;
			if (args.length < 2)
				return true;

			NBTItem item = MMOItems.plugin.getNMS().getNBTItem(player.getInventory().getItemInMainHand());
			player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------------------------------------");
			player.sendMessage(ChatColor.AQUA + "Boolean = " + ChatColor.RESET + item.getBoolean("MMOITEMS_" + args[1].toUpperCase().replace("-", "_")));
			player.sendMessage(ChatColor.AQUA + "Double = " + ChatColor.RESET + item.getDouble("MMOITEMS_" + args[1].toUpperCase().replace("-", "_")));
			player.sendMessage(ChatColor.AQUA + "String = " + ChatColor.RESET + item.getString("MMOITEMS_" + args[1].toUpperCase().replace("-", "_")));
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("settag")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command is only for players.");
				return true;
			}

			Player player = (Player) sender;
			if (args.length < 3)
				return true;
			try {
				player.getInventory().setItemInMainHand(MMOItems.plugin.getNMS().getNBTItem(player.getInventory().getItemInMainHand()).addTag(new ItemTag(args[1].toUpperCase().replace("-", "_"), args[2].replace("%%", " "))).toItem());
				player.sendMessage("Successfully set tag.");

			} catch (Exception e) {
				player.sendMessage("Couldn't set tag.");
			}
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("unidentify")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command is only for players.");
				return true;
			}

			Player player = (Player) sender;
			NBTItem item = MMOItems.plugin.getNMS().getNBTItem(player.getInventory().getItemInMainHand());
			if (item.getType() == null) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + "Couldn't unidentify the item you are holding.");
				return true;
			}

			if (item.getBoolean("MMOITEMS_UNIDENTIFIED")) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + "The item you are holding is already unidentified.");
				return true;
			}

			player.getInventory().setItemInMainHand(item.getType().getUnidentifiedTemplate().newBuilder(item).build());
			sender.sendMessage(MMOItems.plugin.getPrefix() + "Successfully unidentified the item you are holding.");
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("identify")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command is only for players.");
				return true;
			}

			Player player = (Player) sender;
			NBTItem item = MMOItems.plugin.getNMS().getNBTItem(player.getInventory().getItemInMainHand());
			String tag = item.getString("MMOITEMS_UNIDENTIFIED_ITEM");
			if (tag.equals("")) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + "The item you are holding is already identified.");
				return true;
			}

			player.getInventory().setItemInMainHand(new IdentifiedItem(item).identify());
			sender.sendMessage(MMOItems.plugin.getPrefix() + "Successfully identified the item you are holding.");
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("stations")) {
			if (args.length < 2) {
				sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------[" + ChatColor.LIGHT_PURPLE + " Crafting Stations " + ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "]-----------------");
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "/mi stations list" + ChatColor.WHITE + " shows available crafting stations.");
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "/mi stations open <station> (player)" + ChatColor.WHITE + " opens a station.");
				return true;
			}

			if (args[1].equalsIgnoreCase("list")) {
				sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------[" + ChatColor.LIGHT_PURPLE + " Crafting Stations " + ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "]-----------------");
				for (CraftingStation station : MMOItems.plugin.getCrafting().getAll())
					sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.WHITE + station.getId());
			}

			if (args[1].equalsIgnoreCase("open")) {
				if (args.length < 3) {
					sender.sendMessage(ChatColor.RED + "Usage: /mi stations open <station> (player)");
					return true;
				}

				if (!MMOItems.plugin.getCrafting().hasStation(args[2])) {
					sender.sendMessage(ChatColor.RED + "There is no station called " + args[2] + ".");
					return true;
				}

				Player target = args.length > 3 ? Bukkit.getPlayer(args[3]) : (sender instanceof Player ? (Player) sender : null);
				if (target == null) {
					sender.sendMessage(ChatColor.RED + "Please specify a valid player.");
					return true;
				}

				new CraftingStationView(target, MMOItems.plugin.getCrafting().getStation(args[2]), 1).open();
			}
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("info")) {
			Player player = args.length > 1 ? Bukkit.getPlayer(args[1]) : (sender instanceof Player ? (Player) sender : null);
			if (player == null) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Couldn't find the target player.");
				return true;
			}
			
			RPGPlayer rpg = PlayerData.get(player).getRPG();
			sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------[" + ChatColor.LIGHT_PURPLE + " Player Information " + ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "]-----------------");
			sender.sendMessage(ChatColor.WHITE + "Information about " + ChatColor.LIGHT_PURPLE + player.getName());
			sender.sendMessage("");
			sender.sendMessage(ChatColor.WHITE + "Player Class: " + ChatColor.LIGHT_PURPLE + rpg.getClassName());
			sender.sendMessage(ChatColor.WHITE + "Player Level: " + ChatColor.LIGHT_PURPLE + rpg.getLevel());
			sender.sendMessage(ChatColor.WHITE + "Player Mana: " + ChatColor.LIGHT_PURPLE + rpg.getMana());
			sender.sendMessage(ChatColor.WHITE + "Player Stamina: " + ChatColor.LIGHT_PURPLE + rpg.getStamina());
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("heal")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command is only for players.");
				return true;
			}

			Player player = (Player) sender;
			player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			player.setFoodLevel(20);
			player.setFireTicks(0);
			player.setSaturation(12);
			for (PotionEffectType pe : new PotionEffectType[] { PotionEffectType.POISON, PotionEffectType.BLINDNESS, PotionEffectType.CONFUSION, PotionEffectType.HUNGER, PotionEffectType.WEAKNESS, PotionEffectType.SLOW, PotionEffectType.SLOW_DIGGING })
				player.removePotionEffect(pe);
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("reload")) {
			if (args.length > 1) {
				if (args[1].equalsIgnoreCase("stations")) {
					Bukkit.getScheduler().runTaskAsynchronously(MMOItems.plugin, () -> {
						MMOItems.plugin.getCrafting().reload();
						sender.sendMessage(MMOItems.plugin.getPrefix() + "Successfully reloaded the crafting stations..");
						sender.sendMessage(MMOItems.plugin.getPrefix() + "- " + ChatColor.RED + MMOItems.plugin.getCrafting().getAll().size() + ChatColor.GRAY + " Crafting Stations");
						sender.sendMessage(MMOItems.plugin.getPrefix() + "- " + ChatColor.RED + MMOItems.plugin.getCrafting().countRecipes() + ChatColor.GRAY + " Recipes");
					});
				}

				if (args[1].equalsIgnoreCase("adv-recipes")) {
					Bukkit.getScheduler().runTaskAsynchronously(MMOItems.plugin, () -> {
						MMOItems.plugin.getRecipes().loadRecipes();
						sender.sendMessage(MMOItems.plugin.getPrefix() + "Successfully reloaded the advanced recipes.");
						sender.sendMessage(MMOItems.plugin.getPrefix() + "- " + ChatColor.RED + MMOItems.plugin.getRecipes().getRecipes().size() + ChatColor.GRAY + " Recipes");
					});
				}
				return true;
			}

			MMOItems.plugin.getLanguage().reload();
			MMOItems.plugin.getDropTables().reload();
			MMOItems.plugin.getTypes().reload();
			MMOItems.plugin.getTiers().reload();
			MMOItems.plugin.getSets().reload();
			MMOItems.plugin.getUpgrades().reload();
			sender.sendMessage(MMOItems.plugin.getPrefix() + MMOItems.plugin.getName() + " " + MMOItems.plugin.getDescription().getVersion() + " reloaded.");
			sender.sendMessage(MMOItems.plugin.getPrefix() + "- " + ChatColor.RED + MMOItems.plugin.getTypes().getAll().size() + ChatColor.GRAY + " Item Types");
			sender.sendMessage(MMOItems.plugin.getPrefix() + "- " + ChatColor.RED + MMOItems.plugin.getTiers().getAll().size() + ChatColor.GRAY + " Item Tiers");
			sender.sendMessage(MMOItems.plugin.getPrefix() + "- " + ChatColor.RED + MMOItems.plugin.getSets().getAll().size() + ChatColor.GRAY + " Item Sets");
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("copy")) {
			if (args.length < 4) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + "Usage: /mi copy <type> <copied-item-id> <new-item-id>");
				return true;
			}

			if (!Type.isValid(args[1])) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item type called " + args[1].toUpperCase().replace("-", "_") + ".");
				sender.sendMessage(MMOItems.plugin.getPrefix() + "Type " + ChatColor.GREEN + "/mi list type " + ChatColor.GRAY + "to see all the available item types.");
				return true;
			}

			Type type = Type.get(args[1]);
			ConfigFile config = type.getConfigFile();
			String id1 = args[2].toUpperCase();
			if (!config.getConfig().contains(id1)) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item called " + id1 + ".");
				return true;
			}

			String id2 = args[3].toUpperCase();
			if (config.getConfig().contains(id2)) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "There is already an item called " + id2 + "!");
				return true;
			}

			config.getConfig().set(id2, config.getConfig().getConfigurationSection(id1));
			type.registerItemEdition(config, id2);
			if (sender instanceof Player)
				new ItemEdition((Player) sender, type, id2).open();
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GREEN + "You successfully copied " + id1 + " to " + id2 + "!");
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("allitems")) {
			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "-----------------------------------------------------");
			sender.sendMessage(ChatColor.GREEN + "List of all mmoitems:");
			for (Type type : MMOItems.plugin.getTypes().getAll()) {
				FileConfiguration config = type.getConfigFile().getConfig();
				for (String s : config.getKeys(false))
					sender.sendMessage("* " + ChatColor.GREEN + s + (config.getConfigurationSection(s).contains("name") ? " " + ChatColor.WHITE + "(" + ChatColor.translateAlternateColorCodes('&', config.getString(s + ".name")) + ChatColor.WHITE + ")" : ""));
			}
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("itemlist")) {
			if (args.length < 2) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + "Usage: /mi itemlist <type>");
				return false;
			}

			if (!Type.isValid(args[1])) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item type called " + args[1].toUpperCase().replace("-", "_"));
				sender.sendMessage(MMOItems.plugin.getPrefix() + "Type " + ChatColor.GREEN + "/mi list type " + ChatColor.GRAY + "to see all the available item types.");
				return true;
			}

			Type type = Type.get(args[1]);
			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "-----------------------------------------------------");
			sender.sendMessage(ChatColor.GREEN + "List of all items in " + type.getId().toLowerCase() + ".yml:");
			FileConfiguration config = type.getConfigFile().getConfig();
			if (!(sender instanceof Player)) {
				for (String s : config.getKeys(false))
					sender.sendMessage("* " + ChatColor.GREEN + s + (config.getConfigurationSection(s).contains("name") ? " " + ChatColor.WHITE + "(" + ChatColor.translateAlternateColorCodes('&', config.getString(s + ".name")) + ChatColor.WHITE + ")" : ""));
				return true;
			}
			for (String s : config.getKeys(false)) {
				String nameFormat = config.getConfigurationSection(s).contains("name") ? " " + ChatColor.WHITE + "(" + ChatColor.translateAlternateColorCodes('&', config.getString(s + ".name")) + ChatColor.WHITE + ")" : "";
				MMOItems.plugin.getNMS().sendJson((Player) sender, "{\"text\":\"* " + ChatColor.GREEN + s + nameFormat + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/mi edit " + type.getId() + " " + s + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Click to edit " + (nameFormat.equals("") ? s : ChatColor.translateAlternateColorCodes('&', config.getString(s + ".name"))) + ChatColor.WHITE + ".\",\"color\":\"white\"}}}");
			}
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("list")) {
			if (args.length < 2) {
				sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------[" + ChatColor.LIGHT_PURPLE + " MMOItems: lists " + ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "]-----------------");
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "/mi list type " + ChatColor.WHITE + "shows all item types (sword, axe...)");
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "/mi list spirit " + ChatColor.WHITE + "shows all available staff spirits");
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "/mi list lute " + ChatColor.WHITE + "shows all available lute attack effects");
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "/mi list ability " + ChatColor.WHITE + "shows all available abilities");
				if(sender instanceof Player) {
					sender.sendMessage("");
					sender.sendMessage("Spigot Javadoc Links:");
					MMOItems.plugin.getNMS().sendJson((Player) sender, "[{\"text\":\"" + ChatColor.UNDERLINE + ChatColor.GREEN + "Materials/Blocks\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + ChatColor.GREEN + "Click to open webpage.\"}]}}},{\"text\":\" " + ChatColor.LIGHT_PURPLE + "- \"},{\"text\":\"" + ChatColor.UNDERLINE + ChatColor.GREEN + "Potion Effects\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + ChatColor.GREEN + "Click to open webpage.\"}]}}},{\"text\":\" " + ChatColor.LIGHT_PURPLE + "- \"},{\"text\":\"" + ChatColor.UNDERLINE + ChatColor.GREEN + "Sounds\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + ChatColor.GREEN + "Click to open webpage.\"}]}}}]");
					MMOItems.plugin.getNMS().sendJson((Player) sender, "[{\"text\":\"" + ChatColor.UNDERLINE + ChatColor.GREEN + "Entities/Mobs\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + ChatColor.GREEN + "Click to open webpage.\"}]}}},{\"text\":\" " + ChatColor.LIGHT_PURPLE + "- \"},{\"text\":\"" + ChatColor.UNDERLINE + ChatColor.GREEN + "Enchantments\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/enchantments/Enchantment.html\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + ChatColor.GREEN + "Click to open webpage.\"}]}}},{\"text\":\" " + ChatColor.LIGHT_PURPLE + "- \"},{\"text\":\"" + ChatColor.UNDERLINE + ChatColor.GREEN + "Particles\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Particles.html\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + ChatColor.GREEN + "Click to open webpage.\"}]}}}]");
				}
				
				return true;
			}

			// ability list
			if (args[1].equalsIgnoreCase("ability")) {
				sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------[" + ChatColor.LIGHT_PURPLE + " Abilities " + ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "]-----------------");
				sender.sendMessage(ChatColor.WHITE + "Here are all the abilities you can bind to items.");
				sender.sendMessage(ChatColor.WHITE + "The values inside brackets are " + ChatColor.UNDERLINE + "modifiers" + ChatColor.WHITE + " which allow you to change the ability values (cooldown, damage...)");
				for (Ability a : MMOItems.plugin.getAbilities().getAll()) {
					String modFormat = ChatColor.GRAY + String.join(ChatColor.WHITE + ", " + ChatColor.GRAY, a.getModifiers());
					modFormat = ChatColor.WHITE + "(" + modFormat + ChatColor.WHITE + ")";
					sender.sendMessage("* " + ChatColor.LIGHT_PURPLE + a.getName() + " " + modFormat);
				}
			}

			// item type list
			if (args[1].equalsIgnoreCase("type")) {
				sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------[" + ChatColor.LIGHT_PURPLE + " Item Types " + ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "]-----------------");
				for (Type t : MMOItems.plugin.getTypes().getAll())
					sender.sendMessage("* " + ChatColor.LIGHT_PURPLE + t.getName());
			}

			// staff spirit list
			if (args[1].equalsIgnoreCase("spirit")) {
				sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------[" + ChatColor.LIGHT_PURPLE + " Staff Spirits " + ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "]-----------------");
				for (StaffSpirit ss : StaffSpirit.values()) {
					String lore = !ss.hasLore() ? " " + ChatColor.WHITE + ">> " + ChatColor.GRAY + "" + ChatColor.ITALIC + ss.getLore() : "";
					sender.sendMessage("* " + ChatColor.LIGHT_PURPLE + ss.getName() + lore);
				}
			}

			// lute attack effect list
			if (args[1].equalsIgnoreCase("lute")) {
				sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------[" + ChatColor.LIGHT_PURPLE + " Lute Attack Effects " + ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "]-----------------");
				for (LuteAttackEffect lae : LuteAttackEffect.values())
					sender.sendMessage("* " + ChatColor.LIGHT_PURPLE + lae.getName());
			}
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("load")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command is only for players.");
				return true;
			}

			if (args.length < 3) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Usage: /mi " + args[0] + " <type> <item-id>");
				return false;
			}

			if (!Type.isValid(args[1])) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item type called " + args[1].toUpperCase().replace("-", "_") + ".");
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Type " + ChatColor.GREEN + "/mi list type" + ChatColor.RED + " to see all the available item types.");
				return true;
			}

			Type type = Type.get(args[1]);
			String name = args[2].toUpperCase().replace("-", "_");
			ConfigFile config = type.getConfigFile();
			if (config.getConfig().contains(name)) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "There is already an item called " + name + ".");
				return true;
			}

			ItemStack item = ((Player) sender).getInventory().getItemInMainHand();
			if (args[0].equalsIgnoreCase("load")) {
				if (item == null || item.getType() == Material.AIR) {
					sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Please hold something in your hand.");
					return true;
				}

				if (item.hasItemMeta()) {
					if (MMOItems.plugin.getVersion().getDurabilityHandler().isDamaged(item, item.getItemMeta()))
						config.getConfig().set(name + ".durability", MMOItems.plugin.getVersion().getDurabilityHandler().getDurability(item, item.getItemMeta()));
					if (item.getItemMeta().hasDisplayName())
						config.getConfig().set(name + ".name", item.getItemMeta().getDisplayName().replace("§", "&"));
					if (item.getItemMeta().hasLore()) {
						List<String> lore = new ArrayList<>();
						for (String line : item.getItemMeta().getLore())
							lore.add(line.replace("§", "&"));
						config.getConfig().set(name + ".lore", lore);
					}
					if (item.getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS))
						config.getConfig().set(name + ".hide-enchants", true);
					String skullTextureUrl = MMOUtils.getSkullTextureURL(item);
					if (!skullTextureUrl.equals(""))
						config.getConfig().set(name + ".skull-texture", skullTextureUrl);
				}
				if (MMOItems.plugin.getNMS().getNBTItem(item).getBoolean("Unbreakable"))
					config.getConfig().set(name + ".unbreakable", true);
				for (Enchantment enchant : item.getEnchantments().keySet())
					config.getConfig().set(name + ".enchants." + MMOItems.plugin.getVersion().getVersionWrapper().getName(enchant), item.getEnchantmentLevel(enchant));
			}
			config.getConfig().set(name + ".material", args[0].equalsIgnoreCase("load") ? item.getType().name() : type.getItem().getType().name());

			type.registerItemEdition(config, name);
			if (sender instanceof Player)
				new ItemEdition((Player) sender, type, name).open();
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GREEN + "You successfully " + args[0].replace("d", "de") + "d " + name + "!");
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("drop")) {
			if (args.length != 10) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + "Usage: /mi drop <type> <item-id> <world-name> <x> <y> <z> <drop-chance> <[min]-[max]> <unidentified-chance>");
				return true;
			}

			if (!Type.isValid(args[1])) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item type called " + args[1].toUpperCase().replace("-", "_") + ".");
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Type " + ChatColor.GREEN + "/mi list type " + ChatColor.RED + "to see all the available item types.");
				return true;
			}

			Type type = Type.get(args[1]);
			String name = args[2].toUpperCase().replace("-", "_");
			FileConfiguration config = type.getConfigFile().getConfig();
			if (!config.contains(name)) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item called " + name + ".");
				return true;
			}

			World world = Bukkit.getWorld(args[3]);
			if (world == null) {
				((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Couldn't find the world called " + args[3] + ".");
				return true;
			}

			double x, y, z, dropChance, unidentifiedChance;
			int min, max;

			try {
				x = Double.parseDouble(args[4]);
			} catch (Exception e) {
				((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + args[4] + " is not a valid number.");
				return true;
			}

			try {
				y = Double.parseDouble(args[5]);
			} catch (Exception e) {
				((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + args[5] + " is not a valid number.");
				return true;
			}

			try {
				z = Double.parseDouble(args[6]);
			} catch (Exception e) {
				((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + args[6] + " is not a valid number.");
				return true;
			}

			try {
				dropChance = Double.parseDouble(args[7]);
			} catch (Exception e) {
				((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + args[7] + " is not a valid number.");
				return true;
			}

			try {
				unidentifiedChance = Double.parseDouble(args[9]);
			} catch (Exception e) {
				((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + args[9] + " is not a valid number.");
				return true;
			}

			String[] splitAmount = args[8].split("\\-");
			if (splitAmount.length != 2) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "The drop quantity format is incorrect.");
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Format: [min]-[max]");
				return true;
			}

			try {
				min = Integer.parseInt(splitAmount[0]);
			} catch (Exception e) {
				((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + splitAmount[0] + " is not a valid number.");
				return true;
			}

			try {
				max = Integer.parseInt(splitAmount[1]);
			} catch (Exception e) {
				((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + splitAmount[1] + " is not a valid number.");
				return true;
			}

			ItemStack item = new DropItem(type, name, dropChance / 100, unidentifiedChance / 100, min, max).getItem();
			if (item == null || item.getType() == Material.AIR) {
				((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "An error occured while attempting to generate the item called " + name + ".");
				((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "See console for more information!");
				return true;
			}

			world.dropItem(new Location(world, x, y, z), item);
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove")) {
			if (args.length < 3) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Usage: /mi " + args[0] + " <type> <item-id>");
				return false;
			}

			if (!Type.isValid(args[1])) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item type called " + args[1].toUpperCase().replace("-", "_") + ".");
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Type " + ChatColor.GREEN + "/mi list type" + ChatColor.RED + " to see all the available item types.");
				return true;
			}

			Type type = Type.get(args[1]);
			String id = args[2].toUpperCase().replace("-", "_");
			ConfigFile config = type.getConfigFile();
			if (!config.getConfig().contains(id)) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item called " + id + ".");
				return true;
			}

			config.getConfig().set(id, null);
			type.registerItemEdition(config, id);

			/*
			 * remove the item updater data and uuid data from the plugin to
			 * prevent other severe issues from happening that could potentially
			 * spam your console
			 */
			String path = type.getId() + "." + id;
			MMOItems.plugin.getUpdater().disable(path);

			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GREEN + "You successfully deleted " + id + ".");
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("edit")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command is only for players.");
				return true;
			}

			if (args.length < 3) {
				((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Usage: /mi edit <type> <item-id>");
				return false;
			}

			if (!Type.isValid(args[1])) {
				((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item type called " + args[1].toUpperCase().replace("-", "_") + ".");
				((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Type " + ChatColor.GREEN + "/mi list type" + ChatColor.RED + " to see all the available item types.");
				return true;
			}

			Type type = Type.get(args[1]);
			String id = args[2].toUpperCase().replace("-", "_");
			FileConfiguration config = type.getConfigFile().getConfig();
			if (!config.contains(id)) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item called " + id + ".");
				return true;
			}

			ItemStack item = MMOItems.plugin.getItems().getItem(type, id);
			if (item == null || item.getType() == Material.AIR) {
				((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "An error occured while attempting to generate the item called " + args[2].toUpperCase() + ".");
				((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "See console for more information!");
				return true;
			}

			long old = System.currentTimeMillis();
			new ItemEdition((Player) sender, type, args[2], item).open();
			long ms = System.currentTimeMillis() - old;
			MMOItems.plugin.getNMS().sendActionBar((Player) sender, ChatColor.YELLOW + "Took " + ms + "ms (" + new DecimalFormat("#.##").format(ms / 50.) + "tick" + (ms > 99 ? "s" : "") + ") to open the menu.");
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("ability")) {
			if (args.length < 3 && !(sender instanceof Player)) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Please specify a player to use this command.");
				return true;
			}

			if (args.length < 2) {
				((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Usage: /mi ability <ability> (player) (modifier1) (value1) (modifier2) (value2)...");
				return false;
			}

			// target
			Player target = args.length > 2 ? Bukkit.getPlayer(args[2]) : (Player) sender;
			if (target == null) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Couldn't find player called " + args[2] + ".");
				return true;
			}

			// ability
			String key = args[1].toUpperCase().replace("-", "_");
			if (!MMOItems.plugin.getAbilities().hasAbility(key)) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Couldn't find ability " + key + ".");
				return true;
			}

			// modifiers
			AbilityData ability = new AbilityData(MMOItems.plugin.getAbilities().getAbility(key));
			for (int j = 3; j < args.length - 1; j += 2) {
				String name = args[j];
				String value = args[j + 1];

				try {
					ability.setModifier(name, Double.parseDouble(value));
				} catch (Exception e) {
					sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Wrong format: {" + name + " " + value + "}");
					return true;
				}
			}

			PlayerData.get(target).cast(ability);
		}
		// ==================================================================================================================================
		else if (args[0].equalsIgnoreCase("giveall")) {
			if (args.length != 5) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + "Usage: /mi giveall <type> <item-id> <[min]-[max]> <unidentified-chance>");
				return true;
			}

			if (!Type.isValid(args[1])) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item type called " + args[1].toUpperCase().replace("-", "_") + ".");
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Type " + ChatColor.GREEN + "/mi list type " + ChatColor.RED + "to see all the available item types.");
				return true;
			}

			Type type = Type.get(args[1]);
			String name = args[2].toUpperCase().replace("-", "_");
			FileConfiguration config = type.getConfigFile().getConfig();
			if (!config.contains(name)) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item called " + name + ".");
				return true;
			}
			double unidentifiedChance;
			int min, max;

			try {
				unidentifiedChance = Double.parseDouble(args[4]);
			} catch (Exception e) {
				((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + args[4] + " is not a valid number.");
				return true;
			}

			String[] splitAmount = args[3].split("\\-");
			if (splitAmount.length != 2) {
				try {
					min = Integer.parseInt(args[3]);
					max = Integer.parseInt(args[3]);
				} catch (Exception e) {
					sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "The quantity format is incorrect,");
					sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "or " + args[3] + " is not a valid number.");
					sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Format: [min]-[max]");
					return true;
				}
			}
			else
			{
				try {
					min = Integer.parseInt(splitAmount[0]);
				} catch (Exception e) {
					((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + splitAmount[0] + " is not a valid number.");
					return true;
				}

				try {
					max = Integer.parseInt(splitAmount[1]);
				} catch (Exception e) {
					((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + splitAmount[1] + " is not a valid number.");
					return true;
				}
			}

			ItemStack item = new DropItem(type, name, 1, unidentifiedChance / 100, min, max).getItem();
			if (item == null || item.getType() == Material.AIR) {
				((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "An error occured while attempting to generate the item called " + name + ".");
				((Player) sender).sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "See console for more information!");
				return true;
			}

			for(Player target : Bukkit.getOnlinePlayers())
			{
				if (target.getInventory().firstEmpty() == -1) {
					target.getWorld().dropItem(target.getLocation(), item);
					return true;
				}
				target.getInventory().addItem(item);
			}
		}
		// ==================================================================================================================================
		else if (args.length > 1) {
			if (args.length < 3 && !(sender instanceof Player)) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Please specify a player to use this command.");
				return false;
			}

			// type
			if (!Type.isValid(args[0])) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item type called " + args[0] + ".");
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Type " + ChatColor.GREEN + "/mi list type" + ChatColor.RED + " to see all the available item types.");
				return true;
			}

			// item
			Type type = Type.get(args[0]);
			ItemStack item = MMOItems.plugin.getItems().getItem(type, args[1]);
			if (item == null || item.getType() == Material.AIR) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Couldn't find/generate the item called " + args[1].toUpperCase() + ".");
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Check your console for potential item generation errors.");
				return true;
			}

			// amount
			int amount = 1;
			if (args.length > 3) {
				AmountReader amountReader = new AmountReader(args[3]);
				if (!amountReader.isValid()) {
					sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "" + args[3] + " is not a valid amount.");
					return true;
				}
				amount = amountReader.getRandomAmount();
			}
			item.setAmount(amount);

			// target
			Player target = args.length > 2 ? Bukkit.getPlayer(args[2]) : (Player) sender;
			if (target == null) {
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Couldn't find player called " + args[2] + ".");
				return true;
			}

			// unidentified chance
			double unidentifiedChance = 0;
			if (args.length > 4) {
				try {
					unidentifiedChance = Double.parseDouble(args[4]);
				} catch (Exception e) {
					sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "" + args[4] + " is not a valid number.");
					return true;
				}
			}

			// drop chance
			double dropChance = 0;
			if (args.length > 5) {
				try {
					dropChance = Double.parseDouble(args[5]);
				} catch (Exception e) {
					sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "" + args[5] + " is not a valid number.");
					return true;
				}
			}

			if (dropChance > 0 && new Random().nextDouble() > dropChance / 100)
				return true;

			if (unidentifiedChance > 0 && random.nextDouble() < unidentifiedChance / 100)
				item = type.getUnidentifiedTemplate().newBuilder(MMOItems.plugin.getNMS().getNBTItem(item)).build();

			// message
			if (sender != target)
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.YELLOW + "Successfully gave " + ChatColor.GOLD + MMOUtils.getDisplayName(item) + (item.getAmount() > 1 ? " x" + item.getAmount() : "") + ChatColor.YELLOW + " to " + ChatColor.GOLD + target.getName() + ChatColor.YELLOW + ".");
			Message.RECEIVED_ITEM.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(item), "#amount#", (item.getAmount() > 1 ? " x" + item.getAmount() : "")).send(target);

			// item
			if (target.getInventory().firstEmpty() == -1) {
				target.getWorld().dropItem(target.getLocation(), item);
				return true;
			}
			target.getInventory().addItem(item);
		}

		return false;
	}
}

