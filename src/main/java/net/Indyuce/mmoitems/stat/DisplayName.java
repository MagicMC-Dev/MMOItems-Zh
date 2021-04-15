package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.logging.Level;

public class DisplayName extends StringStat {
	private final String[] cleanFilter = {ChatColor.BOLD.toString(), ChatColor.ITALIC.toString(), ChatColor.UNDERLINE.toString(), ChatColor.STRIKETHROUGH.toString(), ChatColor.MAGIC.toString()};
	public DisplayName() {
		super("NAME", VersionMaterial.OAK_SIGN.toMaterial(), "Display Name", new String[] { "The item display name." },
				new String[] { "all" });
	}

	// TODO: 4/12/2021 WHAT THE FUCK IS HAPPENING HERE
	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) {
		String format = data.toString();

		ItemTier tier = MMOItems.plugin.getTiers().findTier(item.getMMOItem());
		format = format.replace("<tier-name>", tier != null ? ChatColor.stripColor(tier.getName()) : "");
		format = format.replace("<tier-color>", tier != null ? ChatColor.getLastColors(tier.getName()) : "&f");
		if (tier != null) {
			for (String filter: cleanFilter){
				if (ChatColor.getLastColors(tier.getName()).contains(filter)){
					format = format.replace("<tier-color-cleaned>", ChatColor.getLastColors(tier.getName().replace(filter, "")));
				}
			}
		}

		// Is this upgradable?
		if (item.getMMOItem().hasUpgradeTemplate()) {
			int upgradeLevel = item.getMMOItem().getUpgradeLevel();
			String suffix = MythicLib.plugin.parseColors(MMOItems.plugin.getConfig().getString("item-upgrading.name-suffix"));

			//MMOItems.getConsole().sendMessage("Level " + upgradeLevel);
			//MMOItems.getConsole().sendMessage("Format " + format);

			if (suffix != null) {

				// Crop lvl
				int lvlOFFSET = suffix.indexOf("#lvl#");
				String sB4 = suffix.substring(0, lvlOFFSET);
				String aFt = suffix.substring(lvlOFFSET + "#lvl#".length());
				String sB4_alt = sB4.replace("+", "-");
				String aFt_alt = aFt.replace("+", "-");

				// Remove it
				if (format.contains(sB4)) {

					// Get offsets
					int sB4_offset = format.indexOf(sB4);
					int aFt_offset = format.lastIndexOf(aFt);

					// No after = to completion
					if (aFt_offset < 0) { aFt_offset = format.length(); } else { aFt_offset += aFt.length(); }

					// Remove that
					String beforePrefix = format.substring(0, sB4_offset);
					String afterPrefix = format.substring(aFt_offset);

					// Replace
					format = beforePrefix + afterPrefix; }

				// Remove it
				if (format.contains(sB4_alt)) {

					// Get offsets
					int sB4_offset = format.indexOf(sB4_alt);
					int aFt_offset = format.lastIndexOf(aFt_alt);

					// No after = to completion
					if (aFt_offset < 0) { aFt_offset = format.length(); } else { aFt_offset += aFt_alt.length(); }

					// Remove that
					String beforePrefix = format.substring(0, sB4_offset);
					String afterPrefix = format.substring(aFt_offset);

					// Replace
					format = beforePrefix + afterPrefix; }

				/*/ Bake old indices for removal
				ArrayList<String> oldSuffixii = new ArrayList<>(); boolean negativity = false;
				if (upgradeLevel < 0) { upgradeLevel = -upgradeLevel; negativity = true; }
				for (int i = upgradeLevel + 3; i >= 1; i--) {
					if (negativity) {
						oldSuffixii.add(levelPrefix(suffix, -i));
					} else {
						oldSuffixii.add(levelPrefix(suffix, i)); } }
				for (String str : oldSuffixii) {

					//MMOItems.getConsole().sendMessage("Found " + str);

					// Remove the one with color parsed from the current name
					format = format.replace(str, "");

					//MMOItems.getConsole().sendMessage("Edited " + format);
				} //*/

				// Add a prefix anew if the upgrade level worked
				if (upgradeLevel != 0) {

					// Get the current suffix
					String actSuffix = levelPrefix(suffix, upgradeLevel);

					//MMOItems.getConsole().sendMessage("Current " + actSuffix);

					// Append it
					format = format + actSuffix;
				}

				//MMOItems.getConsole().sendMessage("Final " + format);
			}
		}

		item.getMeta().setDisplayName(MythicLib.plugin.parseColors(format));
	}


	String levelPrefix(@NotNull String template, int toLevel) {

		// Ez
		template = template.replace("#lvl#", String.valueOf(toLevel));

		// 00f
		template = template.replace("+-", "-");

		// Yes
		return template;
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
