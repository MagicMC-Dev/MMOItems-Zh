package net.Indyuce.mmoitems.stat;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.UpgradeTemplate;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.UpgradingEdition;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StringStat.StringData;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class Upgrade_Stat extends ItemStat {
	public Upgrade_Stat() {
		super(new ItemStack(Material.FLINT), "Item Upgrading", new String[] { "Upgrading your item improves its", "current stats. It requires either a", "consumable or a specific crafting ", "station. Upgrading may sometimes &cfail&7..." }, "upgrade", new String[] { "piercing", "slashing", "blunt", "offhand", "range", "tool", "armor", "consumable" });
	}

	@Override
	public boolean whenLoaded(MMOItem item, ConfigurationSection config) {
		item.setData(this, new UpgradeData(item, config.getConfigurationSection("upgrade")));
		return true;
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		item.addItemTag(new ItemTag("MMOITEMS_UPGRADE", data.toString()));
		return true;
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new UpgradingEdition(inv.getPlayer(), inv.getItemType(), inv.getItemId()).open(inv.getPage());

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			ConfigFile config = inv.getItemType().getConfigFile();
			if (config.getConfig().getConfigurationSection(inv.getItemId()).contains("upgrade")) {
				config.getConfig().set(inv.getItemId() + ".upgrade", null);
				inv.registerItemEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset the upgrading setup.");
			}
		}
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {

		if (info[0].equals("ref")) {
			config.getConfig().set(inv.getItemId() + ".upgrade.reference", message);
			inv.registerItemEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Upgrading reference successfully changed to " + ChatColor.GOLD + message + ChatColor.GRAY + ".");
			return true;
		}

		if (info[0].equals("max")) {

			int i = 0;
			try {
				i = Integer.parseInt(message);
			} catch (NumberFormatException exception) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid number.");
				return false;
			}

			config.getConfig().set(inv.getItemId() + ".upgrade.max", i);
			inv.registerItemEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Max upgrades successfully set to " + ChatColor.GOLD + i + ChatColor.GRAY + ".");
			return true;
		}

		if (info[0].equals("rate")) {

			double d = 0;
			try {
				d = Double.parseDouble(message);
			} catch (NumberFormatException exception) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid number.");
				return false;
			}

			config.getConfig().set(inv.getItemId() + ".upgrade.success", d);
			inv.registerItemEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Upgrading rate successfully set to " + ChatColor.GOLD + d + "%" + ChatColor.GRAY + ".");
			return true;
		}

		if (!MMOItems.plugin.getUpgrades().hasTemplate(message)) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Could not find any upgrade template with ID " + ChatColor.GOLD + message + ChatColor.GRAY + ".");
			return false;
		}

		config.getConfig().set(inv.getItemId() + ".upgrade.template", message);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Upgrading template successfully changed to " + ChatColor.GOLD + message + ChatColor.GRAY + ".");
		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		if (item.hasTag("MMOITEMS_UPGRADE"))
			mmoitem.setData(this, new UpgradeData(new JsonParser().parse(item.getString("MMOITEMS_UPGRADE")).getAsJsonObject()));
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String id) {
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Left click to setup upgrading.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to reset.");
	}

	public class UpgradeData extends StatData {
		private final String reference, template;
		private final boolean workbench, destroy;
		private final double success;
		private final int max;
		private int level;

		public UpgradeData(MMOItem mmoitem, ConfigurationSection section) {
			setMMOItem(mmoitem);

			reference = section.getString("reference");
			template = section.getString("template");
			workbench = section.getBoolean("workbench");
			destroy = section.getBoolean("destroy");
			max = section.getInt("max");
			success = section.getDouble("success") / 100;
		}

		public UpgradeData(JsonObject object) {
			workbench = object.get("Workbench").getAsBoolean();
			destroy = object.get("Destroy").getAsBoolean();
			template = object.has("Template") ? object.get("Template").getAsString() : null;
			reference = object.has("Reference") ? object.get("Reference").getAsString() : null;
			level = object.get("Level").getAsInt();
			max = object.get("Max").getAsInt();
			success = object.get("Success").getAsDouble();
		}

		public UpgradeTemplate getTemplate() {
			return MMOItems.plugin.getUpgrades().getTemplate(template);
		}

		public int getLevel() {
			return level;
		}

		public int getMaxUpgrades() {
			return max;
		}

		public boolean hasMaxUpgrades() {
			return max != 0;
		}

		public boolean canLevelUp() {
			return !hasMaxUpgrades() || level < max;
		}

		public boolean destroysOnFail() {
			return destroy;
		}

		public double getSuccess() {
			return success == 0 ? 1 : success;
		}

		public boolean matchesReference(UpgradeData data) {
			return reference == null || data.reference == null || reference.isEmpty() || data.reference.isEmpty() || reference.equals(data.reference);
		}

		public void upgrade(MMOItem mmoitem) {

			// change display name
			if (mmoitem.hasData(ItemStat.NAME)) {
				String suffix = ChatColor.translateAlternateColorCodes('&', MMOItems.plugin.getConfig().getString("item-upgrading.name-suffix"));
				StringData nameData = (StringData) mmoitem.getData(ItemStat.NAME);
				nameData.setString(level == 0 ? nameData.toString() + suffix.replace("#lvl#", "" + (level + 1)) : nameData.toString().replace(suffix.replace("#lvl#", "" + level), suffix.replace("#lvl#", "" + (level + 1))));
			}

			// apply stat updates
			getTemplate().upgrade(mmoitem, this);

			// increase the level
			level++;
		}

		public JsonObject toJson() {
			JsonObject json = new JsonObject();

			if (reference != null && !reference.isEmpty())
				json.addProperty("Reference", reference);
			if (template != null && !template.isEmpty())
				json.addProperty("Template", template);
			json.addProperty("Workbench", workbench);
			json.addProperty("Destroy", destroy);
			json.addProperty("Level", level);
			json.addProperty("Max", max);
			json.addProperty("Success", success);

			return json;
		}

		@Override
		public String toString() {
			return toJson().toString();
		}
	}
}
