package net.Indyuce.mmoitems.api.item.util;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.util.AdventureUtils;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigItem {
    private final String id;
    private final ItemStack icon;

    // updated when the plugin reloads
    private String name;
    private List<String> lore;

    // generated
    private ItemStack item;

    public ConfigItem(String id, Material material) {
        this(id, material, null);
    }

    public ConfigItem(String id, Material material, String name, String... lore) {
        Validate.notNull(id, "ID cannot be null");
        Validate.notNull(material, "Material cannot be null");

        this.id = id;
        this.icon = new ItemStack(material);
        this.name = name;
        this.lore = Arrays.asList(lore);
    }

    /*
     * used as util to load an item stack from a config
     */
    public ConfigItem(ConfigurationSection config) {
        Validate.notNull(config, "Config cannot be null");
        id = config.getName();

        Validate.isTrue(config.contains("material"), "Could not find material");

        icon = MMOUtils.readIcon(config.getString("material"));

        name = config.getString("name", "");
        lore = config.getStringList("lore");

        updateItem();
    }

    public String getId() {
        return id;
    }

    public void setup(ConfigurationSection config) {
        config.set("name", getName());
        config.set("lore", getLore());
    }

    public void update(ConfigurationSection config) {
        Validate.notNull(config, "Config cannot be null");

        setName(config.getString("name", ""));
        setLore(config.contains("lore") ? config.getStringList("lore") : new ArrayList<>());

        String mat = config.getString("material");
        if (mat != null && !mat.isEmpty()) {
            try {
                // Get the material
                Material m = Material.valueOf(config.getString("material"));

                if (m.isItem()) {

                    // That's the material
                    setMaterial(m);
                }

            } catch (IllegalArgumentException ignored) {
            }
        }

        setModel(SilentNumbers.IntegerParse(config.getString("model")));

        updateItem();
    }

    public void updateItem() {
        setItem(icon);
        if (icon.getType() == Material.AIR)
            return;

        ItemMeta meta = item.getItemMeta();
        AdventureUtils.setDisplayName(meta, name);
        meta.addItemFlags(ItemFlag.values());

        if (hasLore())
            AdventureUtils.setLore(meta, getLore()
                    .stream()
                    .map(s -> ChatColor.GRAY + s)
                    .collect(Collectors.toList()));

        item.setItemMeta(meta);
        item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(item).addTag(new ItemTag("ItemId", id)).toItem();
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public boolean hasLore() {
        return lore != null;
    }

    public ItemStack getItem() {
        return item;
    }

    public ItemStack getNewItem() {
        return item.clone();
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void setLore(List<String> lore) {
        this.lore = lore;
    }

    protected void setItem(ItemStack item) {
        this.item = item;
    }


    /**
     * Unidentified items are ruined when, using a custom resourcepack, they get the material and
     * custom model data of what they should be, making them not really unidentified.... this will
     * kick in for unidentified items when being built, thus making them no longer identifiable by
     * their texture.
     *
     * @param mat Material to set (optional)
     * @author Gunging
     * @see #setModel(Integer)
     */
    protected void setMaterial(@Nullable Material mat) {
        material = mat;
    }

    @Nullable
    protected Material material = null;

    /**
     * Unidentified items are ruined when, using a custom resourcepack, they get the material and
     * custom model data of what they should be, making them not really unidentified.... this will
     * kick in for unidentified items when being built, thus making them no longer identifiable by
     * their texture.
     *
     * @param cmd Custom Model Data to set (optional)
     * @author Gunging
     * @see #setMaterial(Material)
     */
    protected void setModel(@Nullable Integer cmd) {
        customModelData = cmd;
    }

    @Nullable
    protected Integer customModelData = null;
}
