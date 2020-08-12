package net.Indyuce.mmoitems.stat;

import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.lute.BruteLuteAttack;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.lute.CircularLuteAttack;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.lute.LuteAttackHandler;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.lute.SimpleLuteAttack;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.lute.SlashLuteAttack;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.lute.WaveLuteAttack;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;

public class LuteAttackEffectStat extends StringStat {
	public LuteAttackEffectStat() {
		super("LUTE_ATTACK_EFFECT", new ItemStack(VersionMaterial.DIAMOND_HORSE_ARMOR.toMaterial()), "Lute Attack Effect",
				new String[] { "Changes how your lute behaves", "when right clicked.", "&9Tip: /mi list lute" }, new String[] { "lute" });
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			inv.getEditedSection().set("lute-attack-effect", null);
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed the lute attack effect.");
		} else
			new StatEdition(inv, this).enable("Write in the chat the text you want.");
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {
		LuteAttackEffect effect = LuteAttackEffect.valueOf(message.toUpperCase().replace(" ", "_").replace("-", "_"));
		inv.getEditedSection().set("lute-attack-effect", effect.name());
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Lute attack effect successfully changed to " + effect.getDefaultName() + ".");
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		LuteAttackEffect effect = LuteAttackEffect.valueOf(((StringData) data).toString().toUpperCase().replace(" ", "_").replace("-", "_"));
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

		private LuteAttackEffect(LuteAttackHandler handler) {
			this.handler = handler;
		}

		public LuteAttackHandler getAttack() {
			return handler;
		}

		public String getDefaultName() {
			return name().substring(0, 1) + name().substring(1).toLowerCase();
		}

		public String getName() {
			return MMOItems.plugin.getLanguage().getLuteAttackEffectName(this);
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
