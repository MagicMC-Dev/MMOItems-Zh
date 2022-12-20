package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff.*;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import org.jetbrains.annotations.NotNull;


public class StaffSpiritStat extends StringStat {
	public StaffSpiritStat() {
		super("STAFF_SPIRIT", VersionMaterial.BONE_MEAL.toMaterial(), "Staff Spirit",
				new String[] { "Spirit changes the texture", "of the magic attack.", "&9Tip: /mi list spirit" }, new String[] { "staff", "wand" });
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		try {
			StaffSpirit spirit = StaffSpirit.valueOf(message.toUpperCase().replace(" ", "_").replace("-", "_"));
			inv.getEditedSection().set("staff-spirit", spirit.name());
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Staff Spirit successfully changed to " + spirit.getName() + ".");
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException(exception.getMessage() + " (See all Staff Spirits here: /mi list spirit).");
		}
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
		StaffSpirit staffSpirit = StaffSpirit.valueOf(data.toString().toUpperCase().replace(" ", "_").replace("-", "_"));
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

		private final String name, defaultLore;
		private final StaffAttackHandler handler;

		StaffSpirit(String name, String defaultLore, StaffAttackHandler handler) {
			this.name = name;
			this.defaultLore = defaultLore;
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

		public String getName() {
			return MMOItems.plugin.getLanguage().getStaffSpiritName(this);
		}

		public boolean hasLore() {
			return defaultLore != null;
		}

		public String getDefaultLore() {
			return defaultLore;
		}

		public StaffAttackHandler getAttack() {
			return handler;
		}
	}
}
