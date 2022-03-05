package net.Indyuce.mmoitems.api.interaction.weapon.untargeted;

import com.google.gson.JsonObject;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.api.interaction.util.UntargetedDurabilityItem;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import net.Indyuce.mmoitems.api.util.SoundReader;
import net.Indyuce.mmoitems.stat.LuteAttackEffectStat.LuteAttackEffect;
import net.Indyuce.mmoitems.stat.data.ProjectileParticlesData;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class Lute extends UntargetedWeapon {
	public Lute(Player player, NBTItem item) {
		super(player, item, UntargetedWeaponType.RIGHT_CLICK);
	}

	@Override
	public boolean canAttack(EquipmentSlot slot) {
		return true;
	}

	@Override
	public void applyAttackEffect(PlayerMetadata stats, EquipmentSlot slot) {
		double attackDamage = getValue(stats.getStat("ATTACK_DAMAGE"), 7);
		double range = getValue(getNBTItem().getStat(ItemStats.RANGE.getId()), MMOItems.plugin.getConfig().getDouble("default.range"));
		Vector weight = new Vector(0, -.003 * getNBTItem().getStat(ItemStats.NOTE_WEIGHT.getId()), 0);

		// Attack meta
		ItemAttackMetadata attackMeta = new ItemAttackMetadata(new DamageMetadata(attackDamage, DamageType.WEAPON, DamageType.MAGIC, DamageType.PROJECTILE), stats);

		LuteAttackEffect effect = LuteAttackEffect.get(getNBTItem());
		SoundReader sound = new SoundReader(getNBTItem().getString("MMOITEMS_LUTE_ATTACK_SOUND"), VersionSound.BLOCK_NOTE_BLOCK_BELL.toSound());
		if (effect != null) {
			effect.getAttack().handle(attackMeta, getNBTItem(), range, weight, sound);
			return;
		}

		new BukkitRunnable() {
			final Vector vec = getPlayer().getEyeLocation().getDirection().multiply(.4);
			final Location loc = getPlayer().getEyeLocation();
			int ti = 0;

			public void run() {
				if (ti++ > range)
					cancel();
				// If the item has projectile particle attribute, use selected particle
				if (getNBTItem().hasTag("MMOITEMS_PROJECTILE_PARTICLES")) {
					JsonObject obj = MythicLib.plugin.getJson().parse(getNBTItem().getString("MMOITEMS_PROJECTILE_PARTICLES"), JsonObject.class);
					Particle particle = Particle.valueOf(obj.get("Particle").getAsString());
					// If the selected particle is colored, use the provided color
					if (ProjectileParticlesData.isColorable(particle)) {
						double red = Double.parseDouble(String.valueOf(obj.get("Red")));
						double green = Double.parseDouble(String.valueOf(obj.get("Green")));
						double blue = Double.parseDouble(String.valueOf(obj.get("Blue")));
						ProjectileParticlesData.shootParticle(player, particle, loc, red, green, blue);
						// If it's not colored, just shoot the particle
					} else {
						ProjectileParticlesData.shootParticle(player, particle, loc, 0, 0, 0);
					}
					// If no particle has been provided via projectile particle attribute, default to this particle
				} else {
					loc.getWorld().spawnParticle(Particle.NOTE, loc, 0, 1, 0, 0, 1);
				}

				// play the sound
				sound.play(loc, 2, (float) (.5 + (double) ti / range));

				// damage entities
				List<Entity> entities = MMOUtils.getNearbyChunkEntities(loc);
				for (int j = 0; j < 3; j++) {
					loc.add(vec.add(weight));
					if (loc.getBlock().getType().isSolid()) {
						cancel();
						break;
					}

					for (Entity target : entities)
						if (MMOUtils.canTarget(getPlayer(), loc, target, InteractionType.OFFENSE_ACTION)) {
							attackMeta.applyEffectsAndDamage(getNBTItem(), (LivingEntity) target);
							cancel();
							return;
						}
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
