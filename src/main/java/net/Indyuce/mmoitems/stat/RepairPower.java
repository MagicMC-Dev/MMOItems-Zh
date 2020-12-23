package net.Indyuce.mmoitems.stat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.event.item.RepairItemEvent;
import net.Indyuce.mmoitems.api.interaction.Consumable;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.type.ConsumableItemInteraction;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.mmogroup.mmolib.api.item.NBTItem;

public class RepairPower extends DoubleStat implements ConsumableItemInteraction {
	public RepairPower() {
		super("REPAIR", Material.ANVIL, "Repair", new String[] { "The amount of durability your item", "can repair when set an item." },
				new String[] { "consumable" });
	}

	@Override
	public boolean handleConsumableEffect(InventoryClickEvent event, PlayerData playerData, Consumable consumable, NBTItem target, Type targetType) {
		int repairPower = (int) consumable.getNBTItem().getStat(ItemStats.REPAIR.getId());
		if (repairPower <= 0)
			return false;

		// custom durability
		Player player = playerData.getPlayer();
		if (target.hasTag("MMOITEMS_DURABILITY")) {

			RepairItemEvent called = new RepairItemEvent(playerData, consumable.getMMOItem(), target, repairPower);
			Bukkit.getPluginManager().callEvent(called);
			if (called.isCancelled())
				return false;

			DurabilityItem durItem = new DurabilityItem(player, target);
			if (durItem.getDurability() < durItem.getMaxDurability()) {
				target.getItem().setItemMeta(durItem.addDurability(called.getRepaired()).toItem().getItemMeta());
				Message.REPAIRED_ITEM
						.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(target.getItem()), "#amount#", "" + called.getRepaired())
						.send(player);
			}
			return true;
		}

		// vanilla durability
		if (!target.getBoolean("Unbreakable") && target.getItem().hasItemMeta() && target.getItem().getItemMeta() instanceof Damageable
				&& ((Damageable) target.getItem().getItemMeta()).getDamage() > 0) {

			RepairItemEvent called = new RepairItemEvent(playerData, consumable.getMMOItem(), target, repairPower);
			Bukkit.getPluginManager().callEvent(called);
			if (called.isCancelled())
				return false;

			ItemMeta meta = target.getItem().getItemMeta();
			((Damageable) meta).setDamage(Math.max(0, ((Damageable) meta).getDamage() - called.getRepaired()));
			target.getItem().setItemMeta(meta);
			Message.REPAIRED_ITEM.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(target.getItem()), "#amount#", "" + called.getRepaired())
					.send(player);
			return true;
		}

		return false;
	}
}
