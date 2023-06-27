package net.Indyuce.mmoitems.api.item.build;

import com.google.gson.JsonArray;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.util.AdventureUtils;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.event.GenerateLoreEvent;
import net.Indyuce.mmoitems.api.event.ItemBuildEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.stat.data.MaterialData;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.Previewable;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class ItemStackBuilder {
    @NotNull
    private final MMOItem mmoitem;

    private final ItemStack item;
    private final ItemMeta meta;
    private final LoreBuilder lore;
    private final List<ItemTag> tags = new ArrayList<>();

    private static final AttributeModifier fakeModifier = new AttributeModifier(UUID.fromString("87851e28-af12-43f6-898e-c62bde6bd0ec"),
            "mmoitemsDecoy", 0, Operation.ADD_NUMBER);

    /**
     * Used to build an MMOItem into an ItemStack.
     *
     * @param mmoitem The mmoitem you want to build
     */
    public ItemStackBuilder(@NotNull MMOItem mmoitem) {

        // Reference to source MMOItem
        this.mmoitem = mmoitem;

        // Generates a new ItemStack of the specified material (Specified in the Material stat, or a DIAMOND_SWORD if missing).
        item = new ItemStack(
                mmoitem.hasData(ItemStats.MATERIAL) ? ((MaterialData) mmoitem.getData(ItemStats.MATERIAL)).getMaterial() : Material.DIAMOND_SWORD);

        // Gets a lore builder, which will be used to apply the chosen lore format (Choose with the lore format stat, or the default one if unspecified)
        lore = new LoreBuilder(mmoitem.hasData(ItemStats.LORE_FORMAT) ? MMOItems.plugin.getFormats()
                .getFormat(mmoitem.getData(ItemStats.LORE_FORMAT).toString(), mmoitem.getType().getLoreFormat()) : MMOItems.plugin.getFormats()
                .getFormat(mmoitem.getType().getLoreFormat()));

        // Gets the meta, and hides attributes
        meta = item.getItemMeta();
        //noinspection ConstantConditions
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        // Store the internal TYPE-ID Information (not stats, so it must be done manually here)
        tags.add(new ItemTag("MMOITEMS_ITEM_TYPE", mmoitem.getType().getId()));
        tags.add(new ItemTag("MMOITEMS_ITEM_ID", mmoitem.getId()));
    }

    public LoreBuilder getLore() {
        return lore;
    }

    @NotNull
    public MMOItem getMMOItem() {
        return mmoitem;
    }

    /**
     * @return Does NOT return the built item stack. It returns only returns the
     * default item stack with material applied. Built item stack is given
     * by build(). This method should only be used to check if the item is
     * of a specific material (like the Shield Pattern stat which checks if
     * the item is a shield)
     */
    @NotNull
    public ItemStack getItemStack() {
        return item;
    }

    @NotNull
    public ItemMeta getMeta() {
        return meta;
    }

    public void addItemTag(List<ItemTag> newTags) {
        tags.addAll(newTags);
    }

    public void addItemTag(ItemTag... itemTags) {
        tags.addAll(Arrays.asList(itemTags));
    }

    public static final String history_keyword = "HSTRY_";

    /**
     * @return Returns built NBTItem with applied tags and lore
     */
    public NBTItem buildNBT() {
        return buildNBT(false);
    }

    /**
     * @param forDisplay Should this item's lore display potential stats
     *                   (like RNG ranges before rolling) rather than the
     *                   stats it will have?
     * @return Returns built NBTItem with applied tags and lore
     */
    public NBTItem buildNBT(boolean forDisplay) {
        // Clone as to not conflict in any way
        MMOItem builtMMOItem = mmoitem.clone();
        //GEM//MMOItems.log("\u00a7e+ \u00a77Building \u00a7c" + mmoitem.getType().getName() + " " + mmoitem.getId() + "\u00a77 (Size \u00a7e" + mmoitem.getStatHistories().size() + "\u00a77 Historic)");

        /*
         * As an assumption for several enchantment recognition operations,
         * Enchantment data must never be clear and lack history. This is
         * the basis for when an item is 'old'
         */
        if (!builtMMOItem.hasData(ItemStats.ENCHANTS)) {
            builtMMOItem.setData(ItemStats.ENCHANTS, ItemStats.ENCHANTS.getClearStatData()); }
        //GEM// else {MMOItems.log("\u00a73 -?- \u00a77Apparently found enchantment data \u00a7b" + (mmoitem.getData(ItemStats.ENCHANTS) == null ? "null" : ((EnchantListData) mmoitem.getData(ItemStats.ENCHANTS)).getEnchants().size())); }

        /*
         * Similar to how enchantments are saved because they can be modified
         * through non-MMOItems supported sources, the name can be changed in
         * an anvil, so the very original name must be saved.
         */
        if (!builtMMOItem.hasData(ItemStats.NAME)) {
            builtMMOItem.setData(ItemStats.NAME, ItemStats.NAME.getClearStatData()); }
        if (builtMMOItem.getStatHistory(ItemStats.NAME) == null) {
            StatHistory.from(builtMMOItem, ItemStats.NAME); }

        // For every stat within this item
        for (ItemStat stat : builtMMOItem.getStats())

            // Attempt to add
            try {
                //GEM//MMOItems.log("\u00a7e -+- \u00a77Applying \u00a76" + stat.getNBTPath());

                // Does the item have any stat history regarding thay?
                StatHistory s = builtMMOItem.getStatHistory(stat);
                int l = mmoitem.getUpgradeLevel();

                // Found it?
                if (s != null) {
                    //GEM//MMOItems.log("\u00a7a -+- \u00a77History exists...");
                    //GEM//s.log();

                    // Recalculate
                    //GEM//MMOItems.log(" \u00a73-\u00a7a- \u00a77ItemStack Building Recalculation \u00a73-\u00a7a-\u00a73-\u00a7a-\u00a73-\u00a7a-\u00a73-\u00a7a-");
                    builtMMOItem.setData(stat, s.recalculate(l));

                    // Add to NBT, if the gemstones were not purged
                    if (!s.isClear()) {

                        //GEM//MMOItems.log("\u00a7a -+- \u00a77Recording History");
                        addItemTag(new ItemTag(history_keyword + stat.getId(), s.toNBTString()));
                    }
                }

                if (forDisplay && stat instanceof Previewable) {

                    // Get Template
                    MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(builtMMOItem.getType(), builtMMOItem.getId());
                    if (template == null) {
                        throw new IllegalArgumentException(
                                "MMOItem $r" + builtMMOItem.getType().getId() + " " + builtMMOItem.getId() + "$b doesn't exist.");
                    }

                    // Make necessary lore changes
                    ((Previewable) stat).whenPreviewed(this, builtMMOItem.getData(stat), template.getBaseItemData().get(stat));

                } else {

                    // Make necessary lore changes
                    stat.whenApplied(this, builtMMOItem.getData(stat));
                }

                // Something went wrong...
            } catch (IllegalArgumentException | NullPointerException exception) {

                // That
                MMOItems.print(Level.WARNING, "An error occurred while trying to generate item '$f{0}$b' with stat '$f{1}$b': {2}",
                        "ItemStackBuilder", builtMMOItem.getId(), stat.getId(), exception.getMessage());
            }

        // Display gem stone lore hint thing
        if (builtMMOItem.getType() == Type.GEM_STONE)
            lore.insert("gem-stone-lore", ItemStat.translate("gem-stone-lore"));

        // Display item type
        lore.insert("item-type", ItemStat.translate("item-type").replace("{type}",
                builtMMOItem.getStats().contains(ItemStats.DISPLAYED_TYPE) ? builtMMOItem.getData(ItemStats.DISPLAYED_TYPE)
                        .toString() : builtMMOItem.getType().getName()));

        // Calculate extra item lore with placeholders
        if (builtMMOItem.hasData(ItemStats.LORE)) {
            List<String> parsed = new ArrayList<>();
            ((StringListData) builtMMOItem.getData(ItemStats.LORE)).getList().forEach(str -> parsed.add(lore.applySpecialPlaceholders(str)));
            lore.insert("lore", parsed);
        }

        // Calculate and apply item lore
        List<String> unparsedLore = lore.getLore();
        List<String> parsedLore = lore.build();

        GenerateLoreEvent event = new GenerateLoreEvent(builtMMOItem, lore, parsedLore, unparsedLore);
        Bukkit.getPluginManager().callEvent(event);
        AdventureUtils.setLore(meta, event.getParsedLore().stream()
                .map(s -> ChatColor.WHITE + s)
                .toList());
        if (meta.hasDisplayName())
            AdventureUtils.setDisplayName(meta, ChatColor.WHITE + meta.getDisplayName());

        /*
         * Save dynamic lore for later calculations. Not used anymore, but
         * kept in case we need to roll back the lore update change.
         */
        JsonArray array = new JsonArray();
        event.getParsedLore().forEach(array::add);
        if (array.size() != 0) tags.add(new ItemTag("MMOITEMS_DYNAMIC_LORE", array.toString()));

        /*
         * This tag is added to entirely override default vanilla item attribute
         * modifiers, this way armor gives no ARMOR or ARMOR TOUGHNESS to the holder.
         * Since 4.7 attributes are handled via custom calculations
         */
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, fakeModifier);

        item.setItemMeta(meta);

        return NBTItem.get(item).addTag(tags);
    }

    /**
     * @return Builds the item
     */
    @Nullable
    public ItemStack build() {
        ItemBuildEvent event = new ItemBuildEvent(buildNBT().toItem());
        Bukkit.getServer().getPluginManager().callEvent(event);
        return event.getItemStack();
    }

    /**
     * Builds the item without calling a build event
     */
    public ItemStack buildSilently() {
        return buildNBT().toItem();
    }

    /**
     * @return Builds the item
     */
    public ItemStack build(boolean forDisplay) {
        return buildNBT(forDisplay).toItem();
    }
}
