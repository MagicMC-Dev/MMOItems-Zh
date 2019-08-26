package net.Indyuce.mmoitems.stat;

import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityState;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class Max_Custom_Durability extends DoubleStat {
	public Max_Custom_Durability() {
		super(new ItemStack(Material.SHEARS), "Max Custom Durability", new String[] { "The amount of uses before your", "item becomes unusable/breaks." }, "max-durability", new String[] { "all" });
	}

	/*
	 * initializes the custom durability mecanism on an item.
	 */
	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		try {
			double value = ((DoubleData) data).generateNewValue();
			DurabilityState state = DurabilityItem.getExpectedDurabilityState((int) value, (int) value);
			item.addItemTag(new ItemTag("MMOITEMS_MAX_DURABILITY", value), new ItemTag("MMOITEMS_DURABILITY", value), new ItemTag("MMOITEMS_DURABILITY_STATE", state.getID()));
			item.getLore().insert("durability-state", state.getDisplay());
		} catch (Exception e) {
			item.getMMOItem().log(Level.WARNING, "Could not determine the initial durability state. Make sure you have a durability state that has its max use ratio at 100%.");
			return true;
		}
		return true;
	}
}
