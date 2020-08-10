package net.Indyuce.mmoitems.comp.mmocore.load;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmocore.api.loot.LootBuilder;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class ItemTemplateDropItem extends ItemGenerationDropItem {
	private final MMOItemTemplate template;

	public ItemTemplateDropItem(MMOLineConfig config) {
		super(config);

		config.validate("type", "id");

		String format = config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_");
		Validate.isTrue(MMOItems.plugin.getTypes().has(format), "Could not find item type with ID '" + format + "'");
		Type type = MMOItems.plugin.getTypes().get(format);

		String id = config.getString("id").replace("-", "_").toUpperCase();
		Validate.isTrue(MMOItems.plugin.getTemplates().hasTemplate(type, id), "Could not find MMOItem with ID '" + id + "'");
		template = MMOItems.plugin.getTemplates().getTemplate(type, id);
	}

	@Override
	public void collect(LootBuilder builder) {
		RPGPlayer rpgPlayer = builder.getEntity().getMMOPlayerData().getMMOItems().getRPG();

		int itemLevel = MMOItems.plugin.getTemplates().rollLevel(matchLevel ? rpgPlayer.getLevel() : this.level);
		ItemTier itemTier = this.tier != null ? this.tier : MMOItems.plugin.getTemplates().rollTier();

		builder.addLoot(template.newBuilder(itemLevel, itemTier).build().newBuilder().build());
	}
}
