package net.Indyuce.mmoitems.api.crafting.ingredient;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.crafting.ConfigMMOItem;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.mmogroup.mmolib.api.MMOLineConfig;
import net.Indyuce.mmoitems.stat.DisplayName;
import net.Indyuce.mmoitems.stat.MaterialStat.MaterialData;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class MMOItemIngredient extends Ingredient {
	private final Type type;
	private final String id;

	// TODO check level code.
	private final int level;
	private final String display;

	public MMOItemIngredient(MMOLineConfig config) {
		super("mmoitem", config);

		config.validate("type", "id");
		type = MMOItems.plugin.getTypes().get(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
		id = config.getString("id").toUpperCase().replace("-", "_").replace(" ", "_");

		Validate.isTrue(MMOItems.plugin.getItems().hasMMOItem(type, id), "Could not find MMOItem with ID '" + id + "'");

		level = config.getInt("level", 0);
		display = config.contains("display") ? config.getString("display") : findName();
	}

	public MMOItemIngredient(ConfigMMOItem mmoitem) {
		super("mmoitem", mmoitem.getAmount());

		type = mmoitem.getType();
		id = mmoitem.getId();
		level = 0;
		display = findName();
	}

	@Override
	public String getKey() {
		return "mmoitem:" + type.getId().toLowerCase() + (level != 0 ? "-" + level : "") + "_" + id.toLowerCase();
	}

	public Type getType() {
		return type;
	}

	@Override
	public String formatLoreDisplay(String string) {
		return string.replace("#item#", display).replace("#level#", level != 0 ? "lvl." + level + " " : "").replace("#amount#", "" + getAmount());
	}

	@Override
	public ItemStack generateItemStack() {
		ItemStack item = MMOItems.plugin.getItems().getItem(type, id);
		item.setAmount(getAmount());
		return item;
	}

	@Override
	public String toString() {
		return getKey();
	}

	private String findName() {
		MMOItem mmoitem = MMOItems.plugin.getItems().getMMOItem(type, id);
		if (mmoitem.hasData(ItemStat.NAME))
			return ((DisplayName) ItemStat.NAME).getDisplayName(mmoitem.getData(ItemStat.NAME));
		if (mmoitem.hasData(ItemStat.MATERIAL))
			return MMOUtils.caseOnWords(((MaterialData) mmoitem.getData(ItemStat.MATERIAL)).getMaterial().name().toLowerCase().replace("_", " "));
		return "Unrecognized Item";
	}
}
