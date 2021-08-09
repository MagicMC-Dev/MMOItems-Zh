package net.Indyuce.mmoitems.manager;

import io.lumine.mythic.lib.api.DamageType;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.interaction.projectile.ArrowParticles;
import net.Indyuce.mmoitems.api.interaction.projectile.EntityData;
import net.Indyuce.mmoitems.api.interaction.projectile.ProjectileData;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

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

	public void registerCustomProjectile(NBTItem sourceItem, CachedStats stats, Entity entity, boolean customWeapon) {
		registerCustomProjectile(sourceItem, stats, entity, customWeapon, 1);
	}

	/**
	 * Registers a custom projectile
	 *
	 * @param sourceItem        Item used to shoot the projectile
	 * @param stats             Cached stats of the player shooting the projectile
	 * @param entity
	 * @param customWeapon
	 * @param damageCoefficient
	 */
	public void registerCustomProjectile(NBTItem sourceItem, CachedStats stats, Entity entity, boolean customWeapon, double damageCoefficient) {

		/*
		 * By default damage is set to minecraft's default 7. It is then
		 * multiplied by the coefficient proportionnal to the pull force.
		 * 1 corresponds to a fully pulled bow just like in vanilla MC.
		 *
		 * For tridents or crossbows, damage coefficient is always 1
		 */
		double damage = stats.getStat(ItemStats.ATTACK_DAMAGE);
		stats.setStat(ItemStats.ATTACK_DAMAGE, (damage == 0 ? 7 : damage) * damageCoefficient);

		/*
		 * Load arrow particles if the entity is an arrow and if the item has
		 * arrow particles. Currently projectiles are only arrows so there is no
		 * problem with other projectiles like snowballs etc.
		 */
		if (entity instanceof Arrow && sourceItem.hasTag("MMOITEMS_ARROW_PARTICLES"))
			new ArrowParticles((Arrow) entity, sourceItem);

		projectiles.put(entity.getEntityId(), new ProjectileData(sourceItem, stats, customWeapon));
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

	/*
	 * Projectile Damage and Effects
	 *
	 * TODO when throwing a trident, on hit abilities dont cast half
	 *  of the time because you don't hold the item anymore, therefore
	 *  the ability does not register.
	 *  To fix that, not only cache the player statistics using CachedStats
	 *  but also the player abilities as well as elemental stats. in fact
	 *  a lot of extra stats need to be cached when a ranged attack is delivered.
	 *
	 * TODO This bug could also be exploited using a bow, by holding another item
	 *  after shooting an arrow!!
	 */
	@EventHandler(ignoreCancelled = true)
	public void b(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Projectile) || !(event.getEntity() instanceof LivingEntity) || event.getEntity().hasMetadata("NPC"))
			return;

		Projectile projectile = (Projectile) event.getDamager();
		if (!isCustomProjectile(projectile))
			return;

		ProjectileData data = getProjectileData(projectile);
		LivingEntity target = (LivingEntity) event.getEntity();
		CachedStats stats = data.getPlayerStats();

		ItemAttackResult result = new ItemAttackResult(data.isCustomWeapon() ? stats.getStat(ItemStats.ATTACK_DAMAGE) : event.getDamage(),
				DamageType.WEAPON, DamageType.PROJECTILE, DamageType.PHYSICAL).applyOnHitEffects(stats, target);

		// Apply power vanilla enchant
		if (projectile instanceof Arrow && data.getSourceItem().getItem().hasItemMeta()
				&& data.getSourceItem().getItem().getItemMeta().getEnchants().containsKey(Enchantment.ARROW_DAMAGE))
			result.multiplyDamage(1.25 + (.25 * data.getSourceItem().getItem().getItemMeta().getEnchantLevel(Enchantment.ARROW_DAMAGE)));

		// Apply MMOItems specific modifications
		if (data.isCustomWeapon()) {
			data.applyPotionEffects(target);
			result.applyElementalEffects(stats, data.getSourceItem(), target);
		}

		event.setDamage(result.getDamage());
		unregisterCustomProjectile(projectile);
	}
}