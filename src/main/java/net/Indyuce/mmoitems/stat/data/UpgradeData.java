package net.Indyuce.mmoitems.stat.data;

import com.google.gson.JsonObject;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.UpgradeTemplate;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class containing the Upgrading stuff of an item. Things like:
 * <p> • What level it currently is
 * </p> • What upgrade template it upgrades with
 * <p> • Chance of successful upgrade
 * </p> • May it get destroyed if unsucessful upgrade?
 */
public class UpgradeData implements StatData, RandomStatData {
	@Nullable private final String reference, template;
	private final boolean workbench, destroy;
	private final double success;
	private final int max;
	private int level;

	public UpgradeData(@Nullable String referenc, @Nullable String templat, boolean workbenc, boolean destro, int maxx, double succes) {
		reference = referenc;
		template = templat;
		workbench = workbenc;
		destroy = destro;
		max = maxx;
		success = succes;
	}

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

	/**
	 * @return The template associated to this data, if it is loaded.
	 */
	@Nullable public UpgradeTemplate getTemplate() {
		return MMOItems.plugin.getUpgrades().getTemplate(template);
	}

	public int getLevel() { return level; }

	/**
	 * Dont you mean {@link UpgradeTemplate#upgradeTo(MMOItem, int)}?
	 * This sets the level the item thinks it is, does not apply no changes.
	 * <p></p>
	 * <b>Make sure you know what you are doing before using this</b>
	 */
	public void setLevel(int l) { level = l; }

	public int getMaxUpgrades() {
		return max;
	}

	public boolean canLevelUp() {
		return max == 0 || level < max;
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

	/**
	 *  Upgrade this MMOItem by 1 Level
	 */
	public void upgrade(@NotNull MMOItem mmoitem) {

		/*
		 *  Find Upgrade Template
		 */
		if (getTemplate() == null) {
			MMOItems.plugin.getLogger().warning("Couldn't find upgrade template '" + template + "'. Does it exist?");
			return;
		}

		/*
		 *  Go through every stat that must be ugpraded and apply
		 */
		getTemplate().upgrade(mmoitem);
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