package net.Indyuce.mmoitems.stat;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.ReadMMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.itemgen.RandomStatData;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.MaterialData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.util.AltChar;
import net.mmogroup.mmolib.version.VersionMaterial;

public class MaterialStat extends ItemStat {
	public MaterialStat() {
		super("MATERIAL", new ItemStack(VersionMaterial.GRASS_BLOCK.toMaterial()), "Material", new String[] { "Your item material." },
				new String[] { "all" });
	}

	@Override
	public MaterialData whenInitialized(Object object) {
		Validate.isTrue(object instanceof String, "Must specify material name as string");
		return new MaterialData(Material.valueOf(((String) object).toUpperCase().replace("-", "_").replace(" ", "_")));
	}

	@Override
	public RandomStatData whenInitializedGeneration(Object object) {
		return whenInitialized(object);
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		new StatEdition(inv, ItemStat.MATERIAL).enable("Write in the chat the material you want.");
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

		config.getConfig().set(inv.getEdited().getId() + ".material", material.name());
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Material successfully changed to " + material.name() + ".");
		return true;
	}

	@Override
	public void whenApplied(MMOItemBuilder item, StatData data) {
		/*
		 * material is set handled directly in the MMOBuilder constructor
		 * therefore nothing needs to be done here
		 */
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		mmoitem.setData(this, new MaterialData(mmoitem.getNBT().getItem().getType()));
	}

	@Override
	public void whenDisplayed(List<String> lore, MMOItem mmoitem) {

		lore.add(ChatColor.GRAY + "Current Value: "
				+ (mmoitem.hasData(this)
						? ChatColor.GREEN
								+ MMOUtils.caseOnWords(((MaterialData) mmoitem.getData(this)).getMaterial().name().toLowerCase().replace("_", " "))
						: ChatColor.RED + "None"));

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Left click to change this value.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove this value.");
	}
}
