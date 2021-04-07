package net.Indyuce.mmoitems.stat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.ProjectileParticlesData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ProjectileParticles extends StringStat {
  public ProjectileParticles() {
    super("PROJECTILE_PARTICLES", VersionMaterial.LIME_STAINED_GLASS.toMaterial(), "Projectile Particles",
            new String[] { "The projectile particle that your weapon shoots" }, new String[] { "lute", "musket", "whip"} );
  }

  @Override
  public ProjectileParticlesData whenInitialized(Object object) {
    Validate.isTrue(object instanceof ConfigurationSection, "Must specify a valid config section");
    ConfigurationSection config = (ConfigurationSection) object;

    Validate.isTrue(config.contains("particle"), "Could not find projectile particle");
    Particle particle = Particle.valueOf(config.getString("particle").toUpperCase().replace("-", "_").replace(" ", ""));

    return ProjectileParticlesData.isColorable(particle)
            ? new ProjectileParticlesData(particle, config.getInt("color.red"), config.getInt("color.green"), config.getInt("color.blue"))
            : new ProjectileParticlesData(particle);
  }

  @Override
  public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
    if (event.getAction() == InventoryAction.PICKUP_HALF) {
      inv.getEditedSection().set("projectile-particles", null);
      inv.registerTemplateEdition();
      inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed the lute attack effect.");
    } else
      new StatEdition(inv, this).enable("Write in the chat the text you want.");
  }

  @Override
  public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
    String[] msg = message.split(" ");
    Particle particle = Particle.valueOf(msg[0].toUpperCase().replace("-", "_").replace(" ", "_"));
    if (ProjectileParticlesData.isColorable(particle)) {
      Validate.isTrue(msg.length <= 4, "Too many args");
      if (particle.equals(Particle.NOTE)) {
        Validate.isTrue(msg.length == 2, "You must provide a color for NOTE particle between 1 and 24");
        int red = Math.min(24, Math.max(1, Integer.parseInt(msg[1])));
        inv.getEditedSection().set("projectile-particles.particle", particle.name());
        inv.getEditedSection().set("projectile-particles.color.red", red);
        inv.getEditedSection().set("projectile-particles.color.green", 0);
        inv.getEditedSection().set("projectile-particles.color.blue", 0);
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Particle successfully set to " + ChatColor.GOLD
                + MMOUtils.caseOnWords(particle.name().toLowerCase().replace("_", " ")) + " with RGB color " + red);
      } else {
        int red = Integer.parseInt(msg[1]);
        int green = Integer.parseInt(msg[2]);
        int blue = Integer.parseInt(msg[3]);
        inv.getEditedSection().set("projectile-particles.particle", particle.name());
        inv.getEditedSection().set("projectile-particles.color.red", red);
        inv.getEditedSection().set("projectile-particles.color.green", green);
        inv.getEditedSection().set("projectile-particles.color.blue", blue);
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Particle successfully set to " + ChatColor.GOLD
                + MMOUtils.caseOnWords(particle.name().toLowerCase().replace("_", " ")) + " with RGB color " + red + " " + green + " " + blue);
      }
    } else {
      Validate.isTrue(msg.length == 1, "That particle cannot does cannot take a color");
      inv.getEditedSection().set("projectile-particles.particle", particle.name());
      inv.getEditedSection().set("projectile-particles.color.red", 0);
      inv.getEditedSection().set("projectile-particles.color.green", 0);
      inv.getEditedSection().set("projectile-particles.color.blue", 0);
      inv.registerTemplateEdition();
      inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Particle successfully set to " + ChatColor.GOLD
              + MMOUtils.caseOnWords(particle.name().toLowerCase().replace("_", " ")) + ChatColor.GRAY + ".");
    }
  }

  @Override
  public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) {
    item.addItemTag(getAppliedNBT(data));
  }

  @NotNull
  @Override
  public ArrayList<ItemTag> getAppliedNBT(@NotNull StatData data) {
    ArrayList<ItemTag> tags = new ArrayList<>();
    tags.add(new ItemTag(getNBTPath(), data.toString()));
    return tags;
  }

  public void whenLoaded(@NotNull ReadMMOItem mmoItem) {
    // Get tags
    ArrayList<ItemTag> tags = new ArrayList<>();
    if (mmoItem.getNBT().hasTag(getNBTPath()))
      tags.add(ItemTag.getTagAtPath(getNBTPath(), mmoItem.getNBT(), SupportedNBTTagValues.STRING));

    StatData data = getLoadedNBT(tags);

    if (data != null) {
      mmoItem.setData(this, data);
    }
  }

  public StatData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
    // Get tags
    ItemTag tags = ItemTag.getTagAtPath(getNBTPath(), storedTags);

    if (tags != null) {
      try {
        JsonObject json = new JsonParser().parse((String) tags.getValue()).getAsJsonObject();
        Particle particle = Particle.valueOf(json.get("Particle").getAsString());

        if (ProjectileParticlesData.isColorable(particle)) {
          return new ProjectileParticlesData(particle, json.get("Red").getAsInt(), json.get("Green").getAsInt(), json.get("Blue").getAsInt());
        } else {
          return new ProjectileParticlesData(particle);
        }

      } catch (JsonSyntaxException e) {
        e.printStackTrace();
      }


    }

    return null;
  }

}
