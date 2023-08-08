package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.event.item.BreakSoulboundEvent;
import net.Indyuce.mmoitems.api.interaction.Consumable;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.SoulboundData;
import net.Indyuce.mmoitems.stat.type.ConsumableItemInteraction;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class SoulbindingBreakChance extends DoubleStat implements ConsumableItemInteraction {
	private static final Random random = new Random();

	public SoulbindingBreakChance() {
		super("SOULBOUND_BREAK_CHANCE", VersionMaterial.ENDER_EYE.toMaterial(), "解除灵魂绑定机率",
				new String[] { "拖放使用消耗物时打破其", "作用物品上灵魂绑定的几率,", "此几率会根据灵魂绑定", "的等级而降低概率" },
				new String[] { "consumable" });
	}

	@Override
	public boolean handleConsumableEffect(@NotNull InventoryClickEvent event, @NotNull PlayerData playerData, @NotNull Consumable consumable, @NotNull NBTItem target, Type targetType) {
		Player player = playerData.getPlayer();

		double soulboundBreakChance = consumable.getNBTItem().getStat("SOULBOUND_BREAK_CHANCE");
		if (soulboundBreakChance <= 0)
			return false;

		MMOItem targetMMO = new VolatileMMOItem(target);
		if (!targetMMO.hasData(ItemStats.SOULBOUND)) {
			Message.NO_SOULBOUND.format(ChatColor.RED).send(player);
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			return false;
		}

		// check for soulbound level
		SoulboundData soulbound = (SoulboundData) targetMMO.getData(ItemStats.SOULBOUND);
		if (Math.max(1, consumable.getNBTItem().getStat(ItemStats.SOULBOUND_LEVEL.getId())) < soulbound.getLevel()) {
			Message.LOW_SOULBOUND_LEVEL.format(ChatColor.RED, "#level#", MMOUtils.intToRoman(soulbound.getLevel())).send(player);
			return false;
		}

		if (random.nextDouble() < soulboundBreakChance / 100) {
			BreakSoulboundEvent called = new BreakSoulboundEvent(playerData, consumable.getMMOItem(), target);
			Bukkit.getPluginManager().callEvent(called);
			if (called.isCancelled())
				return false;

			(targetMMO = new LiveMMOItem(target)).removeData(ItemStats.SOULBOUND);
			target.getItem().setItemMeta(targetMMO.newBuilder().build().getItemMeta());
			Message.SUCCESSFULLY_BREAK_BIND.format(ChatColor.YELLOW, "#level#", MMOUtils.intToRoman(soulbound.getLevel())).send(player);
			player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 2);

		} else {
			Message.UNSUCCESSFUL_SOULBOUND_BREAK.format(ChatColor.RED).send(player);
			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 0);
		}

		return true;
	}
}
