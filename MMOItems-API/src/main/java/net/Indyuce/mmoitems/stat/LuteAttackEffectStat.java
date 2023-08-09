package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.lute.*;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LuteAttackEffectStat extends StringStat implements GemStoneStat {
	public LuteAttackEffectStat() {
		super("LUTE_ATTACK_EFFECT", VersionMaterial.DIAMOND_HORSE_ARMOR.toMaterial(), "琴攻击效果",
				new String[] { "更改右键单击时琴的行为方式", "&9Tip: /mi list lute" }, new String[] { "lute" });
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			inv.getEditedSection().set("lute-attack-effect", null);
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "成功删除了琴攻击效果");
		} else
			new StatEdition(inv, this).enable("在聊天中输入您想要的文名称");
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		LuteAttackEffect effect = LuteAttackEffect.valueOf(message.toUpperCase().replace(" ", "_").replace("-", "_"));
		inv.getEditedSection().set("lute-attack-effect", effect.name());
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "琴攻击效果成功变更为" + effect.getName() + ".");
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
		LuteAttackEffect effect = LuteAttackEffect.valueOf(data.toString().toUpperCase().replace(" ", "_").replace("-", "_"));
		item.addItemTag(new ItemTag("MMOITEMS_LUTE_ATTACK_EFFECT", effect.name()));
		item.getLore().insert("lute-attack-effect", effect.getName());
	}

	public enum LuteAttackEffect {
		WAVE(new WaveLuteAttack()),
		CIRCULAR(new CircularLuteAttack()),
		SIMPLE(new SimpleLuteAttack()),
		BRUTE(new BruteLuteAttack()),
		SLASH((new SlashLuteAttack()));

		private final LuteAttackHandler handler;

		@NotNull
		private String name = UtilityMethods.caseOnWords(name().toLowerCase().replace("_", " "));

		LuteAttackEffect(LuteAttackHandler handler) {
			this.handler = handler;
		}

		public LuteAttackHandler getAttack() {
			return handler;
		}

		@Deprecated
		public String getDefaultName() {
			return name().charAt(0) + name().substring(1).toLowerCase();
		}

		public String getName() {
			return name;
		}

		public void setName(@NotNull String str) {
			this.name = Objects.requireNonNull(str);
		}

		public static LuteAttackEffect get(NBTItem item) {
			try {
				return LuteAttackEffect.valueOf(item.getString("MMOITEMS_LUTE_ATTACK_EFFECT"));
			} catch (IllegalArgumentException exception) {
				return null;
			}
		}
	}
}
