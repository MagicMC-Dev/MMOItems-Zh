package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.type.ChooseStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.util.StatChoice;
import net.Indyuce.mmoitems.util.VersionDependant;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Jules
 */
@VersionDependant(minor = 20)
public class TrimMaterialStat extends ChooseStat implements GemStoneStat {
    public TrimMaterialStat() {
        super("TRIM_MATERIAL", Material.LEATHER_CHESTPLATE, "纹饰材料", new String[]{"用来装饰你的盔甲的材料"}, new String[]{"armor"});

        // Version dependency
        if (!isEnabled()) return;

        for (TrimMaterial mat : Registry.TRIM_MATERIAL)
            addChoices(new StatChoice(mat.getKey().getKey()));
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
        if (!(item.getMeta() instanceof ArmorMeta)) return;

        @Nullable TrimMaterial material = Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(data.toString().toLowerCase()));
        if (material == null) return;

        final ArmorMeta meta = (ArmorMeta) item.getMeta();
        final ArmorTrim currentTrim = meta.hasTrim() ? meta.getTrim() : new ArmorTrim(TrimMaterial.AMETHYST, TrimPattern.COAST);
        meta.setTrim(new ArmorTrim(material, currentTrim.getPattern()));
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        if (!(mmoitem.getNBT().getItem().getItemMeta() instanceof ArmorMeta)) return;
        final ArmorMeta meta = (ArmorMeta) mmoitem.getNBT().getItem().getItemMeta();
        if (!meta.hasTrim()) return;
        mmoitem.setData(this, new StringData(meta.getTrim().getMaterial().getKey().getKey()));
    }
}
