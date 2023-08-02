package net.Indyuce.mmoitems.comp.mmocore.load;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.loot.LootBuilder;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.data.SoulboundData;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class ItemTemplateDropItem extends ItemGenerationDropItem {
	private final MMOItemTemplate template;

	public ItemTemplateDropItem(MMOLineConfig config) {
		super(config);

		config.validate("type", "id");

		String format = config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_");
		Validate.isTrue(MMOItems.plugin.getTypes().has(format), "找不到ID为 '" + format + "' 的物品类型");
		Type type = MMOItems.plugin.getTypes().get(format);

		String id = config.getString("id").replace("-", "_").toUpperCase();
		Validate.isTrue(MMOItems.plugin.getTemplates().hasTemplate(type, id), "找不到ID为 '" + id + "' 的MMO物品");
		template = MMOItems.plugin.getTemplates().getTemplate(type, id);
	}

	@Override
	public void collect(LootBuilder builder) {
		RPGPlayer rpgPlayer = PlayerData.get(builder.getEntity().getUniqueId()).getRPG();
		MMOItem mmoitem = rollMMOItem(template, rpgPlayer);

		if (rollSoulbound())
			mmoitem.setData(ItemStats.SOULBOUND, new SoulboundData(rpgPlayer.getPlayer(), 1));

		ItemStack stack = rollUnidentification(mmoitem);
		stack.setAmount(rollAmount());
		builder.addLoot(stack);
	}
}
