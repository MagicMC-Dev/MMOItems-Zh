package net.Indyuce.mmoitems.comp.mmocore.load;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.api.loot.LootBuilder;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.loot.ClassFilter;
import net.Indyuce.mmoitems.api.item.template.loot.TypeFilter;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.mmogroup.mmolib.api.MMOLineConfig;

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
		RPGPlayer rpgPlayer = builder.getEntity().getMMOPlayerData().getMMOItems().getRPG();

		int itemLevel = MMOItems.plugin.getItems().rollLevel(matchLevel ? rpgPlayer.getLevel() : this.level);
		ItemTier itemTier = this.tier != null ? this.tier : MMOItems.plugin.getItems().rollTier();

		net.Indyuce.mmoitems.api.item.LootBuilder loot = new net.Indyuce.mmoitems.api.item.LootBuilder(itemLevel, itemTier);

		if (matchClass)
			loot.applyFilter(new ClassFilter(rpgPlayer));
		else if (!profess.isEmpty())
			loot.applyFilter(new ClassFilter(profess));

		if (type != null)
			loot.applyFilter(new TypeFilter(type));

		MMOItem rolled = loot.rollLoot();
		if (rolled == null)
			return;

		ItemStack gen = rolled.newBuilder().build();
		gen.setAmount(rollAmount());
		builder.addLoot(gen);
	}
}
