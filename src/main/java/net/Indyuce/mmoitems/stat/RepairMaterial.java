package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
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
	public void whenInput(EditionInventory inv, String message, Object... info) {
		try {
			Material material = Material.valueOf(message.toUpperCase().replace("-", "_").replace(" ", "_"));
			inv.getEditedSection().set("repair-material", material.name());
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Repair Material successfully changed to " + material.name() + ".");
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException(
					exception.getMessage() + " (all materials can be found here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html)");
		}
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		String path = data.toString().toUpperCase().replace("-", "_").replace(" ", "_");
		item.addItemTag(new ItemTag("MMOITEMS_REPAIR_MATERIAL", Material.valueOf(path).name()));
	}
}
