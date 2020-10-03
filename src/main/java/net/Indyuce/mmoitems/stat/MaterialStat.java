package net.Indyuce.mmoitems.stat;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Enums;
import com.google.common.base.Optional;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.MaterialData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.mmogroup.mmolib.api.util.AltChar;
import net.mmogroup.mmolib.version.VersionMaterial;

public class MaterialStat extends ItemStat {
	public MaterialStat() {
		super("MATERIAL", new ItemStack(VersionMaterial.GRASS_BLOCK.toMaterial()), "Material",
				new String[] { "Your item material." }, new String[] { "all" });
	}

	@Override
	public MaterialData whenInitialized(Object object) {
		Validate.isTrue(object instanceof String, "Must specify material name as string");
		return new MaterialData(Material.valueOf(((String) object).toUpperCase().replace("-", "_").replace(" ", "_")));
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		new StatEdition(inv, ItemStat.MATERIAL).enable("Write in the chat the material you want.");
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {
		Optional<Material> material = Enums.getIfPresent(Material.class,
				message.toUpperCase().replace("-", "_").replace(" ", "_"));
		if (material.isPresent()) {
			inv.getEditedSection().set("material", material.get().name());
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Material successfully changed to " + material.get().name() + ".");
		} else
			inv.getPlayer().spigot().sendMessage(
				new ComponentBuilder("Invalid material! (Click for a list of valid materials)").color(ChatColor.RED).event(
					new ClickEvent(Action.OPEN_URL, "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html")).create());
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		/*
		 * material is handled directly in the MMOBuilder constructor,
		 * therefore nothing needs to be done here
		 */
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		mmoitem.setData(this, new MaterialData(mmoitem.getNBT().getItem().getType()));
	}

	@Override
	public void whenDisplayed(List<String> lore, java.util.Optional<RandomStatData> optional) {
		lore.add(ChatColor.GRAY + "Current Value: "
				+ (optional.isPresent()
						? ChatColor.GREEN + MMOUtils.caseOnWords(
								((MaterialData) optional.get()).getMaterial().name().toLowerCase().replace("_", " "))
						: ChatColor.RED + "None"));

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Left click to change this value.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove this value.");
	}
}
