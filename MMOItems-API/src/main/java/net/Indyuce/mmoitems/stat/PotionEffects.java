package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.PotionEffectData;
import net.Indyuce.mmoitems.stat.data.PotionEffectListData;
import net.Indyuce.mmoitems.stat.data.random.RandomPotionEffectData;
import net.Indyuce.mmoitems.stat.data.random.RandomPotionEffectListData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PotionEffects extends ItemStat<RandomPotionEffectListData, PotionEffectListData> {
	public PotionEffects() {
		super("POTION_EFFECT", Material.POTION, "药水效果", new String[] { "你的药水的效果", " (可能对颜色有影响) ", "不支持尖箭头" },
				new String[] { "all" }, Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION);
	}

	@Override
	public RandomPotionEffectListData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "必须指定配置部分");
		return new RandomPotionEffectListData((ConfigurationSection) object);
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStats.POTION_EFFECTS).enable("在聊天中输入你想要添加的药水效果",
					ChatColor.AQUA + "格式: {效果名称} {持续时间} {等级}",
					ChatColor.AQUA + "其他格式: {效果名称}|{持续时间数值公式}|{增幅数值公式}");

		if (event.getAction() == InventoryAction.PICKUP_HALF && inv.getEditedSection().contains("potion-effect")) {
			Set<String> set = inv.getEditedSection().getConfigurationSection("potion-effect").getKeys(false);
			String last = new ArrayList<>(set).get(set.size() - 1);
			inv.getEditedSection().set("potion-effect." + last, null);
			if (set.size() <= 1)
				inv.getEditedSection().set("potion-effect", null);
			inv.registerTemplateEdition();
			inv.getPlayer()
					.sendMessage(MMOItems.plugin.getPrefix() + "已成功删除" + ChatColor.GOLD + formatName(last) + ChatColor.GRAY + ".");
		}
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		if (message.contains("|")) {
			String[] split = message.split("\\|");

			PotionEffectType effect = PotionEffectType.getByName(split[0].replace("-", "_"));
			Validate.notNull(effect, split[0] + " 不是有效的药水效果, 所有药水效果都可以在这里找到: "
					+ "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");

			NumericStatFormula duration = new NumericStatFormula(split[1]), amplifier = new NumericStatFormula(split[2]);
			duration.fillConfigurationSection(inv.getEditedSection(), "potion-effect." + effect.getName() + ".duration");
			amplifier.fillConfigurationSection(inv.getEditedSection(), "potion-effect." + effect.getName() + ".amplifier");
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GOLD + formatName(effect) + ChatColor.GRAY + " successfully added.");
			return;
		}

		String[] split = message.split(" ");
		Validate.isTrue(split.length == 3,
				message + " 不是有效的{效果名称} {持续时间} {等级}. 例子: 'FAST_DIGGING 30 3' 代表 急速3 30秒.");

		PotionEffectType effect = PotionEffectType.getByName(split[0].replace("-", "_"));
		Validate.notNull(effect, split[0] + " 不是有效的药水效果, 所有药水效果都可以在这里找到: "
				+ "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");

		double duration = MMOUtils.parseDouble(split[1]);
		int amplifier = (int) MMOUtils.parseDouble(split[2]);

		inv.getEditedSection().set("potion-effect." + effect.getName() + ".duration", duration);
		inv.getEditedSection().set("potion-effect." + effect.getName() + ".amplifier", amplifier);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(
				MMOItems.plugin.getPrefix() + ChatColor.GOLD + formatName(effect) + " " + amplifier + ChatColor.GRAY + " successfully added.");
	}

	private String formatName(PotionEffectType effect) {
		return formatName(effect.getName());
	}

	private String formatName(String input) {
		return UtilityMethods.caseOnWords(input.replace("_", " ").toLowerCase());
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomPotionEffectListData> statData) {

		if (statData.isPresent()) {
			lore.add(ChatColor.GRAY + "当前值:");
			RandomPotionEffectListData data = statData.get();
			for (RandomPotionEffectData effect : data.getEffects())
				lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + UtilityMethods.caseOnWords(effect.getType().getName().toLowerCase().replace("_", " "))
						+ " " + effect.getAmplifier().toString() + " " + ChatColor.GRAY + "(" + ChatColor.GREEN + effect.getDuration().toString()
						+ ChatColor.GRAY + "s)");
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
		if (item.getItemStack().getType().name().contains("POTION"))
			for (PotionEffectData effect : data.getEffects())
				((PotionMeta) item.getMeta()).addCustomEffect(effect.toEffect(), false);
	}

	/**
	 * No item tags are added onto the item, I suppose it is saved as the Potion Effects themselves.
	 */
	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull PotionEffectListData data) {
		throw new NotImplementedException();
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

		if (!(mmoitem.getNBT().getItem().getItemMeta() instanceof PotionMeta))
			return;

		// Read all custom potion effects
		PotionEffectListData list = new PotionEffectListData();
		for (PotionEffect effect : ((PotionMeta) mmoitem.getNBT().getItem().getItemMeta()).getCustomEffects())
			list.add(new PotionEffectData(effect));

		mmoitem.setData(this, list);
	}

	@Nullable
	@Override
	public PotionEffectListData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
		throw new NotImplementedException();
	}
}
