package net.Indyuce.mmoitems.manager;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.damage.ProjectileAttackMetadata;
import io.lumine.mythic.lib.player.PlayerMetadata;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.interaction.projectile.ArrowParticles;
import net.Indyuce.mmoitems.api.interaction.projectile.EntityData;
import net.Indyuce.mmoitems.api.interaction.projectile.ProjectileData;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Deprecated
    public void registerCustomProjectile(@NotNull NBTItem sourceItem, @NotNull PlayerMetadata attacker, @NotNull Entity entity, boolean customWeapon, double damageMultiplicator) {
        registerCustomProjectile(sourceItem, attacker, entity, damageMultiplicator);
    }

    /**
     * Registers a custom projectile. This is used for bows, crossbows and tridents.
     *
     * @param sourceItem          Item used to shoot the projectile
     * @param attacker            Cached stats of the player shooting the projectile
     * @param entity              The custom entity
     * @param damageMultiplicator The damage coefficient. For bows, this is basically the pull force.
     *                            For tridents or anything else this is always set to 1
     */
    @NotNull
    public ProjectileData registerCustomProjectile(@NotNull NBTItem sourceItem, @NotNull PlayerMetadata attacker, @NotNull Entity entity, double damageMultiplicator) {

        // Initialize projectile data
        final ProjectileData projectileData = new ProjectileData(attacker, sourceItem, damageMultiplicator);

        /*
         * Load arrow particles if the entity is an arrow and if the item has
         * arrow particles. Currently projectiles are only arrows so there is no
         * problem with other projectiles like snowballs etc.
         */
        if (entity instanceof AbstractArrow && sourceItem.hasTag("MMOITEMS_ARROW_PARTICLES"))
            new ArrowParticles((AbstractArrow) entity, sourceItem);

        projectiles.put(entity.getEntityId(), projectileData);
        return projectileData;
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

    /**
     * This event is called on LOWEST and only edits the custom bow base damage.
     * It does NOT take into account the base damage passed in Bow#getDamage()
     * and fully overrides any change.
     *
     * This applies to tridents, arrows, spectral arrows etc.
     * <p>
     * Event order: ProjectileHit -> EntityDamage / EntityDeathEvent
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void customProjectileDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile) || !(event.getEntity() instanceof LivingEntity) || event.getEntity().hasMetadata("NPC"))
            return;

        final Projectile projectile = (Projectile) event.getDamager();
        final ProjectileData data = projectiles.get(projectile.getEntityId());
        if (data == null)
            return;

        // Calculate custom base damage
        double baseDamage = data.getDamage();

        // Apply power vanilla enchant
        if (projectile instanceof AbstractArrow && data.getSourceItem().getItem().hasItemMeta()
                && data.getSourceItem().getItem().getItemMeta().getEnchants().containsKey(Enchantment.ARROW_DAMAGE))
            baseDamage *= 1.25 + (.25 * data.getSourceItem().getItem().getItemMeta().getEnchantLevel(Enchantment.ARROW_DAMAGE));

        event.setDamage(baseDamage);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHitEffects(PlayerAttackEvent event) {
        if (!(event.getAttack() instanceof ProjectileAttackMetadata))
            return;

        final ProjectileAttackMetadata projAttack = (ProjectileAttackMetadata) event.getAttack();
        final @Nullable ProjectileData data = projectiles.get(projAttack.getProjectile().getEntityId());
        if (data == null)
            return;

        // Apply MMOItems specific modifications
        data.applyPotionEffects(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void unregisterProjectileData(ProjectileHitEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(MMOItems.plugin, () -> unregisterCustomProjectile(event.getEntity()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void unregisterEntityData(EntityDeathEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(MMOItems.plugin, () -> unregisterCustomEntity(event.getEntity()));
    }
}