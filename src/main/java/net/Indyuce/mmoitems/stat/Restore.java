package net.Indyuce.mmoitems.stat;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.RestoreData;
import net.Indyuce.mmoitems.stat.data.random.RandomRestoreData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.util.AltChar;
import net.mmogroup.mmolib.version.VersionMaterial;

public class Restore extends ItemStat {
	public Restore() {
		super("RESTORE", VersionMaterial.RED_DYE.toItem(), "Restore",
				new String[] { "The amount of health/food/saturation", "your consumable item restores." }, new String[] { "consumable" });
	}

	@Override
	public RandomStatData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		return new RandomRestoreData((ConfigurationSection) object);
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		new StatEdition(inv, ItemStat.RESTORE).enable("Write in the chat the values you want.",
				ChatColor.AQUA + "Format: [HEALTH] [FOOD] [SATURATION]");
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		String[] split = message.split("\\ ");
		if (split.length != 3) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid [HEALTH] [FOOD] [SATURATION].");
			return false;
		}
		for (String s : split)
			try {
				Double.parseDouble(s);
			} catch (Exception e1) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + s + " is not a valid number.");
				return false;
			}
		double health = Double.parseDouble(split[0]);
		double food = Double.parseDouble(split[1]);
		double saturation = Double.parseDouble(split[2]);

		config.getConfig().set(inv.getEdited().getId() + ".restore.health", (health <= 0 ? null : health));
		config.getConfig().set(inv.getEdited().getId() + ".restore.food", (food <= 0 ? null : food));
		config.getConfig().set(inv.getEdited().getId() + ".restore.saturation", (saturation <= 0 ? null : saturation));

		inv.registerTemplateEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Restore successfully changed to " + message + ".");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> optional) {

		if (optional.isPresent()) {
			RestoreData data = (RestoreData) optional.get();
			lore.add(ChatColor.GRAY + "* Restore Health: " + ChatColor.GREEN + data.getHealth());
			lore.add(ChatColor.GRAY + "* Restore Food: " + ChatColor.GREEN + data.getFood());
			lore.add(ChatColor.GRAY + "* Restore Saturation: " + ChatColor.GREEN + data.getSaturation());

		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "None");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click change these values.");
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		double health = mmoitem.getNBT().getDouble("MMOITEMS_RESTORE_HEALTH");
		double food = mmoitem.getNBT().getDouble("MMOITEMS_RESTORE_FOOD");
		double saturation = mmoitem.getNBT().getDouble("MMOITEMS_RESTORE_SATURATION");

		if (health > 0 || food > 0 || saturation > 0)
			mmoitem.setData(this, new RestoreData(health, food, saturation));
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		RestoreData restore = (RestoreData) data;

		if (restore.getHealth() != 0) {
			item.addItemTag(new ItemTag("MMOITEMS_RESTORE_HEALTH", restore.getHealth()));
			item.getLore().insert("restore-health",
					ItemStat.translate("restore-health").replace("#", new StatFormat("##").format(restore.getHealth())));
		}
		if (restore.getFood() != 0) {
			item.addItemTag(new ItemTag("MMOITEMS_RESTORE_FOOD", restore.getFood()));
			item.getLore().insert("restore-food", ItemStat.translate("restore-food").replace("#", new StatFormat("##").format(restore.getFood())));
		}
		if (restore.getSaturation() != 0) {
			item.addItemTag(new ItemTag("MMOITEMS_RESTORE_SATURATION", restore.getSaturation()));
			item.getLore().insert("restore-saturation",
					ItemStat.translate("restore-saturation").replace("#", new StatFormat("##").format(restore.getSaturation())));
		}
	}
}
