package net.Indyuce.mmoitems.stat;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.Message;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.Conditional;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.version.VersionMaterial;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class Required_Level extends DoubleStat implements Conditional {
	public Required_Level() {
		super(new ItemStack(VersionMaterial.EXPERIENCE_BOTTLE.toMaterial()), "Required Level", new String[] { "The level your item needs", "in order to be used." }, "required-level", new String[] { "all" });
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		int lvl = (int) ((DoubleData) data).generateNewValue();

		item.addItemTag(new ItemTag("MMOITEMS_REQUIRED_LEVEL", lvl));
		item.getLore().insert("required-level", format(lvl, "#", "" + lvl));
		return true;
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
