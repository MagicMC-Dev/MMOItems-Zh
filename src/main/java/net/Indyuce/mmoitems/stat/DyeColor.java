package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.ColorData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.util.AltChar;
import net.mmogroup.mmolib.version.VersionMaterial;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.List;

public class DyeColor extends ItemStat {
	public DyeColor() {
		super("DYE_COLOR", VersionMaterial.RED_DYE.toItem(), "Dye Color",
				new String[] { "The color of your item", "(for dyeable items).", "In RGB." }, new String[] { "all" }, Material.LEATHER_HELMET,
				Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, VersionMaterial.LEATHER_HORSE_ARMOR.toMaterial());
	}

	@Override
	public ColorData whenInitialized(Object object) {
		Validate.isTrue(object instanceof String, "Must specify a string");
		return new ColorData((String) object);
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStats.DYE_COLOR).enable("Write in the chat the RGB color you want.",
					ChatColor.AQUA + "Format: {Red} {Green} {Blue}");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			inv.getEditedSection().set("dye-color", null);
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed Dye Color.");
		}
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {
		String[] split = message.split(" ");
		Validate.isTrue(split.length == 3, "Use this format: {Red} {Green} {Blue}.");
		for (String str : split) {
			int k = Integer.parseInt(str);
			Validate.isTrue(k >= 0 && k < 256, "Color must be between 0 and 255");
		}

		inv.getEditedSection().set("dye-color", message);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Dye Color successfully changed to " + message + ".");
	}

	@Override
	public void whenDisplayed(List<String> lore, RandomStatData statData) {
		lore.add(ChatColor.GRAY + "Current Value: " + (statData.isPresent() ? ChatColor.GREEN + statData.toString() : ChatColor.RED + "None"));
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the dye color.");
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().getItem().getItemMeta() instanceof LeatherArmorMeta)
			mmoitem.setData(ItemStats.DYE_COLOR, new ColorData(((LeatherArmorMeta) mmoitem.getNBT().getItem().getItemMeta()).getColor()));
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		if (item.getMeta() instanceof LeatherArmorMeta)
			((LeatherArmorMeta) item.getMeta()).setColor(((ColorData) data).getColor());
	}
}
