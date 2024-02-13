package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.SkullTextureData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SkullTextureStat extends ItemStat<SkullTextureData, SkullTextureData> {
    public SkullTextureStat() {
        super("SKULL_TEXTURE", VersionMaterial.PLAYER_HEAD.toMaterial(), "头颅纹理", new String[]{"头颅纹理 &nvalue&7 可以在头颅数据库中找到。",
                "建议 1.20+ 用户使用头颅纹理&n URL&7"}, new String[]{"all"}, VersionMaterial.PLAYER_HEAD.toMaterial());
    }

    @Override
    public SkullTextureData whenInitialized(Object object) {
        Validate.isTrue(object instanceof ConfigurationSection, "必须指定配置部分");
        ConfigurationSection config = (ConfigurationSection) object;

        final String value = config.getString("value");
        Validate.notNull(value, "无法加载头颅纹理值");

        final String uuid = config.getString("uuid");
        Validate.notNull(uuid, "找不到头颅纹理 UUID: 重新输入您的头颅纹理值，系统将随机选择一个");

        final Object profile = MythicLib.plugin.getVersion().getWrapper().newProfile(UUID.fromString(uuid), value);
        final SkullTextureData skullTexture = new SkullTextureData(profile);
        return skullTexture;
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<SkullTextureData> statData) {
        lore.add(ChatColor.GRAY + "当前值: " + (statData.isPresent() ? ChatColor.GREEN + "提供纹理值 " : ChatColor.RED + "None"));
        lore.add("");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击可更改此值");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击可删除此值");
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        inv.getEditedSection().set("skull-texture.value", message);
        inv.getEditedSection().set("skull-texture.uuid", UUID.randomUUID().toString());
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName() + " 成功更改为 " + message + ".");
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull SkullTextureData data) {
        if (item.getItemStack().getType() != VersionMaterial.PLAYER_HEAD.toMaterial()) return;

        if (data.getGameProfile() != null)
            MythicLib.plugin.getVersion().getWrapper().setProfile((SkullMeta) item.getMeta(), data.getGameProfile());
    }

    /**
     * This stat is saved not as a custom tag, but as the vanilla HideFlag itself.
     * Alas this is an empty array
     */
    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull SkullTextureData data) {
        return new ArrayList<>();
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.PICKUP_HALF) {
            inv.getEditedSection().set(getPath(), null);
            inv.registerTemplateEdition();
            inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "已成功删除 " + getName() + ".");
        } else new StatEdition(inv, this).enable("在聊天中输入您想要的纹理");
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        try {
            final ItemMeta meta = mmoitem.getNBT().getItem().getItemMeta();
            Validate.isTrue(meta instanceof SkullMeta);
            final Object profile = MythicLib.plugin.getVersion().getWrapper().getProfile((SkullMeta) meta);
            mmoitem.setData(ItemStats.SKULL_TEXTURE, new SkullTextureData(profile));
        } catch (RuntimeException ignored) {
        }
    }

    /**
     * This stat is saved not as a custom tag, but as the vanilla Head Texture itself.
     * Alas this method returns null.
     */
    @Nullable
    @Override
    public SkullTextureData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
        return null;
    }

    @NotNull
    @Override
    public SkullTextureData getClearStatData() {
        return new SkullTextureData(null);
    }
}
