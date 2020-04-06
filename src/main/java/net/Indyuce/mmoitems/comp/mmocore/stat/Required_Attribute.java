package net.Indyuce.mmoitems.comp.mmocore.stat;

import org.bukkit.ChatColor;
import org.bukkit.Sound;

import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.AddonMessage;
import net.Indyuce.mmoitems.comp.mmocore.MMOCoreHook.MMOCoreRPGPlayer;
import net.Indyuce.mmoitems.stat.type.Conditional;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;

public class Required_Attribute extends DoubleStat implements Conditional {
	private final PlayerAttribute attribute;

	public Required_Attribute(PlayerAttribute attribute) {
		super("REQUIRED_" + attribute.getId().toUpperCase().replace("-", "_"), VersionMaterial.GRAY_DYE.toItem(), attribute.getName() + " Requirement (MMOCore)", new String[] { "Amount of " + attribute.getName() + " points the", "player needs to use the item." });

		this.attribute = attribute;
	}

	@Override
	public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
		MMOCoreRPGPlayer mmocore = (MMOCoreRPGPlayer) player;
		if (mmocore.getData().getAttributes().getAttribute(attribute) < item.getStat(this)) {
			if (message) {
				new AddonMessage("not-enough-attribute").format(ChatColor.RED, "#attribute#", attribute.getName()).send(player.getPlayer(), "cant-use-item");
				player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
			}
			return false;
		}
		return true;
	}
}
