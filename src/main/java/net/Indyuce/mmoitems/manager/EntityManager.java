package net.Indyuce.mmoitems.manager;

import java.util.HashMap;
import java.util.Map;

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

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ArrowParticles;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ProjectileData;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.item.NBTItem;

public class EntityManager implements Listener {

	/*
	 * entity data used by abilities or staff attacks that utilize entities like
	 * evoker fangs or shulker missiles. it can correspond to the damage the
	 * entity is supposed to deal, etc
	 */
	private Map<Integer, Object[]> entities = new HashMap<>();

	private Map<Integer, ProjectileData> projectiles = new HashMap<>();

	public void registerCustomProjectile(NBTItem sourceItem, CachedStats stats, Entity entity, boolean customWeapon) {
		registerCustomProjectile(sourceItem, stats, entity, customWeapon, 1);
	}

	public void registerCustomProjectile(NBTItem sourceItem, CachedStats stats, Entity entity, boolean customWeapon, double damageCoefficient) {
		/*
		 * if damage is null, then it uses the default minecraft bow damage.
		 * it's then multiplied by the damage coefficient which corresponds for
		 * bows to the pull force just like vanilla. it does not work with
		 * tridents
		 */
		double damage = stats.getStat(ItemStat.ATTACK_DAMAGE);
		stats.setStat(ItemStat.ATTACK_DAMAGE, (damage == 0 ? 7 : damage) * damageCoefficient);

		/*
		 * load arrow particles if the entity is an arrow and if the item has
		 * arrow particles. currently projectiles are only arrows so there is no
		 * problem with other projectiles like snowballs etc.
		 */
		if (entity instanceof Arrow) {
			ArrowParticles particles = new ArrowParticles((Arrow) entity).load(sourceItem);
			if (particles.isValid())
				particles.runTaskTimer(MMOItems.plugin, 0, 1);
		}

		projectiles.put(entity.getEntityId(), new ProjectileData(sourceItem, stats, customWeapon));
	}

	public void registerCustomEntity(Entity entity, Object... data) {
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

	public Object[] getEntityData(Entity entity) {
		return entities.get(entity.getEntityId());
	}

	public void unregisterCustomProjectile(Projectile projectile) {
		projectiles.remove(projectile.getEntityId());
	}

	public void unregisterCustomEntity(Entity entity) {
		entities.remove(entity.getEntityId());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(EntityDeathEvent event) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(MMOItems.plugin, () -> unregisterCustomEntity(event.getEntity()));
	}

	@EventHandler
	public void b(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Projectile) || !(event.getEntity() instanceof LivingEntity) || event.getEntity().hasMetadata("NPC") || event.isCancelled())
			return;

		Projectile arrow = (Projectile) event.getDamager();
		if (!isCustomProjectile(arrow))
			return;

		ProjectileData data = getProjectileData(arrow);
		LivingEntity target = (LivingEntity) event.getEntity();
		CachedStats stats = data.getPlayerStats();

		ItemAttackResult result = new ItemAttackResult(data.isCustomWeapon() ? stats.getStat(ItemStat.ATTACK_DAMAGE) : event.getDamage(), DamageType.WEAPON, DamageType.PROJECTILE, DamageType.PHYSICAL).applyOnHitEffects(stats, target);

		/*
		 * only modify the damage when the bow used is a custom weapon.
		 */
		if (data.isCustomWeapon()) {
			result.applyElementalEffects(stats, data.getSourceItem(), target);

			if (data.getSourceItem().getItem().hasItemMeta())
				if (data.getSourceItem().getItem().getItemMeta().getEnchants().containsKey(Enchantment.ARROW_DAMAGE))
					result.addRelativeDamage(.25 + (.25 * data.getSourceItem().getItem().getItemMeta().getEnchantLevel(Enchantment.ARROW_DAMAGE)));
		}

		event.setDamage(result.getDamage());
		unregisterCustomProjectile(arrow);
		return;
	}
}