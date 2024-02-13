package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.api.util.EnumUtils;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.MaterialData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaterialStat extends ItemStat<MaterialData, MaterialData> {
	public MaterialStat() {
		super("MATERIAL", VersionMaterial.GRASS_BLOCK.toMaterial(), "材料", new String[] { "你的物品材料(原版)" }, new String[] { "all" });
	}

	@Override
	public MaterialData whenInitialized(Object object) {
		Validate.isTrue(object instanceof String, "必须将材料名称指定为字符串");
		return new MaterialData(Material.valueOf(((String) object).toUpperCase().replace("-", "_").replace(" ", "_")));
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		new StatEdition(inv, ItemStats.MATERIAL).enable("在聊天中输入您想要的材料");
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		Optional<Material> material = EnumUtils.getIfPresent(Material.class, message.toUpperCase().replace("-", "_").replace(" ", "_"));
		if (material.isPresent()) {
			inv.getEditedSection().set("material", material.get().name());
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "材料成功更改为 " + material.get().name() + ".");
		} else
			inv.getPlayer().spigot().sendMessage(new ComponentBuilder("无效材料！  (点击查看有效材料清单) ").color(ChatColor.RED)
					.event(new ClickEvent(Action.OPEN_URL, "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html")).create());
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull MaterialData data) {
		/*
		 * material is handled directly in the MMOBuilder constructor, therefore
		 * nothing needs to be done here
		 */
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
		mmoitem.setData(this, new MaterialData(mmoitem.getNBT().getItem().getType()));
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<MaterialData> statData) {
		lore.add(ChatColor.GRAY + "当前值: "
				+ (statData.isPresent()
						? ChatColor.GREEN + UtilityMethods.caseOnWords(statData.get().getMaterial().name().toLowerCase().replace("_", " "))
						: ChatColor.RED + "None"));

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击可更改此值");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击可删除该值");
	}

	@NotNull
	@Override
	public MaterialData getClearStatData() {
		return new MaterialData(Material.IRON_ORE);
	}

	/**
	 * This stat is saved not as a custom tag, but as the vanilla material itself.
	 * Alas this is an empty array
	 */
	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull MaterialData data) { return new ArrayList<>(); }

	/**
	 * This stat is saved not as a custom tag, but as the vanilla material itself.
	 * Alas this method returns null.
	 */
	@Nullable
	@Override
	public MaterialData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) { return null; }
}
