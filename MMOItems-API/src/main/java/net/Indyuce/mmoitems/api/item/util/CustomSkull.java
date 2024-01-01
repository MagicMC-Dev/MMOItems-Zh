package net.Indyuce.mmoitems.api.item.util;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.util.AdventureUtils;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.stream.Collectors;

public class CustomSkull extends ConfigItem {
    private final String textureValue;

    public CustomSkull(String id, String textureValue) {
        this(id, textureValue, null);
    }

    public CustomSkull(String id, String textureValue, String name, String... lore) {
        super(id, VersionMaterial.PLAYER_HEAD.toMaterial(), name, lore);

        this.textureValue = textureValue;
    }

    public void updateItem() {
        setItem(VersionMaterial.PLAYER_HEAD.toItem());
        SkullMeta meta = (SkullMeta) getItem().getItemMeta();
        AdventureUtils.setDisplayName(meta, getName());
        meta.addItemFlags(ItemFlag.values());

        UtilityMethods.setTextureValue(meta, textureValue);

        if (hasLore())
            AdventureUtils.setLore(meta, getLore()
                    .stream()
                    .map(s -> ChatColor.GRAY + s)
                    .collect(Collectors.toList()));

        getItem().setItemMeta(meta);
        setItem(MythicLib.plugin.getVersion().getWrapper().getNBTItem(getItem()).addTag(new ItemTag("ItemId", getId())).toItem());
    }
}
