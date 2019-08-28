package net.Indyuce.mmoitems.version.wrapper;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.util.MMORayTraceResult;
import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import net.minecraft.server.v1_12_R1.MovingObjectPosition;
import net.minecraft.server.v1_12_R1.Vec3D;

@SuppressWarnings("deprecation")
public class LegacyVersionWrapper implements VersionWrapper {

	@Override
	public void spawnParticle(Particle particle, Location loc, int amount, double x, double y, double z, double speed, float size, Color color) {
		loc.getWorld().spawnParticle(particle, loc, 0, (double) color.getRed() / 255, (double) color.getGreen() / 255, (double) color.getBlue() / 255, 0);
	}

	@Override
	public void spawnParticle(Particle particle, Location loc, int amount, double x, double y, double z, double speed, Material material) {
		loc.getWorld().spawnParticle(particle, loc, amount, x, y, z, 0, new MaterialData(material));
	}

	@Override
	public String getName(Enchantment enchant) {
		return enchant.getName();
	}

	@Override
	public FurnaceRecipe getFurnaceRecipe(String path, ItemStack item, Material material, float exp, int cook) {
		try {
			return (FurnaceRecipe) Class.forName("org.bukkit.inventory.FurnaceRecipe").getConstructor(ItemStack.class, Material.class, Integer.TYPE, Integer.TYPE).newInstance(item, material, 0, (int) exp);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException exception) {
			exception.printStackTrace();
			return null;
		}
	}

	@Override
	public MMORayTraceResult rayTrace(Player player, Vector direction, double range) {

		Location loc = player.getEyeLocation();
		Vec3D vec = new Vec3D(loc.getDirection().getX(), loc.getDirection().getY(), loc.getDirection().getZ());
		MovingObjectPosition block = ((CraftPlayer) player).getHandle().getBoundingBox().b(vec, new Vec3D(vec.x, vec.y, vec.z).add(range * vec.x, range * vec.y, range * vec.z));

		double d = block == null ? range : Math.sqrt(block.pos.distanceSquared(new Vec3D(loc.getX(), loc.getY(), loc.getZ())));
		Ray3D line = new Ray3D(player.getEyeLocation());
		for (Entity entity : player.getNearbyEntities(d, d, d))
			if (line.intersectsRay(((CraftEntity) entity).getHandle().getBoundingBox()) && MMOUtils.canDamage(player, entity))
				return new MMORayTraceResult((LivingEntity) entity, range);

		return new MMORayTraceResult(null, range);
	}

	public class Ray3D extends Vec3D {
		public final Vec3D dir;

		/*
		 * warning, direction is not normalized
		 */
		public Ray3D(Vec3D origin, Vec3D direction) {
			super(origin.x, origin.y, origin.z);
			dir = direction;
		}

		/**
		 * Construct a 3D ray from a location.
		 * 
		 * @param loc
		 *            - the Bukkit location.
		 */
		public Ray3D(Location loc) {
			this(new Vec3D(loc.getX(), loc.getY(), loc.getZ()), new Vec3D(loc.getDirection().getX(), loc.getDirection().getY(), loc.getDirection().getZ()));
		}

		public Vec3D getDirection() {
			return dir;
		}

		public String toString() {
			return "origin: " + super.toString() + " dir: " + dir;
		}

		/**
		 * Calculates intersection with the given ray between a certain distance
		 * interval.
		 * <p>
		 * Ray-box intersection is using IEEE numerical properties to ensure the
		 * test is both robust and efficient, as described in: <br>
		 * <code>Amy Williams, Steve Barrus, R. Keith Morley, and Peter Shirley: "An
		 * Efficient and Robust Ray-Box Intersection Algorithm" Journal of graphics
		 * tools, 10(1):49-54, 2005</code>
		 * 
		 * @param ray
		 *            incident ray
		 * @param minDist
		 * @param maxDist
		 * @return intersection point on the bounding box (only the first is
		 *         returned) or null if no intersection
		 */
		public boolean intersectsRay(AxisAlignedBB box) {
			Vec3D invDir = new Vec3D(1f / dir.x, 1f / dir.y, 1f / dir.z);

			Vec3D min = new Vec3D(box.a, box.b, box.c);
			Vec3D max = new Vec3D(box.d, box.e, box.f);

			boolean signDirX = invDir.x < 0;
			boolean signDirY = invDir.y < 0;
			boolean signDirZ = invDir.z < 0;

			Vec3D bbox = signDirX ? max : min;
			double tmin = (bbox.x - x) * invDir.x;
			bbox = signDirX ? min : max;
			double tmax = (bbox.x - x) * invDir.x;
			bbox = signDirY ? max : min;
			double tymin = (bbox.y - y) * invDir.y;
			bbox = signDirY ? min : max;
			double tymax = (bbox.y - y) * invDir.y;

			if ((tmin > tymax) || (tymin > tmax)) {
				return false;
			}
			if (tymin > tmin) {
				tmin = tymin;
			}
			if (tymax < tmax) {
				tmax = tymax;
			}

			bbox = signDirZ ? max : min;
			double tzmin = (bbox.z - z) * invDir.z;
			bbox = signDirZ ? min : max;
			double tzmax = (bbox.z - z) * invDir.z;

			if ((tmin > tzmax) || (tzmin > tmax)) {
				return false;
			}
			if (tzmin > tmin) {
				tmin = tzmin;
			}
			if (tzmax < tmax) {
				tmax = tzmax;
			}
			return true;
		}
	}
}
