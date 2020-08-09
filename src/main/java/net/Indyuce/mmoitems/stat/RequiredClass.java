package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.ReadMMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.itemgen.RandomStatData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.ProperStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.api.util.AltChar;
import net.mmogroup.mmolib.version.VersionMaterial;

public class RequiredClass extends ItemStat implements ItemRestriction, ProperStat {
	public RequiredClass() {
		super("REQUIRED_CLASS", new ItemStack(VersionMaterial.WRITABLE_BOOK.toMaterial()), "Required Class",
				new String[] { "The class you need to", "profess to use your item." }, new String[] { "all" });
	}

	@Override
	@SuppressWarnings("unchecked")
	public StringListData whenInitialized(Object object) {
		Validate.isTrue(object instanceof List<?>, "Must specify a string list");
		return new StringListData((List<String>) object);
	}

	@Override
	public RandomStatData whenInitializedGeneration(Object object) {
		return whenInitialized(object);
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getEdited().getType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, this).enable("Write in the chat the class you want your item to support.");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (config.getConfig().getConfigurationSection(inv.getEdited().getId()).getKeys(false).contains("required-class")) {
				List<String> supportedClasses = config.getConfig().getStringList(inv.getEdited().getId() + ".required-class");
				if (supportedClasses.size() < 1)
					return;

				String last = supportedClasses.get(supportedClasses.size() - 1);
				supportedClasses.remove(last);
				config.getConfig().set(inv.getEdited().getId() + ".required-class", supportedClasses.size() == 0 ? null : supportedClasses);
				inv.registerItemEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + last + ".");
			}
		}
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		List<String> lore = (config.getConfig().getConfigurationSection(inv.getEdited().getId()).getKeys(false).contains("required-class")
				? config.getConfig().getStringList(inv.getEdited().getId() + ".required-class")
				: new ArrayList<>());
		lore.add(message);
		config.getConfig().set(inv.getEdited().getId() + ".required-class", lore);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Required Class successfully added.");
		return true;
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			mmoitem.setData(this, new StringListData(mmoitem.getNBT().getString(getNBTPath()).split(Pattern.quote(", "))));
	}

	@Override
	public void whenDisplayed(List<String> lore, MMOItem mmoitem) {

		if (mmoitem.hasData(this)) {
			lore.add(ChatColor.GRAY + "Current Value:");
			StringListData data = (StringListData) mmoitem.getData(this);
			data.getList().forEach(el -> lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + el));

		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "None");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add a class.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last class.");
	}

	@Override
	public void whenApplied(MMOItemBuilder item, StatData data) {
		String joined = String.join(", ", ((StringListData) data).getList());
		item.getLore().insert("required-class", translate().replace("#", joined));
		item.addItemTag(new ItemTag("MMOITEMS_REQUIRED_CLASS", joined));
	}

	@Override
	public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
		String requiredClass = item.getString("MMOITEMS_REQUIRED_CLASS");
		if (!requiredClass.equals("") && !hasRightClass(player, requiredClass) && !player.getPlayer().hasPermission("mmoitems.bypass.class")) {
			if (message) {
				Message.WRONG_CLASS.format(ChatColor.RED).send(player.getPlayer(), "cant-use-item");
				player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
			}
			return false;
		}
		return true;
	}

	private boolean hasRightClass(RPGPlayer player, String requiredClass) {
		String name = ChatColor.stripColor(player.getClassName());

		for (String found : requiredClass.split(Pattern.quote(", ")))
			if (found.equalsIgnoreCase(name))
				return true;

		return false;
	}
}
