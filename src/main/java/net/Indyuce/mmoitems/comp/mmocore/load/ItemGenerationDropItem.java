package net.Indyuce.mmoitems.comp.mmocore.load;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmocore.api.droptable.dropitem.DropItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.mmogroup.mmolib.api.MMOLineConfig;

public abstract class ItemGenerationDropItem extends DropItem {
	protected final boolean matchLevel;
	protected final int level;
	protected final ItemTier tier;

	public ItemGenerationDropItem(MMOLineConfig config) {
		super(config);

		matchLevel = config.getBoolean("match-level", false);
		level = config.getInt("level", 1);

		if (config.contains("tier")) {
			String format = config.getString("tier").toUpperCase().replace("-", "_").replace(" ", "_");
			Validate.isTrue(MMOItems.plugin.getTiers().has(format), "Could not find item tier with ID '" + format + "'");
			tier = MMOItems.plugin.getTiers().get(format);
		} else
			tier = null;
	}
}
