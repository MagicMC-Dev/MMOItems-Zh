package net.Indyuce.mmoitems.comp.mmocore.load;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.api.droptable.dropitem.DropItem;
import net.Indyuce.mmocore.api.loot.LootBuilder;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class MMOItemDropItem extends DropItem {
	private final Type type;
	private final String id;

	public MMOItemDropItem(MMOLineConfig config) {
		super(config);

		config.validate("type", "id");

		String type = config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_");
		Validate.isTrue(MMOItems.plugin.getTypes().has(type), "Could not find item type " + type);

		this.type = MMOItems.plugin.getTypes().get(type);
		this.id = config.getString("id").toUpperCase().replace("-", "_").replace(" ", "_");
	}

	@Override
	public void collect(LootBuilder builder) {
		if (!MMOItems.plugin.getItems().hasTemplate(type, id))
			return;

		ItemStack item = MMOItems.plugin.getItem(type, id, builder.getEntity().getMMOPlayerData().getMMOItems());
		if (item != null && item.getType() != Material.AIR) {
			item.setAmount(rollAmount());
			builder.addLoot(item);
		}
	}
}
