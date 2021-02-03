package net.Indyuce.mmoitems.comp.mmocore.load;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.Indyuce.mmocore.api.quest.ObjectiveProgress;
import net.Indyuce.mmocore.api.quest.QuestProgress;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import net.Indyuce.mmocore.comp.citizens.CitizenInteractEvent;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.item.NBTItem;

public class GetMMOItemObjective extends Objective {
	private final Type type;
	private final String id;
	private final int required, npcId;

	public GetMMOItemObjective(ConfigurationSection section, MMOLineConfig config) {
		super(section);

		config.validate("type", "id", "npc");

		String format = config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_");
		Validate.isTrue(MMOItems.plugin.getTypes().has(format), "Could not find item type " + format);
		type = MMOItems.plugin.getTypes().get(format);

		id = config.getString("id");
		required = config.contains("amount") ? Math.max(config.getInt("amount"), 1) : 1;
		npcId = config.getInt("npc");
	}

	@Override
	public ObjectiveProgress newProgress(QuestProgress questProgress) {
		return new GotoProgress(questProgress, this);
	}

	public class GotoProgress extends ObjectiveProgress implements Listener {
		public GotoProgress(QuestProgress questProgress, Objective objective) {
			super(questProgress, objective);
		}

		@EventHandler
		public void a(CitizenInteractEvent event) {
			Player player = event.getPlayer();
			if(!getQuestProgress().getPlayer().isOnline()) return;
			if (player.equals(getQuestProgress().getPlayer().getPlayer()) && event.getNPC().getId() == npcId && player.getInventory().getItemInMainHand() != null) {
				NBTItem item = NBTItem.get(player.getInventory().getItemInMainHand());
				int amount;
				if (item.getString("MMOITEMS_ITEM_TYPE").equals(type.getId()) && item.getString("MMOITEMS_ITEM_ID").equals(id) && (amount = player.getInventory().getItemInMainHand().getAmount()) >= required) {
					if (amount <= required)
						player.getInventory().setItemInMainHand(null);
					else
						player.getInventory().getItemInMainHand().setAmount(amount - required);
					getQuestProgress().completeObjective();
				}
			}
		}

		@Override
		public String formatLore(String lore) {
			return lore;
		}
	}
}
