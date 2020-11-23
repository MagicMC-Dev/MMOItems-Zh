package net.Indyuce.mmoitems.stat;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.SoulboundData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.InternalStat;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.regex.Pattern;

public class Soulbound extends InternalStat implements ItemRestriction {
	public Soulbound() {
		super("SOULBOUND", new ItemStack(VersionMaterial.ENDER_EYE.toMaterial()), "Soulbound", new String[0], new String[] { "all" });
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		item.addItemTag(new ItemTag("MMOITEMS_SOULBOUND", ((SoulboundData) data).toJson().toString()));
		String formattedLoreTag = Message.SOULBOUND_ITEM_LORE.getUpdated().replace("#player#", ((SoulboundData) data).getName()).replace("#level#",
				MMOUtils.intToRoman(((SoulboundData) data).getLevel()));
		item.getLore().insert("soulbound", formattedLoreTag.split(Pattern.quote("//")));
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag("MMOITEMS_SOULBOUND"))
			try {
				mmoitem.setData(ItemStats.SOULBOUND,
						new SoulboundData(new JsonParser().parse(mmoitem.getNBT().getString("MMOITEMS_SOULBOUND")).getAsJsonObject()));
			} catch (JsonSyntaxException exception) {
				/*
				 * OLD ITEM WHICH MUST BE UPDATED.
				 */
			}
	}

	public SoulboundData newSoulboundData(UUID uuid, String name, int level) {
		return new SoulboundData(uuid, name, level);
	}

	@Override
	public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
		if (item.hasTag("MMOITEMS_SOULBOUND") && !item.getString("MMOITEMS_SOULBOUND").contains(player.getPlayer().getUniqueId().toString())) {
			if (message) {
				int level = new JsonParser().parse(item.getString("MMOITEMS_SOULBOUND")).getAsJsonObject().get("Level").getAsInt();
				Message.SOULBOUND_RESTRICTION.format(ChatColor.RED).send(player.getPlayer(), "cant-use-item");
				player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
				player.getPlayer()
						.damage(MMOItems.plugin.getLanguage().soulboundBaseDamage + level * MMOItems.plugin.getLanguage().soulboundPerLvlDamage);
			}
			return false;
		}
		return true;
	}
}
