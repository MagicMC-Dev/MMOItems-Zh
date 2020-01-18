package net.Indyuce.mmoitems.api.interaction.weapon.untargeted;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import net.Indyuce.mmoitems.api.interaction.util.InteractItem;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.api.util.SoundReader;
import net.Indyuce.mmoitems.stat.Lute_Attack_Effect.LuteAttackEffect;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionSound;

public class Lute extends UntargetedWeapon {
	public Lute(Player player, NBTItem item, Type type) {
		super(player, item, type, WeaponType.RIGHT_CLICK);
	}

	@Override
	public void untargetedAttack(EquipmentSlot slot) {

		CachedStats stats = getPlayerData().getStats().newTemporary();
		double attackSpeed = 1 / getValue(stats.getStat(ItemStat.ATTACK_SPEED), MMOItems.plugin.getConfig().getDouble("default.attack-speed"));
		if (!hasEnoughResources(attackSpeed, CooldownType.ATTACK, false))
			return;

		DurabilityItem durItem = new DurabilityItem(getPlayer(), getNBTItem());
		if (durItem.isValid())
			new InteractItem(getPlayer(), slot).setItem(durItem.decreaseDurability(1).toItem());

		double attackDamage = getValue(stats.getStat(ItemStat.ATTACK_DAMAGE), 1);
		double range = getValue(getNBTItem().getStat(ItemStat.RANGE), MMOItems.plugin.getConfig().getDouble("default.range"));
		Vector weight = new Vector(0, -.003 * getNBTItem().getStat(ItemStat.NOTE_WEIGHT), 0);

		LuteAttackEffect effect = LuteAttackEffect.get(getNBTItem());
		Sound sound = new SoundReader(getNBTItem().getString("MMOITEMS_LUTE_ATTACK_SOUND"), VersionSound.BLOCK_NOTE_BLOCK_BELL.toSound()).getSound();
		if (effect != null) {
			effect.getAttack().handle(stats, getNBTItem(), attackDamage, range, weight, sound);
			return;
		}

		new BukkitRunnable() {
			Vector vec = getPlayer().getEyeLocation().getDirection().multiply(.4);
			Location loc = getPlayer().getEyeLocation();
			int ti = 0;

			public void run() {
				if (ti++ > range)
					cancel();

				List<Entity> entities = MMOUtils.getNearbyChunkEntities(loc);
				loc.getWorld().spawnParticle(Particle.NOTE, loc, 0);
				loc.getWorld().playSound(loc, sound, 2, (float) (.5 + (double) ti / range));
				for (int j = 0; j < 3; j++) {
					loc.add(vec.add(weight));
					if (loc.getBlock().getType().isSolid()) {
						cancel();
						break;
					}

					for (Entity target : entities)
						if (MMOUtils.canDamage(getPlayer(), loc, target)) {
							new ItemAttackResult(attackDamage, DamageType.WEAPON, DamageType.PROJECTILE, DamageType.MAGIC).applyEffectsAndDamage(stats, getNBTItem(), (LivingEntity) target);
							cancel();
							return;
						}
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
