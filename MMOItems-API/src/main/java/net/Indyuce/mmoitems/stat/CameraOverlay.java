package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.annotation.VersionDependant;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jules
 */
@VersionDependant(version = {1, 21, 4})
public class CameraOverlay extends StringStat implements GemStoneStat {
    public CameraOverlay() {
        super("CAMERA_OVERLAY", Material.GLASS, "Camera Overlay", new String[]{"Namespaced key of camera overlay texture.", "Available only on 1.20.4+"}, new String[0]);
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
        EquippableComponent comp = item.getMeta().getEquippable();
        comp.setCameraOverlay(NamespacedKey.fromString(data.getString()));
        item.getMeta().setEquippable(comp);
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        ItemMeta meta = mmoitem.getNBT().getItem().getItemMeta();
        if (!meta.hasEquippable()) return;

        var camOverlay = meta.getEquippable().getCameraOverlay();
        if (camOverlay == null) return;

        mmoitem.setData(this, new StringData(camOverlay.toString()));
    }
}
