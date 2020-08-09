package net.Indyuce.mmoitems.stat;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.mmogroup.mmolib.api.item.ItemTag;

public class RepairMaterial extends StringStat {
	public RepairMaterial() {
		super("REPAIR_MATERIAL", new ItemStack(Material.ANVIL), "Repair Material",
				new String[] { "The material to be used when", "repairing this item in an anvil.", "", "Currently serves no purpose!" },
				new String[] { "all" });
		
		disable();
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		new StatEdition(inv, this).enable("Write in the chat the material you want.");
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		Material material = null;
		String format = message.toUpperCase().replace("-", "_").replace(" ", "_");
		try {
			material = Material.valueOf(format);
		} catch (Exception e1) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + format + " is not a valid material!");
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix()
					+ "All materials can be found here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html");
			return false;
		}

		config.getConfig().set(inv.getEdited().getId() + ".repair-material", material.name());
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Repair Material successfully changed to " + material.name() + ".");
		return true;
	}

	@Override
	public void whenApplied(MMOItemBuilder item, StatData data) {
		String path = data.toString().toUpperCase().replace("-", "_").replace(" ", "_");
		item.addItemTag(new ItemTag("MMOITEMS_REPAIR_MATERIAL", Material.valueOf(path).name()));
	}
}
