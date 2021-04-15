package net.Indyuce.mmoitems.api.interaction;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.event.item.ApplyGemStoneEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.Enchants;
import net.Indyuce.mmoitems.stat.GemUpgradeScaling;
import net.Indyuce.mmoitems.stat.data.GemSocketsData;
import net.Indyuce.mmoitems.stat.data.GemstoneData;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.UpgradeData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class GemStone extends UseItem {

	public GemStone(Player player, NBTItem item) {
		super(player, item);
	}

	@NotNull public ApplyResult applyOntoItem(@NotNull NBTItem target, @NotNull Type targetType) {

		/*
		 * Entirely loads the MMOItem and checks if it has the required empty
		 * socket for the gem
		 */
		MMOItem targetMMO = new LiveMMOItem(target);
		return applyOntoItem(targetMMO, targetType, MMOUtils.getDisplayName(target.getItem()), true, false);
	}

	@NotNull public ApplyResult applyOntoItem(@NotNull MMOItem targetMMO, @NotNull Type targetType, @NotNull String itemName, boolean buildStack, boolean silent){

		if (!targetMMO.hasData(ItemStats.GEM_SOCKETS))
			return new ApplyResult(ResultType.NONE);

		String gemType = getNBTItem().getString(ItemStats.GEM_COLOR.getNBTPath());

		GemSocketsData sockets = (GemSocketsData) targetMMO.getData(ItemStats.GEM_SOCKETS);
		String foundSocketColor = sockets.getEmptySocket(gemType);
		if (foundSocketColor == null)
			return new ApplyResult(ResultType.NONE);

		/*
		 * Checks if the gem supports the item type, or the item set, or a
		 * weapon
		 */
		String appliableTypes = getNBTItem().getString(ItemStats.ITEM_TYPE_RESTRICTION.getNBTPath());
		if (!appliableTypes.equals("") && (!targetType.isWeapon() || !appliableTypes.contains("WEAPON"))
				&& !appliableTypes.contains(targetType.getItemSet().name()) && !appliableTypes.contains(targetType.getId()))
			return new ApplyResult(ResultType.NONE);

		// check for success rate
		double successRate = getNBTItem().getStat(ItemStats.SUCCESS_RATE.getId());
		if (successRate != 0 && RANDOM.nextDouble() > successRate / 100) {

			if (!silent) {
				player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
				Message.GEM_STONE_BROKE.format(ChatColor.RED, "#gem#", MMOUtils.getDisplayName(getItem()), "#item#", itemName).send(player); }

			return new ApplyResult(ResultType.FAILURE);
		}

		ApplyGemStoneEvent called = new ApplyGemStoneEvent(playerData, mmoitem, targetMMO);
		Bukkit.getPluginManager().callEvent(called);
		if (called.isCancelled())
			return new ApplyResult(ResultType.NONE);

		/*
		 * To not clear enchantments put by players
		 */
		Enchants.separateEnchantments(targetMMO);

		/*
		 * Gem stone can be successfully applied. apply stats then abilities and
		 * permanent effects. also REGISTER gem stone in the item gem stone
		 * list.
		 */
		LiveMMOItem gemMMOItem = new LiveMMOItem(getNBTItem());
		GemstoneData gemData = new GemstoneData(gemMMOItem, foundSocketColor);

		/*
		 * Now must apply the gem sockets data to the Stat History and then recalculate.
		 *
		 * Gotta, however, find the correct StatData to which apply it to. Damn this can
		 * be pretty complicated!
		 */
		StatHistory gemStory = StatHistory.from(targetMMO, ItemStats.GEM_SOCKETS);

		// Original?
		if (((GemSocketsData) gemStory.getOriginalData()).getEmptySocket(gemType) != null) {
			//UPGRD//MMOItems.log("\u00a77Applied Gemstone @\u00a76Original\u00a77: \u00a73" + foundSocketColor);

			// Charmer
			((GemSocketsData) gemStory.getOriginalData()).apply(gemType, gemData);

		} else {

			// Check gem gems lol
			boolean success = false;
			for (UUID registeredGem : gemStory.getAllGemstones()) {

				// Get that gem
				GemSocketsData registeredGemData = (GemSocketsData) gemStory.getGemstoneData(registeredGem);
				if (registeredGemData == null) { continue; }

				if (registeredGemData.getEmptySocket(gemType) != null) {
					//UPGRD//MMOItems.log("\u00a77Applied Gemstone @\u00a76Gemstone\u00a77: \u00a73" + foundSocketColor);

					// Charmer
					success = true;
					registeredGemData.apply(gemType, gemData); break; } }

			if (!success) {

				for (StatData extraneousGem : gemStory.getExternalData()) {

					// Get that gem
					GemSocketsData registeredGemData = (GemSocketsData) extraneousGem;
					if (registeredGemData == null) { continue; }

					if (registeredGemData.getEmptySocket(gemType) != null) {
						//UPGRD//MMOItems.log("\u00a77Applied Gemstone @\u00a76External\u00a77: \u00a73" + foundSocketColor);

						// Charmer
						registeredGemData.apply(gemType, gemData); break; } } } }

		// Recalculate
		//HSY//MMOItems.log(" \u00a73-\u00a7a- \u00a77Gem Application Recalculation \u00a73-\u00a7a-\u00a73-\u00a7a-\u00a73-\u00a7a-\u00a73-\u00a7a-");
		targetMMO.setData(ItemStats.GEM_SOCKETS, gemStory.recalculate(targetMMO.getUpgradeLevel()));
		//UPGRD//MMOItems.log("Applied Gemstone: \u00a73" + foundSocketColor);

		/*
		 * Get the item's level, important for the GemScalingStat
		 */
		Integer levelIdentified = null; String scaling = GemUpgradeScaling.SUBSEQUENT;
		if (gemMMOItem.hasData(ItemStats.GEM_UPGRADE_SCALING)) { scaling = gemMMOItem.getData(ItemStats.GEM_UPGRADE_SCALING).toString(); }
		//UPGRD//MMOItems.log("Scaling Identified: \u00a73" + scaling);
		switch (scaling) {
			case GemUpgradeScaling.HISTORIC:
				levelIdentified = 0;
				break;
			case GemUpgradeScaling.SUBSEQUENT:
				levelIdentified = targetMMO.getUpgradeLevel();
				break;
			case GemUpgradeScaling.NEVER:
			default: break; }

		gemData.setLevel(levelIdentified);
		//UPGRD//MMOItems.log("Set Level: \u00a7b" + gemData.getLevel());
		/*
		 * Only applies NON PROPER and MERGEABLE item stats
		 */
		for (ItemStat stat : gemMMOItem.getStats()) {

			// If it is not PROPER
			if (!(stat instanceof GemStoneStat)) {

				// Get the stat data
				StatData data = gemMMOItem.getData(stat);

				// If the data is MERGEABLE
				if (data instanceof Mergeable) {
					//UPGRD//MMOItems.log("\u00a79>>> \u00a77Gem-Merging \u00a7c" + stat.getNBTPath());

					// Merge into it
					targetMMO.mergeData(stat, data, gemData.getHistoricUUID());
				}
			}
		}

		if (!silent) {
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
			Message.GEM_STONE_APPLIED.format(ChatColor.YELLOW, "#gem#", MMOUtils.getDisplayName(getItem()), "#item#", itemName).send(player); }

		if (buildStack) {
			return new ApplyResult(targetMMO.newBuilder().build());

		} else { return new ApplyResult(targetMMO, ResultType.SUCCESS); }
	}

	public static class ApplyResult {
		@NotNull private final ResultType type;
		@Nullable private final ItemStack result;
		@Nullable private final MMOItem resultAsMMOItem;

		public ApplyResult(@NotNull ResultType type) {
			this((ItemStack) null, type);
		}

		public ApplyResult(@Nullable ItemStack result) { this(result, ResultType.SUCCESS); }

		public ApplyResult(@Nullable ItemStack result, @NotNull ResultType type) {
			this.type = type;
			this.result = result;
			this.resultAsMMOItem = null;
		}
		public ApplyResult(@Nullable MMOItem result, @NotNull  ResultType type) {
			this.type = type;
			this.result = null;
			this.resultAsMMOItem = result;
		}

		@NotNull public ResultType getType() {
			return type;
		}

		@Nullable public ItemStack getResult() {
			return result;
		}
		@Nullable public MMOItem getResultAsMMOItem() { return resultAsMMOItem; }
	}

	public enum ResultType {
		/*
		 * when the gem stone is not successfully applied onto the item and when
		 * it needs to be destroyed
		 */
		FAILURE,

		/*
		 * when a gem stone, for some reason, cannot be applied onto an item (if
		 * it has no more empty gem socket), but when the gem must not be
		 * destroyed
		 */
		NONE,

		/*
		 * when a gem stone is successfully applied onto an item without any
		 * error
		 */
		SUCCESS
	}
}
