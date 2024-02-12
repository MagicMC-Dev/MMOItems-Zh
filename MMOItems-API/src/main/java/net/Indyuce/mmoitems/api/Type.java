package net.Indyuce.mmoitems.api;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.player.cooldown.CooldownObject;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.util.PostLoadAction;
import io.lumine.mythic.lib.util.PreloadedObject;
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
public class Type implements CooldownObject, PreloadedObject {

    // Slashing
    public static final Type SWORD = new Type("SWORD", true, ModifierSource.MELEE_WEAPON);

    // Piercing
    public static final Type DAGGER = new Type("DAGGER", true, ModifierSource.MELEE_WEAPON);
    public static final Type SPEAR = new Type("SPEAR", true, ModifierSource.MELEE_WEAPON);

    // Blunt
    public static final Type HAMMER = new Type("HAMMER", true, ModifierSource.MELEE_WEAPON);
    public static final Type GAUNTLET = new Type("GAUNTLET", true, ModifierSource.MELEE_WEAPON);

    // Range
    public static final Type WHIP = new Type("WHIP", true, ModifierSource.RANGED_WEAPON);
    public static final Type STAFF = new Type("STAFF", true, ModifierSource.RANGED_WEAPON);
    public static final Type BOW = new Type("BOW", true, ModifierSource.RANGED_WEAPON);
    public static final Type CROSSBOW = new Type("CROSSBOW", true, ModifierSource.RANGED_WEAPON);
    public static final Type MUSKET = new Type("MUSKET", true, ModifierSource.RANGED_WEAPON);
    public static final Type LUTE = new Type("LUTE", true, ModifierSource.RANGED_WEAPON);

    // Hand Accessories
    public static final Type CATALYST = new Type("CATALYST", false, ModifierSource.HAND_ITEM);
    public static final Type OFF_CATALYST = new Type("OFF_CATALYST", false, ModifierSource.OFFHAND_ITEM);
    public static final Type MAIN_CATALYST = new Type("MAIN_CATALYST", false, ModifierSource.MAINHAND_ITEM);

    // Any
    public static final Type ORNAMENT = new Type("ORNAMENT", false, ModifierSource.VOID);

    // Extra
    public static final Type ARMOR = new Type("ARMOR", false, ModifierSource.ARMOR);
    public static final Type TOOL = new Type("TOOL", false, ModifierSource.MELEE_WEAPON);
    public static final Type CONSUMABLE = new Type("CONSUMABLE", false, ModifierSource.MAINHAND_ITEM);
    public static final Type MISCELLANEOUS = new Type("MISCELLANEOUS", false, ModifierSource.MAINHAND_ITEM);
    public static final Type GEM_STONE = new Type("GEM_STONE", false, ModifierSource.VOID);
    public static final Type SKIN = new Type("SKIN", false, ModifierSource.VOID);
    public static final Type ACCESSORY = new Type("ACCESSORY", false, ModifierSource.ACCESSORY);
    public static final Type BLOCK = new Type("BLOCK", false, ModifierSource.VOID);

    private final String id;
    private final ModifierSource modifierSource;
    private final boolean weapon;

    private String name;

    @Nullable
    private String loreFormat;

    @NotNull
    private String attackCooldownKey;

    /**
     * Used to display the item in the item explorer and in the item recipes
     * list in the advanced workbench. can also be edited using the config
     * files.
     */
    private ItemStack item;

    /**
     * The parent item type determines:
     * - the item options compatible with this new item type
     * - if this item type is a weapon or not
     * - types of modifiers an item of this type would give
     * - the lore format
     */
    private Type parent;

    private UnidentifiedItem unidentifiedTemplate;

    private SkillHandler onLeftClick, onRightClick, onAttack, onEntityInteract;

    public Script ent;

    private boolean meleeAttacks;

    /**
     * List of stats which can be applied onto an item which has this type. This
     * improves performance when generating an item by a significant amount.
     */
    private final List<ItemStat> available = new ArrayList<>();

