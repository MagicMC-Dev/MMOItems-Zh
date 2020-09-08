package net.Indyuce.mmoitems.comp.mmoinventory.stat;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.version.VersionMaterial;
import org.bukkit.ChatColor;

public class AccessorySet extends StringStat {
    public AccessorySet() {
        super("ACCESSORY_SET", VersionMaterial.OAK_SIGN.toItem(), "Accessory Set (MMOInventory)", new String[] { "Used with MMOInventory's unique", "restriction to only allow one", "accessory to be equipped per set." }, new String[] { "!block", "all" });
    }

    @Override
    public void whenApplied(ItemStackBuilder item, StatData data) {
        item.addItemTag(new ItemTag(getNBTPath(), data.toString()));
    }
    @Override
    public void whenInput(EditionInventory inv, String message, Object... info) {
        message = message.toLowerCase();
        inv.getEditedSection().set(getPath(), message);
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(
                MMOItems.plugin.getPrefix() + getName() + " successfully changed to " + MMOLib.plugin.parseColors(message) + ChatColor.GRAY + ".");
    }
}
