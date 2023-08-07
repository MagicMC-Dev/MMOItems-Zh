package net.Indyuce.mmoitems.api;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.util.identify.UnidentifiedItem;
import net.Indyuce.mmoitems.manager.TypeManager;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class Type {

    // Slashing
    public static final Type SWORD = new Type(TypeSet.SLASHING, "SWORD", true, ModifierSource.MELEE_WEAPON);

    // Piercing
    public static final Type DAGGER = new Type(TypeSet.PIERCING, "DAGGER", true, ModifierSource.MELEE_WEAPON);
    public static final Type SPEAR = new Type(TypeSet.PIERCING, "SPEAR", true, ModifierSource.MELEE_WEAPON);

    // Blunt
    public static final Type HAMMER = new Type(TypeSet.BLUNT, "HAMMER", true, ModifierSource.MELEE_WEAPON);
    public static final Type GAUNTLET = new Type(TypeSet.BLUNT, "GAUNTLET", true, ModifierSource.MELEE_WEAPON);

    // Range
    public static final Type WHIP = new Type(TypeSet.RANGE, "WHIP", true, ModifierSource.RANGED_WEAPON);
    public static final Type STAFF = new Type(TypeSet.RANGE, "STAFF", true, ModifierSource.RANGED_WEAPON);
    public static final Type BOW = new Type(TypeSet.RANGE, "BOW", true, ModifierSource.RANGED_WEAPON);
    public static final Type CROSSBOW = new Type(TypeSet.RANGE, "CROSSBOW", false, ModifierSource.RANGED_WEAPON);
    public static final Type MUSKET = new Type(TypeSet.RANGE, "MUSKET", true, ModifierSource.RANGED_WEAPON);
    public static final Type LUTE = new Type(TypeSet.RANGE, "LUTE", true, ModifierSource.RANGED_WEAPON);

    // Hand Accessories
    public static final Type CATALYST = new Type(TypeSet.CATALYST, "CATALYST", false, ModifierSource.HAND_ITEM);
    public static final Type OFF_CATALYST = new Type(TypeSet.CATALYST, "OFF_CATALYST", false, ModifierSource.OFFHAND_ITEM);
    public static final Type MAIN_CATALYST = new Type(TypeSet.CATALYST, "MAIN_CATALYST", false, ModifierSource.MAINHAND_ITEM);

    // Any
    public static final Type ORNAMENT = new Type(TypeSet.EXTRA, "ORNAMENT", false, ModifierSource.VOID);

    // Extra
    public static final Type ARMOR = new Type(TypeSet.EXTRA, "ARMOR", false, ModifierSource.ARMOR);
    public static final Type TOOL = new Type(TypeSet.EXTRA, "TOOL", false, ModifierSource.MELEE_WEAPON);
    public static final Type CONSUMABLE = new Type(TypeSet.EXTRA, "CONSUMABLE", false, ModifierSource.MAINHAND_ITEM);
    public static final Type MISCELLANEOUS = new Type(TypeSet.EXTRA, "MISCELLANEOUS", false, ModifierSource.MAINHAND_ITEM);
    public static final Type GEM_STONE = new Type(TypeSet.EXTRA, "GEM_STONE", false, ModifierSource.VOID);
    public static final Type SKIN = new Type(TypeSet.EXTRA, "SKIN", false, ModifierSource.VOID);
    public static final Type ACCESSORY = new Type(TypeSet.EXTRA, "ACCESSORY", false, ModifierSource.ACCESSORY);
    public static final Type BLOCK = new Type(TypeSet.EXTRA, "BLOCK", false, ModifierSource.VOID);

    private final String id;
    private final TypeSet set;
    private final ModifierSource modifierSource;
    private final boolean weapon;

    private String name;

    @Nullable
    private String loreFormat;

    /**
     * Used to display the item in the item explorer and in the item recipes
     * list in the advanced workbench. can also be edited using the config
     * files.
     */
    private ItemStack item;

    /**
     * Any type can have a subtype which basically dictates what the item type
     * does.
     */
    private Type parent;

    private UnidentifiedItem unidentifiedTemplate;

    /**
     * List of stats which can be applied onto an item which has this type. This
     * improves performance when generating an item by a significant amount.
     */
    private final List<ItemStat> available = new ArrayList<>();

    @Deprecated
    public Type(TypeSet set, String id, boolean weapon, ModifierSource modSource, boolean fourGUIMode) {
        this(set, id, weapon, modSource);
    }

    public Type(TypeSet set, String id, boolean weapon, ModifierSource modSource) {
        this.set = set;
        this.id = id.toUpperCase().replace("-", "_").replace(" ", "_");
        this.modifierSource = modSource;
        this.weapon = weapon;
        this.loreFormat = null;
    }

    public Type(@NotNull TypeManager manager, @NotNull ConfigurationSection config) {
        id = config.getName().toUpperCase().replace("-", "_").replace(" ", "_");

        parent = manager.get(config.getString("parent", "").toUpperCase().replace("-", "_").replace(" ", "_"));

        set = (parent != null ? parent.set : TypeSet.EXTRA);
        weapon = (parent != null && parent.weapon);
        modifierSource = (parent != null ? parent.modifierSource : ModifierSource.OTHER);
        this.loreFormat = config.getString("LoreFormat", (parent != null ? parent.loreFormat : null));
    }

    public void load(ConfigurationSection config) {
        Validate.notNull(config, "Could not find config for " + getId());

        name = config.getString("name", name);
        item = read(config.getString("display", item == null ? Material.STONE.toString() : item.getType().toString()));

        (unidentifiedTemplate = new UnidentifiedItem(this)).update(config.getConfigurationSection("unident-item"));

        // Getting overridden?
        loreFormat = config.getString("LoreFormat", (parent != null ? parent.loreFormat : loreFormat));
    }

    /**
     * @deprecated Type is no longer an enum so that external plugins
     *         can register their own types. Use getId() instead
     */
    @Deprecated
    public String name() {
        return getId();
    }

    /**
     * Example <code>OFF_CATALYST</code>
     *
     * @return Internal name in uppercase and no spaces.
     */
    public String getId() {
        return id;
    }

    public TypeSet getItemSet() {
        return set;
    }

    public boolean isWeapon() {
        return weapon;
    }

    public String getName() {
        return name;
    }

    public ModifierSource getModifierSource() {
        return modifierSource;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    /**
     * @deprecated Use {@link #getSupertype()}
     */
    @Deprecated
    public boolean isSubtype() {
        return parent != null;
    }

    /**
     * @deprecated Use {@link #getSupertype()}
     */
    @Deprecated
    public Type getParent() {
        return parent;
    }

    /**
     * @return Does it display as four rows in /mmoitems browse?
     */
    public boolean isFourGUIMode() {
        return getSupertype().modifierSource == ModifierSource.ARMOR;
    }

    /**
     * @return Default lore format used by this Type
     */
    @Nullable
    public String getLoreFormat() {
        return loreFormat;
    }

    /**
     * @return The highest parent of this type, or itself, if it has no parent.
     */
    public Type getSupertype() {
        Type parentMost = this;
        while (parentMost.parent != null)
            parentMost = parentMost.parent;
        return parentMost;
    }

    /**
     * @return Either if the two types are the same,
     *         or if this type is a subtype of the given type.
     */
    public boolean corresponds(Type type) {
        return getSupertype().equals(type);
    }

    @Deprecated
    public boolean corresponds(TypeSet set) {
        return getSupertype().getItemSet() == set;
    }

    /**
     * @return The collection of all stats which can be applied onto this
     *         specific item type. This list is cached when types are being
     *         loaded and is a PRETTY GOOD performance improvement.
     */
    public List<ItemStat> getAvailableStats() {
        return available;
    }

    /**
     * @return Finds the /item config file corresponding to the item type and
     *         loads it
     */
    public ConfigFile getConfigFile() {
        return new ConfigFile("/item", getId().toLowerCase());
    }

    public UnidentifiedItem getUnidentifiedTemplate() {
        return unidentifiedTemplate;
    }

    /**
     * @param stat The stat to check
     * @return If the stat can be handled by this type of item
     * @deprecated Use ItemStat.isCompatible(Type) instead
     */
    @Deprecated
    public boolean canHave(ItemStat stat) {
        return stat.isCompatible(this);
    }

    private ItemStack read(String str) {
        Validate.notNull(str, "Input must not be null");

        String[] split = str.split(":");
        Material material = Material.valueOf(split[0]);
        return split.length > 1 ? MythicLib.plugin.getVersion().getWrapper().textureItem(material, Integer.parseInt(split[1])) : new ItemStack(material);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type type = (Type) o;
        return id.equals(type.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Type{" +
                "id='" + id + '\'' +
                '}';
    }

    /**
     * Reads an ItemStack in hopes for finding its MMOItem Type.
     *
     * @param item The item to retrieve the type from
     * @return The type of the item, if it has a type.
     */
    @Nullable
    public static Type get(@Nullable ItemStack item) {
        if (item == null) {
            return null;
        }
        return get(NBTItem.get(item).getType());
    }

    /**
     * Used in command executors and completions for easier manipulation
     *
     * @param id The type id
     * @return The type or null if it couldn't be found
     */
    @Nullable
    public static Type get(@Nullable String id) {
        if (id == null) return null;

        String format = UtilityMethods.enumName(id);
        return MMOItems.plugin.getTypes().has(format) ? MMOItems.plugin.getTypes().get(format) : null;
    }

    /**
     * Used in command executors and completions for easier manipulation
     *
     * @param id The type id
     * @return If a registered type with this ID could be found
     */
    public static boolean isValid(@Nullable String id) {
        return id != null && MMOItems.plugin.getTypes().has(id.toUpperCase().replace("-", "_").replace(" ", "_"));
    }
}