    private final PostLoadAction postLoadAction = new PostLoadAction(true, config -> {
        onLeftClick = config.contains("on-left-click") ? MythicLib.plugin.getSkills().loadSkillHandler(config.get("on-left-click")) : null;
        onRightClick = config.contains("on-right-click") ? MythicLib.plugin.getSkills().loadSkillHandler(config.get("on-right-click")) : null;
        onAttack = config.contains("on-attack") ? MythicLib.plugin.getSkills().loadSkillHandler(config.get("on-attack")) : null;
        onEntityInteract = config.contains("on-entity-interact") ? MythicLib.plugin.getSkills().loadSkillHandler(config.get("on-entity-interact")) : null;
    });

    /**
     * Hard-coded type with given parameters
     */
    public Type(String id, boolean weapon, ModifierSource modSource) {
        this.id = UtilityMethods.enumName(id);
        this.modifierSource = modSource;
        this.weapon = weapon;
    }

    /**
     * Custom type
     */
    public Type(@NotNull TypeManager manager, @NotNull ConfigurationSection config) {
        id = UtilityMethods.enumName(config.getName());
        parent = manager.get(config.getString("parent", "").toUpperCase().replace("-", "_").replace(" ", "_"));
        weapon = config.getBoolean("weapon", parent != null && parent.weapon);
        modifierSource = config.contains("modifier-source") ? ModifierSource.valueOf(UtilityMethods.enumName(config.getString("modifier-source"))) : (parent != null ? parent.modifierSource : ModifierSource.OTHER);
    }

    public void load(@NotNull ConfigurationSection config) {
        postLoadAction.cacheConfig(config);

        name = config.getString("name", name);
        item = read(config.getString("display", item == null ? Material.STONE.toString() : item.getType().toString()));
        (unidentifiedTemplate = new UnidentifiedItem(this)).update(config.getConfigurationSection("unident-item"));
        loreFormat = config.getString("LoreFormat", (parent != null ? parent.loreFormat : null));
        attackCooldownKey = config.getString("attack-cooldown-key", "default");
        meleeAttacks = !config.getBoolean("disable-melee-attacks");
    }

    @Deprecated
    public void postload(ConfigurationSection config) {
        postLoadAction.cacheConfig(config);
        postLoadAction.performAction();
    }

    @NotNull
    @Override
    public PostLoadAction getPostLoadAction() {
        return postLoadAction;
    }

    /**
     * @deprecated Type is no longer an enum so that external plugins
     * can register their own types. Use getId() instead
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

    public boolean isWeapon() {
        return weapon;
    }

    public boolean hasMeleeAttacks() {
        return meleeAttacks;
    }

    public String getName() {
        return name;
    }

    @NotNull
    public ModifierSource getModifierSource() {
        return modifierSource;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    @Nullable
    public SkillHandler onLeftClick() {
        return onLeftClick;
    }

    @Override
    public String getCooldownPath() {
        return "mmoitems_weapons:" + attackCooldownKey;
    }

    @Nullable
    public SkillHandler onRightClick() {
        return onRightClick;
    }

    @Nullable
    public SkillHandler onAttack() {
        return onAttack;
    }

    @Nullable
    public SkillHandler onEntityInteract() {
        return onEntityInteract;
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
     * or if this type is a subtype of the given type.
     */
    public boolean corresponds(Type type) {
        return getSupertype().equals(type);
    }

    /**
     * @return The collection of all stats which can be applied onto this
     * specific item type. This list is cached when types are being
     * loaded and is a PRETTY GOOD performance improvement.
     */
    public List<ItemStat> getAvailableStats() {
        return available;
    }

    /**
     * @return Finds the /item config file corresponding to the item type and
     * loads it
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
        return id == null ? null : MMOItems.plugin.getTypes().get(UtilityMethods.enumName(id));
    }

    @Nullable
    public static Type get(@NotNull NBTItem item) {
        return get(item.getType());
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
