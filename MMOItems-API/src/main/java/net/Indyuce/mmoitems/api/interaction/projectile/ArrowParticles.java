package net.Indyuce.mmoitems.api.interaction.projectile;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.gson.JsonParser;
import io.lumine.mythic.lib.player.particle.ParticleInformation;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.scheduler.BukkitRunnable;

public class ArrowParticles extends BukkitRunnable {
    private final AbstractArrow arrow;
    private final ParticleInformation particleInfo;

    public ArrowParticles(AbstractArrow arrow, NBTItem item) {
        this.arrow = arrow;
        this.particleInfo = new ParticleInformation(JsonParser.parseString(item.getString("MMOITEMS_ARROW_PARTICLES")).getAsJsonObject());

        runTaskTimer(MMOItems.plugin, 0, 1);
    }

    @Override
    public void run() {
        if (arrow.isDead() || arrow.isOnGround()) {
            cancel();
            return;
        }

        particleInfo.display(arrow.getLocation().add(0, 0, 0));
    }
}
