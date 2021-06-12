package net.Indyuce.mmoitems.api.interaction;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.LegacyComponent;
import io.lumine.mythic.lib.api.util.SmartGive;
import io.lumine.mythic.utils.adventure.text.Component;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.event.item.ConsumableConsumedEvent;
import net.Indyuce.mmoitems.comp.flags.FlagPlugin.CustomFlag;
import net.Indyuce.mmoitems.stat.type.ConsumableItemInteraction;
import net.Indyuce.mmoitems.stat.type.SelfConsumable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Consumable extends UseItem {
	public Consumable(Player player, NBTItem item) {
		super(player, item);
	}

	@Override
	public boolean applyItemCosts() {
		return MMOItems.plugin.getFlags().isFlagAllowed(player, CustomFlag.MI_CONSUMABLES) && playerData.getRPG().canUse(getNBTItem(), true);
	}

	/**
	 * Applies a consumable onto an item
	 * 
	 * @param  event  The click event
	 * @param  target The item on which the consumable is being applied
	 * @return        If the consumable was successfully applied on the item
	 */
	public boolean useOnItem(@NotNull InventoryClickEvent event, @NotNull NBTItem target) {
		if (event.getClickedInventory() != event.getWhoClicked().getInventory())
			return false;

		Type targetType = Type.get(target.getType());
		for (ConsumableItemInteraction action : MMOItems.plugin.getStats().getConsumableActions())
			if (action.handleConsumableEffect(event, playerData, this, target, targetType))
				return true;

		return false;
	}

	/**
	 * @return If the item should be consumed
	 */
	public boolean useWithoutItem() {
		NBTItem nbtItem = getNBTItem();

		// Inedible stat cancels this operation from the beginning
		if (nbtItem.getBoolean(ItemStats.INEDIBLE.getNBTPath())) { return false; }

		// So a consumable is being consumed, eh
		ConsumableConsumedEvent event = new ConsumableConsumedEvent(mmoitem, player, this);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) { return If(event.isConsume()); }

		// Run through all all
		boolean success = false;
		for (SelfConsumable sc : MMOItems.plugin.getStats().getSelfConsumables()) { if (sc.onSelfConsume(mmoitem, player)) { success = true;} }

		int maxConsume = (int) nbtItem.getStat(ItemStats.MAX_CONSUME.getNBTPath());
		if (maxConsume > 1 && success) {
			ItemStack item = nbtItem.toItem().clone();
			String configMaxConsumeLore = MythicLib.plugin.parseColors(MMOItems.plugin.getLanguage().getStatFormat("max-consume"));
			String maxConsumeLore = configMaxConsumeLore.replace("#", Integer.toString(maxConsume));

			maxConsume -= 1;
			nbtItem.addTag(new ItemTag(ItemStats.MAX_CONSUME.getNBTPath(), maxConsume));


			List<String> itemLores = nbtItem.toItem().clone().getItemMeta().getLore();

			for (int i = 0; i < itemLores.size(); i++) {
				if (itemLores.get(i).equals(maxConsumeLore)) {
					maxConsumeLore = configMaxConsumeLore.replace("#", Integer.toString(maxConsume));
					itemLores.set(i, maxConsumeLore);

					List<Component> componentLore = new ArrayList<>();
					itemLores.forEach(line -> componentLore.add(LegacyComponent.parse(line)));
					nbtItem.setLoreComponents(componentLore);

					break;
				}
			}
			ItemStack usedItem = nbtItem.toItem().clone();
			usedItem.setAmount(1);

			if (player.getInventory().getItemInMainHand().equals(item))
				player.getInventory().setItemInMainHand(usedItem);
			else if (player.getInventory().getItemInOffHand().equals(item))
				player.getInventory().setItemInOffHand(usedItem);

			if (item.getAmount() > 1) {
				item.setAmount(item.getAmount() - 1);
				new SmartGive(player).give(item);
			}

			return false;
		}

		return (!nbtItem.getBoolean(ItemStats.DISABLE_RIGHT_CLICK_CONSUME.getNBTPath()) && success) || If(event.isConsume());
	}

	boolean If(@Nullable Boolean cond) { if (cond == null) { return false; } return cond; }

	public boolean hasVanillaEating() {
		return (getItem().getType().isEdible() || getItem().getType() == Material.POTION || getItem().getType() == Material.MILK_BUCKET)
				&& getNBTItem().hasTag("MMOITEMS_VANILLA_EATING");
	}
}
