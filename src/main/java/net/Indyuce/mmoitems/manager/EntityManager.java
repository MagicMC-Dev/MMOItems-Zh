package net.Indyuce.mmoitems.manager;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.player.PlayerMetadata;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ElementalAttack;
import net.Indyuce.mmoitems.api.interaction.projectile.ArrowParticles;
import net.Indyuce.mmoitems.api.interaction.projectile.EntityData;
import net.Indyuce.mmoitems.api.interaction.projectile.ProjectileData;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

public class EntityManager implements Listener {

    /**
     * Entity data used by abilities or staff attacks that utilize entities like
     * evoker fangs or shulker missiles. It can correspond to the damage the
     * entity is supposed to deal, etc
     */
    private final Map<Integer, EntityData> entities = new HashMap<>();

    private final Map<Integer, ProjectileData> projectiles = new WeakHashMap<>();

    /**
     * Registers a custom projectile. This is used for bows, crossbows and tridents.
     * <p>
     * Default bow/trident damage is set to 7 just like vanilla Minecraft.
     *
     * @param sourceItem          Item used to shoot the projectile
     * @param attacker            Cached stats of the player shooting the projectile
     * @param entity              The custom entity
     * @param customWeapon        Is the source weapon is a custom item
     * @param damageMultiplicator The damage coefficient. For bows, this is basically the pull force.
     *                            For tridents or anything else this is always set to 1
     */
    public void registerCustomProjectile(@NotNull NBTItem sourceItem, @NotNull PlayerMetadata attacker, @NotNull Entity entity, boolean customWeapon, double damageMultiplicator) {

        // Initialize projectile data
        ProjectileData projectileData = new ProjectileData(attacker, sourceItem, customWeapon);

        /*
         * For bows, MC default value is 7. When using custom bows, the attack
         * damage stats returns the correct amount of damage. When using a vanilla
         * bow, attack damage is set to 2 because it's the default fist damage value.
         * Therefore MMOItems adds 5 to match the vanilla bow damage which is 7.
         *
         * Damage coefficient is how much you pull the bow. It's a float between
         * 0 and 1 for bows, and it is always 1 for tridents or crossbows.
         */
        projectileData.setDamage((attacker.getStat("ATTACK_DAMAGE") + (customWeapon ? 0 : 5)) * damageMultiplicator);

        /*
         * Load arrow particles if the entity is an arrow and if the item has
         * arrow particles. Currently projectiles are only arrows so there is no
         * problem with other projectiles like snowballs etc.
         */
        if (entity instanceof Arrow && sourceItem.hasTag("MMOITEMS_ARROW_PARTICLES"))
            new ArrowParticles((Arrow) entity, sourceItem);

        projectiles.put(entity.getEntityId(), projectileData);
    }

    public void registerCustomEntity(Entity entity, EntityData data) {
        entities.put(entity.getEntityId(), data);
    }

    public boolean isCustomProjectile(Projectile projectile) {
        return projectiles.containsKey(projectile.getEntityId());
    }

    public boolean isCustomEntity(Entity entity) {
        return entities.containsKey(entity.getEntityId());
    }

    public ProjectileData getProjectileData(Projectile projectile) {
        return Objects.requireNonNull(projectiles.get(projectile.getEntityId()), "Provided entity is not a custom projectile");
    }

    public EntityData getEntityData(Entity entity) {
        return Objects.requireNonNull(entities.get(entity.getEntityId()), "Provided entity is not a custom entity");
    }

    public void unregisterCustomProjectile(Projectile projectile) {
        projectiles.remove(projectile.getEntityId());
    }

    public void unregisterCustomEntity(Entity entity) {
        entities.remove(entity.getEntityId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void unregisterEntityData(EntityDeathEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(MMOItems.plugin, () -> unregisterCustomEntity(event.getEntity()));
    }

    /**
     * This fixes an issue with Heroes and MythicMobs as they are
     * plugins which MODIFY or apply damage modifiers to bow hit events.
     * <p>
     * By caching the event damage with LOWEST priority you basically store
     * the VANILLA amount of damage the bow would have dealt if there was no
     * plugin.
     * <p>
     * The main problem comes from not being able to SET the bow damage. You are
     * only allowed to add flat modifiers to it, and if all the plugins do that
     * the calculations are fully correct.
     * <p>
     * On NORMAL priority, MMOItems calculates the bow damage, substract from that
     * the vanilla bow damage which outputs the damage modifier from MMOItems. This
     * makes it compatible with other plugins modifying the damage.
     *
     * @see {@link #applyOnHitEffects(EntityDamageByEntityEvent)}
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void cacheInitialProjectileDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Projectile)
            projectiles.get(event.getEntity().getEntityId()).cacheInitialDamage(event.getDamage());
    }

    // Projectile damage and effects
    @EventHandler(ignoreCancelled = true)
    public void applyOnHitEffects(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile) || !(event.getEntity() instanceof LivingEntity) || event.getEntity().hasMetadata("NPC"))
            return;

        Projectile projectile = (Projectile) event.getDamager();
        ProjectileData data = projectiles.get(projectile.getEntityId());
        if (data == null)
            return;

        LivingEntity target = (LivingEntity) event.getEntity();
        double damage = data.getDamage();

        // Apply power vanilla enchant
        if (projectile instanceof Arrow && data.getSourceItem().getItem().hasItemMeta()
                && data.getSourceItem().getItem().getItemMeta().getEnchants().containsKey(Enchantment.ARROW_DAMAGE))
            damage *= 1.25 + (.25 * data.getSourceItem().getItem().getItemMeta().getEnchantLevel(Enchantment.ARROW_DAMAGE));

        // Apply MMOItems specific modifications
        if (data.isCustomWeapon()) {
            data.applyPotionEffects(target);
            damage += new ElementalAttack(data.getShooter(), data.getSourceItem(), damage, target).getDamageModifier();
        }

        event.setDamage(event.getDamage() + damage - data.getCachedInitialDamage());

        // Remove projectile if it has no piercing anymore
        if (!(projectile instanceof AbstractArrow) || ((AbstractArrow) projectile).getPierceLevel() <= 1)
            unregisterCustomProjectile(projectile);
    }

    // Unregister custom projectiles from the map

    @EventHandler(priority = EventPriority.HIGHEST)
    public void unregisterOnBlockHit(ProjectileHitEvent event) {
        if (event.getHitBlock() != null)
            projectiles.remove(event.getEntity().getEntityId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void unregisterOnEntityHit(EntityDeathEvent event) {
        projectiles.remove(event.getEntity().getEntityId());
    }
}