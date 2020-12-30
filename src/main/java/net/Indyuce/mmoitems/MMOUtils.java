package net.Indyuce.mmoitems;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.mmogroup.mmolib.MMOLib;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
public class MMOUtils {
	public static String getSkullTextureURL(ItemStack item) {
		try {
			ItemMeta meta = item.getItemMeta();
			Field profileField = meta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			Collection<Property> properties = ((GameProfile) profileField.get(item.getItemMeta())).getProperties().get("textures");
			Property property = properties.toArray(new Property[0])[0];
			return new String(Base64.decodeBase64(property.getValue())).replace("{textures:{SKIN:{url:\"", "").replace("\"}}}", "");
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Returns either the normalized vector, or null vector if input is null
	 * vector which cannot be normalized.
	 *
	 * @param vector Vector which can be of length 0
	 * @return Normalized vector or 0 depending on input
	 */
	public static Vector normalize(Vector vector) {
		return vector.getX() == 0 && vector.getY() == 0 ? vector : vector.normalize();
	}

	/**
	 * Double.parseDouble(String) cannot be used when asking for player input in
	 * stat edition because the exception message is confusing. This method has
	 * a better exception message
	 *
	 * @param format Format to parse into a number
	 * @return Parsed double
	 */
	public static double parseDouble(String format) {
		try {
			return Double.parseDouble(format);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Could not read number from '" + format + "'");
		}
	}

	public static String getProgressBar(double ratio, int n, String barChar) {
		StringBuilder bar = new StringBuilder();
		for (int k = 0; k < n; k++)
			bar.append(barChar);
		return bar.substring(0, (int) (ratio * n)) + ChatColor.WHITE + bar.substring((int) (ratio * n));
	}

	public static LivingEntity getDamager(EntityDamageByEntityEvent event) {
		/*
		 * check direct damager
		 */
		if (event.getDamager() instanceof LivingEntity) return (LivingEntity) event.getDamager();

		/*
		 * checks projectile and add damage type, which supports every vanilla
		 * projectile like snowballs, tridents and arrows
		 */
		if (event.getDamager() instanceof Projectile) {
			Projectile proj = (Projectile) event.getDamager();
			if (proj.getShooter() instanceof LivingEntity) return (LivingEntity) proj.getShooter();
		}

		/*
		 * check for last damage
		 */
		// if (event.getEntity().getLastDamageCause() instanceof
		// EntityDamageByEntityEvent && checkLastDamageCause)
		// return getDamager(result, (EntityDamageByEntityEvent)
		// event.getEntity().getLastDamageCause(), false);

		return null;
	}

	/**
	 * The last 5 seconds of nausea are useless, night vision flashes in the
	 * last 10 seconds, blindness takes a few seconds to decay as well, and
	 * there can be small server lags. It's best to apply a specific duration
	 * for every type of permanent effect.
	 *
	 * @param type Potion effect type
	 * @return The duration that MMOItems should be using to give player
	 * "permanent" potion effects, depending on the potion effect type
	 */
	public static int getEffectDuration(PotionEffectType type) {
		return type.equals(PotionEffectType.NIGHT_VISION) || type.equals(PotionEffectType.CONFUSION) ? 260 : type.equals(PotionEffectType.BLINDNESS) ? 140 : 80;
	}

	public static String getDisplayName(ItemStack item) {
		return item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : caseOnWords(item.getType().name().toLowerCase().replace("_", " "));
	}

	public static boolean twoHandedCase(Player player) {
		int normal = 0, twoHanded = 0;

		for (ItemStack item : new ItemStack[]{player.getInventory().getItemInMainHand(), player.getInventory().getItemInOffHand()})
			if (item.getType() != Material.AIR) {
				normal++;
				if (MMOLib.plugin.getVersion().getWrapper().getNBTItem(item).getBoolean("MMOITEMS_TWO_HANDED"))
					twoHanded++;
			}

		return twoHanded > 0 && normal > 1;
	}

	public static String caseOnWords(String s) {
		StringBuilder builder = new StringBuilder(s);
		boolean isLastSpace = true;
		for (int i = 0; i < builder.length(); i++) {
			char ch = builder.charAt(i);
			if (isLastSpace && ch >= 'a' && ch <= 'z') {
				builder.setCharAt(i, (char) (ch + ('A' - 'a')));
				isLastSpace = false;
			} else isLastSpace = ch == ' ';
		}
		return builder.toString();
	}

	/**
	 * @param item The item to check
	 * @param lore Whether or not MI should check for an item lore
	 * @return If the item is not null, has an itemMeta and has a display name.
	 * If 'lore' is true, also checks if the itemMeta has a lore.
	 */
	public static boolean isMetaItem(ItemStack item, boolean lore) {
		return item != null && item.getType() != Material.AIR && item.getItemMeta() != null && item.getItemMeta().getDisplayName() != null && (!lore || item.getItemMeta().getLore() != null);
	}

	public static void saturate(Player player, double saturation) {
		if (saturation > 0) player.setSaturation(Math.min(20, player.getSaturation() + (float) saturation));
	}

	public static void feed(Player player, int feed) {
		if (feed > 0) player.setFoodLevel(Math.min(20, player.getFoodLevel() + feed));
	}

	public static void heal(LivingEntity player, double heal) {
		if (heal > 0)
			player.setHealth(Math.min(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), player.getHealth() + heal));
	}

	public static boolean canDamage(Player player, Entity target) {
		return canDamage(player, null, target);
	}

	public static boolean canDamage(Entity target) {
		return canDamage(null, null, target);
	}

	/**
	 * @param player Player hitting the entity which can be null
	 * @param loc    If the given location is not null, this method checks if this
	 *               location is inside the bounding box
	 * @param target The entity being hit
	 * @return If the entity can be damaged, by a specific player, at a specific
	 * spot
	 */
	public static boolean canDamage(@Nullable Player player, @Nullable Location loc, Entity target) {

		/*
		 * Cannot hit himself or non-living entities. Careful, some entities are
		 * weirdly considered as livingEntities like the armor stand. Also check
		 * if the entity is dead since a dying entity (dying effect takes some
		 * time) can still be targeted but we dont want that
		 */
		if (target.equals(player) || target.isDead() || !(target instanceof LivingEntity) || target instanceof ArmorStand)
			return false;

		/*
		 * Extra plugin compatibility, everything is handled via MMOLib because
		 * the same system is used by MMOCore
		 */
		if (MMOLib.plugin.getEntities().findCustom(target)) return false;

		/*
		 * The ability player damage option is cached for quicker access in the
		 * config manager instance since it is used in runnables
		 */
		if (target instanceof Player && (!MMOItems.plugin.getLanguage().abilityPlayerDamage || !MMOItems.plugin.getFlags().isPvpAllowed(target.getLocation())))
			return false;

		return loc == null || MMOLib.plugin.getVersion().getWrapper().isInBoundingBox(target, loc);
	}

	private static final String[] romanChars = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
	private static final int[] romanValues = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};

