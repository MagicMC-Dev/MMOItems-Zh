package net.Indyuce.mmoitems.stat;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;

public class MaterialStat extends StringStat {
	public MaterialStat() {
		super("MATERIAL", new ItemStack(VersionMaterial.GRASS_BLOCK.toMaterial()), "Material", new String[] { "Your item material." },
				new String[] { "all" });
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
	public StatData whenInitialized(MMOItem item, Object object) {
		Validate.isTrue(object instanceof String, "Must specify material name as string");
		return new MaterialData(Material.valueOf(((String) object).toUpperCase().replace("-", "_").replace(" ", "_")));
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

	public class MaterialData implements StatData {
		private Material material;

		/*
		 * material must not be null because it is called directly in the
		 * MMOBuilder constructor.
		 */
		public MaterialData(Material material) {
			Validate.notNull(material, "Material must not be null");
			this.material = material;
		}

		public void setMaterial(Material material) {
			Validate.notNull(material, "Material must not be null");
			this.material = material;
		}

		public Material getMaterial() {
			return material;
		}
	}
}
