package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.TypeSet;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.util.AltChar;

public class ItemTypeRestriction extends StringStat {
	public ItemTypeRestriction() {
		super("ITEM_TYPE_RESTRICTION", new ItemStack(Material.EMERALD), "Item Type Restriction",
				new String[] { "This option defines the item types", "on which your gem can be applied." }, new String[] { "gem_stone" });
	}

	@Override
	@SuppressWarnings("unchecked")
	public RandomStatData whenInitialized(Object object) {
		Validate.isTrue(object instanceof List<?>, "Must specify a string list");
		return new StringListData((List<String>) object);
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getEdited().getType().getConfigFile();

		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStat.ITEM_TYPE_RESTRICTION).enable("Write in the chat the item type you want your gem to support.",
					"Supported formats: WEAPON or BLUNT, PIERCING, SLASHING, OFFHAND, EXTRA.");

		// if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
		// StatEdition.put(player, new StatEdition(type, path,
		// ItemStat.ITEM_TYPE_RESTRICTION, event.getInventory(), 2));
		// player.closeInventory();
		// PluginInventory.startChatEdition(player, player);
		// player.sendMessage(MMOItems.plugin.getPrefix() + "Write in the chat
		// the value you want to display in the lore.");
		// }
		//
		// if (event.getAction() == InventoryAction.DROP_ONE_SLOT)
		// if (config.getConfigurationSection(path).contains(getPath()))
		// if (config.getConfigurationSection(path + "." +
		// getPath()).contains("display")) {
		// config.getConfig().set(path + "." + getPath() + ".display", null);
		// type.saveConfigFile(config, path);
		// new ItemEdition(player, type, path).open();
		// player.sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset
		// the type restrictions display.");
		// }

		if (event.getAction() == InventoryAction.PICKUP_HALF)
			if (config.getConfig().getConfigurationSection(inv.getEdited().getId()).contains(getPath())) {
				List<String> list = config.getConfig().getStringList(inv.getEdited().getId() + "." + getPath());
				if (list.size() < 1)
					return;

				String last = list.get(list.size() - 1);
				list.remove(last);
				config.getConfig().set(inv.getEdited().getId() + "." + getPath(), list.size() == 0 ? null : list);
				inv.registerTemplateEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + last + ".");
			}
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		// if (counter > 1) {
		// config.getConfig().set(path + "." + getPath() + ".display", message);
		// type.saveConfigFile(config, path);
		// new ItemEdition(player, type, path).open();
		// player.sendMessage(MMOItems.plugin.getPrefix() + "Type restrictions
		// display successfully changed to " + message + ChatColor.GRAY + ".");
		// return true;
		// }

		String format = message.toUpperCase().replace(" ", "_").replace("-", "_");
		if (!isValid(format)) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + format + " is not a valid item type/set.");
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "See all item types here: /mi list type.");
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "You can also enter WEAPON or BLUNT, PIERCING, SLASHING, OFFHAND, EXTRA.");
			return false;
		}

		List<String> list = config.getConfig().getConfigurationSection(inv.getEdited().getId()).contains(getPath())
				? config.getConfig().getStringList(inv.getEdited().getId() + "." + getPath())
				: new ArrayList<>();
		list.add(format);
		config.getConfig().set(inv.getEdited().getId() + "." + getPath(), list);
		inv.registerTemplateEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Your gem now supports " + format + ".");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> optional) {

		if (optional.isPresent()) {
			lore.add(ChatColor.GRAY + "Current Value:");
			StringListData data = (StringListData) optional.get();
			data.getList().forEach(el -> lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + el));

		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "Compatible with any type.");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add a supported item type/set.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last element.");
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		// List<String> displayedTypes = new ArrayList<String>();
		//
		// for (String typeId : (List<String>) values[0])
		// try {
		// displayedTypes.add(Type.valueOf(typeId).getName());
		// } catch (Exception e) {
		// }
		//
		// String joined = String.join(", ", displayedTypes);
		// item.getLore().insert(getPath(), translate().replace("#", joined));
		item.addItemTag(new ItemTag("MMOITEMS_ITEM_TYPE_RESTRICTION", String.join(",", ((StringListData) data).getList())));
	}

	private boolean isValid(String format) {
		if (format.equals("WEAPON"))
			return true;

		for (Type type : MMOItems.plugin.getTypes().getAll())
			if (type.getId().equals(format))
				return true;

		for (TypeSet set : TypeSet.values())
			if (set.name().equals(format))
				return true;

		return false;
	}
}
