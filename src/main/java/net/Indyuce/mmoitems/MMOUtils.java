package net.Indyuce.mmoitems;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.version.VersionMaterial;

public class MMOUtils {
	public static String getSkullTextureURL(ItemStack item) {
		try {
			ItemMeta meta = item.getItemMeta();
			Field profileField = meta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			Collection<Property> properties = ((GameProfile) profileField.get(item.getItemMeta())).getProperties().get("textures");
			Property property = properties.toArray(new Property[properties.size()])[0];
			return new String(Base64.decodeBase64(property.getValue())).replace("{textures:{SKIN:{url:\"", "").replace("\"}}}", "");
		} catch (Exception e) {
			return "";
		}
	}

	/*
	 * used by many plugin abilities and mecanisms. sometimes vector cannot be
	 * normalized because its length is equal to 0 (normalizing a vector just
	 * divides the vector coordinates by its length), return vec if null length
	 */
	public static Vector normalize(Vector vector) {
		return vector.getX() == 0 && vector.getY() == 0 ? vector : vector.normalize();
	}

	public static String getProgressBar(double ratio, int n, String barChar) {
		String bar = "";
		for (int k = 0; k < n; k++)
			bar += barChar;
		return bar.substring(0, (int) (ratio * n)) + ChatColor.WHITE + bar.substring((int) (ratio * n));
	}

	public static void giveOrDrop(Player player, ItemStack item) {
		for (ItemStack drop : player.getInventory().addItem(item).values())
			player.getWorld().dropItem(player.getLocation(), drop);
	}

//	public static PotionEffectType valueOfPotionEffectType(String effect) {
//		for (PotionEffectType checked : PotionEffectType.values())
//			if (checked.getName().equals(effect.toUpperCase().replace("-", "_")))
//				return checked;
//		return null;
//	}

