package net.Indyuce.mmoitems.inventory;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.handler.StatHandler;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierSupplier;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.player.modifier.SimpleModifierSupplier;
import io.lumine.mythic.lib.player.particle.ParticleEffect;
import io.lumine.mythic.lib.player.permission.PermissionModifier;
import io.lumine.mythic.lib.player.potion.PermanentPotionEffect;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.util.Closeable;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemSet;
import net.Indyuce.mmoitems.api.event.RefreshInventoryEvent;
import net.Indyuce.mmoitems.api.event.inventory.ItemEquipEvent;
import net.Indyuce.mmoitems.api.event.inventory.ItemUnequipEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.stat.data.*;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.WeaponBaseStat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Bukkit-MMOItems interface class.
 * <p>
 * Makes sure MMOItems is up-to-date with the most recent version
 * of the player's inventory, in a performant way. Then, applies
 * modifiers like permanent effects, permissions, set bonuses...
 * <p>
 * TODO refactor abilities/permissions/... and move them to individual stat classes
 * TODO after adding some interface like StatThatRegistersPlayerModifiers
 *
 * @author jules
 */
public class InventoryResolver implements Closeable {
    private final PlayerData playerData;
    private final List<InventoryWatcher> watchers;

    /**
     * Item registry
     */
    private final Set<EquippedItem> activeItems = new HashSet<>();

    // Item set logic
    private final Map<ItemSet, Integer> itemSetCount = new HashMap<>();
    private final ModifierSupplier setModifierSupplier = new SimpleModifierSupplier();

    // Two-Handed-ness
    private @Nullable Boolean encumbered;

    private static final String MODIFIER_KEY = "MMOItems";
    public static boolean ENABLE_ORNAMENTS = false;

    public InventoryResolver(PlayerData playerData) {
        this.playerData = playerData;
        this.watchers = MMOItems.plugin.getInventory().getWatchers(this);

        // TODO reset all item modifiers on join? extra safety
    }

    @Override
    public void close() {
        // Empty all registered modifiers from items and item set
        for (EquippedItem equipped : activeItems)
            equipped.getModifierCache().forEach(mod -> mod.unregister(playerData.getMMOPlayerData()));
        setModifierSupplier.getModifierCache().forEach(mod -> mod.unregister(playerData.getMMOPlayerData()));
    }

    @NotNull
    public PlayerData getPlayerData() {
        return playerData;
    }

    //region Resolving Inventory

    public void watchVanillaSlot(@NotNull EquipmentSlot slot, Optional<ItemStack> newItem) {
        for (InventoryWatcher watcher : watchers)
            InventoryWatcher.callbackIfNotNull(watcher.watchVanillaSlot(slot, newItem), this::processUpdate);
    }

    public void watchInventory(int slotIndex, Optional<ItemStack> newItem) {
        for (InventoryWatcher watcher : watchers)
            InventoryWatcher.callbackIfNotNull(watcher.watchInventory(slotIndex, newItem), this::processUpdate);
    }

    public <T extends InventoryWatcher> void watch(Class<T> instanceOf, Function<T, ItemUpdate> action) {
        for (InventoryWatcher watcher : watchers)
            if (instanceOf.isInstance(watcher))
                InventoryWatcher.callbackIfNotNull(action.apply(instanceOf.cast(watcher)), this::processUpdate);
    }

    public void processUpdate(@NotNull ItemUpdate recorded) {

        // Register changes
        if (recorded.getOld() != null) unregisterItem(recorded.getOld());
        if (recorded.getNew() != null) registerItem(recorded.getNew());

        // Reset emcumbered status
        if (recorded.getEquipmentSlot().isHand()) encumbered = null;
    }

    @NotNull
    public Set<EquippedItem> getEquipped() {
        return activeItems;
    }

    public void resolveInventory() {
        playerData.getMMOPlayerData().getStatMap().bufferUpdates(() -> {
            for (InventoryWatcher watcher : watchers) watcher.watchAll(this::processUpdate);
        });
    }

    private void registerItem(@NotNull EquippedItem equippedItem) {
        Validate.isTrue(activeItems.add(equippedItem), "Item already registered");
        Bukkit.getPluginManager().callEvent(new ItemEquipEvent(playerData, equippedItem));
        callBackwardsCompatibleEvent();
        resolveModifiers(equippedItem);
    }

    private void unregisterItem(@NotNull EquippedItem unequippedItem) {
        Validate.isTrue(activeItems.remove(unequippedItem), "Item not found");
        Bukkit.getPluginManager().callEvent(new ItemUnequipEvent(playerData, unequippedItem));
        callBackwardsCompatibleEvent();
        if (unequippedItem.applied) unapplyModifiers(unequippedItem);
    }

