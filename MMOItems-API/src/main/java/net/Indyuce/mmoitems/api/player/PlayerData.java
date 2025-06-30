package net.Indyuce.mmoitems.api.player;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.player.permission.PermissionModifier;
import io.lumine.mythic.lib.player.potion.PermanentPotionEffect;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.util.Closeable;
import io.lumine.mythic.lib.version.VPotionEffectType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.CraftingStatus;
import net.Indyuce.mmoitems.api.interaction.Tool;
import net.Indyuce.mmoitems.api.item.ItemReference;
import net.Indyuce.mmoitems.inventory.InventoryResolver;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerData extends SynchronizedDataHolder implements Closeable {
    // Reloaded everytime the player reconnects in case of major change.
    private RPGPlayer rpgPlayer;

    private final InventoryResolver inventoryResolver = new InventoryResolver(this);
    private final CraftingStatus craftingStatus = new CraftingStatus();

    public PlayerData(@NotNull MMOPlayerData mmoData) {
        super(MMOItems.plugin, mmoData);

        rpgPlayer = MMOItems.plugin.getMainRPG().getInfo(this);
    }

    @Override
    public void close() {
        inventoryResolver.close();
    }

    public boolean isOnline() {
        return getMMOPlayerData().isOnline();
    }

    public RPGPlayer getRPG() {
        return rpgPlayer;
    }

    public boolean isEncumbered() {
        return inventoryResolver.isEncumbered();
    }

    /**
     * Some plugins require to update the RPGPlayer after server startup
     *
     * @param rpgPlayer New RPGPlayer instance
     */
    public void setRPGPlayer(RPGPlayer rpgPlayer) {
        this.rpgPlayer = rpgPlayer;
    }

    public void resolveModifiersLater() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(MMOItems.plugin, inventoryResolver::resolveModifiers);
    }

    /**
     * One should try to avoid calling this method
     */
    public void resolveInventory() {

        // Cannot update inventory unless online and fully sync
        if (!isOnline() || !getMMOPlayerData().hasFullySynchronized()) return;

        inventoryResolver.resolveInventory();

        // Update stats from external plugins
        // TODO improve?
        MMOItems.plugin.getRPGs().forEach(rpg -> rpg.refreshStats(this));
    }

    public void timedRunnable() {

        // Two-handed slowness
        if (inventoryResolver.isEncumbered())
            getPlayer().addPotionEffect(new PotionEffect(VPotionEffectType.SLOWNESS.get(), 40, 1, true, false));
    }

    @NotNull
    public InventoryResolver getInventory() {
        return inventoryResolver;
    }

    @NotNull
    public CraftingStatus getCrafting() {
        return craftingStatus;
    }

    public double getStat(@NotNull ItemStat<?, ?> stat) {
        return getMMOPlayerData().getStatMap().getStat(stat.getId());
    }

    public boolean isOnCooldown(CooldownType type) {
        return getMMOPlayerData().getCooldownMap().isOnCooldown(type.name());
    }

    public void applyCooldown(CooldownType type, double value) {
        getMMOPlayerData().getCooldownMap().applyCooldown(type.name(), value);
    }

    @NotNull
    public static PlayerData get(@NotNull MMOPlayerData playerData) {
        return get(playerData.getPlayer());
    }

    @NotNull
    public static PlayerData get(@NotNull OfflinePlayer player) {
        return get(player.getUniqueId());
    }

    @Nullable
    public static PlayerData getOrNull(@NotNull OfflinePlayer player) {
        try {
            return get(player.getUniqueId());
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     * @see #getOrNull(OfflinePlayer)
     * @deprecated
     */
    @Deprecated
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
     * @deprecated
     * @see #getOrNull(OfflinePlayer)
     */
    @Deprecated
    public static boolean has(UUID uuid) {
        return MMOItems.plugin.getPlayerDataManager().isLoaded(uuid);
    }

    @NotNull
    public static PlayerData get(UUID uuid) {
        return MMOItems.plugin.getPlayerDataManager().get(uuid);
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
         * @see Tool#miningEffects(Block)
         */
        BOUNCING_CRACK
    }

    //region Deprecated

    @Deprecated
    public PlayerStats getStats() {
        return new PlayerStats(this);
    }

    /**
     * @see MMOPlayerData#getPermissionMap()
     * @deprecated
     */
    @Deprecated
    public Set<String> getPermissions() {
        return getMMOPlayerData().getPermissionMap().getModifiers().stream().map(PermissionModifier::getPermission).collect(Collectors.toSet());
    }

    /**
     * @see MMOPlayerData#getPermanentEffectMap()
     * @deprecated
     */
    @Deprecated
    public Collection<PotionEffect> getPermanentPotionEffects() {
        return getMMOPlayerData().getPermanentEffectMap().getModifiers().stream().map(PermanentPotionEffect::toBukkit).collect(Collectors.toList());
    }

    @Deprecated
    public boolean areHandsFull() {
        return isEncumbered();
    }

    @Deprecated
    @NotNull
    public static Collection<PlayerData> getLoaded() {
        return MMOItems.plugin.getPlayerDataManager().getLoaded();
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

    @Deprecated
    public void cast(@Nullable AttackMetadata attack, @Nullable LivingEntity target, @NotNull AbilityData ability) {
        PlayerMetadata caster = getMMOPlayerData().getStatMap().cache(EquipmentSlot.MAIN_HAND);
        ability.cast(new TriggerMetadata(caster, target, attack));
    }

    /**
     * @see #resolveInventory()
     * @deprecated
     */
    @Deprecated
    public void updateInventory() {
        resolveInventory();
    }

    //endregion
}
