package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.lute;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.player.PlayerMetadata;
import net.Indyuce.mmoitems.api.util.SoundReader;
import org.bukkit.util.Vector;

import java.util.Random;

public interface LuteAttackHandler {
    static final Random random = new Random();

    void handle(PlayerMetadata caster, double damage, NBTItem nbt, double range, Vector weight, SoundReader sound);
}
