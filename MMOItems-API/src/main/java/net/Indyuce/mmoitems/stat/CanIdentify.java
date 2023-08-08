package net.Indyuce.mmoitems.stat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.event.item.IdentifyItemEvent;
import net.Indyuce.mmoitems.api.interaction.Consumable;
import net.Indyuce.mmoitems.api.item.util.identify.IdentifiedItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import net.Indyuce.mmoitems.stat.type.ConsumableItemInteraction;
import io.lumine.mythic.lib.api.item.NBTItem;
import org.jetbrains.annotations.NotNull;

public class CanIdentify extends BooleanStat implements ConsumableItemInteraction {
	public CanIdentify() {
		super("CAN_IDENTIFY", Material.PAPER, "能否识别",
				new String[] { "玩家可以使用这个消耗品鉴定", "并使自己未鉴定的物品可用" }, new String[] { "consumable" });
	}

	@Override
	public boolean handleConsumableEffect(@NotNull InventoryClickEvent event, @NotNull PlayerData playerData, @NotNull Consumable consumable, @NotNull NBTItem target, Type targetType) {
		if (targetType != null)
			return false;

		if (!consumable.getNBTItem().getBoolean("MMOITEMS_CAN_IDENTIFY") || !target.hasTag("MMOITEMS_UNIDENTIFIED_ITEM"))
			return false;

		Player player = playerData.getPlayer();
		if (target.getItem().getAmount() > 1) {
			Message.CANNOT_IDENTIFY_STACKED_ITEMS.format(ChatColor.RED).send(player);
			return false;
		}

		IdentifyItemEvent called = new IdentifyItemEvent(playerData, consumable.getMMOItem(), target);
		Bukkit.getPluginManager().callEvent(called);
		if (called.isCancelled())
			return false;

		event.setCurrentItem(new IdentifiedItem(target).identify());
		Message.SUCCESSFULLY_IDENTIFIED.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(target.getItem())).send(player);
		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
		return true;
	}
}
