package net.Indyuce.mmoitems.stat.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnchantListData implements StatData, Mergeable {
	private final Map<Enchantment, Integer> enchants = new HashMap<>();

	public Set<Enchantment> getEnchants() {
		return enchants.keySet();
	}

	public int getLevel(@NotNull Enchantment enchant) {
		if (!enchants.containsKey(enchant)) { return 0; }
		return enchants.get(enchant);
	}

	public void addEnchant(Enchantment enchant, int level) {
		enchants.put(enchant, level);
	}
	public void clear() { enchants.clear(); }

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EnchantListData)) { return false; }
		if (((EnchantListData) obj).enchants.size() != enchants.size()) { return false; }

		for (Enchantment e : getEnchants()) {

			// Compare
			if (getLevel(e) != ((EnchantListData) obj).getLevel(e)) { return false; } }
		return true;
	}

	@Override
	public void merge(StatData data) {
		Validate.isTrue(data instanceof EnchantListData, "Cannot merge two different stat data types");
		boolean additiveMerge = MMOItems.plugin.getConfig().getBoolean("stat-merging.additive-enchantments", false);

		for (Enchantment enchant : ((EnchantListData) data).getEnchants()) {
			if (additiveMerge) {

				// Additive
				enchants.put(enchant, ((EnchantListData) data).getLevel(enchant) + enchants.get(enchant));
			} else {

				// Max Enchantment
				addEnchant(enchant,

						// Does this one already have the enchant?
						enchants.containsKey(enchant) ?

								// Use the better of the two
								Math.max(((EnchantListData) data).getLevel(enchant), enchants.get(enchant)) :

								// No enchant yet, just copy over
								((EnchantListData) data).getLevel(enchant));
			}
		}
	}

	@Override
	public @NotNull StatData cloneData() {

		// Start Fresh
		EnchantListData ret = new EnchantListData();

		// Enchant
		for (Enchantment enchant : enchants.keySet()) { ret.addEnchant(enchant, enchants.getOrDefault(enchant, 0)); }

		// Thats it
		return ret;
	}

	@Override
	public boolean isClear() {
		// Any non lvl 0 enchantment?
		for (Enchantment e : getEnchants()) { if (getLevel(e) != 0) { return false; } }
		return true;
	}

	/**
	 *  todo We cannot yet assume (for a few months) that the Original Enchantment Data
	 *   registered into the Stat History is actually true to the template (since it may
	 *   be the enchantments of an old-enchanted item, put by the player).
	 *   _
	 *   Thus this block of code checks the enchantment data of the newly generated
	 *   MMOItem and follows the following logic to give our best guess if the Original
	 *   stats are actually Original:
	 *
	 *   1: Is the item unenchantable and unrepairable? Then they must be original
	 *
	 *   2: Does the template have no enchantments? Then they must be external
	 *
	 *   3: Does the template have this enchantment at an unobtainable level? Then it must be original
	 *
	 *   4: Does the template have this enchantment at a lesser level? Then it must be external (player upgraded it)
	 *
	 *   Original: Included within the template at first creation
	 *   External: Enchanted manually by a player
	 *
	 * @param mmoItem The item, to provide context for adequate guessing.
	 * @param output Merges all 'Extraneous' enchantments onto this one
	 *
	 */
	public void identifyTrueOriginalEnchantments(@NotNull MMOItem mmoItem, @NotNull EnchantListData output) {

		//UPDT//MMOItems.log(" \u00a7b> \u00a77Original Enchantments Upkeep");

		// 1: The item is unenchantable and unrepairable? Cancel this operation, the cached are Original
		if (mmoItem.hasData(ItemStats.DISABLE_ENCHANTING) && mmoItem.hasData(ItemStats.DISABLE_REPAIRING)) {
			//UPDT//MMOItems.log(" \u00a7bType-1 \u00a77Original Identification");

			clear();

			//UPDT//MMOItems.log(" \u00a7b:\u00a73:\u00a7: \u00a77Trime Arcane Report: \u00a73-------------------------");
			//UPDT//MMOItems.log("  \u00a73> \u00a77Output:");
			//UPDT//for (Enchantment e : output.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + output.getLevel(e)); }

			return;
		}
		if (!mmoItem.hasData(ItemStats.ENCHANTS)) { mmoItem.setData(ItemStats.ENCHANTS, new EnchantListData());}

		// 2: If it has data (It always has) and the amount of enchants is zero, the cached are Extraneous
		if (((EnchantListData) mmoItem.getData(ItemStats.ENCHANTS)).getEnchants().size() == 0) {
			//UPDT//MMOItems.log(" \u00a73Type-2 \u00a77Extraneous Identification");

			// All right, lets add those to cached enchantments
			output.merge(this);

			//UPDT//MMOItems.log(" \u00a7b:\u00a73:\u00a7: \u00a77Trime Arcane Report: \u00a73-------------------------");
			//UPDT//MMOItems.log("  \u00a73> \u00a77Output:");
			//UPDT//for (Enchantment e : output.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + output.getLevel(e)); }
			return;
		}

		// Which enchantments are deemed external, after all?
		EnchantListData processed = new EnchantListData();

		// Identify material
		mmoItem.hasData(ItemStats.MATERIAL); MaterialData mData = (MaterialData) mmoItem.getData(ItemStats.MATERIAL); Material mat = mData.getMaterial();

		// 3 & 4: Lets examine every stat
		for (Enchantment e : getEnchants()) {
			//UPDT//MMOItems.log(" \u00a7b  = \u00a77Per Enchant - \u00a7f" + e.getName());

			// Lets see hmm
			int current = getLevel(e);
			int updated = ((EnchantListData) mmoItem.getData(ItemStats.ENCHANTS)).getLevel(e);
			//UPDT//MMOItems.log(" \u00a73  <=: \u00a77Current \u00a7f" + current);
			//UPDT//MMOItems.log(" \u00a73  <=: \u00a77Updated \u00a7f" + updated);

			// 3: Is it at an unobtainable level? Then its Original
			if (updated > e.getMaxLevel() || !e.getItemTarget().includes(mat)) {
				//UPDT//MMOItems.log(" \u00a7bType-3 \u00a77Original Identification");

				continue;
			}

			// 4: Is it at a lesser level? Player must have enchanted, take them as External
			if (updated < current) {
				//UPDT//MMOItems.log(" \u00a73Type-4 \u00a77Extraneous Identification");
				processed.addEnchant(e, current);
				//noinspection UnnecessaryContinue
				continue;
			}

			//UPDT//MMOItems.log(" \u00a73Type-5 \u00a77Original Identification");
		}

		// All right, lets add those to cached enchantments
		output.merge(processed);

		//UPDT//MMOItems.log(" \u00a7b:\u00a73:\u00a7: \u00a77Trime Arcane Report: \u00a73-------------------------");
		//UPDT//MMOItems.log("  \u00a73> \u00a77Output:");
		//UPDT//for (Enchantment e : output.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + output.getLevel(e)); }

		//UPDT//MMOItems.log("  \u00a73> \u00a77Processed:");
		//UPDT//for (Enchantment e : processed.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + processed.getLevel(e)); }
	}
}