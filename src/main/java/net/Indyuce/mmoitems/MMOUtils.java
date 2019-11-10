package net.Indyuce.mmoitems;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class MMOUtils {
	private static final Random random = new Random();

	public static String getSkullTextureURL(ItemStack i) {
		try {
			ItemMeta meta = i.getItemMeta();
			Field profileField = meta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			Collection<Property> properties = ((GameProfile) profileField.get(i.getItemMeta())).getProperties().get("textures");
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

	public static boolean isUndead(Entity entity) {
		return entity instanceof Zombie || entity instanceof Skeleton || entity instanceof Wither;
	}

	public static void giveOrDrop(Player player, ItemStack item) {
		for (ItemStack drop : player.getInventory().addItem(item).values())
			player.getWorld().dropItem(player.getLocation(), drop);
	}

	// random offset between -a and a
	public static double rdm(double a) {
		return (random.nextDouble() - .5) * 2 * a;
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

	public static String getDisplayName(ItemStack i) {
		if (!i.hasItemMeta())
			return MMOUtils.caseOnWords(i.getType().name().toLowerCase().replace("_", " "));
		return i.getItemMeta().hasDisplayName() ? i.getItemMeta().getDisplayName() : MMOUtils.caseOnWords(i.getType().name().toLowerCase().replace("_", " "));
	}

	public static Integer[] getSocketSlots(List<String> lore) {
		List<Integer> list = new ArrayList<Integer>();
		for (int j = 0; j < lore.size(); j++)
			if (lore.get(j).equals(ItemStat.translate("empty-gem-socket").replace("#d", AltChar.diamond)))
				list.add(j);
		return list.toArray(new Integer[list.size()]);
	}

	public static boolean twoHandedCase(Player player) {
		int normal = 0;
		int twoHanded = 0;
		for (ItemStack item : new ItemStack[] { player.getInventory().getItemInMainHand(), player.getInventory().getItemInOffHand() }) {
			if (item.getType() != Material.AIR)
				normal++;
			if (MMOItems.plugin.getNMS().getNBTItem(item).getBoolean("MMOITEMS_TWO_HANDED"))
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

	public static boolean isPluginItem(ItemStack item, boolean lore) {
		return item != null && item.getType() != Material.AIR && item.getItemMeta() != null && item.getItemMeta().getDisplayName() != null && (!lore || item.getItemMeta().getLore() != null);
	}

	public static boolean isType(String s) {
		for (Type type : MMOItems.plugin.getTypes().getAll())
			if (type.getId().equalsIgnoreCase(s.replace("-", "_")))
				return true;
		return false;
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

		return loc == null ? true : MMOItems.plugin.getNMS().isInBoundingBox(target, loc);
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
