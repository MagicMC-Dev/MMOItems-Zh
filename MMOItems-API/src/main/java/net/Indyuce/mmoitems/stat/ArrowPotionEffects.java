package net.Indyuce.mmoitems.stat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.AltChar;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.interaction.projectile.ArrowPotionEffectArrayItem;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.PotionEffectData;
import net.Indyuce.mmoitems.stat.data.PotionEffectListData;
import net.Indyuce.mmoitems.stat.data.random.RandomPotionEffectData;
import net.Indyuce.mmoitems.stat.data.random.RandomPotionEffectListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ArrowPotionEffects extends ItemStat<RandomPotionEffectListData, PotionEffectListData> {
	public ArrowPotionEffects() {
		super("ARROW_POTION_EFFECTS", Material.TIPPED_ARROW, "箭药效果",
				new String[] { "实体被这把弓射中时要应用的效果" }, new String[] { "bow", "crossbow" });
	}

	@Override
	public RandomPotionEffectListData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "必须指定配置部分");
		return new RandomPotionEffectListData((ConfigurationSection) object);
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStats.ARROW_POTION_EFFECTS).enable("在聊天中输入您要添加的药水效果",
					ChatColor.AQUA + "格式: [药水效果] [持续时间] [效果级别]");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (inv.getEditedSection().contains("arrow-potion-effects")) {
				Set<String> set = inv.getEditedSection().getConfigurationSection("arrow-potion-effects").getKeys(false);
				String last = Arrays.asList(set.toArray(new String[0])).get(set.size() - 1);
				inv.getEditedSection().set("arrow-potion-effects." + last, null);
				if (set.size() <= 1)
					inv.getEditedSection().set("arrow-potion-effects", null);
				inv.registerTemplateEdition();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "已成功删除" + last.substring(0, 1).toUpperCase()
						+ last.substring(1).toLowerCase() + ChatColor.GRAY + ".");
			}
		}
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		String[] split = message.split(" ");
		Validate.isTrue(split.length == 3,
				message + " 不是有效的 [药剂效果] [持续时间] [效果级别].例如：'FAST_DIGGING 30 3' 表示 急速 3,持续 30 秒");

		PotionEffectType effect = null;
		for (PotionEffectType effect1 : PotionEffectType.values())
			if (effect1 != null)
				if (effect1.getName().equalsIgnoreCase(split[0].replace("-", "_"))) {
					effect = effect1;
					break;
				}
		Validate.notNull(effect, split[0] + " 不是有效的药水效果");

		double duration = MMOUtils.parseDouble(split[1]);
		int amplifier = (int) MMOUtils.parseDouble(split[2]);

		ConfigurationSection section = inv.getEditedSection().createSection("arrow-potion-effects." + effect.getName());
		section.set("duration", duration);
		section.set("amplifier", amplifier); 
		inv.getEditedSection().set("arrow-potion-effects." + effect.getName(), section);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + effect.getName() + " " + amplifier + " 已成功添加");
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomPotionEffectListData> statData) {
		if (statData.isPresent()) {
			lore.add(ChatColor.GRAY + "当前值: ");
			RandomPotionEffectListData data = statData.get();
			for (RandomPotionEffectData effect : data.getEffects())
				lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + UtilityMethods.caseOnWords(effect.getType().getName().toLowerCase().replace("_", " "))
				+ ChatColor.GRAY + " 等级: " + ChatColor.GREEN + effect.getAmplifier() + ChatColor.GRAY + " 持续时间: " + ChatColor.GREEN
				+ effect.getDuration());
		} else
			lore.add(ChatColor.GRAY + "当前值: " + ChatColor.RED + "None");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " 单击以添加效果");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击以删除最后一个效果");
	}

	@NotNull
	@Override
	public PotionEffectListData getClearStatData() {
		return new PotionEffectListData();
	}


	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull PotionEffectListData data) {
		List<String> lore = new ArrayList<>();

		String permEffectFormat = getGeneralStatFormat();
		data.getEffects().forEach(effect -> {
			lore.add(permEffectFormat.replace("{effect}", MMOItems.plugin.getLanguage().getPotionEffectName(effect.getType())
					+ " " + MMOUtils.intToRoman(effect.getLevel()) + "(" + MythicLib.plugin.getMMOConfig().decimal.format(effect.getDuration()) + "s)"));
		});

		item.getLore().insert(getPath(), lore);

		// Add tags
		item.addItemTag(getAppliedNBT(data));
	}

	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull PotionEffectListData data) {

		// Build Tags list
		ArrayList<ItemTag> tags = new ArrayList<>();

		// Will make use of a JsonArray
		JsonArray array = new JsonArray();

		// For every potion effect
		data.getEffects().forEach(effect -> {

			// Get as Json Object
			JsonObject object = new JsonObject();
			object.addProperty("type", effect.getType().getName());
			object.addProperty("level", effect.getLevel());
			object.addProperty("duration", effect.getDuration());
			array.add(object);
		});

		// Add as tag
		tags.add(new ItemTag(getNBTPath(), array.toString()));

		// Thats it
		return tags;
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

		// Find relevant tag
		ArrayList<ItemTag> relevantTags = new ArrayList<>();
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING));

		// Generate Data
		StatData data = getLoadedNBT(relevantTags);

		// Valid?
		if (data != null) { mmoitem.setData(this, data); }
	}

	@Nullable
	@Override
	public PotionEffectListData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

		// Found at path?
		ItemTag tg = ItemTag.getTagAtPath(getNBTPath(), storedTags);

		// Yes?
		if (tg != null) {

			// Fresh data
			PotionEffectListData effects = new PotionEffectListData();

			// All right, parse Json
			for (ArrowPotionEffectArrayItem entry : MythicLib.plugin.getJson().parse((String) tg.getValue(), ArrowPotionEffectArrayItem[].class))
				effects.add(new PotionEffectData(PotionEffectType.getByName(entry.type), entry.duration, entry.level));


			// Return
			return effects;
		}

		return null;
	}
}
