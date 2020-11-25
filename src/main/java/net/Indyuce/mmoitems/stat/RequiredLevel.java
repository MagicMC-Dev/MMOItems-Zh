package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.RequiredLevelData;
import net.Indyuce.mmoitems.stat.data.random.RandomRequiredLevelData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

public class RequiredLevel extends DoubleStat implements ItemRestriction {

	/*
	 * stat that uses a custom DoubleStatData because the merge algorithm is
	 * slightly different. when merging two "required level", MMOItems should
	 * only keep the highest levels of the two and not sum the two values
	 */
	public RequiredLevel() {
		super("REQUIRED_LEVEL", VersionMaterial.EXPERIENCE_BOTTLE.toMaterial(), "Required Level",
				new String[] { "The level your item needs", "in order to be used." }, new String[] { "!block", "all" });
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		int lvl = (int) ((DoubleData) data).getValue();

		item.addItemTag(new ItemTag("MMOITEMS_REQUIRED_LEVEL", lvl));
		item.getLore().insert("required-level", formatNumericStat(lvl, "#", "" + lvl));
	}

	@Override
	public RandomStatData whenInitialized(Object object) {
		return new RandomRequiredLevelData(object);
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			mmoitem.setData(this, new RequiredLevelData(mmoitem.getNBT().getDouble(getNBTPath())));
	}

	@Override
	public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
		int level = item.getInteger("MMOITEMS_REQUIRED_LEVEL");
		if (player.getLevel() < level && !player.getPlayer().hasPermission("mmoitems.bypass.level")) {
			if (message) {
				Message.NOT_ENOUGH_LEVELS.format(ChatColor.RED).send(player.getPlayer(), "cant-use-item");
				player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
			}
			return false;
		}
		return true;
	}
}
