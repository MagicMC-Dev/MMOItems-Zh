package net.Indyuce.mmoitems.stat.data;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.gson.JsonObject;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

public class ProjectileParticlesData implements StatData, RandomStatData<ProjectileParticlesData> {
    private final Particle particle;
    private final int red, green, blue;
    private final boolean colored;

    public static final ProjectileParticlesData DEFAULT = new ProjectileParticlesData(Particle.NOTE);

    /**
     * Uncolored particles
     */
    public ProjectileParticlesData(Particle particle) {
        this.particle = particle;
        this.red = 0;
        this.green = 0;
        this.blue = 0;
        this.colored = false;
    }

    /**
     * Colored particles
     */
    public ProjectileParticlesData(Particle particle, int red, int green, int blue) {
        this.particle = particle;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.colored = true;
    }

    /**
     * Colored particles
     */
    public ProjectileParticlesData(String jsonObject) {
        final JsonObject obj = MythicLib.plugin.getGson().fromJson(jsonObject, JsonObject.class);
        particle = Particle.valueOf(obj.get("Particle").getAsString());

        if (isColorable(particle)) {
            colored = true;
            red = obj.get("Red").getAsInt();
            green = obj.get("Green").getAsInt();
            blue = obj.get("Blue").getAsInt();
        } else {
            colored = false;
            red = 0;
            green = 0;
            blue = 0;
        }
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

    public void shootParticle(Location loc) {
        shootParticle(loc, 1, 0);
    }

    public void shootParticle(Location loc, int amount, double offset) {
        if (isColorable(particle)) {
            switch (particle) {
                case REDSTONE:
                    // REDSTONE particles take dustOptions with RGB values. Normal REDSTONE particles are size 1 but can realistically be anything.
                    Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(red, green, blue), 1);
                    loc.getWorld().spawnParticle(Particle.REDSTONE, loc, amount, offset, offset, offset, 0, dustOptions);
                    break;
                case NOTE:
                    // NOTE particles only have 24 colors. offsetX must be a number between 0 and 1 in intervals of 1/24. offsetY and offsetZ must be 0. Count should be 0 and "extra" should be 1.
                    double note = red / 24D;
                    loc.getWorld().spawnParticle(Particle.NOTE, loc, 0, note, 0, 0, 1);
                    break;
                default:
                    // SPELL_MOB and SPELL_MOB_AMBIENT must be a value between 0 and 1 in intervals in 1/255. "Extra" must be 1 or the color will not be correct. 0 will be black and anything else will be random.
                    double red = this.red / 255D;
                    double green = this.green / 255D;
                    double blue = this.blue / 255D;
                    loc.getWorld().spawnParticle(particle, loc, 0, red, green, blue, 1);
            }

        } else if (particle == Particle.ITEM_CRACK) {
            // Some particles require a material. I don't really want to handle this right now so just make it stone.
            ItemStack materialData = new ItemStack(Material.STONE);
            loc.getWorld().spawnParticle(particle, loc, amount, offset, offset, offset, 0, materialData);

        } else if (particle == Particle.ITEM_CRACK || particle == Particle.BLOCK_CRACK || particle == Particle.BLOCK_DUST || particle == Particle.FALLING_DUST) {
            BlockData fallingDustData = Material.STONE.createBlockData();
            loc.getWorld().spawnParticle(particle, loc, amount, offset, offset, offset, 0, fallingDustData);

        } else
            // All non-material and non-colorable particles just get shot normally.
            // Changing the count to anything other than 0 will cause certain particles to shoot off in random directions as they take directional parameters via offsets.
            loc.getWorld().spawnParticle(particle, loc, amount, offset, offset, offset, 0);
    }

    @Override
    public ProjectileParticlesData randomize(MMOItemBuilder builder) {
        return this;
    }
}
