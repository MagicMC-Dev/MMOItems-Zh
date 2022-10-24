package net.Indyuce.mmoitems.stat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.event.item.DeconstructItemEvent;
import net.Indyuce.mmoitems.api.interaction.Consumable;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import net.Indyuce.mmoitems.stat.type.ConsumableItemInteraction;
import io.lumine.mythic.lib.api.item.NBTItem;
import org.jetbrains.annotations.NotNull;

public class CanDeconstruct extends BooleanStat implements ConsumableItemInteraction {
	public CanDeconstruct() {
		super("CAN_DECONSTRUCT", Material.PAPER, "Can Deconstruct?",
				new String[] { "Players can deconstruct their item", "using this consumable, creating", "another random item." },
				new String[] { "consumable" });
	}

	@Override
	public boolean handleConsumableEffect(@NotNull InventoryClickEvent event, @NotNull PlayerData playerData, @NotNull Consumable consumable, @NotNull NBTItem target, Type targetType) {
		String itemTierTag = target.getString("MMOITEMS_TIER");
		if (itemTierTag.equals("") || !consumable.getNBTItem().getBoolean("MMOITEMS_CAN_DECONSTRUCT"))
			return false;

		ItemTier tier = MMOItems.plugin.getTiers().get(itemTierTag);
		List<ItemStack> loot = tier.getDeconstructedLoot(playerData);
		if (loot.isEmpty())
			return false;

		DeconstructItemEvent called = new DeconstructItemEvent(playerData, consumable.getMMOItem(), target, loot);
		Bukkit.getPluginManager().callEvent(called);
		if (called.isCancelled())
			return false;

		Player player = playerData.getPlayer();
		Message.SUCCESSFULLY_DECONSTRUCTED.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(event.getCurrentItem())).send(player);
		event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() - 1);
		for (ItemStack drop : player.getInventory().addItem(loot.toArray(new ItemStack[0])).values())
			player.getWorld().dropItem(player.getLocation(), drop);
		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
		return true;
	}
}
