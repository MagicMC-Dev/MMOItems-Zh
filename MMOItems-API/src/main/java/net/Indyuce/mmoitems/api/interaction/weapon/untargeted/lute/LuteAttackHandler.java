package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.lute;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.player.PlayerMetadata;
import net.Indyuce.mmoitems.api.util.SoundReader;
import net.Indyuce.mmoitems.stat.data.ProjectileParticlesData;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@Deprecated
public interface LuteAttackHandler {
    static final Random RANDOM = new Random();

    void handle(PlayerMetadata caster, double damage, NBTItem nbt, double range, @NotNull Vector weight, @NotNull SoundReader sound, @NotNull ProjectileParticlesData projParticle);
}