    @BackwardsCompatibility(version = "6.10.1")
    @SuppressWarnings("deprecation")
    private void callBackwardsCompatibleEvent() {
        Bukkit.getPluginManager().callEvent(new RefreshInventoryEvent(this));
    }

    //endregion

    //region Resolving Modifiers

    public void resolveModifiers() {
        for (EquippedItem equippedItem : activeItems) {
            equippedItem.flushCache();
            resolveModifiers(equippedItem);
        }
    }

    private void resolveModifiers(@NotNull EquippedItem equippedItem) {
        boolean valid = equippedItem.isPlacementLegal() && equippedItem.isUsable(playerData.getRPG());
        if (valid && !equippedItem.applied) applyModifiers(equippedItem);
        else if (!valid && equippedItem.applied) unapplyModifiers(equippedItem);
    }

    private void applyModifiers(@NotNull EquippedItem equippedItem) {
        Validate.isTrue(!equippedItem.applied, "Item modifiers already applied");
        equippedItem.applied = true;

        final VolatileMMOItem item = equippedItem.reader();

        ///////////////////////////////////////
        // Abilities
        ///////////////////////////////////////
        if (item.hasData(ItemStats.ABILITIES))
            registerAbilities(equippedItem, ((AbilityListData) item.getData(ItemStats.ABILITIES)).getAbilities());

        ///////////////////////////////////////
        // Permanent potion effects
        ///////////////////////////////////////
        if (item.hasData(ItemStats.PERM_EFFECTS))
            registerPotionEffects(equippedItem, ((PotionEffectListData) item.getData(ItemStats.PERM_EFFECTS)).getEffects().stream().map(PotionEffectData::toEffect).collect(Collectors.toList()));

        ///////////////////////////////////////
        // Item particles
        ///////////////////////////////////////
        if (item.hasData(ItemStats.ITEM_PARTICLES)) {
            ParticleData particleData = (ParticleData) item.getData(ItemStats.ITEM_PARTICLES);
            registerParticleEffect(equippedItem, particleData);
        }

        ///////////////////////////////////////
        // Permissions (not using Vault)
        ///////////////////////////////////////
        if (item.hasData(ItemStats.GRANTED_PERMISSIONS))
            registerPermissions(equippedItem, ((StringListData) item.getData(ItemStats.GRANTED_PERMISSIONS)).getList());

        ///////////////////////////////////////
        // Item Set
        ///////////////////////////////////////
        if (equippedItem.getSet() != null) {
            itemSetCount.merge(equippedItem.getSet(), 1, Integer::sum);
            resolveItemSet();
        }

        ///////////////////////////////////////
        // Numeric Stats
        ///////////////////////////////////////
        for (ItemStat<?, ?> stat : MMOItems.plugin.getStats().getNumericStats()) {

            // TODO MI7 do a full stat lookup and lookup stat by nbtpath
            double statValue = equippedItem.getItem().getStat(stat.getId());
            if (statValue == 0) continue;

            StatInstance statInstance = playerData.getMMOPlayerData().getStatMap().getInstance(stat.getId());
            final ModifierSource modifierSource = equippedItem.getModifierSource();

            // Apply hand weapon stat offset
            if (modifierSource.isWeapon() && stat instanceof WeaponBaseStat)
                statValue = fixWeaponBase(statInstance, stat, statValue);

            StatModifier modifier = new StatModifier(MODIFIER_KEY, stat.getId(), statValue, ModifierType.FLAT, equippedItem.getEquipmentSlot(), modifierSource);
            statInstance.registerModifier(modifier);
            equippedItem.getModifierCache().add(modifier);
        }
    }

    private double fixWeaponBase(StatInstance statInstance, ItemStat<?, ?> stat, double statValue) {
        @NotNull Optional<StatHandler> opt = MythicLib.plugin.getStats().getHandler(stat.getId());
        return opt.map(statHandler -> statValue - statHandler.getBaseValue(statInstance)).orElse(statValue);
    }

    private void registerPotionEffects(ModifierSupplier supplier, Collection<PotionEffect> effects) {
        for (PotionEffect bukkit : effects) {
            // TODO Support for slot and source
            PermanentPotionEffect modifier = new PermanentPotionEffect(MODIFIER_KEY, bukkit.getType(), bukkit.getAmplifier());
            modifier.register(playerData.getMMOPlayerData());
            supplier.getModifierCache().add(modifier);
        }
    }

