package net.Indyuce.mmoitems.api.crafting.ingredient;

import net.Indyuce.mmoitems.ItemStats;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.crafting.ConfigMMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.data.MaterialData;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class MMOItemIngredient extends Ingredient {
	private final MMOItemTemplate template;

	// TODO check level code.
	private final int level;
	private final String display;

	public MMOItemIngredient(MMOLineConfig config) {
		super("mmoitem", config);

		config.validate("type", "id");
		Type type = MMOItems.plugin.getTypes().getOrThrow(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
		template = MMOItems.plugin.getTemplates().getTemplateOrThrow(type, config.getString("id"));

		level = config.getInt("level", 0);
		display = config.contains("display") ? config.getString("display") : findName();
	}

	public MMOItemIngredient(ConfigMMOItem mmoitem) {
		super("mmoitem", mmoitem.getAmount());

		template = mmoitem.getTemplate();
		level = 0;
		display = findName();
	}

	public MMOItemTemplate getTemplate() {
		return template;
	}

	@Override
	public String getKey() {
		return "mmoitem:" + template.getType().getId().toLowerCase() + (level != 0 ? "-" + level : "") + "_" + template.getId().toLowerCase();
	}

	@Override
	public String formatLoreDisplay(String string) {
		return string.replace("#item#", display).replace("#level#", level != 0 ? "lvl." + level + " " : "").replace("#amount#", "" + getAmount());
	}

	@Override
	public ItemStack generateItemStack(RPGPlayer player) {
		ItemStack item = template.newBuilder(player).build().newBuilder().build();
		item.setAmount(getAmount());
		return item;
	}

	@Override
	public String toString() {
		return getKey();
	}

	private String findName() {
		if (template.getBaseItemData().containsKey(ItemStats.NAME))
			return template.getBaseItemData().get(ItemStats.NAME).toString().replace("<tier-color>", "").replace("<tier-name>", "");
		if (template.getBaseItemData().containsKey(ItemStats.MATERIAL))
			return MMOUtils.caseOnWords(
					((MaterialData) template.getBaseItemData().get(ItemStats.MATERIAL)).getMaterial().name().toLowerCase().replace("_", " "));
		return "Unrecognized Item";
	}
}
