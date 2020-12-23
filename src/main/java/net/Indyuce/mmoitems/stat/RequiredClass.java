package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import net.Indyuce.mmoitems.stat.type.StringListStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.api.util.AltChar;
import net.mmogroup.mmolib.version.VersionMaterial;

public class RequiredClass extends StringListStat implements ItemRestriction, GemStoneStat {
	public RequiredClass() {
		super("REQUIRED_CLASS", VersionMaterial.WRITABLE_BOOK.toMaterial(), "Required Class",
				new String[] { "The class you need to", "profess to use your item." }, new String[] { "!block", "all" });
	}

	@Override
	@SuppressWarnings("unchecked")
	public StringListData whenInitialized(Object object) {
		Validate.isTrue(object instanceof List<?>, "Must specify a string list");
		return new StringListData((List<String>) object);
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, this).enable("Write in the chat the class you want your item to support.");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (inv.getEditedSection().getKeys(false).contains("required-class")) {
				List<String> supportedClasses = inv.getEditedSection().getStringList("required-class");
				if (supportedClasses.size() < 1)
					return;

				String last = supportedClasses.get(supportedClasses.size() - 1);
				supportedClasses.remove(last);
				inv.getEditedSection().set("required-class", supportedClasses.size() == 0 ? null : supportedClasses);
				inv.registerTemplateEdition();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + last + ".");
			}
		}
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {
		List<String> lore = (inv.getEditedSection().getKeys(false).contains("required-class") ? inv.getEditedSection().getStringList("required-class")
				: new ArrayList<>());
		lore.add(message);
		inv.getEditedSection().set("required-class", lore);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Required Class successfully added.");
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			mmoitem.setData(this, new StringListData(mmoitem.getNBT().getString(getNBTPath()).split(Pattern.quote(", "))));
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> statData) {

		if (statData.isPresent()) {
			lore.add(ChatColor.GRAY + "Current Value:");
			StringListData data = (StringListData) statData.get();
			data.getList().forEach(el -> lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + el));

		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "None");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add a class.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last class.");
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		String joined = String.join(", ", ((StringListData) data).getList());
		item.getLore().insert("required-class", MMOItems.plugin.getLanguage().getStatFormat(getPath()).replace("#", joined));
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
