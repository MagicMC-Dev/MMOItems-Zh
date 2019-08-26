package net.Indyuce.mmoitems.stat;

import java.util.logging.Level;

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
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.Indyuce.mmoitems.version.VersionMaterial;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class Lute_Attack_Effect extends StringStat {
	public Lute_Attack_Effect() {
		super(new ItemStack(VersionMaterial.GOLDEN_HORSE_ARMOR.toMaterial()), "Lute Attack Effect", new String[] { "Changes how your lute behaves", "when right clicked." }, "lute-attack-effect", new String[] { "lute" });
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getItemType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			config.getConfig().set(inv.getItemId() + ".lute-attack-effect", null);
			inv.registerItemEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed Lute Attack Effect.");
			return true;
		}

		new StatEdition(inv, this).enable("Write in the chat the text you want.");
		inv.getPlayer().sendMessage("");
		inv.getPlayer().sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Available Lute Attacks");
		for (LuteAttackEffect effect : LuteAttackEffect.values())
			inv.getPlayer().sendMessage("* " + ChatColor.GREEN + effect.name());
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		LuteAttackEffect effect = null;
		String format = message.toUpperCase().replace(" ", "_").replace("-", "_");
		try {
			effect = LuteAttackEffect.valueOf(format);
		} catch (Exception e1) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + format + " is not a valid lute attack effect.");
			return false;
		}

		config.getConfig().set(inv.getItemId() + ".lute-attack-effect", effect.name());
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Staff Spirit successfully changed to " + effect.getDefaultName() + ".");
		return true;
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		try {
			LuteAttackEffect effect = LuteAttackEffect.valueOf(((StringData) data).toString().toUpperCase().replace(" ", "_").replace("-", "_"));
			item.addItemTag(new ItemTag("MMOITEMS_LUTE_ATTACK_EFFECT", effect.name()));
			item.getLore().insert("lute-attack-effect", effect.getName());
		} catch (Exception e) {
			item.getMMOItem().log(Level.WARNING, "Coudln't read lute attack effect from " + ((StringData) data).toString());
		}
		return true;
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
			} catch (Exception e) {
				return null;
			}
		}
	}
}