	public static LivingEntity getDamager(EntityDamageByEntityEvent event) {

		/*
		 * check direct damager
		 */
		if (event.getDamager() instanceof LivingEntity)
			return (LivingEntity) event.getDamager();

		/*
		 * checks projectile and add damage type, which supports every vanilla
		 * projectile like snowballs, tridents and arrows
		 */
		if (event.getDamager() instanceof Projectile) {
			Projectile proj = (Projectile) event.getDamager();
			if (proj.getShooter() instanceof LivingEntity)
				return (LivingEntity) proj.getShooter();
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

	public static int getEffectDuration(PotionEffectType type) {

		// confusion takes a lot of time to decay
		// night vision flashes your screen for the last 10sec of effect
		if (type.equals(PotionEffectType.NIGHT_VISION) || type.equals(PotionEffectType.CONFUSION))
			return 260;

		// takes some time to decay
		if (type.equals(PotionEffectType.BLINDNESS))
			return 140;

		// otherwise 4sec is high enough to maintain the effect even when the
		// server laggs
		return 80;
	}

	public static String getDisplayName(ItemStack item) {
		if (!item.hasItemMeta())
			return MMOUtils.caseOnWords(item.getType().name().toLowerCase().replace("_", " "));
		return item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : MMOUtils.caseOnWords(item.getType().name().toLowerCase().replace("_", " "));
	}

	public static boolean twoHandedCase(Player player) {
		int normal = 0;
		int twoHanded = 0;
		for (ItemStack item : new ItemStack[] { player.getInventory().getItemInMainHand(), player.getInventory().getItemInOffHand() }) {
			if (item.getType() != Material.AIR)
				normal++;
			if (MMOLib.plugin.getNMS().getNBTItem(item).getBoolean("MMOITEMS_TWO_HANDED"))
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
			} else if (ch != ' ')
				isLastSpace = false;
			else
				isLastSpace = true;
		}
		return builder.toString();
	}

	public static boolean isMetaItem(ItemStack item, boolean lore) {
		return item != null && item.getType() != Material.AIR && item.getItemMeta() != null && item.getItemMeta().getDisplayName() != null && (!lore || item.getItemMeta().getLore() != null);
	}

	public static void saturate(Player player, double saturation) {
		if (saturation > 0)
			player.setSaturation(Math.min(20, player.getSaturation() + (float) saturation));
	}

	public static void feed(Player player, int feed) {
		if (feed > 0)
			player.setFoodLevel(Math.min(20, player.getFoodLevel() + feed));
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

	public static boolean canDamage(Player player, Location loc, Entity target) {

		/*
		 * cannot hit himself or non-living entities. careful, some entities are
		 * weirdly considered as livingEntities like the armor stand. also check
		 * if the entity is dead since a dying entity (dying effect takes some
		 * time) can still be targeted but we dont want that
		 */
		if (target.equals(player) || !(target instanceof LivingEntity) || target instanceof ArmorStand || target.isDead())
			return false;

		/*
		 * can spam your console - an error message is sent each time an NPC
		 * gets damaged since it is considered as a player.
		 */
		if (target.hasMetadata("NPC"))
			return false;

		/*
		 * the ability player damage option is cached for quicker access in the
		 * config manager instance since it is used in runnables
		 */
		if (target instanceof Player && (!MMOItems.plugin.getLanguage().abilityPlayerDamage || !MMOItems.plugin.getFlags().isPvpAllowed(target.getLocation())))
			return false;

		return loc == null ? true : MMOLib.plugin.getNMS().isInBoundingBox(target, loc);
	}

	public static String intToRoman(int input) {
		if (input < 1 || input > 499)
			return ">499";

		String s = "";
		while (input >= 400) {
			s += "CD";
			input -= 400;
		}
		while (input >= 100) {
			s += "C";
			input -= 100;
		}
		while (input >= 90) {
			s += "XC";
			input -= 90;
		}
		while (input >= 50) {
			s += "L";
			input -= 50;
		}
		while (input >= 40) {
			s += "XL";
			input -= 40;
		}
		while (input >= 10) {
			s += "X";
			input -= 10;
		}
		while (input >= 9) {
			s += "IX";
			input -= 9;
		}
		while (input >= 5) {
			s += "V";
			input -= 5;
		}
		while (input >= 4) {
			s += "IV";
			input -= 4;
		}
		while (input >= 1) {
			s += "I";
			input -= 1;
		}
		return s;
	}

	@Deprecated
	public static boolean areSimilar(ItemStack item1, ItemStack iitem2) {
		if (item1.getType() == VersionMaterial.PLAYER_HEAD.toMaterial() && iitem2.getType() == VersionMaterial.PLAYER_HEAD.toMaterial()) {
			ItemMeta meta1 = item1.getItemMeta();
			ItemMeta meta2 = iitem2.getItemMeta();

			if (meta1.hasDisplayName() && meta2.hasDisplayName())
				return meta1.getDisplayName().equalsIgnoreCase(meta2.getDisplayName());
		}

		return item1.isSimilar(iitem2);
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

	/*
	 * method to get all entities surrounding a location. this method does not
	 * take every entity in the world but rather takes all the entities from the
	 * 9 chunks around the entity, so even if the location is at the border of a
	 * chunk (worst case border of 4 chunks), the entity will still be included
	 */
	public static List<Entity> getNearbyChunkEntities(Location loc) {

		/*
		 * another method to save performance is if an entity bounding box
		 * calculation is made twice in the same tick then the method does not
		 * need to be called twice, it can utilize the same entity list since
		 * the entities have not moved (e.g fireball which does 2+ calculations
		 * per tick)
		 */
		List<Entity> entities = new ArrayList<>();

		int cx = loc.getChunk().getX();
		int cz = loc.getChunk().getZ();

		for (int x = -1; x < 2; x++)
			for (int z = -1; z < 2; z++)
				for (Entity entity : loc.getWorld().getChunkAt(cx + x, cz + z).getEntities())
					entities.add(entity);

		return entities;
	}
}
