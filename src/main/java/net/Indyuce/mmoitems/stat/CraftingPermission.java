package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.Indyuce.mmoitems.stat.type.TemplateOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class CraftingPermission extends StringStat implements TemplateOption, GemStoneStat {
    public CraftingPermission() {
        super("CRAFT_PERMISSION", VersionMaterial.OAK_SIGN.toMaterial(), "Crafting Recipe Permission",
                new String[]{"The permission needed to craft this item.", "Changing this value requires &o/mi reload recipes&7."},
                new String[]{"all"});
    }

    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        throw new RuntimeException("Not supported");
    }

    /**
     * This stat is not saved onto items. This method always returns null
     */
    @Nullable
    public StringData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
        throw new RuntimeException("Not supported");
    }

    /**
     * This stat is not saved onto items. This method is empty.
     */
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
        throw new RuntimeException("Not supported");
    }

    /**
     * This stat is not saved onto items. This method returns an empty array.
     */
    @NotNull
    public ArrayList<ItemTag> getAppliedNBT(@NotNull StringData data) {
        throw new RuntimeException("Not supported");
    }
}