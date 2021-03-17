package net.Indyuce.mmoitems.api.interaction.weapon.untargeted;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.interaction.util.UntargetedDurabilityItem;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.api.util.SoundReader;
import net.Indyuce.mmoitems.listener.ItemUse;
import net.Indyuce.mmoitems.stat.LuteAttackEffectStat.LuteAttackEffect;
import io.lumine.mythic.lib.api.DamageType;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.VersionSound;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class Lute extends UntargetedWeapon {
	public Lute(Player player, NBTItem item) {
		super(player, item, WeaponType.RIGHT_CLICK);
	}

	@Override
	public void untargetedAttack(EquipmentSlot slot) {

		if (!ItemUse.eitherHandSuccess(getPlayer(), getNBTItem(), slot))
			return;
		CachedStats stats = getPlayerData().getStats().newTemporary();
		double attackSpeed = 1 / getValue(stats.getStat(ItemStats.ATTACK_SPEED), MMOItems.plugin.getConfig().getDouble("default.attack-speed"));
		if (!applyWeaponCosts(attackSpeed, CooldownType.ATTACK))
			return;

		UntargetedDurabilityItem durItem = new UntargetedDurabilityItem(getPlayer(), getNBTItem(), slot);
		if (durItem.isBroken())
			return;

		if (durItem.isValid())
			durItem.decreaseDurability(1).update();

		double attackDamage = getValue(stats.getStat(ItemStats.ATTACK_DAMAGE), 1);
		double range = getValue(getNBTItem().getStat(ItemStats.RANGE.getId()), MMOItems.plugin.getConfig().getDouble("default.range"));
		Vector weight = new Vector(0, -.003 * getNBTItem().getStat(ItemStats.NOTE_WEIGHT.getId()), 0);

		LuteAttackEffect effect = LuteAttackEffect.get(getNBTItem());
		SoundReader sound = new SoundReader(getNBTItem().getString("MMOITEMS_LUTE_ATTACK_SOUND"), VersionSound.BLOCK_NOTE_BLOCK_BELL.toSound());
		if (effect != null) {
			effect.getAttack().handle(stats, getNBTItem(), attackDamage, range, weight, sound);
			return;
		}

		new BukkitRunnable() {
			final Vector vec = getPlayer().getEyeLocation().getDirection().multiply(.4);
			final Location loc = getPlayer().getEyeLocation();
			int ti = 0;

			public void run() {
				if (ti++ > range)
					cancel();

				List<Entity> entities = MMOUtils.getNearbyChunkEntities(loc);
				loc.getWorld().spawnParticle(Particle.NOTE, loc, 0);
				sound.play(loc, 2, (float) (.5 + (double) ti / range));
				for (int j = 0; j < 3; j++) {
					loc.add(vec.add(weight));
					if (loc.getBlock().getType().isSolid()) {
						cancel();
						break;
					}

					for (Entity target : entities)
						if (MMOUtils.canDamage(getPlayer(), loc, target)) {
							new ItemAttackResult(attackDamage, DamageType.WEAPON, DamageType.PROJECTILE, DamageType.MAGIC)
									.applyEffectsAndDamage(stats, getNBTItem(), (LivingEntity) target);
							cancel();
							return;
						}
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
