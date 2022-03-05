package net.Indyuce.mmoitems.api.player;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.ItemSet;
import net.Indyuce.mmoitems.api.ItemSet.SetBonuses;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.crafting.CraftingStatus;
import net.Indyuce.mmoitems.api.event.RefreshInventoryEvent;
import net.Indyuce.mmoitems.api.interaction.Tool;
import net.Indyuce.mmoitems.api.item.ItemReference;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import net.Indyuce.mmoitems.api.player.inventory.EquippedPlayerItem;
import net.Indyuce.mmoitems.api.player.inventory.InventoryUpdateHandler;
import net.Indyuce.mmoitems.particle.api.ParticleRunnable;
import net.Indyuce.mmoitems.stat.data.*;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerData {
    private static final Map<UUID, PlayerData> data = new HashMap<>();
    @NotNull
    private final MMOPlayerData mmoData;

    /*
     * reloaded everytime the player reconnects in case of major change.
     */
    private RPGPlayer rpgPlayer;

    private final InventoryUpdateHandler inventory = new InventoryUpdateHandler(this);
    private final CraftingStatus craftingStatus = new CraftingStatus();

    /*
     * specific stat calculation TODO compress it in Map<ItemStat, DynamicStatData>
     */
    private final Map<PotionEffectType, PotionEffect> permanentEffects = new HashMap<>();
    private final Set<ParticleRunnable> itemParticles = new HashSet<>();
    private ParticleRunnable overridingItemParticles = null;
    private boolean fullHands = false;
    private SetBonuses setBonuses = null;
    private final PlayerStats stats;

    // Cached so they can be properly removed again
    private final Set<String> permissions = new HashSet<>();

    private PlayerData(@NotNull MMOPlayerData mmoData) {
        this.mmoData = mmoData;
        rpgPlayer = MMOItems.plugin.getRPG().getInfo(this);
        stats = new PlayerStats(this);

        load(new ConfigFile("/userdata", getUniqueId().toString()).getConfig());
    }

    private void load(FileConfiguration config) {
        if (config.contains("crafting-queue"))
            craftingStatus.load(this, config.getConfigurationSection("crafting-queue"));

        if (MMOItems.plugin.hasPermissions() && config.contains("permissions-from-items")) {
            Permission perms = MMOItems.plugin.getVault().getPermissions();
            config.getStringList("permissions-from-items").forEach(perm -> {
                if (perms.has(getPlayer(), perm))
                    perms.playerRemove(getPlayer(), perm);
            });
        }
    }

    public void save() {
        cancelRunnables();

        ConfigFile config = new ConfigFile("/userdata", getUniqueId().toString());
        config.getConfig().createSection("crafting-queue");
        config.getConfig().set("permissions-from-items", new ArrayList<>(permissions));
        craftingStatus.save(config.getConfig().getConfigurationSection("crafting-queue"));
        config.save();
    }

    public MMOPlayerData getMMOPlayerData() {
        return mmoData;
    }

    public UUID getUniqueId() {
        return mmoData.getUniqueId();
    }

    public boolean isOnline() {
        return mmoData.isOnline();
    }

    public Player getPlayer() {
        return mmoData.getPlayer();
    }

    public RPGPlayer getRPG() {
        return rpgPlayer;
    }

    public void cancelRunnables() {
        itemParticles.forEach(BukkitRunnable::cancel);
        if (overridingItemParticles != null)
            overridingItemParticles.cancel();
    }

    /*
     * returns true if the player hands are full, i.e if the player is holding
     * one two handed item and one other item at the same time
     */
    public boolean areHandsFull() {
        if (!mmoData.isOnline())
            return false;

        // Get the mainhand and offhand items.
        NBTItem main = MythicLib.plugin.getVersion().getWrapper().getNBTItem(getPlayer().getInventory().getItemInMainHand());
        NBTItem off = MythicLib.plugin.getVersion().getWrapper().getNBTItem(getPlayer().getInventory().getItemInOffHand());

        // Is either hand two-handed?
        boolean mainhand_twohanded = main.getBoolean(ItemStats.TWO_HANDED.getNBTPath());
        boolean offhand_twohanded = off.getBoolean(ItemStats.TWO_HANDED.getNBTPath());

        // Is either hand encumbering: Not NULL, not AIR, and not Handworn
        boolean mainhand_encumbering = (main.getItem() != null && main.getItem().getType() != Material.AIR && !main.getBoolean(ItemStats.HANDWORN.getNBTPath()));
        boolean offhand_encumbering = (off.getItem() != null && off.getItem().getType() != Material.AIR && !off.getBoolean(ItemStats.HANDWORN.getNBTPath()));

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

    @SuppressWarnings("deprecation")
    public void updateInventory() {
        if (!mmoData.isOnline())
            return;

        /*
         * very important, clear particle data AFTER canceling the runnable
         * otherwise it cannot cancel and the runnable keeps going (severe)
         */
        inventory.getEquipped().clear();
        permanentEffects.clear();
        cancelRunnables();
        mmoData.getPassiveSkillMap().removeModifiers("MMOItemsItem");
        itemParticles.clear();
        overridingItemParticles = null;
        if (MMOItems.plugin.hasPermissions()) {
            Permission perms = MMOItems.plugin.getVault().getPermissions();
            permissions.forEach(perm -> {
                if (perms.has(getPlayer(), perm))
                    perms.playerRemove(getPlayer(), perm);
            });
        }
        permissions.clear();

        /*
         * updates the full-hands boolean, this way it can be cached and used in
         * the updateEffects() method
         */
        fullHands = areHandsFull();

        /*
         * Find all the items the player can actually use
         */
        for (EquippedItem item : MMOItems.plugin.getInventory().getInventory(getPlayer())) {
            NBTItem nbtItem = item.getItem();
            if (nbtItem.getItem() == null || nbtItem.getItem().getType() == Material.AIR)
                continue;

            /*
             * If the item is a custom item, apply slot and item use
             * restrictions (items which only work in a specific equipment slot)
             */
            Type type = Type.get(nbtItem.getType());
            if (type == null || !item.matches(type) || !getRPG().canUse(nbtItem, false, false))
                continue;

            inventory.getEquipped().add(new EquippedPlayerItem(item));
        }

        RefreshInventoryEvent riev = new RefreshInventoryEvent(inventory.getEquipped(), getPlayer(), this);
        Bukkit.getPluginManager().callEvent(riev);

        for (EquippedPlayerItem equipped : inventory.getEquipped()) {
            VolatileMMOItem item = equipped.getItem();

            /*
             * Apply permanent potion effects
             */
            if (item.hasData(ItemStats.PERM_EFFECTS))
                ((PotionEffectListData) item.getData(ItemStats.PERM_EFFECTS)).getEffects().forEach(effect -> {
                    if (getPermanentPotionEffectAmplifier(effect.getType()) < effect.getLevel() - 1)
                        permanentEffects.put(effect.getType(), effect.toEffect());
                });

            /*
             * Apply item particles
             */
            if (item.hasData(ItemStats.ITEM_PARTICLES)) {
                ParticleData particleData = (ParticleData) item.getData(ItemStats.ITEM_PARTICLES);

                if (particleData.getType().hasPriority()) {
                    if (overridingItemParticles == null)
                        overridingItemParticles = particleData.start(this);
                } else
                    itemParticles.add(particleData.start(this));
            }

            /*
             * Apply abilities
             */
            if (item.hasData(ItemStats.ABILITIES) && (MMOItems.plugin.getConfig().getBoolean("abilities-bypass-encumbering") || !fullHands))
                if (equipped.getSlot() != EquipmentSlot.OFF_HAND || !MMOItems.plugin.getConfig().getBoolean("disable-abilities-in-offhand"))
                    for (AbilityData abilityData : ((AbilityListData) item.getData(ItemStats.ABILITIES)).getAbilities()) {
                        ModifierSource modSource = equipped.getItem().getType() == null ? ModifierSource.OTHER : equipped.getItem().getType().getItemSet().getModifierSource();
                        mmoData.getPassiveSkillMap().addModifier(new PassiveSkill("MMOItemsItem", abilityData, equipped.getSlot(), modSource));
                    }

            /*
             * Apply permissions if vault exists
             */
            if (MMOItems.plugin.hasPermissions() && item.hasData(ItemStats.GRANTED_PERMISSIONS)) {
                permissions.addAll(((StringListData) item.getData(ItemStats.GRANTED_PERMISSIONS)).getList());
                Permission perms = MMOItems.plugin.getVault().getPermissions();
                permissions.forEach(perm -> {
                    if (!perms.has(getPlayer(), perm))
                        perms.playerAdd(getPlayer(), perm);
                });
            }
        }

        /*
         * calculate the player's item set and add the bonus permanent effects /
         * bonus abilities to the playerdata maps
         */
        int max = 0;
        ItemSet set = null;
        Map<ItemSet, Integer> sets = new HashMap<>();
        for (EquippedPlayerItem equipped : inventory.getEquipped()) {
            VolatileMMOItem item = equipped.getItem();
            String tag = item.getNBT().getString("MMOITEMS_ITEM_SET");
            ItemSet itemSet = MMOItems.plugin.getSets().get(tag);
            if (itemSet == null)
                continue;

            int nextInt = (sets.getOrDefault(itemSet, 0)) + 1;
            sets.put(itemSet, nextInt);
            if (nextInt >= max) {
                max = nextInt;
                set = itemSet;
            }
        }
        setBonuses = set == null ? null : set.getBonuses(max);

        if (hasSetBonuses()) {
            for (AbilityData ability : setBonuses.getAbilities())
                mmoData.getPassiveSkillMap().addModifier(new PassiveSkill("MMOItemsItem", ability, EquipmentSlot.OTHER, ModifierSource.OTHER));
            for (ParticleData particle : setBonuses.getParticles())
                itemParticles.add(particle.start(this));
            for (PotionEffect effect : setBonuses.getPotionEffects())
                if (getPermanentPotionEffectAmplifier(effect.getType()) < effect.getAmplifier())
                    permanentEffects.put(effect.getType(), effect);
        }

        /*
         * calculate all stats.
         */
        stats.updateStats();

        /*
         * update stuff from the external MMOCore plugins. the 'max mana' stat
         * currently only supports Heroes since other APIs do not allow other
         * plugins to easily increase this type of stat.
         */
        MMOItems.plugin.getRPG().refreshStats(this);

        /*
         * actually update the player inventory so the task doesn't infinitely
         * loop on updating
         */
        inventory.helmet = getPlayer().getInventory().getHelmet();
        inventory.chestplate = getPlayer().getInventory().getChestplate();
        inventory.leggings = getPlayer().getInventory().getLeggings();
        inventory.boots = getPlayer().getInventory().getBoots();
        inventory.hand = getPlayer().getInventory().getItemInMainHand();
        inventory.offhand = getPlayer().getInventory().getItemInOffHand();
    }

    public void updateStats() {
        if (!mmoData.isOnline())
            return;

        // perm effects
        permanentEffects.values().forEach(effect -> getPlayer().addPotionEffect(effect));

        // two handed
        if (fullHands)
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
        ability.cast(new TriggerMetadata(caster, attack, target));
    }

    public boolean isOnCooldown(CooldownType type) {
        return mmoData.getCooldownMap().isOnCooldown(type.name());
    }

    public void applyCooldown(CooldownType type, double value) {
        mmoData.getCooldownMap().applyCooldown(type.name(), value);
    }

    public boolean isOnCooldown(ItemReference ref) {
        return mmoData.getCooldownMap().isOnCooldown(ref);
    }

    public void applyItemCooldown(ItemReference ref, double value) {
        mmoData.getCooldownMap().applyCooldown(ref, value);
    }

    public double getItemCooldown(ItemReference ref) {
        return mmoData.getCooldownMap().getInfo(ref).getRemaining() / 1000d;
    }

    @NotNull
    public static PlayerData get(@NotNull OfflinePlayer player) {
        return get(player.getUniqueId());
    }

    /**
     * See {@link #has(UUID)}
     *
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
        return data.containsKey(uuid);
    }

    @NotNull
    public static PlayerData get(UUID uuid) {
        return Objects.requireNonNull(data.get(uuid), "Player data not loaded");
    }

    /**
     * Called when the corresponding MMOPlayerData has already been initialized.
     */
    public static void load(@NotNull Player player) {
        load(player.getUniqueId());
    }

    /**
     * Called when the corresponding MMOPlayerData has already been initialized.
     */
    public static void load(@NotNull UUID player) {

        /*
         * Double check they are online, for some reason even if this is fired
         * from the join event the player can be offline if they left in the
         * same tick or something.
         */
        if (!data.containsKey(player)) {
            PlayerData playerData = new PlayerData(MMOPlayerData.get(player));
            data.put(player, playerData);
            playerData.updateInventory();
            return;
        }

        /*
         * Update the cached RPGPlayer in case of any major change in the player
         * data of other rpg plugins
         */
        PlayerData playerData = data.get(player);
        playerData.rpgPlayer = MMOItems.plugin.getRPG().getInfo(playerData);
    }

    public static Collection<PlayerData> getLoaded() {
        return data.values();
    }

    public enum CooldownType {

        /**
         * Basic attack cooldown like staffs and lutes
         */
        BASIC_ATTACK,

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
        BOUNCING_CRACK,

        /**
         * Special item set attack effects including slashing, piercing and
         * blunt attack effects
         */
        SET_TYPE_ATTACK
    }
}
