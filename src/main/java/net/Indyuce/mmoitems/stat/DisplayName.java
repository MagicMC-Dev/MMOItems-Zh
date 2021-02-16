package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
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

		item.getMeta().setDisplayName(ChatColor.translateAlternateColorCodes('&', format));
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
