package net.Indyuce.mmoitems.stat.type;

import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class ItemStat<R extends RandomStatData<S>, S extends StatData> {
    @NotNull
    private final String id, name, configPath, nbtPath;
    @NotNull
    private final Material material;

    private final String[] lore;
    private final List<String> compatibleTypes;
    private final List<Material> compatibleMaterials;

    /**
     * The stat can be enabled or not, depending on the server version to
     * prevent from displaying useless editable stats in the edition menu.
     */
    private boolean enabled = true;

    /**
     * Initializes an item stat
     *
     * @param id        The item stat ID, used internally. Also determines the
     *                  lower case path for config files
     * @param material  The material used to display the stat in the item
     *                  edition GUI
     * @param name      The stat name which has a translation in the language
     *                  files
     * @param lore      The stat description used in the edition GUI
     * @param types     Compatible types. Use 'all' to support all item types or
     *                  !{type-name} to blacklist an item type
     * @param materials Materials compatible with the item stat (eg Shield
     *                  Pattern), any if empty
     */
    public ItemStat(@NotNull String id, @NotNull Material material, @NotNull String name, String[] lore, String[] types, Material... materials) {
        this.id = id;
        this.material = material;
        this.lore = lore == null ? new String[0] : lore;
        this.compatibleTypes = types == null ? new ArrayList<>() : Arrays.asList(types);
        this.name = name;
        this.compatibleMaterials = Arrays.asList(materials);

        this.configPath = id.toLowerCase().replace("_", "-");
        this.nbtPath = "MMOITEMS_" + id;
    }

    /**
     * When random stat data is being read from a config file
     *
     * @param object Could be a config section, a string, a string list, etc.
     * @return Random stat data read from config, or throws an IAE
     */
    public abstract R whenInitialized(Object object);

    /**
     * Called when applying a stat onto an mmoitem builder instance. Applies
     * item tags, adds required lines to the item lore, etc.
     *
     * @param item MMOItem builder which must be completed
     * @param data Stat data being applied
     */
    public abstract void whenApplied(@NotNull ItemStackBuilder item, @NotNull S data);

    /**
     * Usually called within <code>whenApplied</code>, this generates the
     * actual NBT tag that will be written onto the item. Reverses
     * <code>fromAppliedNBT()</code> actually.
     * <p></p>
     * Note that only the following types are supported:
     * <p><b>+ </b> Number
     * </p><b>+ </b> String
     * <p><b>+ </b> Boolean
     * </p><b>+ </b> List of any of these
     *
     * @author gunging
     */
    @NotNull
    public abstract ArrayList<ItemTag> getAppliedNBT(@NotNull S data);

    /**
     * Called when the stat item is clicked in the item edition menu
     *
     * @param inv   Inventory clicked
     * @param event Click event
     */
    public abstract void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event);

    /**
     * When inputting data using player input in order to edit the item using
     * the GUI editor. IAE's are handled and exception messages are sent back
     * to the player. Stat edition is not canceled until a right input is given
     * or the player inputs "cancel".
     * <p>
     * This method is called async inside of an AsyncPlayerChatEvent
     *
     * @param inv     Previously opened edition menu
     * @param message Player input
     * @param info    Extra information given by the stat when instanciating
     *                StatEdition given to this method to identify what is being
     *                edited
     */
    public abstract void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info);

    /**
     * Called when stat data is read from an ItemStack in a player inventory
     *
     * @param mmoitem NBTItem being read and transformed into a MMOItem instance
     */
    public abstract void whenLoaded(@NotNull ReadMMOItem mmoitem);

    /**
     * Usually called within <code>whenLoaded</code>, this generates the
     * actual Stat Data from the NBT stored within the item. Reverses
     * <code>getAppliedNBT()</code> actually.
     * <p></p>
     * Shall return null if the tags passed dont provide enough
     * information to generate a useable StatData.
     *
     * @author gunging
     */
    @Nullable
    public abstract S getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags);

    /**
     * Called when stat data is displayed in the edition GUI. We cannot use
     * MMOItemTemplate as input here because we need to know if we are editing a
     * modifier or base item data. It is much easier to display RandomStatData
     * if it exists
     *
     * @param lore     Current item lore which must be completed
     * @param statData Stat data being displayed, optional is empty if there is
     *                 no stat data
     */
    public abstract void whenDisplayed(List<String> lore, Optional<R> statData);

    protected String generalStatFormat;

    @Deprecated
    public void loadConfiguration(@NotNull ConfigurationSection legacyLanguageFile, @NotNull Object configObject) {
        loadConfiguration(configObject);
    }

    public void loadConfiguration(@NotNull Object configObject) {
        generalStatFormat = configObject.toString();
    }

    @Deprecated
    public String getLegacyTranslationPath() {
        return getPath();
    }

    public String getGeneralStatFormat() {
        return generalStatFormat;
    }

    @NotNull
    public String getName() {
        return name;
    }

    /**
     * The internal name of this ItemStat.
     * <p></p>
     * Example, from attack damage: <b>ATTACK_DAMAGE</b>
     */
    @NotNull
    public String getId() {
        return id;
    }

    /**
     * @return The stat ID
     * @deprecated Use getId() instead. Type is no longer an util since they can
     *         now be registered from external plugins
     */
    @Deprecated
    @NotNull
    public String name() {
        return id;
    }

    /**
     * @return Path being used to reference this item stat in MMOItems config files. It's the stat path users are familiar with
     */
    @NotNull
    public String getPath() {
        return configPath;
    }

    /**
     * @return The NBT path used by the stat to save data in an item's NBTTags.
     *         The format is 'MMOITEMS_' followed by the stat name in capital
     *         letters only using _
     */
    @NotNull
    public String getNBTPath() {
        return nbtPath;
    }

    public Material getDisplayMaterial() {
        return material;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String[] getLore() {
        return lore;
    }

    public List<String> getCompatibleTypes() {
        return compatibleTypes;
    }

    /**
     * @param type The item type to check
     * @return If a certain item type is compatible with this item stat
     */
    public boolean isCompatible(Type type) {
        String lower = type.getId().toLowerCase();
        return type.isSubtype() ? isCompatible(type.getParent())
                : !compatibleTypes.contains("!" + lower) && (compatibleTypes.contains("all") || compatibleTypes.contains(lower)
                || compatibleTypes.contains(type.getItemSet().getName().toLowerCase()));
    }

    public boolean hasValidMaterial(ItemStack item) {
        return compatibleMaterials.size() == 0 || compatibleMaterials.contains(item.getType());
    }

    public void disable() {
        enabled = false;
    }

    /**
     * @deprecated See {@link DoubleStat#formatPath(String, String, boolean, boolean, double, double)}
     */
    @Deprecated
    public String formatNumericStat(double value, String... replace) {
        String format = getGeneralStatFormat().replace("<plus>", value > 0 ? "+" : "");
        for (int j = 0; j < replace.length; j += 2)
            format = format.replace(replace[j], replace[j + 1]);
        return format;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemStat<?, ?> itemStat = (ItemStat<?, ?>) o;
        return id.equals(itemStat.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Deprecated
    public static String translate(String path) {
        return MMOItems.plugin.getLanguage().getStatFormat(path);
    }

    /**
     * The default value of this ItemStat.
     * <p></p>
     * Must be blank, easiest examples are <code>DoubleStat</code>s which
     * usually return a <code>new DoubleData(0.0)</code>.
     * <p></p>
     * Used when a gem stone is applied onto an item, but this item did
     * not have the stat provided by the gem stone. In this case, the 'original'
     * value of the item will be given by this.
     * <p></p>
     * Also used to know what kind of data to expect, because it may not be super obvious.
     * <p>Take <code>Commands</code> stat that has a data of <code>CommandListData</code>
     * where one may thing it takes a <code>StringListData</code></p>
     *
     * @author gunging
     */
    @NotNull
    public abstract S getClearStatData();
}