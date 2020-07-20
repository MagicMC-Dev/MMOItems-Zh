package net.Indyuce.mmoitems.comp.mmocore.load;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmocore.api.loot.LootBuilder;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.itemgen.GenerationTemplate;
import net.Indyuce.mmoitems.api.itemgen.tier.RolledTier;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class ItemTemplateDropItem extends ItemGenerationDropItem {
	private final GenerationTemplate template;

	public ItemTemplateDropItem(MMOLineConfig config) {
		super(config);

		config.validate("id");

		String id = config.getString("id");
		Validate.isTrue(MMOItems.plugin.getItemGenerator().hasTemplate(id), "Could not find gen template with ID '" + id + "'");
		template = MMOItems.plugin.getItemGenerator().getTemplate(id);
	}

	@Override
	public void collect(LootBuilder builder) {
		RPGPlayer rpgPlayer = builder.getEntity().getMMOPlayerData().getMMOItems().getRPG();

		int itemLevel = MMOItems.plugin.getItemGenerator().rollLevel(matchLevel ? rpgPlayer.getLevel() : this.level);
		RolledTier itemTier = this.tier != null ? new RolledTier(MMOItems.plugin.getItemGenerator().getTierInfo(this.tier), itemLevel)
				: MMOItems.plugin.getItemGenerator().rollTier(itemLevel);

		builder.addLoot(template.newBuilder(itemLevel, itemTier).build().newBuilder().build());
	}
}
