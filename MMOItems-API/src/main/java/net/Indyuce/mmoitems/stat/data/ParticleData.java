package net.Indyuce.mmoitems.stat.data;

import io.lumine.mythic.lib.gson.JsonObject;
import io.lumine.mythic.lib.player.particle.ParticleEffect;
import io.lumine.mythic.lib.player.particle.ParticleEffectType;
import io.lumine.mythic.lib.player.particle.ParticleInformation;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.util.MMOUtils;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ParticleData implements StatData, RandomStatData<ParticleData> {
    private final ParticleEffectType type;
    private final Particle particle;
    private final Map<String, Double> modifiers = new HashMap<>();
    private final Color color;

    public ParticleData(JsonObject object) {
        particle = Particle.valueOf(object.get("Particle").getAsString());
        type = ParticleEffectType.get(object.get("Type").getAsString());

        if (object.has("Color")) {
            JsonObject color = object.getAsJsonObject("Color");
            this.color = Color.fromRGB(color.get("Red").getAsInt(), color.get("Green").getAsInt(), color.get("Blue").getAsInt());
        } else
            color = Color.fromRGB(255, 0, 0);

        object.getAsJsonObject("Modifiers").entrySet().forEach(entry -> setModifier(entry.getKey(), entry.getValue().getAsDouble()));
    }

    public ParticleData(ConfigurationSection config) {
        Validate.isTrue(config.contains("type") && config.contains("particle"), "粒子缺少类型或选定的粒子");

        String format = config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_");
        type = ParticleEffectType.get(format);

        format = config.getString("particle").toUpperCase().replace("-", "_").replace(" ", "_");
        particle = Particle.valueOf(format);

        color = config.contains("color") ? Color.fromRGB(config.getInt("color.red"), config.getInt("color.green"), config.getInt("color.blue"))
                : Color.fromRGB(255, 0, 0);

        for (String key : config.getKeys(false))
            if (!key.equalsIgnoreCase("particle") && !key.equalsIgnoreCase("type") && !key.equalsIgnoreCase("color"))
                setModifier(key, config.getDouble(key));
    }

    public ParticleData(ParticleEffectType type, Particle particle) {
        this.type = type;
        this.particle = particle;
        this.color = Color.fromRGB(255, 0, 0);
    }

    @NotNull
    public ParticleEffectType getType() {
        return type;
    }

    public Particle getParticle() {
        return particle;
    }

    public Color getColor() {
        return color;
    }

    public double getModifier(@NotNull String path) {
        return modifiers.getOrDefault(path, type.getDefaultModifierValue(path));
    }

    public Set<String> getModifiers() {
        return type.getModifiers();
    }

    public void setModifier(String path, double value) {
        modifiers.put(path, value);
    }

    private ParticleInformation toMythicLib() {
        if (color != null) return new ParticleInformation(particle, 1, 0, 0, color, 1);
        else return new ParticleInformation(particle);
    }

    @NotNull
    public ParticleEffect toModifier(String key) {
        return type.getInstantiator().apply(key, modifiers, toMythicLib());
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("Particle", getParticle().name());
        object.addProperty("Type", getType().getId());

        if (MMOUtils.isColorable(particle)) {
            JsonObject color = new JsonObject();
            color.addProperty("Red", getColor().getRed());
            color.addProperty("Green", getColor().getGreen());
            color.addProperty("Blue", getColor().getBlue());
            object.add("Color", color);
        }

        JsonObject modifiers = new JsonObject();
        getModifiers().forEach(name -> modifiers.addProperty(name, getModifier(name)));
        object.add("Modifiers", modifiers);
        return object;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ParticleData randomize(MMOItemBuilder builder) {
        return this;
    }
}
