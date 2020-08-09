package net.Indyuce.mmoitems.stat.data;

import org.bukkit.configuration.ConfigurationSection;

import com.google.gson.JsonObject;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.UpgradeTemplate;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;

public class UpgradeData implements StatData, RandomStatData {
	private final String reference, template;
	private final boolean workbench, destroy;
	private final double success;
	private final int max;
	private int level;

	public UpgradeData(ConfigurationSection section) {
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
		if(!MMOItems.plugin.getUpgrades().hasTemplate(template)) {
			MMOItems.plugin.getLogger().warning("Couldn't find upgrade template '" + template + "'. Does it exist?");
			return;
		}
		
		// change display name
		String suffix = MMOLib.plugin.parseColors(MMOItems.plugin.getConfig().getString("item-upgrading.name-suffix"));
		if (MMOItems.plugin.getConfig().getBoolean("item-upgrading.display-in-name"))
			if (mmoitem.hasData(ItemStat.NAME)) {
				StringData nameData = (StringData) mmoitem.getData(ItemStat.NAME);
				nameData.setString(level == 0 ? nameData.toString() + suffix.replace("#lvl#", "" + (level + 1))
						: nameData.toString().replace(suffix.replace("#lvl#", "" + level), suffix.replace("#lvl#", "" + (level + 1))));
			} else if (mmoitem.hasData(ItemStat.LORE)) {
				StringListData loreData = (StringListData) mmoitem.getData(ItemStat.LORE);
				loreData.getList().forEach(line -> {
					if (line.contains("%upgrade_level%") || line.contains(suffix.replace("#lvl#", "" + level))) {
						line.replace("%upgrade_level%", suffix.replace("#lvl#", "" + level + 1));
						line.replace(suffix.replace("#lvl#", "" + level), suffix.replace("#lvl#", "" + level + 1));
					}
				});
			}

		// apply stat updates
		getTemplate().upgrade(mmoitem);

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

	@Override
	public StatData randomize(MMOItemBuilder builder) {
		return this;
	}
}