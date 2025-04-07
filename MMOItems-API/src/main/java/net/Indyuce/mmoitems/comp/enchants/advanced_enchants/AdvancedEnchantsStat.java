package net.Indyuce.mmoitems.comp.enchants.advanced_enchants;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.type.InternalStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.advancedplugins.ae.api.AEAPI;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * List of enchantments in an item yes
 */
public class AdvancedEnchantsStat extends ItemStat<RandomStatData<AdvancedEnchantMap>, AdvancedEnchantMap> implements InternalStat {
    private final Plugin ae;
    private final String aeNamespace;

    public AdvancedEnchantsStat() {
        super("ADVANCED_ENCHANTS", Material.EXPERIENCE_BOTTLE, "Advanced Enchants", new String[]{"The AEnchants of this item. Format:", "\u00a7e[internal_name] [level]"}, new String[]{"!miscellaneous", "!block", "all"});

        ae = Bukkit.getPluginManager().getPlugin("AdvancedEnchantments");
        Validate.notNull(ae, "Could not find plugin AdvancedEnchants");
        aeNamespace = new NamespacedKey(ae, "any").getNamespace();
    }

    @Override
    public RandomStatData<AdvancedEnchantMap> whenInitialized(Object object) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull AdvancedEnchantMap data) {

        // Enchant the item
        data.enchants.forEach((ench, lvl) -> {

            // Skip trash
            if (ench == null || lvl == null || ench.isEmpty()) return;

            // Add lore and tag
            item.getLore().insert(0, AEAPI.getEnchantLore(ench, lvl));
            item.getMeta().getPersistentDataContainer().set(new NamespacedKey(ae, AE_KEY_PREFIX + ench), PersistentDataType.INTEGER, lvl);
        });
    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull AdvancedEnchantMap data) {
        // Stat history is not supported
        return new ArrayList<>();
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        // Not supported
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        // Not supported
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<RandomStatData<AdvancedEnchantMap>> statData) {
        // Not supported
    }

    @NotNull
    @Override
    public AdvancedEnchantMap getClearStatData() {
        return new AdvancedEnchantMap();
    }

    private static final String AE_KEY_PREFIX = "ae_enchantment-";


    @BackwardsCompatibility(version = "i_love_this_plugin_sooooooooo_much_so_so_much")
    private static final String LEGACY_AE_KEY_PREFIX = "ae_enchantment;";

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        final ItemMeta meta = mmoitem.getNBT().getItem().getItemMeta();
        if (meta == null) return;

        final AdvancedEnchantMap enchants = new AdvancedEnchantMap();

        // Backwards compatibility
        for (String tag : mmoitem.getNBT().getTags()) {
            if (tag == null) continue;
            if (!tag.startsWith(LEGACY_AE_KEY_PREFIX)) continue;

            final String enchantTag = tag.substring(LEGACY_AE_KEY_PREFIX.length());
            final int lvl = mmoitem.getNBT().getInteger(tag);
            enchants.enchants.put(enchantTag, lvl);
        }

        for (NamespacedKey nsk : meta.getPersistentDataContainer().getKeys()) {
            if (!nsk.getNamespace().equals(aeNamespace)) continue;

            final String tag = nsk.getKey();
            if (!tag.startsWith(AE_KEY_PREFIX)) continue;

            final Integer lvlInteger = meta.getPersistentDataContainer().get(nsk, PersistentDataType.INTEGER);
            if (lvlInteger == null || lvlInteger == 0) continue;

            final String enchantTag = tag.substring(AE_KEY_PREFIX.length());
            enchants.enchants.put(enchantTag, lvlInteger);
        }

        // Not empty?
        if (!enchants.enchants.isEmpty()) mmoitem.setData(this, enchants);
    }

    @Nullable
    @Override
    public AdvancedEnchantMap getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
        // Stat history is not supported
        return null;
    }
}
