package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.CommandListEdition;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;

public class Commands extends ItemStat {
	private static final int max = 15;

	public Commands() {
		super(new ItemStack(VersionMaterial.COMMAND_BLOCK_MINECART.toMaterial()), "Commands", new String[] { "The commands your item", "performs when right clicked." }, "commands", new String[] { "!armor", "!gem_stone", "all" });
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		new CommandListEdition(inv.getPlayer(), inv.getItemType(), inv.getItemId()).open(inv.getPage());
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		if (config.getConfig().getConfigurationSection(inv.getItemId()).contains("commands"))
			if (config.getConfig().getConfigurationSection(inv.getItemId() + ".commands").getKeys(false).size() >= max) {
				// max command number = 8
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Your item has reached the " + max + " commands limit.");
				return false;
			}

		double delay = 0;
		boolean console = false, op = false;

		String[] split = message.split("\\ ");
		for (int j = 0; j < split.length && split[j].startsWith("-"); j++) {
			String arg = split[j];
			if (arg.startsWith("-d:")) {
				try {
					delay = Double.parseDouble(arg.substring(3));
				} catch (NumberFormatException e) {
					// cant read delay.
				}
				message = message.replaceFirst(arg + " ", "");
				continue;
			} else if (arg.equalsIgnoreCase("-c")) {
				console = true;
				message = message.replaceFirst(arg + " ", "");
				continue;
			} else if (arg.equalsIgnoreCase("-op")) {
				op = true;
				message = message.replaceFirst(arg + " ", "");
				continue;
			}
		}

		/*
		 * determine the command ID based on the command IDs which have been
		 * registered before.
		 */
		ConfigurationSection commands = config.getConfig().getConfigurationSection(inv.getItemId() + ".commands");
		String path = "cmd" + (max + 1);
		if (commands == null)
			path = "cmd0";
		else
			for (int j = 0; j < max; j++)
				if (!commands.contains("cmd" + j)) {
					path = "cmd" + j;
					break;
				}

		config.getConfig().set(inv.getItemId() + ".commands." + path + ".format", message);
		config.getConfig().set(inv.getItemId() + ".commands." + path + ".delay", delay);
		config.getConfig().set(inv.getItemId() + ".commands." + path + ".console", console ? console : null);
		config.getConfig().set(inv.getItemId() + ".commands." + path + ".op", op ? op : null);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Command successfully registered.");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		lore.add(ChatColor.GRAY + "Current Commands: " + ChatColor.RED + (config.getConfigurationSection(path).contains("commands") ? config.getConfigurationSection(path + ".commands").getKeys(false).size() : 0));
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to edit item commands.");
	}

	@Override
	public boolean whenLoaded(MMOItem item, ConfigurationSection config) {
		if (!config.contains("commands"))
			return true;

		CommandListData list = new CommandListData();
		
		for (String key : config.getConfigurationSection("commands").getKeys(false)) {
			ConfigurationSection section = config.getConfigurationSection("commands." + key);
			try {
				list.add(list.newCommandData(section.getString("format"), section.getDouble("delay"), section.getBoolean("console"), section.getBoolean("op")));
			} catch (IllegalArgumentException exception) {
				item.log(Level.WARNING, "Couldn't load command ID " + section.getName());
			}
		}
		
		item.setData(ItemStat.COMMANDS, list);
		return true;
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		JsonArray array = new JsonArray();
		List<String> lore = new ArrayList<>();

		String commandFormat = ItemStat.translate("command");
		((CommandListData) data).getCommands().forEach(command -> {

			JsonObject object = new JsonObject();
			object.addProperty("Command", command.getCommand());
			object.addProperty("Delay", command.getDelay());
			object.addProperty("Console", command.isConsoleCommand());
			object.addProperty("Op", command.hasOpPerms());
			array.add(object);

			lore.add(commandFormat.replace("#c", "/" + command.getCommand()).replace("#d", "" + command.getDelay()));
		});

		item.getLore().insert("commands", lore);
		item.addItemTag(new ItemTag("MMOITEMS_COMMANDS", array.toString()));
		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem nbtItem) {
		if (nbtItem.hasTag("MMOITEMS_COMMANDS"))
			try {
				CommandListData commands = new CommandListData();

				new JsonParser().parse(nbtItem.getString("MMOITEMS_COMMANDS")).getAsJsonArray().forEach(element -> {
					JsonObject key = element.getAsJsonObject();
					commands.add(commands.newCommandData(key.get("Command").getAsString(), key.get("Delay").getAsDouble(), key.get("Console").getAsBoolean(), key.get("Op").getAsBoolean()));
				});

				mmoitem.setData(ItemStat.COMMANDS, commands);
			} catch (JsonSyntaxException exception) {
				/*
				 * OLD ITEM WHICH MUST BE UPDATED.
				 */
			}
	}

	public class CommandListData extends StatData {
		private Set<CommandData> commands = new HashSet<>();

		public CommandListData() {
		}

		public CommandListData(CommandData... commands) {
			add(commands);
		}

		public void add(CommandData... commands) {
			for (CommandData command : commands)
				this.commands.add(command);
		}

		public Set<CommandData> getCommands() {
			return commands;
		}

		public CommandData newCommandData(String command, double delay, boolean console, boolean op) {
			return new CommandData(command, delay, console, op);
		}

		public class CommandData {
			private String command;
			private double delay;
			private boolean console, op;

			private CommandData(String command, double delay, boolean console, boolean op) {
				Validate.notNull(command, "Command cannot be null");

				this.command = command;
				this.delay = delay;
				this.console = console;
				this.op = op;
			}

			public String getCommand() {
				return command;
			}

			public double getDelay() {
				return delay;
			}

			public boolean hasDelay() {
				return delay > 0;
			}

			public boolean isConsoleCommand() {
				return console;
			}

			public boolean hasOpPerms() {
				return op;
			}

			public String getParsed(Player player) {
				return MMOItems.plugin.getPlaceholderParser().parse(player, command);
			}
		}
	}
}
