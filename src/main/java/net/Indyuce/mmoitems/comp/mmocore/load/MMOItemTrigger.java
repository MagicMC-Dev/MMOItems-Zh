package net.Indyuce.mmoitems.comp.mmocore.load;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.mmogroup.mmolib.api.MMOLineConfig;
import net.mmogroup.mmolib.api.util.SmartGive;

public class MMOItemTrigger extends Trigger {
	private final Type type;
	private final String id;
	private final int amount;

	public MMOItemTrigger(MMOLineConfig config) {
		super(config);

		config.validate("type", "id");

		String format = config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_");
		Validate.isTrue(MMOItems.plugin.getTypes().has(format), "Could not find item type " + format);
		type = MMOItems.plugin.getTypes().get(format);

		id = config.getString("id").replace("-", "_").toUpperCase();
		amount = config.args().length > 0 ? Math.max(1, Integer.parseInt(config.args()[0])) : 1;
		Validate.isTrue(type.getConfigFile().getConfig().contains(id), "Could not find item id " + id);
	}

	@Override
	public void apply(PlayerData player) {
		ItemStack item = MMOItems.plugin.getItems().getItem(type, id);
		if (item == null || item.getType() == Material.AIR)
			return;

		item.setAmount(amount);
		if (item != null && item.getType() != Material.AIR)
			new SmartGive(player.getPlayer()).give(item);
	}
}
