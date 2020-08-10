package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.CommandListEdition;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.CommandData;
import net.Indyuce.mmoitems.stat.data.CommandListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.util.AltChar;
import net.mmogroup.mmolib.version.VersionMaterial;

public class Commands extends ItemStat {
	private static final int max = 15;

	public Commands() {
		super("COMMANDS", new ItemStack(VersionMaterial.COMMAND_BLOCK_MINECART.toMaterial()), "Commands",
				new String[] { "The commands your item", "performs when right clicked." }, new String[] { "!armor", "!block", "!gem_stone", "all" });
	}

	@Override
	public CommandListData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		ConfigurationSection config = (ConfigurationSection) object;

		CommandListData list = new CommandListData();

		for (String key : config.getKeys(false)) {
			ConfigurationSection section = config.getConfigurationSection(key);
			list.add(new CommandData(section.getString("format"), section.getDouble("delay"), section.getBoolean("console"),
					section.getBoolean("op")));
		}

		return list;
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		new CommandListEdition(inv.getPlayer(), inv.getEdited()).open(inv.getPage());
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {
		if (inv.getEditedSection().contains("commands"))
			if (inv.getEditedSection().getConfigurationSection("commands").getKeys(false).size() >= max) {
				// max command number = 8
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Your item has reached the " + max + " commands limit.");
				return;
			}

		double delay = 0;
		boolean console = false, op = false;

		String[] split = message.split("\\ ");
		for (int j = 0; j < split.length && split[j].startsWith("-"); j++) {
			String arg = split[j];
			if (arg.startsWith("-d:")) {
				delay = Double.parseDouble(arg.substring(3));
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
		ConfigurationSection commands = inv.getEditedSection().getConfigurationSection("commands");
		String path = "cmd" + (max + 1);
		if (commands == null)
			path = "cmd0";
		else
			for (int j = 0; j < max; j++)
				if (!commands.contains("cmd" + j)) {
					path = "cmd" + j;
					break;
				}

		inv.getEditedSection().set("commands." + path + ".format", message);
		inv.getEditedSection().set("commands." + path + ".delay", delay);
		inv.getEditedSection().set("commands." + path + ".console", console ? console : null);
		inv.getEditedSection().set("commands." + path + ".op", op ? op : null);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Command successfully registered.");
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> optional) {
		lore.add(ChatColor.GRAY + "Current Commands: " + ChatColor.RED
				+ (optional.isPresent() ? ((CommandListData) optional.get()).getCommands().size() : "0"));
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to edit item commands.");
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
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
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag("MMOITEMS_COMMANDS"))
			try {
				CommandListData commands = new CommandListData();

				new JsonParser().parse(mmoitem.getNBT().getString("MMOITEMS_COMMANDS")).getAsJsonArray().forEach(element -> {
					JsonObject key = element.getAsJsonObject();
					commands.add(new CommandData(key.get("Command").getAsString(), key.get("Delay").getAsDouble(), key.get("Console").getAsBoolean(),
							key.get("Op").getAsBoolean()));
				});

				mmoitem.setData(ItemStat.COMMANDS, commands);
			} catch (JsonSyntaxException exception) {
				/*
				 * OLD ITEM WHICH MUST BE UPDATED.
				 */
			}
	}
}
