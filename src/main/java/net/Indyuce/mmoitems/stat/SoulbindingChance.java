package net.Indyuce.mmoitems.stat;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.event.item.ApplySoulboundEvent;
import net.Indyuce.mmoitems.api.interaction.Consumable;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.SoulboundData;
import net.Indyuce.mmoitems.stat.type.ConsumableItemInteraction;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.jetbrains.annotations.NotNull;

public class SoulbindingChance extends DoubleStat implements ConsumableItemInteraction {
	private static final Random random = new Random();

	public SoulbindingChance() {
		super("SOULBINDING_CHANCE", VersionMaterial.ENDER_EYE.toMaterial(), "Soulbinding Chance",
				new String[] { "Defines the chance your item has to", "link another item to your soul,", "preventing other players from using it." },
				new String[] { "consumable" });
	}

	@Override
	public boolean handleConsumableEffect(@NotNull InventoryClickEvent event, @NotNull PlayerData playerData, @NotNull Consumable consumable, @NotNull NBTItem target, Type targetType) {
		Player player = playerData.getPlayer();

		double soulbindingChance = consumable.getNBTItem().getStat("SOULBINDING_CHANCE");
		if (soulbindingChance <= 0)
			return false;

		if (target.getItem().getAmount() > 1) {
			Message.CANT_BIND_STACKED.format(ChatColor.RED).send(player, "soulbound");
			return false;
		}

		MMOItem targetMMO = new VolatileMMOItem(target);
		if (targetMMO.hasData(ItemStats.SOULBOUND)) {
			SoulboundData data = (SoulboundData) targetMMO.getData(ItemStats.SOULBOUND);
			Message.CANT_BIND_ITEM.format(ChatColor.RED, "#player#", data.getName(), "#level#", MMOUtils.intToRoman(data.getLevel())).send(player,
					"soulbound");
			return false;
		}

		if (random.nextDouble() < soulbindingChance / 100) {
			ApplySoulboundEvent called = new ApplySoulboundEvent(playerData, consumable.getMMOItem(), target);
			Bukkit.getPluginManager().callEvent(called);
			if (called.isCancelled())
				return false;

			int soulboundLevel = (int) Math.max(1, consumable.getNBTItem().getStat("SOULBOUND_LEVEL"));
			(targetMMO = new LiveMMOItem(target)).setData(ItemStats.SOULBOUND,
					new SoulboundData(player.getUniqueId(), player.getName(), soulboundLevel));
			target.getItem().setItemMeta(targetMMO.newBuilder().build().getItemMeta());
			Message.SUCCESSFULLY_BIND_ITEM
					.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(target.getItem()), "#level#", MMOUtils.intToRoman(soulboundLevel))
					.send(player, "soulbound");
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
			return true;
		}

		Message.UNSUCCESSFUL_SOULBOUND.format(ChatColor.RED).send(player, "soulbound");
		player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
		return true;
	}
}
