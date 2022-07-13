package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.lute;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.damage.AttackMetadata;
import net.Indyuce.mmoitems.api.util.SoundReader;
import org.bukkit.util.Vector;

import java.util.Random;

public interface LuteAttackHandler {
	void handle(AttackMetadata attackMeta, NBTItem nbt, double range, Vector weight, SoundReader sound);

	Random random = new Random();
}
