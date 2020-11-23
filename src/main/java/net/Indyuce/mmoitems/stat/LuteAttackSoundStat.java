package net.Indyuce.mmoitems.stat;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.version.VersionMaterial;

public class LuteAttackSoundStat extends StringStat {
	public LuteAttackSoundStat() {
		super("LUTE_ATTACK_SOUND", new ItemStack(VersionMaterial.GOLDEN_HORSE_ARMOR.toMaterial()), "Lute Attack Sound", new String[] { "The sound played when", "basic attacking with this lute." }, new String[] { "lute" });
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		item.addItemTag(new ItemTag("MMOITEMS_LUTE_ATTACK_SOUND", data.toString().toUpperCase().replace("-", "_").replace(" ", "_")));
	}
}
