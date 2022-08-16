package net.Indyuce.mmoitems.api.crafting.trigger;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.util.SmartGive;

public class MMOItemTrigger extends Trigger {
	private final MMOItemTemplate template;
	private final int amount;

	public MMOItemTrigger(MMOLineConfig config) {
		super("mmoitem");

		config.validate("type", "id");

		Type type = MMOItems.plugin.getTypes().getOrThrow(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
		String id = config.getString("id").replace("-", "_").toUpperCase();
		Validate.isTrue(MMOItems.plugin.getTemplates().hasTemplate(type, id), "Could not find MMOItem with ID '" + id + "'");

		template = MMOItems.plugin.getTemplates().getTemplate(type, id);
		amount = config.contains("amount") ? Math.max(1, config.getInt("amount")) : 1;
	}

	@Override
	public void whenCrafting(PlayerData data) {
		if(!data.isOnline()) return;
		ItemStack item = template.newBuilder(data.getRPG()).build().newBuilder().build();
		if (item == null || item.getType() == Material.AIR)
			return;

		item.setAmount(amount);
		if (item != null && item.getType() != Material.AIR)
			new SmartGive(data.getPlayer()).give(item);
	}
}
