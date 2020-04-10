package net.Indyuce.mmoitems.stat;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.itemgen.RandomStatData;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.MaterialData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.NBTItem;
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
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		new StatEdition(inv, ItemStat.MATERIAL).enable("Write in the chat the material you want.");
		return true;
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

		config.getConfig().set(inv.getItemId() + ".material", material.name());
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Material successfully changed to " + material.name() + ".");
		return true;
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		/*
		 * material is set handled directly in the MMOBuilder constructor
		 * therefore nothing needs to be done here
		 */
		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		mmoitem.setData(this, new MaterialData(item.getItem().getType()));
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String id) {
		lore.add("");
		if (!config.getConfigurationSection(id).contains(getPath())) {
			lore.add(ChatColor.GRAY + "Current Value:");
			lore.add(ChatColor.RED + "No value.");
		} else {
			String value = ChatColor.translateAlternateColorCodes('&', config.getString(id + "." + getPath()));
			value = value.length() > 40 ? value.substring(0, 40) + "..." : value;
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GREEN + value);
		}
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Left click to change this value.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove this value.");
	}
}
