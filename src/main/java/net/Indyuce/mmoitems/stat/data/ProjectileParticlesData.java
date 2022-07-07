package net.Indyuce.mmoitems.stat.data;

import com.google.gson.JsonObject;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ProjectileParticlesData implements StatData, RandomStatData<ProjectileParticlesData> {
    private final Particle particle;
    private final int red, green, blue;
    private final boolean colored;

    // Particles that can't be colored
    public ProjectileParticlesData(Particle particle) {
        this.particle = particle;
        this.red = 0;
        this.green = 0;
        this.blue = 0;
        this.colored = false;
    }

    // Particles that can be colored
    public ProjectileParticlesData(Particle particle, int red, int green, int blue) {
        this.particle = particle;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.colored = true;
    }

    public Particle getParticle() {
        return particle;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public static boolean isColorable(Particle particle) {
        return particle == Particle.REDSTONE || particle == Particle.SPELL_MOB || particle == Particle.SPELL_MOB_AMBIENT || particle == Particle.NOTE;
    }

    @Override
    public String toString() {
        JsonObject object = new JsonObject();
        object.addProperty("Particle", particle.name());
        if (colored) {
            object.addProperty("Red", red);
            object.addProperty("Green", green);
            object.addProperty("Blue", blue);
        }

        return object.toString();
    }

    public static void shootParticle(Player player, Particle particle, Location loc, double offsetX, double offsetY, double offsetZ) {
        if (isColorable(particle)) {
            switch (particle) {
                case REDSTONE:
                    // REDSTONE particles take dustOptions with RGB values. Normal REDSTONE particles are size 1 but can realistically be anything.
                    Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB((int) offsetX, (int) offsetY, (int) offsetZ), 1);
                    player.getWorld().spawnParticle(Particle.REDSTONE, loc, 25, dustOptions);
                    break;
                case NOTE:
                    // NOTE particles only have 24 colors. offsetX must be a number between 0 and 1 in intervals of 1/24. offsetY and offsetZ must be 0. Count should be 0 and "extra" should be 1.
                    double note = offsetX / 24D;
                    player.getWorld().spawnParticle(Particle.NOTE, loc, 0, note, 0, 0, 1);
                    break;
                default:
                    // SPELL_MOB and SPELL_MOB_AMBIENT must be a value between 0 and 1 in intervals in 1/255. "Extra" must be 1 or the color will not be correct. 0 will be black and anything else will be random.
                    double red = offsetX / 255D;
                    double green = offsetY / 255D;
                    double blue = offsetZ / 255D;
                    player.getWorld().spawnParticle(particle, loc, 0, red, green, blue, 1);
            }
        } else {
            // Some particles require a material. I don't really want to handle this right now so just make it stone.
            if (particle == Particle.ITEM_CRACK || particle == Particle.BLOCK_CRACK || particle == Particle.BLOCK_DUST || particle == Particle.FALLING_DUST) {
                if (particle == Particle.ITEM_CRACK) {
                    ItemStack materialData = new ItemStack(Material.STONE);
                    player.getWorld().spawnParticle(particle, loc, 0, materialData);
                } else {
                    BlockData fallingDustData = Material.STONE.createBlockData();
                    player.getWorld().spawnParticle(particle, loc, 0, fallingDustData);
                }
            } else {
                // All non-material and non-colorable particles just get shot normally.
                // Changing the count to anything other than 0 will cause certain particles to shoot off in random directions as they take directional parameters via offsets.
                player.getWorld().spawnParticle(particle, loc, 0);
            }
        }
    }

    @Override
    public ProjectileParticlesData randomize(MMOItemBuilder builder) {
        return this;
    }
}
