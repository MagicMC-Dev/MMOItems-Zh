package net.Indyuce.mmoitems.version.wrapper;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.util.MMORayTraceResult;

public class DefaultVersionWrapper implements VersionWrapper {

	@Override
	public void spawnParticle(Particle particle, Location loc, int amount, double x, double y, double z, double speed, float size, Color color) {
		loc.getWorld().spawnParticle(particle, loc, amount, x, y, z, speed, new Particle.DustOptions(color, size));
	}

	@Override
	public void spawnParticle(Particle particle, Location loc, int amount, double x, double y, double z, double speed, Material material) {
		loc.getWorld().spawnParticle(particle, loc, amount, x, y, z, 0, material.createBlockData());
	}

	@Override
	public String getName(Enchantment enchant) {
		return enchant.getKey().getKey();
	}

	@Override
	public FurnaceRecipe getFurnaceRecipe(String path, ItemStack item, Material material, float exp, int cook) {
		return new FurnaceRecipe(new NamespacedKey(MMOItems.plugin, "mmoitems_furnace_" + path), item, material, exp, cook);
	}

	@Override
	public MMORayTraceResult rayTrace(Player player, Vector direction, double range) {
		RayTraceResult hit = player.getWorld().rayTraceEntities(player.getEyeLocation(), direction, range, (entity) -> MMOUtils.canDamage(player, entity));
		return new MMORayTraceResult(hit != null ? (LivingEntity) hit.getHitEntity() : null, hit != null ? hit.getHitPosition().distance(player.getEyeLocation().toVector()) : range);
	}
}