    private void registerParticleEffect(ModifierSupplier supplier, ParticleData particleData) {
        // TODO Support for slot and source
        ParticleEffect modifier = particleData.toModifier(MODIFIER_KEY);
        modifier.register(playerData.getMMOPlayerData());
        supplier.getModifierCache().add(modifier);
    }

    private void registerPermissions(ModifierSupplier supplier, Collection<String> permissions) {
        for (String permission : permissions) {
            PermissionModifier modifier = new PermissionModifier(MODIFIER_KEY, permission, supplier.getEquipmentSlot(), supplier.getModifierSource());
            modifier.register(playerData.getMMOPlayerData());
            supplier.getModifierCache().add(modifier);
        }
    }

    private void registerAbilities(ModifierSupplier supplier, Collection<AbilityData> abilities) {
        for (AbilityData abilityData : abilities) {
            PassiveSkill modifier = new PassiveSkill(MODIFIER_KEY, abilityData, supplier.getEquipmentSlot(), supplier.getModifierSource());
            modifier.register(playerData.getMMOPlayerData());
            supplier.getModifierCache().add(modifier);
        }
    }

    private void unapplyModifiers(@NotNull EquippedItem equippedItem) {
        Validate.isTrue(equippedItem.applied, "Item modifiers not applied");
        equippedItem.applied = false;

        // Unload ALL modifiers
        equippedItem.getModifierCache().forEach(mod -> mod.unregister(playerData.getMMOPlayerData()));
        equippedItem.getModifierCache().clear(); // Clear cache!!!

        ///////////////////////////////////////
        // Item Set
        ///////////////////////////////////////
        if (equippedItem.getSet() != null) {
            itemSetCount.merge(equippedItem.getSet(), 0, (oldValue, value) -> oldValue - 1);
            resolveItemSet();
        }
    }

    // TODO make it not fully reset everything like a retard everytime
    private void resolveItemSet() {

        // Clear all modifiers due to item set
        setModifierSupplier.getModifierCache().forEach(mod -> mod.unregister(playerData.getMMOPlayerData()));
        setModifierSupplier.getModifierCache().clear();

        // Reset and compute item set bonuses
        ItemSet.SetBonuses setBonuses = null;
        for (Map.Entry<ItemSet, Integer> equippedSetBonus : itemSetCount.entrySet()) {
            if (setBonuses == null)
                setBonuses = equippedSetBonus.getKey().getBonuses(equippedSetBonus.getValue()); // Set
            else setBonuses.merge(equippedSetBonus.getKey().getBonuses(equippedSetBonus.getValue()));  // Merge bonuses
        }

        // Apply set bonuses
        if (setBonuses != null) {
            registerAbilities(setModifierSupplier, setBonuses.getAbilities());
            registerPotionEffects(setModifierSupplier, setBonuses.getPotionEffects());
            for (ParticleData particle : setBonuses.getParticles())
                registerParticleEffect(setModifierSupplier, particle);
            registerPermissions(setModifierSupplier, setBonuses.getPermissions());
            setBonuses.getStats().forEach((stat, statValue) -> {
                StatModifier modifier = new StatModifier(MODIFIER_KEY, stat.getId(), statValue, ModifierType.FLAT, EquipmentSlot.OTHER, ModifierSource.OTHER);
                modifier.register(playerData.getMMOPlayerData());
                setModifierSupplier.getModifierCache().add(modifier);
            });
        }
    }

    public boolean isEncumbered() {
        if (encumbered != null) return encumbered;

        // Get the mainhand and offhand items.
        final NBTItem main = MythicLib.plugin.getVersion().getWrapper().getNBTItem(playerData.getPlayer().getInventory().getItemInMainHand());
        final NBTItem off = MythicLib.plugin.getVersion().getWrapper().getNBTItem(playerData.getPlayer().getInventory().getItemInOffHand());

        // Is either hand two-handed?
        final boolean mainhand_twohanded = main.getBoolean(ItemStats.TWO_HANDED.getNBTPath());
        final boolean offhand_twohanded = off.getBoolean(ItemStats.TWO_HANDED.getNBTPath());

        // Is either hand encumbering: Not NULL, not AIR, and not Handworn
        final boolean mainhand_encumbering = (main.getItem() != null && main.getItem().getType() != Material.AIR && !main.getBoolean(ItemStats.HANDWORN.getNBTPath()));
        final boolean offhand_encumbering = (off.getItem() != null && off.getItem().getType() != Material.AIR && !off.getBoolean(ItemStats.HANDWORN.getNBTPath()));

        // Will it encumber?
        return encumbered = ((mainhand_twohanded && offhand_encumbering) || (mainhand_encumbering && offhand_twohanded));
    }

    //endregion
}