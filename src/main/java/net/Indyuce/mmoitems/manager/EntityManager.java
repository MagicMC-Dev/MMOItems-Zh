package net.Indyuce.mmoitems.manager;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.api.event.MMOItemsProjectileFireEvent;
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
import org.bukkit.event.entity.EntityShootBowEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class EntityManager implements Listener {

    /**
     * Entity data used by abilities or staff attacks that utilize entities like
     * evoker fangs or shulker missiles. It can correspond to the damage the
     * entity is supposed to deal, etc
     */
    private final Map<Integer, EntityData> entities = new HashMap<>();

    private final WeakHashMap<Integer, ProjectileData> projectiles = new WeakHashMap<>();

    @Deprecated public void registerCustomProjectile(NBTItem sourceItem, PlayerMetadata attacker, Entity entity, boolean customWeapon) { registerCustomProjectile(sourceItem, attacker, entity, customWeapon, 1); }
    @Deprecated public void registerCustomProjectile(@NotNull NBTItem sourceItem, @NotNull PlayerMetadata attacker, @NotNull Entity entity, boolean customWeapon, double damageMultiplicator) { registerCustomProjectile(sourceItem, attacker, entity, null, customWeapon, damageMultiplicator); }
    /**
     * Registers a custom projectile. This is used for bows, crossbows and tridents.
     * <p>
     * Default bow/trident damage is set to 7 just like vanilla Minecraft.
     *
     * @param sourceItem          Item used to shoot the projectile
     * @param attacker            Cached stats of the player shooting the projectile
     * @param shootEvent          Event that caused this projectile registration.
     * @param entity              The custom entity
     * @param customWeapon        Is the source weapon is a custom item
     * @param damageMultiplicator The damage coefficient. For bows, this is basically the pull force.
     *                            For tridents or anything else this is always set to 1
     */
    public void registerCustomProjectile(@NotNull NBTItem sourceItem, @NotNull PlayerMetadata attacker, @NotNull Entity entity, @Nullable EntityShootBowEvent shootEvent, boolean customWeapon, double damageMultiplicator) {

        /*
         * For bows, MC default value is 7. When using custom bows, the attack
         * damage stats returns the correct amount of damage. When using a vanilla
         * bow, attack damage is set to 1 because it's the default fist damage value.
         * Therefore MMOItems adds 6 to match the vanilla bow damage which is 7.
         *
         * Damage coefficient is how much you pull the bow. It's something between 0
         * and 1 for bows, and it's always 1 for tridents or crossbows.
         */
        double damage = attacker.getStat("ATTACK_DAMAGE");

        // Sweet event
        MMOItemsProjectileFireEvent event = new MMOItemsProjectileFireEvent(attacker, entity, sourceItem, shootEvent, (customWeapon ? damage : 5 + damage), damageMultiplicator);
        Bukkit.getPluginManager().callEvent(event);

        // Update based one vent
        double finalDamage = event.getFinalDamage();

        ItemAttackMetadata attackMeta = new ItemAttackMetadata(new DamageMetadata(finalDamage, event.getDamageTypes()), attacker);
        attacker.setStat("ATTACK_DAMAGE", finalDamage);

        /*
         * Load arrow particles if the entity is an arrow and if the item has
         * arrow particles. Currently projectiles are only arrows so there is no
         * problem with other projectiles like snowballs etc.
         */
        if (entity instanceof Arrow && sourceItem.hasTag("MMOITEMS_ARROW_PARTICLES"))
            new ArrowParticles((Arrow) entity, sourceItem);

        projectiles.put(entity.getEntityId(), new ProjectileData(sourceItem, attackMeta, customWeapon));
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
        return projectiles.get(projectile.getEntityId());
    }

    public EntityData getEntityData(Entity entity) {
        return entities.get(entity.getEntityId());
    }

    public void unregisterCustomProjectile(Projectile projectile) {
        projectiles.remove(projectile.getEntityId());
    }

    public void unregisterCustomEntity(Entity entity) {
        entities.remove(entity.getEntityId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void a(EntityDeathEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(MMOItems.plugin, () -> unregisterCustomEntity(event.getEntity()));
    }

    // Projectile damage and effects
    @EventHandler(ignoreCancelled = true)
    public void b(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile) || !(event.getEntity() instanceof LivingEntity) || event.getEntity().hasMetadata("NPC"))
            return;

        Projectile projectile = (Projectile) event.getDamager();
        if (!isCustomProjectile(projectile))
            return;

        ProjectileData data = getProjectileData(projectile);
        LivingEntity target = (LivingEntity) event.getEntity();

        // Apply power vanilla enchant
        if (projectile instanceof Arrow && data.getSourceItem().getItem().hasItemMeta()
                && data.getSourceItem().getItem().getItemMeta().getEnchants().containsKey(Enchantment.ARROW_DAMAGE))
            data.getAttackMetadata().getDamage().multiplicativeModifier(1.25 + (.25 * data.getSourceItem().getItem().getItemMeta().getEnchantLevel(Enchantment.ARROW_DAMAGE)), DamageType.WEAPON);

        // Apply MMOItems specific modifications
        if (data.isCustomWeapon()) {
            data.applyPotionEffects(target);
            data.getAttackMetadata().applyElementalEffects(data.getSourceItem(), target);
        }

        event.setDamage(data.getAttackMetadata().getDamage().getDamage());

        // Remove projectile if it has no piercing anymore
        if (projectile instanceof AbstractArrow) {

            if (((AbstractArrow) projectile).getPierceLevel() > 1) { return; } }
        unregisterCustomProjectile(projectile);
    }
}