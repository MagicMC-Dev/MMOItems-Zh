package net.Indyuce.mmoitems.stat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.ProjectileParticlesData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Projectile Particles are the particles fired in place of a projectile for certain weapons such as the Lute, Musket, or Whip [WIP]
 *
 * @author Kasprr
 */

public class ProjectileParticles extends ItemStat<ProjectileParticlesData, ProjectileParticlesData> {
    public ProjectileParticles() {
        super("PROJECTILE_PARTICLES", VersionMaterial.LIME_STAINED_GLASS.toMaterial(), "射弹粒子",
                new String[]{"你的武器发射的射弹粒子"}, new String[]{"lute"});
    }

    @Override
    public ProjectileParticlesData whenInitialized(Object object) {
        Validate.isTrue(object instanceof ConfigurationSection, "必须指定有效的配置部分");
        ConfigurationSection config = (ConfigurationSection) object;

        Validate.isTrue(config.contains("particle"), "找不到粒子");
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
            inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "成功删除粒子");
        } else
            new StatEdition(inv, this).enable("在聊天中输入您想要的粒子以及颜色 (如果适用) ",
                    ChatColor.AQUA + "格式: {粒子} {颜色}",
                    "所有粒子都可以在这里找到: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Particle.html");
    }

    @NotNull
    @Override
    public ProjectileParticlesData getClearStatData() {
        // Arbitrary choice
        return new ProjectileParticlesData(Particle.FLAME);
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<ProjectileParticlesData> statData) {
        if (statData.isPresent()) {
            final ProjectileParticlesData data = statData.get();
            Particle particle = data.getParticle();

            lore.add(ChatColor.GRAY + "当前值: " + ChatColor.GREEN + particle);

            if (ProjectileParticlesData.isColorable(particle)) {
                String colorStr = particle == Particle.NOTE ? String.valueOf(data.getRed()) : data.getRed() + " " + data.getGreen() + " " + data.getBlue();
                lore.add(ChatColor.GRAY + "颜色: " + ChatColor.GREEN + colorStr);
            }
        } else
            lore.add(ChatColor.GRAY + "当前值: " + ChatColor.RED + "None");

        lore.add("");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击可更改此值");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击可删除该值");
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        String[] msg = message.replace(", ", " ").replace(",", " ").split(" ");
        Particle particle = Particle.valueOf(msg[0].toUpperCase().replace("-", "_").replace(" ", "_"));
        if (ProjectileParticlesData.isColorable(particle)) {
            Validate.isTrue(msg.length <= 4, "提供了太多参数");
            if (particle.equals(Particle.NOTE)) {
                Validate.isTrue(msg.length == 2, "您必须为此粒子提供颜色\n"
                        + MMOItems.plugin.getPrefix() + "注意粒子颜色仅采用 1 到 24 之间的单个值\n"
                        + MMOItems.plugin.getPrefix() + ChatColor.AQUA + "格式: {粒子} {颜色}");
                int red = Math.min(24, Math.max(1, Integer.parseInt(msg[1])));
                inv.getEditedSection().set("projectile-particles.particle", particle.name());
                inv.getEditedSection().set("projectile-particles.color.red", red);
                inv.getEditedSection().set("projectile-particles.color.green", 0);
                inv.getEditedSection().set("projectile-particles.color.blue", 0);
                inv.registerTemplateEdition();
                inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "粒子成功设置为 "
                        + UtilityMethods.caseOnWords(particle.name().toLowerCase().replace("_", " ")) + " 颜色: " + red);
            } else {
                Validate.isTrue(msg.length == 4, "您必须为此粒子提供颜色\n"
                        + MMOItems.plugin.getPrefix() + ChatColor.AQUA + "格式: {粒子} {R G B}");
                int red = msg[1] != null ? Math.min(255, Math.max(0, Integer.parseInt(msg[1]))) : 0;
                int green = msg[2] != null ? Math.min(255, Math.max(0, Integer.parseInt(msg[2]))) : 0;
                int blue = msg[3] != null ? Math.min(255, Math.max(0, Integer.parseInt(msg[3]))) : 0;
                inv.getEditedSection().set("projectile-particles.particle", particle.name());
                inv.getEditedSection().set("projectile-particles.color.red", red);
                inv.getEditedSection().set("projectile-particles.color.green", green);
                inv.getEditedSection().set("projectile-particles.color.blue", blue);
                inv.registerTemplateEdition();
                inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "粒子成功设置为 "
                        + UtilityMethods.caseOnWords(particle.name().toLowerCase().replace("_", " ")) + " RGB 颜色为: " + red + " " + green + " " + blue);
            }
        } else {
            Validate.isTrue(msg.length == 1, "该粒子无法指定颜色");
            inv.getEditedSection().set("projectile-particles.particle", particle.name());
            inv.getEditedSection().set("projectile-particles.color.red", 0);
            inv.getEditedSection().set("projectile-particles.color.green", 0);
            inv.getEditedSection().set("projectile-particles.color.blue", 0);
            inv.registerTemplateEdition();
            inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "粒子成功设置为 "
                    + UtilityMethods.caseOnWords(particle.name().toLowerCase().replace("_", " ")));
        }
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull ProjectileParticlesData data) {
        item.addItemTag(getAppliedNBT(data));
    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull ProjectileParticlesData data) {
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

    public ProjectileParticlesData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
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
