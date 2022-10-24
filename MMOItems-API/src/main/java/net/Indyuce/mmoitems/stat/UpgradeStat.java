package net.Indyuce.mmoitems.stat;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.AltChar;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.event.item.UpgradeItemEvent;
import net.Indyuce.mmoitems.api.interaction.Consumable;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.UpgradingEdition;
import net.Indyuce.mmoitems.stat.data.UpgradeData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ConsumableItemInteraction;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class UpgradeStat extends ItemStat<UpgradeData, UpgradeData> implements ConsumableItemInteraction {
	private static final Random random = new Random();

	public UpgradeStat() {
		super("UPGRADE", Material.FLINT, "Item Upgrading",
				new String[] { "Upgrading your item improves its", "current stats. It requires either a", "consumable or a specific crafting ",
						"station. Upgrading may sometimes &cfail&7..." },
				new String[] { "piercing", "slashing", "blunt", "catalyst", "range", "tool", "armor", "consumable", "accessory" });
	}

	@Override
	public UpgradeData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		return new UpgradeData((ConfigurationSection) object);
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull UpgradeData data) {
		if (!(data instanceof UpgradeData)) { return; }

		// Show in lore
		item.addItemTag(getAppliedNBT(data));

		// Show in lore
		if (data.getMaxUpgrades() > 0)
			item.getLore().insert(getPath(),
					MMOItems.plugin.getLanguage().getStatFormat(getPath()).replace("{value}", String.valueOf(data.getMaxUpgrades())));
	}

	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull UpgradeData data) {
		ArrayList<ItemTag> ret = new ArrayList<>();
		ret.add(new ItemTag(getNBTPath(), data.toString()));
		return ret;
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new UpgradingEdition(inv.getPlayer(), inv.getEdited()).open(inv.getPage());

		if (event.getAction() == InventoryAction.PICKUP_HALF && inv.getEditedSection().contains("upgrade")) {
			inv.getEditedSection().set("upgrade", null);
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset the upgrading setup.");
		}
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {

		if (info[0].equals("ref")) {
			inv.getEditedSection().set("upgrade.reference", message);
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(
					MMOItems.plugin.getPrefix() + "Upgrading reference successfully changed to " + ChatColor.GOLD + message + ChatColor.GRAY + ".");
			return;
		}

		if (info[0].equals("max")) {
			int i = Integer.parseInt(message);
			inv.getEditedSection().set("upgrade.max", i);
			inv.registerTemplateEdition();
			inv.getPlayer()
					.sendMessage(MMOItems.plugin.getPrefix() + "Max upgrades successfully set to " + ChatColor.GOLD + i + ChatColor.GRAY + ".");
			return;
		}

		if (info[0].equals("min")) {
			int i = Integer.parseInt(message);
			inv.getEditedSection().set("upgrade.min", i);
			inv.registerTemplateEdition();
			inv.getPlayer()
					.sendMessage(MMOItems.plugin.getPrefix() + "Min level successfully set to " + ChatColor.GOLD + i + ChatColor.GRAY + ".");
			return;
		}

		if (info[0].equals("rate")) {
			double d = MMOUtils.parseDouble(message);
			inv.getEditedSection().set("upgrade.success", d);
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(
					MMOItems.plugin.getPrefix() + "Upgrading rate successfully set to " + ChatColor.GOLD + d + "%" + ChatColor.GRAY + ".");
			return;
		}

		Validate.isTrue(MMOItems.plugin.getUpgrades().hasTemplate(message), "Could not find any upgrade template with ID '" + message + "'.");
		inv.getEditedSection().set("upgrade.template", message);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(
				MMOItems.plugin.getPrefix() + "Upgrading template successfully changed to " + ChatColor.GOLD + message + ChatColor.GRAY + ".");
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

		// Get Tags
		ArrayList<ItemTag> tags = new ArrayList<>();
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			tags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING));
		StatData data = getLoadedNBT(tags);
		if (data != null) { mmoitem.setData(this, data);}
	}

	@Nullable
	@Override
	public UpgradeData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

		// Gettag
		ItemTag uTag = ItemTag.getTagAtPath(getNBTPath(), storedTags);

		if (uTag != null) {

			try {

				// Cook Upgrade Data
				return new UpgradeData(new JsonParser().parse((String) uTag.getValue()).getAsJsonObject());

			} catch (JsonSyntaxException |IllegalStateException exception) {
				/*
				 * OLD ITEM WHICH MUST BE UPDATED.
				 */
			}
		}

		// Nope
		return null;
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<UpgradeData> statData) {
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Left click to setup upgrading.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to reset.");
	}

	@NotNull
	@Override
	public UpgradeData getClearStatData() { return new UpgradeData(null, null, false, false, 0, 0, 0D); }

	@Override
	public boolean handleConsumableEffect(@NotNull InventoryClickEvent event, @NotNull PlayerData playerData, @NotNull Consumable consumable, @NotNull NBTItem target, Type targetType) {
		VolatileMMOItem mmoitem = consumable.getMMOItem();
		Player player = playerData.getPlayer();

		if (mmoitem.hasData(ItemStats.UPGRADE) && target.hasTag(ItemStats.UPGRADE.getNBTPath())) {
			if (target.getItem().getAmount() > 1) {
				Message.CANT_UPGRADED_STACK.format(ChatColor.RED).send(player);
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
				return false;
			}

			MMOItem targetMMO = new LiveMMOItem(target);
			UpgradeData targetSharpening = (UpgradeData) targetMMO.getData(ItemStats.UPGRADE);
			if (targetSharpening.isWorkbench())
				return false;

			if (!targetSharpening.canLevelUp()) {
				Message.MAX_UPGRADES_HIT.format(ChatColor.RED).send(player);
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
				return false;
			}

			UpgradeData consumableSharpening = (UpgradeData) mmoitem.getData(ItemStats.UPGRADE);
			if (!consumableSharpening.matchesReference(targetSharpening)) {
				Message.WRONG_UPGRADE_REFERENCE.format(ChatColor.RED).send(player);
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
				return false;
			}

			UpgradeItemEvent called = new UpgradeItemEvent(playerData, mmoitem, targetMMO, consumableSharpening, targetSharpening);
			Bukkit.getPluginManager().callEvent(called);
			if (called.isCancelled())
				return false;

			targetSharpening.upgrade(targetMMO);
			NBTItem result = targetMMO.newBuilder().buildNBT();

			/*
			 * Safe check, if the specs the item has after ugprade are too high
			 * for the player, then cancel upgrading because the player would
			 * not be able to use it.
			 */
			if (MMOItems.plugin.getLanguage().upgradeRequirementsCheck && !playerData.getRPG().canUse(result, false)) {
				Message.UPGRADE_REQUIREMENT_SAFE_CHECK.format(ChatColor.RED).send(player);
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
				return false;
			}

			if (random.nextDouble() > consumableSharpening.getSuccess() * targetSharpening.getSuccess()) {
				Message.UPGRADE_FAIL.format(ChatColor.RED).send(player);
				if (targetSharpening.destroysOnFail())
					event.getCurrentItem().setAmount(0);
				player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 2);
				return true;
			}

			Message.UPGRADE_SUCCESS.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(event.getCurrentItem())).send(player);
			event.getCurrentItem().setItemMeta(result.toItem().getItemMeta());
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
			return true;
		}
		return false;
	}
}
