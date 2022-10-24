package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.SoulboundData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.InternalStat;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Soulbound extends ItemStat<RandomStatData<SoulboundData>, SoulboundData> implements InternalStat, ItemRestriction {
	public Soulbound() {
		super("SOULBOUND", VersionMaterial.ENDER_EYE.toMaterial(), "Soulbound", new String[0], new String[] { "all" });
	}

	@Nullable
	@Override
	public RandomStatData<SoulboundData> whenInitialized(Object object) {
		throw new NotImplementedException();
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		throw new NotImplementedException();
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		throw new NotImplementedException();
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData<SoulboundData>> statData) {
		throw new NotImplementedException();
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull SoulboundData data) {
		item.addItemTag(getAppliedNBT(data));

		// Lore stuff
		String formattedLoreTag = Message.SOULBOUND_ITEM_LORE.getUpdated().replace("#player#", ((SoulboundData) data).getName()).replace("#level#",
				MMOUtils.intToRoman(data.getLevel()));
		item.getLore().insert("soulbound", formattedLoreTag.split(Pattern.quote("//")));
	}

	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull SoulboundData data) {
		ArrayList<ItemTag> a = new ArrayList<>();
		a.add(new ItemTag(getNBTPath(), data.toJson().toString()));
		return a;
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
		ArrayList<ItemTag> rTags = new ArrayList<>();
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			rTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING));
		StatData data = getLoadedNBT(rTags);
		if (data != null) { mmoitem.setData(this, data);}
	}

	@Nullable
	@Override
	public SoulboundData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
		ItemTag tag = ItemTag.getTagAtPath(getNBTPath(), storedTags);
		if (tag != null) {
			try {

				// Parse as Json
				return new SoulboundData(new JsonParser().parse((String) tag.getValue()).getAsJsonObject());

			} catch (JsonSyntaxException|IllegalStateException exception) {
				/*
				 * OLD ITEM WHICH MUST BE UPDATED.
				 */
			}
		}
		return null;
	}

	@NotNull
	@Override
	public SoulboundData getClearStatData() { return new SoulboundData(UUID.fromString("df930b7b-a84d-4f76-90ac-33be6a5b6c88"), "gunging", 0); }

	@Override
	public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
		if (item.hasTag(ItemStats.SOULBOUND.getNBTPath()) && !item.getString(ItemStats.SOULBOUND.getNBTPath()).contains(player.getPlayer().getUniqueId().toString()) && !player.getPlayer().hasPermission("mmoitems.bypass.soulbound")) {
			if (message) {
				int level = new JsonParser().parse(item.getString(ItemStats.SOULBOUND.getNBTPath())).getAsJsonObject().get("Level").getAsInt();
				Message.SOULBOUND_RESTRICTION.format(ChatColor.RED).send(player.getPlayer());
				player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
				player.getPlayer()
						.damage(MMOItems.plugin.getLanguage().soulboundBaseDamage + level * MMOItems.plugin.getLanguage().soulboundPerLvlDamage);
			}
			return false;
		}
		return true;
	}
}
