package net.Indyuce.mmoitems.stat;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.Indyuce.mmoitems.version.VersionMaterial;

public class MaterialStat extends StringStat {
	public MaterialStat() {
		super(new ItemStack(VersionMaterial.GRASS_BLOCK.toMaterial()), "Material", new String[] { "Your item material." }, "material", new String[] { "all" });
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
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "All materials can be found here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html");
			return false;
		}

		config.getConfig().set(inv.getItemId() + ".material", material.name());
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Material successfully changed to " + material.name() + ".");
		return true;
	}

	@Override
	public boolean whenLoaded(MMOItem item, ConfigurationSection config) {
		MaterialData material = new MaterialData();

		try {
			material.setMaterial(Material.valueOf(config.getString("material").toUpperCase().replace("-", "_").replace(" ", "_")));
		} catch (Exception e) {
			material.setMaterial(VersionMaterial.NETHER_WART.toMaterial());
			item.log(Level.WARNING, "Could not read material from item, using default");
		}

		item.setData(ItemStat.MATERIAL, material);
		return true;
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		item.setMaterial(((MaterialData) data).getMaterial());
		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		mmoitem.setData(this, new MaterialData(item.getItem().getType()));
	}

	public class MaterialData extends StatData {
		private Material material;

		public MaterialData() {
		}

		public MaterialData(Material material) {
			this.material = material;
		}

		public void setMaterial(Material value) {
			material = value;
		}

		public Material getMaterial() {
			return material;
		}
	}
}
