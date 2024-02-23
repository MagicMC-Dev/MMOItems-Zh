package net.Indyuce.mmoitems.api.player;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.util.Closeable;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemSet;
import net.Indyuce.mmoitems.api.ItemSet.SetBonuses;
import net.Indyuce.mmoitems.api.crafting.CraftingStatus;
import net.Indyuce.mmoitems.api.event.RefreshInventoryEvent;
import net.Indyuce.mmoitems.api.interaction.Tool;
import net.Indyuce.mmoitems.api.item.ItemReference;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import net.Indyuce.mmoitems.api.player.inventory.InventoryUpdateHandler;
import net.Indyuce.mmoitems.particle.api.ParticleRunnable;
import net.Indyuce.mmoitems.stat.data.*;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerData extends SynchronizedDataHolder implements Closeable {
    // Reloaded everytime the player reconnects in case of major change.
    private RPGPlayer rpgPlayer;

    private final InventoryUpdateHandler inventory = new InventoryUpdateHandler(this);
    private final CraftingStatus craftingStatus = new CraftingStatus();

    // Specific stat calculation TODO compress it in Map<ItemStat, DynamicStatData>
    private final Map<PotionEffectType, PotionEffect> permanentEffects = new HashMap<>();
    private final Set<ParticleRunnable> itemParticles = new HashSet<>();
    private ParticleRunnable overridingItemParticles = null;
    private boolean encumbered = false;
    @Nullable
    private SetBonuses setBonuses = null;
    private final PlayerStats stats;
    private final Set<String> permissions = new HashSet<>();

    public PlayerData(@NotNull MMOPlayerData mmoData) {
        super(MMOItems.plugin, mmoData);

        rpgPlayer = MMOItems.plugin.getMainRPG().getInfo(this);
        stats = new PlayerStats(this);
    }

    @Override
    public void close() {
        cancelRunnables();
    }

    public boolean isOnline() {
        return getMMOPlayerData().isOnline();
    }

    public RPGPlayer getRPG() {
        return rpgPlayer;
    }

    public void cancelRunnables() {
        itemParticles.forEach(BukkitRunnable::cancel);
        if (overridingItemParticles != null)
            overridingItemParticles.cancel();
    }

    @Deprecated
    public boolean areHandsFull() {
        return isEncumbered();
    }

    /**
     * @return If the player hands are full i.e if the player is holding
     * two items in their hands, one being two handed
     */
    public boolean isEncumbered() {

        // Get the mainhand and offhand items.
        final NBTItem main = MythicLib.plugin.getVersion().getWrapper().getNBTItem(getPlayer().getInventory().getItemInMainHand());
        final NBTItem off = MythicLib.plugin.getVersion().getWrapper().getNBTItem(getPlayer().getInventory().getItemInOffHand());

        // Is either hand two-handed?
        final boolean mainhand_twohanded = main.getBoolean(ItemStats.TWO_HANDED.getNBTPath());
        final boolean offhand_twohanded = off.getBoolean(ItemStats.TWO_HANDED.getNBTPath());

        // Is either hand encumbering: Not NULL, not AIR, and not Handworn
        final boolean mainhand_encumbering = (main.getItem() != null && main.getItem().getType() != Material.AIR && !main.getBoolean(ItemStats.HANDWORN.getNBTPath()));
        final boolean offhand_encumbering = (off.getItem() != null && off.getItem().getType() != Material.AIR && !off.getBoolean(ItemStats.HANDWORN.getNBTPath()));

        // Will it encumber?
        return (mainhand_twohanded && offhand_encumbering) || (mainhand_encumbering && offhand_twohanded);
    }

    /**
     * Some plugins require to update the RPGPlayer after server startup
     *
     * @param rpgPlayer New RPGPlayer instance
     */
    public void setRPGPlayer(RPGPlayer rpgPlayer) {
        this.rpgPlayer = rpgPlayer;
    }

    public void updateInventory() {
        if (!isOnline()) return;

        /*
         * Very important, clear particle data AFTER canceling the runnable
         * otherwise it cannot cancel and the runnable keeps going (severe)
         */
        inventory.getEquipped().clear();
        permanentEffects.clear();
        cancelRunnables();
        getMMOPlayerData().getPassiveSkillMap().removeModifiers("MMOItemsItem");
        itemParticles.clear();
        overridingItemParticles = null;
        if (MMOItems.plugin.hasPermissions()) {
            final Permission perms = MMOItems.plugin.getVault().getPermissions();
            permissions.forEach(perm -> {
                if (perms.has(getPlayer(), perm)) {
                    perms.playerRemove(getPlayer(), perm);
                }
            });
        }
        permissions.clear();

        /*
         * Updates the encumbered boolean, this way it can be
         * cached and used in the updateEffects() method
         */
        encumbered = isEncumbered();

        // Find all the items the player can actually use
        for (EquippedItem item : MMOItems.plugin.getInventory().getInventory(getPlayer())) {
            NBTItem nbtItem = item.getNBT();
            if (nbtItem.getItem() == null || nbtItem.getItem().getType() == Material.AIR)
                continue;

            /*
             * If the item is a custom item, apply slot and item use
             * restrictions (items which only work in a specific equipment slot)
             */
            if (!item.isPlacementLegal() || !getRPG().canUse(nbtItem, false, false))
                continue;

            item.cacheItem();
            inventory.getEquipped().add(item);
        }

        // Call Bukkit event
        Bukkit.getPluginManager().callEvent(new RefreshInventoryEvent(inventory.getEquipped(), getPlayer(), this));

        for (EquippedItem equipped : inventory.getEquipped()) {
            final VolatileMMOItem item = equipped.getCached();

            // Abilities
            if (item.hasData(ItemStats.ABILITIES))
                for (AbilityData abilityData : ((AbilityListData) item.getData(ItemStats.ABILITIES)).getAbilities()) {
                    ModifierSource modSource = equipped.getCached().getType().getModifierSource();
                    getMMOPlayerData().getPassiveSkillMap().addModifier(new PassiveSkill("MMOItemsItem", abilityData, equipped.getSlot(), modSource));
                }

            // Modifier application rules
            final ModifierSource source = item.getType().getModifierSource();
            final EquipmentSlot equipmentSlot = equipped.getSlot();
            if (!EquipmentSlot.MAIN_HAND.isCompatible(source, equipmentSlot))
                continue;

            // Apply permanent potion effects
            if (item.hasData(ItemStats.PERM_EFFECTS))
                ((PotionEffectListData) item.getData(ItemStats.PERM_EFFECTS)).getEffects().forEach(effect -> {
                    if (getPermanentPotionEffectAmplifier(effect.getType()) < effect.getLevel() - 1)
                        permanentEffects.put(effect.getType(), effect.toEffect());
                });

            // Item particles
            if (item.hasData(ItemStats.ITEM_PARTICLES)) {
                ParticleData particleData = (ParticleData) item.getData(ItemStats.ITEM_PARTICLES);

                if (particleData.getType().hasPriority()) {
                    if (overridingItemParticles == null)
                        overridingItemParticles = particleData.start(this);
                } else
                    itemParticles.add(particleData.start(this));
            }

            // Apply permissions if Vault exists
            if (MMOItems.plugin.hasPermissions() && item.hasData(ItemStats.GRANTED_PERMISSIONS)) {
                final Permission perms = MMOItems.plugin.getVault().getPermissions();
                permissions.addAll(((StringListData) item.getData(ItemStats.GRANTED_PERMISSIONS)).getList());
                permissions.forEach(perm -> {
                    if (!perms.has(getPlayer(), perm))
                        perms.playerAdd(getPlayer(), perm);
                });
            }
        }

        // Calculate the player's item set
        final Map<ItemSet, Integer> itemSetCount = new HashMap<>();
        for (EquippedItem equipped : inventory.getEquipped()) {
            final String tag = equipped.getCached().getNBT().getString("MMOITEMS_ITEM_SET");
            final @Nullable ItemSet itemSet = MMOItems.plugin.getSets().get(tag);
            if (itemSet == null)
                continue;

            itemSetCount.put(itemSet, itemSetCount.getOrDefault(itemSet, 0) + 1);
        }

        // Reset and compute item set bonuses
        setBonuses = null;
        for (Map.Entry<ItemSet, Integer> equippedSetBonus : itemSetCount.entrySet()) {

            if (setBonuses == null) {
                // Set set bonuses
                setBonuses = equippedSetBonus.getKey().getBonuses(equippedSetBonus.getValue());

            } else {

                // Merge bonuses
                setBonuses.merge(equippedSetBonus.getKey().getBonuses(equippedSetBonus.getValue()));
            }
        }

        // Apply item set bonuses
        if (setBonuses != null) {
            if (MMOItems.plugin.hasPermissions()) {
                final Permission perms = MMOItems.plugin.getVault().getPermissions();
                permissions.addAll(setBonuses.getPermissions());
                for (String perm : setBonuses.getPermissions())
                    if (!perms.has(getPlayer(), perm))
                        perms.playerAdd(getPlayer(), perm);
            }
            for (AbilityData ability : setBonuses.getAbilities())
                getMMOPlayerData().getPassiveSkillMap().addModifier(new PassiveSkill("MMOItemsItem", ability, EquipmentSlot.OTHER, ModifierSource.OTHER));
            for (ParticleData particle : setBonuses.getParticles())
                itemParticles.add(particle.start(this));
            for (PotionEffect effect : setBonuses.getPotionEffects())
                if (getPermanentPotionEffectAmplifier(effect.getType()) < effect.getAmplifier())
                    permanentEffects.put(effect.getType(), effect);
        }

        // Calculate player stats
        stats.updateStats();

        // Update stats from external plugins
        MMOItems.plugin.getRPGs().forEach(rpg -> rpg.refreshStats(this));

        // Actually update cached player inventory so the task doesn't infinitely loop
        inventory.helmet = getPlayer().getInventory().getHelmet();
        inventory.chestplate = getPlayer().getInventory().getChestplate();
        inventory.leggings = getPlayer().getInventory().getLeggings();
        inventory.boots = getPlayer().getInventory().getBoots();
        inventory.hand = getPlayer().getInventory().getItemInMainHand();
        inventory.offhand = getPlayer().getInventory().getItemInOffHand();
    }

    public void timedRunnable() {

        // Permanent effects
        permanentEffects.values().forEach(effect -> getPlayer().addPotionEffect(effect));

        // Two handed slowness
        if (encumbered)
            getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, true, false));
    }

    public InventoryUpdateHandler getInventory() {
        return inventory;
    }

    public SetBonuses getSetBonuses() {
        return setBonuses;
    }

    public boolean hasSetBonuses() {
        return setBonuses != null;
    }

    public CraftingStatus getCrafting() {
        return craftingStatus;
    }

    public int getPermanentPotionEffectAmplifier(PotionEffectType type) {
        return permanentEffects.containsKey(type) ? permanentEffects.get(type).getAmplifier() : -1;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public Collection<PotionEffect> getPermanentPotionEffects() {
        return permanentEffects.values();
    }

    public PlayerStats getStats() {
        return stats;
    }

    /**
     * Makes the player cast an ability. Checks for cooldown and mana cost before casting it.
     * Also calls a Bukkit event right before casting it.
     *
     * @param attack  Current attack
     * @param target  Ability target, can be null
     * @param ability Ability to cast
     * @deprecated
     */
    @Deprecated
    public void cast(@Nullable AttackMetadata attack, @Nullable LivingEntity target, @NotNull AbilityData ability) {
        PlayerMetadata caster = getMMOPlayerData().getStatMap().cache(EquipmentSlot.MAIN_HAND);
        ability.cast(new TriggerMetadata(caster, target, attack));
    }

    public boolean isOnCooldown(CooldownType type) {
        return getMMOPlayerData().getCooldownMap().isOnCooldown(type.name());
    }

    public void applyCooldown(CooldownType type, double value) {
        getMMOPlayerData().getCooldownMap().applyCooldown(type.name(), value);
    }

    /**
     * @deprecated Deprecated due to cooldown references
     */
    @Deprecated
    public boolean isOnCooldown(ItemReference ref) {
        return getMMOPlayerData().getCooldownMap().isOnCooldown(ref);
    }

    /**
     * @deprecated Deprecated due to cooldown references
     */
    @Deprecated
    public void applyItemCooldown(ItemReference ref, double value) {
        getMMOPlayerData().getCooldownMap().applyCooldown(ref, value);
    }

    /**
     * @deprecated Deprecated due to cooldown references
     */
    @Deprecated
    public double getItemCooldown(ItemReference ref) {
        return getMMOPlayerData().getCooldownMap().getInfo(ref).getRemaining() / 1000d;
    }

    @NotNull
    public static PlayerData get(@NotNull MMOPlayerData playerData) {
        return get(playerData.getPlayer());
    }

    @NotNull
    public static PlayerData get(@NotNull OfflinePlayer player) {
        return get(player.getUniqueId());
    }

    /**
     * @return If player data is loaded for a player
     */
    public static boolean has(Player player) {
        return has(player.getUniqueId());
    }

    /**
     * Used to check if the UUID is associated to a real player
     * or a Citizens/Sentinel NPC. Citizens NPCs do not have
     * a player data associated to them so it's an easy O(1) way
     * to check instead of checking for an entity metadta.
     *
     * @return If player data is loaded for a player UUID
     */
    public static boolean has(UUID uuid) {
        return MMOItems.plugin.getPlayerDataManager().isLoaded(uuid);
    }

    @NotNull
    public static PlayerData get(UUID uuid) {
        return MMOItems.plugin.getPlayerDataManager().get(uuid);
    }

    @Deprecated
    @NotNull
    public static Collection<PlayerData> getLoaded() {
        return MMOItems.plugin.getPlayerDataManager().getLoaded();
    }

    public enum CooldownType {

        /**
         * Elemental attacks cooldown
         */
        ELEMENTAL_ATTACK,

        /**
         * Special attacks like staffs or gauntlets right clicks
         */
        SPECIAL_ATTACK,

        /**
         * Bouncing Crack calls block breaking events which can
         * trigger Bouncing Crack again and crash the game. A
         * cooldown is therefore required. Bouncing Crack max
         * duration is 10 ticks so a 1s cooldown is perfect
         *
         * @see {@link Tool#miningEffects(Block)}
         */
        BOUNCING_CRACK;
    }
}
