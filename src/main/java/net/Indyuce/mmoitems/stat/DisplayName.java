package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class DisplayName extends StringStat {
	public DisplayName() {
		super("NAME", VersionMaterial.OAK_SIGN.toMaterial(), "Display Name", new String[] { "The item display name." },
				new String[] { "all" });
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) {
		String format = data.toString();

		ItemTier tier = MMOItems.plugin.getTiers().findTier(item.getMMOItem());
		format = format.replace("<tier-name>", tier != null ? ChatColor.stripColor(tier.getName()) : "");
		format = format.replace("<tier-color>", tier != null ? ChatColor.getLastColors(tier.getName()) : "&f");

		// Is this upgradable?
		if (item.getMMOItem().hasUpgradeTemplate()) {
			int upgradeLevel = item.getMMOItem().getUpgradeLevel();
			String suffix = MMOItems.plugin.getConfig().getString("item-upgrading.name-suffix");
			//MMOItems.getConsole().sendMessage("Level " + upgradeLevel);
			//MMOItems.getConsole().sendMessage("Format " + format);
			if (suffix != null) {
				//MMOItems.getConsole().sendMessage("Suffix " + suffix);

				// Bake old indices for removal
				ArrayList<String> oldSuffixii = new ArrayList<>(); boolean negativity = false;
				if (upgradeLevel < 0) { upgradeLevel = -upgradeLevel; negativity = true; }
				for (int i = 1; i <= upgradeLevel + 3; i++) {
					if (negativity) {
						oldSuffixii.add(suffix.replace("#lvl#", String.valueOf(-i)));
					} else {
						oldSuffixii.add(suffix.replace("#lvl#", String.valueOf(i))); }}
				for (String str : oldSuffixii) {
					//MMOItems.getConsole().sendMessage("Found " + str);
					str = MythicLib.plugin.parseColors(str);
					//MMOItems.getConsole().sendMessage("Colored " + str);
					format = format.replace(MythicLib.plugin.parseColors(str), "");
					//MMOItems.getConsole().sendMessage("Edited " + format);
				}

				String actSuffix = suffix.replace("#lvl#", String.valueOf(upgradeLevel));
				//MMOItems.getConsole().sendMessage("Current " + actSuffix);
				if (upgradeLevel != 0) { format = format + MythicLib.plugin.parseColors(actSuffix); }
				//MMOItems.getConsole().sendMessage("Final " + format);
			}
		}

		item.getMeta().setDisplayName(MythicLib.plugin.parseColors(format));
	}

	/**
	 * This is not saved as a custom NBT data, instead it is stored as the name of the item itself.
	 * Alas this returns an empty list
	 */
	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull StatData data) {

		// Thats it
		return new ArrayList<>();
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

		// No need to continue if the item has no display name
		if (!mmoitem.getNBT().getItem().getItemMeta().hasDisplayName()) { return; }

		// Get tags
		ArrayList<ItemTag> relevantTags = new ArrayList<>();

		// Add sole tag
		relevantTags.add(new ItemTag(getNBTPath(), mmoitem.getNBT().getItem().getItemMeta().getDisplayName()));

		// Use that
		StringData bakedData = (StringData) getLoadedNBT(relevantTags);

		// Valid?
		if (bakedData != null) {

			// Set
			mmoitem.setData(this, bakedData);
		}
	}

	@Nullable
	@Override
	public StatData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

		// You got a double righ
		ItemTag tg = ItemTag.getTagAtPath(getNBTPath(), storedTags);

		// Found righ
		if (tg != null) {

			// Get number
			String value = (String) tg.getValue();

			// Thats it
			return new StringData(value);
		}

		// Fail
		return null;
	}
}
