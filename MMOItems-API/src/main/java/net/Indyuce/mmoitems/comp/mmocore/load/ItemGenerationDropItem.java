package net.Indyuce.mmoitems.comp.mmocore.load;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.loot.droptable.dropitem.DropItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate.TemplateOption;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import io.lumine.mythic.lib.api.MMOLineConfig;
import org.jetbrains.annotations.NotNull;

public abstract class ItemGenerationDropItem extends DropItem {
	protected final int level;
	protected final ItemTier tier;

	// chance to get one of these two modifiers
	private final double unidentified, soulbound;

	public ItemGenerationDropItem(MMOLineConfig config) {
		super(config);

		level = config.getInt("level", 0);

		if (config.contains("tier")) {
			String format = config.getString("tier").toUpperCase().replace("-", "_").replace(" ", "_");
			Validate.isTrue(MMOItems.plugin.getTiers().has(format), "Could not find item tier with ID '" + format + "'");
			tier = MMOItems.plugin.getTiers().get(format);
		} else
			tier = null;

		unidentified = config.getDouble("unidentified", 0);
		soulbound = config.getDouble("soulbound", 0);
	}

	@NotNull
	public MMOItem rollMMOItem(MMOItemTemplate template, RPGPlayer rpgPlayer) {
		final int itemLevel = level > 0 ? level
				: template.hasOption(TemplateOption.LEVEL_ITEM) ? MMOItems.plugin.getTemplates().rollLevel(rpgPlayer.getLevel()) : 0;
		final ItemTier itemTier = tier != null ? tier : template.hasOption(TemplateOption.TIERED) ? MMOItems.plugin.getTemplates().rollTier() : null;
		return new MMOItemBuilder(template, itemLevel, itemTier).build();
    }

    @NotNull
    public ItemStack rollUnidentification(MMOItem mmoitem) {
        ItemStack item = mmoitem.newBuilder().build();
        if (random.nextDouble() < unidentified)
            item = mmoitem.getType().getUnidentifiedTemplate().newBuilder(NBTItem.get(item)).build();
        return item;
    }

	public boolean rollSoulbound() {
		return random.nextDouble() < soulbound;
	}
}