	public static String intToRoman(int input) {
		if (input < 1 || input > 3999) throw new IllegalArgumentException("Input must be between 1 and 3999");

		StringBuilder format = new StringBuilder();

		for (int i = 0; i < romanChars.length; i++)
			while (input >= romanValues[i]) {
				format.append(romanChars[i]);
				input -= romanValues[i];
			}

		return format.toString();
	}

	public static double truncation(double x, int n) {
		double pow = Math.pow(10.0, n);
		return Math.floor(x * pow) / pow;
	}

	public static Vector rotAxisX(Vector v, double a) {
		double y = v.getY() * Math.cos(a) - v.getZ() * Math.sin(a);
		double z = v.getY() * Math.sin(a) + v.getZ() * Math.cos(a);
		return v.setY(y).setZ(z);
	}

	public static Vector rotAxisY(Vector v, double b) {
		double x = v.getX() * Math.cos(b) + v.getZ() * Math.sin(b);
		double z = v.getX() * -Math.sin(b) + v.getZ() * Math.cos(b);
		return v.setX(x).setZ(z);
	}

	public static Vector rotAxisZ(Vector v, double c) {
		double x = v.getX() * Math.cos(c) - v.getY() * Math.sin(c);
		double y = v.getX() * Math.sin(c) + v.getY() * Math.cos(c);
		return v.setX(x).setY(y);
	}

	public static Vector rotateFunc(Vector v, Location loc) {
		double yaw = loc.getYaw() / 180 * Math.PI;
		double pitch = loc.getPitch() / 180 * Math.PI;
		v = rotAxisX(v, pitch);
		v = rotAxisY(v, -yaw);
		return v;
	}

	/**
	 * @param loc Where we are looking for nearby entities
	 * @return List of all entities surrounding a location. This method loops
	 * through the 9 surrounding chunks and collect all entities from
	 * them. This list can be cached and used multiple times in the same
	 * tick for projectile based spells which need to run entity
	 * checkups
	 */
	public static List<Entity> getNearbyChunkEntities(Location loc) {
		List<Entity> entities = new ArrayList<>();

		int cx = loc.getChunk().getX();
		int cz = loc.getChunk().getZ();

		for (int x = -1; x < 2; x++)
			for (int z = -1; z < 2; z++)
				entities.addAll(Arrays.asList(loc.getWorld().getChunkAt(cx + x, cz + z).getEntities()));

		return entities;
	}

	public static ItemStack readIcon(String string) throws IllegalArgumentException {
		String[] split = string.split(":");
		Material material = Material.valueOf(split[0].toUpperCase().replace("-", "_").replace(" ", "_"));
		return split.length > 1 ? MMOLib.plugin.getVersion().getWrapper().textureItem(material, Integer.parseInt(split[1])) : new ItemStack(material);
	}
}
