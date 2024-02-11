package net.Indyuce.mmoitems.api.item.build;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate.TemplateOption;
import net.Indyuce.mmoitems.api.item.template.NameModifier;
import net.Indyuce.mmoitems.api.item.template.NameModifier.ModifierType;
import net.Indyuce.mmoitems.api.item.template.TemplateModifier;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.NameData;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import net.Indyuce.mmoitems.util.Buildable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MMOItemBuilder extends Buildable<MMOItem> {
    private final MMOItemTemplate template;
    private final MMOItem mmoitem;
    private final int level;
    private final ItemTier tier;

    private double capacity;

    /**
     * Name modifiers, prefixes or suffixes, with priorities. They are saved
     * because they must be applied after the modifier selection process
     */
    private final Map<UUID, NameModifier> nameModifiers = new HashMap<>();

    public MMOItemBuilder(MMOItemTemplate template, int level, @Nullable ItemTier tier) {
        this(template, level, tier, false);
    }

    /**
     * Instance which is created everytime an mmoitem is being randomly
     * generated
     *
     * @param template   The mmoitem template used to generate an item.
     * @param level      Specified item level.
     * @param tier       Specified item tier which determines how many capacity it will
     *                   have. If no tier is given, item uses the default capacity
     *                   formula given in the main config file
     * @param forDisplay Should it take modifiers into account
     */
    public MMOItemBuilder(@NotNull MMOItemTemplate template, int level, @Nullable ItemTier tier, boolean forDisplay) {
        this.template = template;
        this.level = level;
        this.tier = tier;

        // Either use provided tier or look into the template base data
        tier = tier != null ? tier : template.getBaseItemData().containsKey(ItemStats.TIER) ? MMOItems.plugin.getTiers().get(template.getBaseItemData().get(ItemStats.TIER).toString()) : null;

        // Capacity is not final as it keeps lowering as modifiers are selected
        capacity = (template.hasModifierCapacity() ? template.getModifierCapacity() : tier != null && tier.hasCapacity() ? tier.getModifierCapacity() : MMOItems.plugin.getLanguage().defaultItemCapacity).calculate(level);
        mmoitem = new MMOItem(template.getType(), template.getId());

        // Apply base item data
        template.getBaseItemData().forEach((stat, random) -> applyData(stat, random.randomize(this)));

        if (tier != null) mmoitem.setData(ItemStats.TIER, new StringData(tier.getId()));
        if (level > 0) mmoitem.setData(ItemStats.ITEM_LEVEL, new DoubleData(level));
        if (tier != null && tier.getTooltip() != null && !mmoitem.hasData(ItemStats.TOOLTIP))
            mmoitem.setData(ItemStats.TOOLTIP, new StringData(tier.getTooltip().getId()));

        // Apply modifiers from the parent node
        if (!forDisplay && template.hasModifierGroup()) template.getModifierGroup().collect(this);
    }

    @NotNull
    public MMOItemTemplate getTemplate() {
        return template;
    }

    public int getLevel() {
        return level;
    }

    @Nullable
    public ItemTier getTier() {
        return tier;
    }

    public double getCapacity() {
        return capacity;
    }

    public void reduceCapacity(double weight) {
        capacity -= weight;
    }

    /**
     * Calculates the item display name after applying name modifiers. If name
     * modifiers are specified but the item has no display name, MMOItems uses
     * "Item".
     * <p>
     * MMOItemBuilder can only be used to build the MMOItem once, it will throw
     * an IllegalArgumentException when trying to call this method twice from
     * the same object.
     *
     * @return Built MMOItem instance
     */
    @Override
    protected MMOItem whenBuilt() {
        if (!nameModifiers.isEmpty()) {

            // Get name data
            StatHistory hist = StatHistory.from(mmoitem, ItemStats.NAME);
            if (!mmoitem.hasData(ItemStats.NAME)) mmoitem.setData(ItemStats.NAME, new NameData("Item"));

            nameModifiers.forEach((obs, mod) -> {

                // Create new Name Data
                NameData modName = new NameData("");

                // Include modifier information
                if (mod.getType() == ModifierType.PREFIX) modName.addPrefix(mod.getFormat());
                if (mod.getType() == ModifierType.SUFFIX) modName.addSuffix(mod.getFormat());

                // Register onto SH
                hist.registerModifierBonus(obs, modName);
            });

            // Recalculate name
            mmoitem.setData(ItemStats.NAME, hist.recalculate(mmoitem.getUpgradeLevel()));
        }

        return mmoitem;
    }

    /**
     * Applies statData to the builder, either merges it if statData is
     * mergeable like lore, abilities.. or entirely replaces current data
     *
     * @param stat Stat owning the data
     * @param data StatData to apply
     */
    public void applyData(@NotNull ItemStat stat, @NotNull StatData data) {
        final StatData found = mmoitem.getData(stat);
        if (found != null && found instanceof Mergeable) ((Mergeable) found).mergeWith(data);
        else mmoitem.setData(stat, data);
    }

    /**
     * Registers the modifier onto the item
     *
     * @param stat Stat owning the data
     * @param data StatData to apply
     */
    public void addModifierData(@NotNull ItemStat stat, @NotNull StatData data, @NotNull UUID uuid) {
        if (stat.getClearStatData() instanceof Mergeable)
            StatHistory.from(mmoitem, stat).registerModifierBonus(uuid, data);
        else mmoitem.setData(stat, data);
    }

    /**
     * Adds a modifier only if there aren't already modifier of the same type
     * with strictly higher priority. If there are none, adds modifier and
     * clears less priority modifiers
     *
     * @param modifier   Name modifier which needs to be added
     * @param modifierId UUID of storage into the Stat History of name
     */
    public void addModifier(@NotNull NameModifier modifier, @NotNull UUID modifierId) {

        // Might overwrite a modifier
        final Iterator<NameModifier> ite = nameModifiers.values().iterator();
        while (ite.hasNext()) {
            final NameModifier obs = ite.next();
            if (obs.getType() == modifier.getType()) {
                if (obs.getPriority() > modifier.getPriority()) return;
                else if (obs.getPriority() < modifier.getPriority()) ite.remove();
            }
        }

        nameModifiers.put(modifierId, modifier);
    }

    /**
     * @param template The template to list modifiers from
     * @return A sorted (or unsorted depending on the template options) list of
     * modifiers that can be later rolled and applied to the builder
     */
    @NotNull
    @Deprecated
    public static Collection<TemplateModifier> rollModifiers(@NotNull MMOItemTemplate template) {
        if (!template.hasOption(TemplateOption.ROLL_MODIFIER_CHECK_ORDER)) return template.getModifiers().values();

        List<TemplateModifier> modifiers = new ArrayList<>(template.getModifiers().values());
        Collections.shuffle(modifiers);
        return modifiers;
    }

    /**
     * Backwards compatibility. Java interpreter cannot find a method
     * if the method has the same name but a different location (the
     * #build method was moved to a parent class and no longer has the
     * same location)
     */
    @NotNull
    @Override
    public MMOItem build() {
        return super.build();
    }
}
