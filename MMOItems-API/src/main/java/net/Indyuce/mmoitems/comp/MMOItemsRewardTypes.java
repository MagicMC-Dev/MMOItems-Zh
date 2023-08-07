package net.Indyuce.mmoitems.comp;

import java.util.List;

import org.black_ixx.bossshop.core.BSBuy;
import org.black_ixx.bossshop.core.rewards.BSRewardType;
import org.black_ixx.bossshop.managers.ClassManager;
import org.black_ixx.bossshop.managers.misc.InputReader;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class MMOItemsRewardTypes extends BSRewardType {

	/*
	 * the config gives the list of all the mmoitems that need to be given to
	 * the player
	 */
	@Override
	public Object createObject(Object object, boolean force_final_state) {
		return InputReader.readStringList(object);
	}

	public boolean validityCheck(String itemName, Object object) {
		if (object != null || !(object instanceof List<?>))
			return true;

		ClassManager.manager.getBugFinder().severe("Couldn't load the MMOItems reward type" + itemName
				+ ". The reward object needs to be a list of types & IDs (format: [ITEM_TYPE].[ITEM_ID]).");
		return false;
	}

	/*
	 * since this is a buyable item, the player can always buy it so it should
	 * always return true.
	 */
	@Override
	public boolean canBuy(Player player, BSBuy buy, boolean message_if_no_success, Object reward, ClickType clickType) {
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void giveReward(Player player, BSBuy buy, Object reward, ClickType clickType) {
		for (String item : (List<String>) reward)
			try {
				String[] split = item.split("\\.");
				Type type = MMOItems.plugin.getTypes().get(split[0].toUpperCase().replace("-", "_"));
				for (ItemStack drop : player.getInventory().addItem(MMOItems.plugin.getItem(type, split[1], PlayerData.get(player)))
						.values())
					player.getWorld().dropItem(player.getLocation(), drop);
			} catch (Exception e) {
				ClassManager.manager.getBugFinder().severe("Couldn't load the MMOItems reward type" + item + ". Format: [ITEM_TYPE].[ITEM_ID]).");
			}
	}

	@Override
	public String getDisplayReward(Player p, BSBuy buy, Object reward, ClickType clickType) {
		// List<String> permissions = (List<String>) reward;
		// String permissions_formatted =
		// StringManipulationLib.formatList(permissions);
		// return
		// ClassManager.manager.getMessageHandler().get("Display.Permission").replace("%permissions%",
		// permissions_formatted);
		return "";
	}

	@Override
	public String[] createNames() {
		return new String[] { "mmoitem", "mmoitems" };
	}

	@Override
	public boolean mightNeedShopUpdate() {
		return false;
	}

	@Override
	public void enableType() {
	}
}
