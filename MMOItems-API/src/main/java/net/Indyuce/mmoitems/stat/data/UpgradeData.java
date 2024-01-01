package net.Indyuce.mmoitems.stat.data;

import com.google.gson.JsonObject;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.UpgradeTemplate;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.util.MMOUtils;
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
public class UpgradeData implements StatData, RandomStatData<UpgradeData> {

	/**
	 * @return The String a consumable must match to Upgrade this Item
	 */
	@Nullable public String getReference() { return reference; }

	/**
	 * @return If upgrades must be done through workbench
	 */
	public boolean isWorkbench() {
		return workbench;
	}

	/**
	 * @return Will this destroy the item if they fail?
	 */
	public boolean isDestroy() {
		return destroy;
	}

	/**
	 * @return Max amount of upgrades this can hold
	 */
	public int getMax() { return max; }
	/**
	 * @return Minimum level this item can be downgraded to
	 */
	public int getMin() { return min; }

	@Nullable private final String reference, template;
	private final boolean workbench, destroy;
	private final double success;
	private final int max;
	private final int min;
	private int level;

	/**
	 * Create a new UpgradeData
	 *
	 * @param reference			Upgrade Reference to use
	 * @param template			Upgrade Template to follow
	 * @param workbench			If it is upgraded in workbench (I don't know for sure)?
	 * @param destroy			If it will destroy the item if the upgrade fails
	 * @param max				Max Level attainable
	 * @param success			Success Chance
	 */
	public UpgradeData(@Nullable String reference, @Nullable String template, boolean workbench, boolean destroy, int max, double success) {
		this(reference, template, workbench, destroy, max, 0, success);
	}

	/**
	 * Create a new UpgradeData
	 *
	 * @param reference			Upgrade Reference to use
	 * @param template			Upgrade Template to follow
	 * @param workbench			If it is upgraded in workbench (I don't know for sure)?
	 * @param destroy			If it will destroy the item if the upgrade fails
	 * @param max				Max Level attainable
	 * @param min				Min Level attainable through downgrading
	 * @param success			Success Chance
	 */
	public UpgradeData(@Nullable String reference, @Nullable String template, boolean workbench, boolean destroy, int max, int min, double success) {
		this.reference = reference;
		this.template = template;
		this.workbench = workbench;
		this.destroy = destroy;
		this.max = max;
		this.min = min;
		this.success = success;
	}

	public UpgradeData(ConfigurationSection section) {
		reference = section.getString("reference");
		template = section.getString("template");
		workbench = section.getBoolean("workbench");
		destroy = section.getBoolean("destroy");
		max = section.getInt("max");
		min = section.getInt("min", 0);
		success = section.getDouble("success") / 100;
	}

	public UpgradeData(JsonObject object) {
		workbench = object.get("Workbench").getAsBoolean();
		destroy = object.get("Destroy").getAsBoolean();
		template = object.has("Template") ? object.get("Template").getAsString() : null;
		reference = object.has("Reference") ? object.get("Reference").getAsString() : null;
		level = object.get("Level").getAsInt();
		max = object.get("Max").getAsInt();
		min = object.has("Min") ? object.get("Min").getAsInt() : 0;
		success = object.get("Success").getAsDouble();
	}

	/**
	 * @return The template associated to this data, if it is loaded.
	 */
	@Nullable public UpgradeTemplate getTemplate() {
		if (template == null) { return null; }
		return MMOItems.plugin.getUpgrades().getTemplate(template);
	}

	@Nullable public String getTemplateName() {return template; }

	public int getLevel() { return level; }

	/**
	 * Dont you mean {@link UpgradeTemplate#upgradeTo(MMOItem, int)}?
	 * This sets the level the item thinks it is, does not apply no changes.
	 * <p></p>
	 * <b>Make sure you know what you are doing before using this</b>
	 */
	public void setLevel(int l) { level = l; }

	public int getMaxUpgrades() { return max; }

	public boolean canLevelUp() {
		return max == 0 || level < max;
	}

	public boolean destroysOnFail() {
		return destroy;
	}

	public double getSuccess() {
		return success == 0 ? 1 : success;
	}

	@Deprecated
	public boolean matchesReference(UpgradeData data) {
		return MMOUtils.checkReference(reference, data.reference);
	}

	@Override
	public boolean isEmpty() {
		return false;
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
		json.addProperty("Min", min);
		json.addProperty("Success", success);

		return json;
	}

	@Override
	public String toString() {
		return toJson().toString();
	}

	@Override
	public UpgradeData randomize(MMOItemBuilder builder) {
		return this;
	}

	@Override
	public UpgradeData clone() {
		return new UpgradeData(reference, template, workbench, destroy, max, min, success);
	}
}