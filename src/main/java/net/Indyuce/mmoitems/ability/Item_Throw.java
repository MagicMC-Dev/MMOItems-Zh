package net.Indyuce.mmoitems.ability;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.AttackResult;
import io.lumine.mythic.lib.api.DamageType;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.VectorAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.api.util.NoClipItem;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class Item_Throw extends Ability implements Listener {
	public Item_Throw() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 6);
		addModifier("force", 1);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		return new VectorAbilityResult(ability, stats.getPlayer(), target);
	}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {
		ItemStack itemStack = stats.getPlayer().getInventory().getItemInMainHand().clone();
		NBTItem nbtItem = NBTItem.get(itemStack);
		if (itemStack.getType() == Material.AIR || !nbtItem.hasType()) {
			result.setSuccessful(false);
			return;
		}
		boolean hasAbility = false;

		for (JsonElement entry : MythicLib.plugin.getJson().parse(nbtItem.getString("MMOITEMS_ABILITY"), JsonArray.class)) {
			if (!entry.isJsonObject())
				continue;

			JsonObject object = entry.getAsJsonObject();
			if (object.get("Id").getAsString().equalsIgnoreCase(getID())) {
				hasAbility = true;
				break;
			}
		}

		if (!hasAbility)
			return;

		final NoClipItem item = new NoClipItem(stats.getPlayer().getLocation().add(0, 1.2, 0), itemStack);
		item.getEntity().setVelocity(((VectorAbilityResult) ability).getTarget().multiply(1.5 * ability.getModifier("force")));
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1, 0);
		new BukkitRunnable() {
			double ti = 0;

			public void run() {
				ti++;
				if (ti > 20 || item.getEntity().isDead()) {
					item.close();
					cancel();
				}

				item.getEntity().getWorld().spawnParticle(Particle.CRIT, item.getEntity().getLocation(), 0);
				for (Entity target : item.getEntity().getNearbyEntities(1, 1, 1))
					if (MMOUtils.canDamage(stats.getPlayer(), target)) {
						new AttackResult(ability.getModifier("damage"), DamageType.SKILL, DamageType.PHYSICAL, DamageType.PROJECTILE).damage(stats.getPlayer(), (LivingEntity) target);
						item.close();
						cancel();
					}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
