package net.Indyuce.mmoitems.stat.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.random.RandomBooleanData;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BooleanStat extends ItemStat<RandomBooleanData, BooleanData> {
	public BooleanStat(String id, Material mat, String name, String[] lore, String[] types, Material... materials) {
		super(id, mat, name, lore, types, materials);
	}

	@Override
	public RandomBooleanData whenInitialized(Object object) {

		if (object instanceof Boolean)
			return new RandomBooleanData((boolean) object);

		if (object instanceof Number)
			return new RandomBooleanData(Double.parseDouble(object.toString()));

		throw new IllegalArgumentException("必须指定一个数字 (机率) 或 是/否");
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull BooleanData data) {

		// Only if enabled yo
		if (data.isEnabled()) {

			// Add those
			item.addItemTag(getAppliedNBT(data));

			// Show in lore
			item.getLore().insert(getPath(), getGeneralStatFormat());
		}
	}

	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull BooleanData data) {

		// Create Fresh
		ArrayList<ItemTag> ret = new ArrayList<>();

		if (data.isEnabled()) {

			// Add sole tag
			ret.add(new ItemTag(getNBTPath(), true));
		}

		// Return thay
		return ret;
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL) {
			inv.getEditedSection().set(getPath(), inv.getEditedSection().getBoolean(getPath()) ? null : true);
			inv.registerTemplateEdition();
		}

		else if (event.getAction() == InventoryAction.PICKUP_HALF)
			new StatEdition(inv, this).enable("在聊天中输入您想要的概率 (百分比) ");
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {

		double probability = MMOUtils.parseDouble(message);
		Validate.isTrue(probability >= 0 && probability <= 100, "几率必须介于 0 到 100 之间");

		inv.getEditedSection().set(getPath(), probability / 100);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName() + "成功更改为 " + ChatColor.GREEN
				+ MythicLib.plugin.getMMOConfig().decimal.format(probability) + "% 几率" + ChatColor.GRAY + "");
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

		// Find the useful tags
		ArrayList<ItemTag> relevantTags = new ArrayList<>();

		// That one is useful
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.BOOLEAN));

		BooleanData data = (BooleanData) getLoadedNBT(relevantTags);

		// Success?
		if (data != null) {

			// Set the data if it was successful
			mmoitem.setData(this, data);
		}
	}

	@Nullable
	@Override
	public BooleanData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

		// Find relevant tag
		ItemTag encoded = ItemTag.getTagAtPath(getNBTPath(), storedTags);

		// Found it?
		if (encoded != null) {

			// Well read it!
			return new BooleanData((Boolean) encoded.getValue());
		}

		return null;
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomBooleanData> statData) {

		if (statData.isPresent()) {
			final double chance = statData.get().getChance();
			lore.add(ChatColor.GRAY + "当前值: " + (chance >= 1 ? ChatColor.GREEN + "Ture"
					: chance <= 0 ? ChatColor.RED + "False" : ChatColor.GREEN + MythicLib.plugin.getMMOConfig().decimal.format(chance * 100) + "%"));

		} else
			lore.add(ChatColor.GRAY + "当前值: " + ChatColor.RED + "False");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击可切换该值");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击选择有此选项的概率");
	}

	@NotNull
	@Override
	public BooleanData getClearStatData() {
		return new BooleanData(false);
	}
}
