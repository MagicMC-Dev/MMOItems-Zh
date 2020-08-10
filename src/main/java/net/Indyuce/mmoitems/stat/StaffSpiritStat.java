package net.Indyuce.mmoitems.stat;

import org.bukkit.ChatColor;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff.LightningSpirit;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff.ManaSpirit;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff.NetherSpirit;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff.StaffAttackHandler;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff.SunfireSpirit;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff.ThunderSpirit;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff.VoidSpirit;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff.XRaySpirit;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;

public class StaffSpiritStat extends StringStat {
	public StaffSpiritStat() {
		super("STAFF_SPIRIT", VersionMaterial.BONE_MEAL.toItem(), "Staff Spirit",
				new String[] { "Spirit changes the texture", "of the magic attack.", "&9Tip: /mi list spirit" }, new String[] { "staff", "wand" });
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		StaffSpirit ss = null;
		String format = message.toUpperCase().replace(" ", "_").replace("-", "_");
		try {
			ss = StaffSpirit.valueOf(format);
		} catch (Exception e1) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + format + " is not a valid staff spirit.");
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "See all Staff Spirits here: /mi list spirit.");
			return false;
		}

		config.getConfig().set(inv.getEdited().getId() + ".staff-spirit", ss.name());
		inv.registerTemplateEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Staff Spirit successfully changed to " + ss.getName() + ".");
		return true;
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		StaffSpirit staffSpirit = StaffSpirit.valueOf(((StringData) data).toString().toUpperCase().replace(" ", "_").replace("-", "_"));
		item.addItemTag(new ItemTag("MMOITEMS_STAFF_SPIRIT", staffSpirit.name()));
		item.getLore().insert("staff-spirit", staffSpirit.getName());
	}

	public enum StaffSpirit {
		NETHER_SPIRIT("Nether Spirit", "Shoots fire beams.", new NetherSpirit()),
		VOID_SPIRIT("Void Spirit", "Shoots shulker missiles.", new VoidSpirit()),
		MANA_SPIRIT("Mana Spirit", "Summons mana bolts.", new ManaSpirit()),
		LIGHTNING_SPIRIT("Lightning Spirit", "Summons lightning bolts.", new LightningSpirit()),
		XRAY_SPIRIT("X-Ray Spirit", "Fires piercing & powerful X-rays.", new XRaySpirit()),
		THUNDER_SPIRIT("Thunder Spirit", "Fires AoE damaging thunder strikes.", new ThunderSpirit()),
		SUNFIRE_SPIRIT("Sunfire Spirit", "Fires AoE damaging fire comets.", new SunfireSpirit()),
		// CURSED_SPIRIT(ChatColor.DARK_PURPLE, "Cursed Spirit", "Fires a
		// targeted cursed projectile."), new CursedSpirit()),
		;

		private final String lore;
		private final StaffAttackHandler handler;

		private String name;

		private StaffSpirit(String name, String lore, StaffAttackHandler handler) {
			this.name = name;
			this.lore = lore;
			this.handler = handler;
		}

		public static StaffSpirit get(NBTItem item) {
			try {
				return StaffSpirit.valueOf(item.getString("MMOITEMS_STAFF_SPIRIT"));
			} catch (Exception e) {
				return null;
			}
		}

		public String getDefaultName() {
			return name;
		}

		/*
		 * TODO make update
		 */
		public String getName() {
			return MMOItems.plugin.getLanguage().getStaffSpiritName(this);
		}

		public boolean hasLore() {
			return lore != null;
		}

		public String getLore() {
			return lore;
		}

		public StaffAttackHandler getAttack() {
			return handler;
		}
	}
}
