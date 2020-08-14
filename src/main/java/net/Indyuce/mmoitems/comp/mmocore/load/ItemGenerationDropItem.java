package net.Indyuce.mmoitems.comp.mmocore.load;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.api.droptable.dropitem.DropItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.mmogroup.mmolib.api.MMOLineConfig;

public abstract class ItemGenerationDropItem extends DropItem {
	protected final boolean matchLevel, tiered;
	protected final int level;
	protected final ItemTier tier;

	// chance to get one of these two modifiers
	private final double unidentified, soulbound;

	public ItemGenerationDropItem(MMOLineConfig config) {
		super(config);

		matchLevel = config.getBoolean("match-level", false);
		level = config.getInt("level", 0);
		tiered = config.getBoolean("tiered");

		if (config.contains("tier")) {
			String format = config.getString("tier").toUpperCase().replace("-", "_").replace(" ", "_");
			Validate.isTrue(MMOItems.plugin.getTiers().has(format), "Could not find item tier with ID '" + format + "'");
			tier = MMOItems.plugin.getTiers().get(format);
		} else
			tier = null;

		unidentified = config.getDouble("unidentified", 0);
		soulbound = config.getDouble("soulbound", 0);
	}

	public ItemStack rollUnidentification(MMOItem mmoitem) {
		return random.nextDouble() < unidentified ? mmoitem.getType().getUnidentifiedTemplate().newBuilder(mmoitem.newBuilder().buildNBT()).build()
				: mmoitem.newBuilder().build();
	}

	public boolean rollSoulbound() {
		return random.nextDouble() < soulbound;
	}
}
