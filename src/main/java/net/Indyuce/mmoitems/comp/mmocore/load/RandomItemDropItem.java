package net.Indyuce.mmoitems.comp.mmocore.load;

import java.util.Optional;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.loot.LootBuilder;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.item.template.explorer.ClassFilter;
import net.Indyuce.mmoitems.api.item.template.explorer.TemplateExplorer;
import net.Indyuce.mmoitems.api.item.template.explorer.TypeFilter;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.data.SoulboundData;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class RandomItemDropItem extends ItemGenerationDropItem {

	// generation options
	private final boolean matchClass;
	private final String profess;
	private final Type type;

	public RandomItemDropItem(MMOLineConfig config) {
		super(config);

		matchClass = config.getBoolean("match-class", false);
		profess = config.getString("class", "");

		if (config.contains("type")) {
			String format = config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_");
			Validate.isTrue(MMOItems.plugin.getTypes().has(format), "Could not find item type with ID '" + format + "'");
			type = MMOItems.plugin.getTypes().get(format);
		} else
			type = null;
	}

	@Override
	public void collect(LootBuilder builder) {
		RPGPlayer rpgPlayer = PlayerData.get(builder.getEntity().getUniqueId()).getRPG();

		TemplateExplorer explorer = new TemplateExplorer();
		if (matchClass)
			explorer.applyFilter(new ClassFilter(rpgPlayer));
		else if (!profess.isEmpty())
			explorer.applyFilter(new ClassFilter(profess));

		if (type != null)
			explorer.applyFilter(new TypeFilter(type));

		Optional<MMOItemTemplate> optional = explorer.rollLoot();
		if (!optional.isPresent())
			return;

		MMOItem rolled = rollMMOItem(optional.get(), rpgPlayer);

		if (rollSoulbound())
			rolled.setData(ItemStats.SOULBOUND, new SoulboundData(rpgPlayer.getPlayer(), 1));

		ItemStack stack = rollUnidentification(rolled);
		stack.setAmount(rollAmount());
		builder.addLoot(stack);
	}
}
