package net.Indyuce.mmoitems.api.interaction.projectile;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.player.PlayerMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.manager.EntityManager;
import net.Indyuce.mmoitems.stat.data.PotionEffectData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

public class ProjectileData {
	private final NBTItem sourceItem;
	private final PlayerMetadata shooter;
	private final boolean customWeapon;

    private double cachedInitialDamage;

	public ProjectileData(PlayerMetadata shooter, NBTItem sourceItem, boolean customWeapon) {
		this.shooter = shooter;
		this.sourceItem = sourceItem;
		this.customWeapon = customWeapon;
	}

	public NBTItem getSourceItem() {
		return sourceItem;
	}

	public PlayerMetadata getShooter() {
		return shooter;
	}

	/**
	 * Used to check if that projectile data is linked to
	 * a projectile that want sent using a MMOItems bow.
	 * <p>
	 * If so, it needs to apply on-hit effects like
	 * elemental damage or on-hit potion effects
	 */
	public boolean isCustomWeapon() {
		return customWeapon;
	}

    /**
     * Attack damage is handled in a weird fashion for projectile
     * attacks. Attack damage is not stored in a simple field but
     * rather the player's attack damage stat is temporarily changed.
     * <p>
     * Using this convention the attack damage is handled the same
     * way for both melee and projectile attacks.
     *
     * @return Projectile damage, not taking into account crits and
     * on-hit effects which are only applied afterwards by MythicLib
     */
    public double getDamage() {
        return shooter.getStat("ATTACK_DAMAGE");
    }

    /**
     * @see {@link #getDamage()}
     */
    public void setDamage(double damage) {
        shooter.setStat("ATTACK_DAMAGE", damage);
    }

    /**
     * @see {@link EntityManager#cacheInitialProjectileDamage(EntityDamageByEntityEvent)}
     */
    public double getCachedInitialDamage() {
        return cachedInitialDamage;
    }

    /**
     * @see {@link EntityManager#cacheInitialProjectileDamage(EntityDamageByEntityEvent)}
     */
    public void cacheInitialDamage(double cachedInitialDamage) {
        this.cachedInitialDamage = cachedInitialDamage;
    }

	public void applyPotionEffects(LivingEntity target) {
		if (sourceItem.hasTag("MMOITEMS_ARROW_POTION_EFFECTS"))
			for (JsonElement entry : MythicLib.plugin.getJson().parse(sourceItem.getString("MMOITEMS_ARROW_POTION_EFFECTS"), JsonArray.class)) {
				if (!entry.isJsonObject())
					continue;

				JsonObject object = entry.getAsJsonObject();
				target.addPotionEffect(new PotionEffectData(PotionEffectType.getByName(object.get("type").getAsString()),
						object.get("duration").getAsDouble(), object.get("level").getAsInt()).toEffect());
			}
	}
}
