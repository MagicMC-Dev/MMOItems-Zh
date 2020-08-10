package net.Indyuce.mmoitems.stat;

import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
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
		ConfigFile config = inv.getEdited().getType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			config.getConfig().set(inv.getEdited().getId() + ".lute-attack-effect", null);
			inv.registerTemplateEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed the lute attack effect.");
			return;
		}

		new StatEdition(inv, this).enable("Write in the chat the text you want.");
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		LuteAttackEffect effect = null;
		String format = message.toUpperCase().replace(" ", "_").replace("-", "_");
		try {
			effect = LuteAttackEffect.valueOf(format);
		} catch (Exception e1) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + format + " is not a valid lute attack effect.");
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "See all lute attack effects here: /mi list lute.");
			return false;
		}

		config.getConfig().set(inv.getEdited().getId() + ".lute-attack-effect", effect.name());
		inv.registerTemplateEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Lute attack effect successfully changed to " + effect.getDefaultName() + ".");
		return true;
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
