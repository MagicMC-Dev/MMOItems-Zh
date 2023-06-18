package net.Indyuce.mmoitems.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

@SuppressWarnings("unused")
public class MMOUtils {
    public MMOUtils() {
        throw new IllegalArgumentException("This class cannot be instantiated.");
    }

    public static boolean isColorable(@NotNull Particle particle) {
        return particle.getDataType() == Particle.DustOptions.class;
    }

    /**
     * Should cancel interaction if one of the two cases:
     * - the item type no longer exists
     * - no template with the given (type, id) pair can be found
     *
     * @param item Target item
     * @return If the item USED to exist, but no longer does
     */
    public static boolean hasBeenRemoved(@NotNull NBTItem item) {
        if (!item.hasType()) return false;

        final @Nullable String type = item.getType();
        return MMOUtils.isNonEmpty(type) && (!Type.isValid(type) || !MMOItems.plugin.getTemplates().hasTemplate(Type.get(type), item.getString("MMOITEMS_ITEM_ID")));
    }

    public static boolean isNonEmpty(@Nullable String str) {
        return str != null && !str.isEmpty();
    }

    /**
     * @return The skull texture URL from a given player head
     */
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

    private static final String UNIVERSAL_REFERENCE = "all";

    /**
     * References are helpful to classify items that can interact together.
     * They are a piece of text stored as an NBTTag for instance. Items can
     * interact (in a certain way) only if the corresponding reference match.
     * <p>
     * Any item can interact with an item with the universal reference 'all'.
     * A null/empty reference is considered like the universal reference.
     * <p>
     * This is a simple symmetrical computation. Used for:
     * - for item upgrading TODO
     * - item repairing
     *
     * @param ref1      First reference
     * @param ref2      Second reference
     * @return If items can interact
     */
    public static boolean checkReference(@Nullable String ref1, @Nullable String ref2) {
        return ref1 == null || ref1.isEmpty() || ref2 == null || ref2.isEmpty() || ref1.equals(UNIVERSAL_REFERENCE) || ref2.equals(UNIVERSAL_REFERENCE) || ref1.equals(ref2);
    }

    /**
     * Source: https://gist.github.com/Mystiflow/c42f45bac9916c84e381155f72a96d84
     */
    private static final Map<ChatColor, Color> COLOR_MAPPINGS = ImmutableMap.<ChatColor, Color>builder()
            .put(ChatColor.BLACK, Color.fromRGB(0, 0, 0))
            .put(ChatColor.DARK_BLUE, Color.fromRGB(0, 0, 170))
            .put(ChatColor.DARK_GREEN, Color.fromRGB(0, 170, 0))
            .put(ChatColor.DARK_AQUA, Color.fromRGB(0, 170, 170))
            .put(ChatColor.DARK_RED, Color.fromRGB(170, 0, 0))
            .put(ChatColor.DARK_PURPLE, Color.fromRGB(170, 0, 170))
            .put(ChatColor.GOLD, Color.fromRGB(255, 170, 0))
            .put(ChatColor.GRAY, Color.fromRGB(170, 170, 170))
            .put(ChatColor.DARK_GRAY, Color.fromRGB(85, 85, 85))
            .put(ChatColor.BLUE, Color.fromRGB(85, 85, 255))
            .put(ChatColor.GREEN, Color.fromRGB(85, 255, 85))
            .put(ChatColor.AQUA, Color.fromRGB(85, 255, 255))
            .put(ChatColor.RED, Color.fromRGB(255, 85, 85))
            .put(ChatColor.LIGHT_PURPLE, Color.fromRGB(255, 85, 255))
            .put(ChatColor.YELLOW, Color.fromRGB(255, 255, 85))
            .put(ChatColor.WHITE, Color.fromRGB(255, 255, 255))
            .build();

    @NotNull
    public static Color toRGB(ChatColor color) {
        return Objects.requireNonNull(COLOR_MAPPINGS.get(color), "Not a color");
    }

    public static int getPickaxePower(Player player) {
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR)
            return 0;

        final NBTItem nbt = NBTItem.get(item);
        if (nbt.hasTag("MMOITEMS_PICKAXE_POWER"))
            return nbt.getInteger("MMOITEMS_PICKAXE_POWER");

        switch (item.getType().name()) {
            case "WOODEN_PICKAXE":
            case "WOOD_PICKAXE":
                return 5;
            case "STONE_PICKAXE":
                return 10;
            case "GOLDEN_PICKAXE":
            case "GOLD_PICKAXE":
                return 15;
            case "IRON_PICKAXE":
                return 20;
            case "DIAMOND_PICKAXE":
                return 25;
            case "NETHERITE_PICKAXE":
                return 30;
            default:
                return 0;
        }
    }

    /**
     * @param name The trigger name that may be in old format
     * @return The trigger type this represents
     * @throws IllegalArgumentException If this does not match any trigger type
     */
    @NotNull
    public static TriggerType backwardsCompatibleTriggerType(@NotNull String name) throws IllegalArgumentException {
        if (name == null)
            throw new IllegalArgumentException("Trigger cannot be null");

        switch (name) {
            case "ON_HIT":
                return TriggerType.ATTACK;
            case "WHEN_HIT":
                return TriggerType.DAMAGED;
            default:
                return TriggerType.valueOf(name);
        }
    }

    /**
     * @param item The item stack you are testing.
     * @param type MMOItem Type you are expecting {@link Type#getId()}
     * @param id   MMOItem ID you are expecting
     * @return If the given item is the desired MMOItem
     */
    public static boolean isMMOItem(@Nullable ItemStack item, @NotNull String type, @NotNull String id) {
        if (item == null)
            return false;

        // Make it into an NBT Item
        NBTItem asNBT = NBTItem.get(item);

        // ID Matches?
        String itemID = getID(asNBT);

        // Not a MMOItem
        if (itemID == null)
            return false;

        // ID matches?
        if (!itemID.equals(id))
            return false;

        // If the type matches too, we are set.
        return asNBT.getType().equals(type);
    }

    /**
     * @param nbtItem The NBTItem you are testing
     * @return The MMOItem Type of this item, if it is a MMOItem
     */
    @Nullable
    public static Type getType(@Nullable NBTItem nbtItem) {
        if (nbtItem == null || !nbtItem.hasType())
            return null;

        // Try that one instead
        return MMOItems.plugin.getTypes().get(nbtItem.getType());
    }

    /**
     * @param nbtItem The NBTItem you are testing
     * @return The MMOItem ID of this item, if it is a MMOItem
     */
    @Nullable
    public static String getID(@Nullable NBTItem nbtItem) {
        if (nbtItem == null || !nbtItem.hasType())
            return null;

        ItemTag type = ItemTag.getTagAtPath("MMOITEMS_ITEM_ID", nbtItem, SupportedNBTTagValues.STRING);
        if (type == null)
            return null;

        return (String) type.getValue();
    }

    /**
     * * Returns either the normalized vector, or null vector if input is null
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

    /**
     * Returns an UUID from thay string, or null if it is not in UUID format.
     */
    @Nullable
    public static UUID UUIDFromString(@org.jetbrains.annotations.Nullable String anything) {
        if (anything == null)
            return null;

        // Correct Format?
        if (anything.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"))

            // Return thay
            return UUID.fromString(anything);

        // No
        return null;
    }

    public static LivingEntity getDamager(EntityDamageByEntityEvent event) {

        // Check direct damager
        if (event.getDamager() instanceof LivingEntity) return (LivingEntity) event.getDamager();

        /*
         * Checks projectile and add damage type, which supports every vanilla
         * projectile like snowballs, tridents and arrows
         */
        if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();
            if (proj.getShooter() instanceof LivingEntity)
                return (LivingEntity) proj.getShooter();
        }

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
     *         "permanent" potion effects, depending on the potion effect type
     */
    public static int getEffectDuration(PotionEffectType type) {
        return type.equals(PotionEffectType.NIGHT_VISION) || type.equals(PotionEffectType.CONFUSION) ? 260 : type.equals(PotionEffectType.BLINDNESS) ? 140 : 80;
    }

    @NotNull
    public static String getDisplayName(@Nullable ItemStack item) {
        if (item == null) {
            return "null";
        }
        return (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) ?
                item.getItemMeta().getDisplayName() :
                caseOnWords(item.getType().name().toLowerCase().replace("_", " "));
    }

    /**
     * Super useful to display enum names like DIAMOND_SWORD in chat
     *
     * @param s String with lower cases and spaces only
     * @return Same string with capital letters at the beginning of each word.
     */
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
     *         If 'lore' is true, also checks if the itemMeta has a lore.
     */
    public static boolean isMetaItem(ItemStack item, boolean lore) {
        return item != null && item.getType() != Material.AIR && item.getItemMeta() != null && item.getItemMeta().getDisplayName() != null && (!lore || item.getItemMeta().getLore() != null);
    }

    //region Restoration

    /**
     * @param player     Player to heal
     * @param saturation Saturation amount
     *                   <br>
     *                   Negative values are just ignored
     */
    public static void saturate(@NotNull Player player, double saturation) {
        saturate(player, saturation, false);
    }

    /**
     * @param player         Player to heal
     * @param saturation     Saturation amount
     * @param allowNegatives If passing a negative saturation value will desaturate the entity x)
     *                       <br>
     *                       If <code>false</code>, negative values are just ignored
     */
    public static void saturate(@NotNull Player player, double saturation, boolean allowNegatives) {
        if (saturation > 0 || allowNegatives)
            player.setSaturation(Math.max(0, Math.min(20, player.getSaturation() + (float) saturation)));
    }

    /**
     * @param player Player to heal
     * @param feed   Food amount
     *               <br>
     *               Negative values are just ignored
     */
    public static void feed(@NotNull Player player, int feed) {
        feed(player, feed, false);
    }

    /**
     * @param player         Player to heal
     * @param feed           Food amount
     * @param allowNegatives If passing a negative feed value will hunger the entity x)
     *                       <br>
     *                       If <code>false</code>, negative values are just ignored
     */
    public static void feed(@NotNull Player player, int feed, boolean allowNegatives) {
        if (feed > 0 || allowNegatives) player.setFoodLevel(Math.max(Math.min(20, player.getFoodLevel() + feed), 0));
    }

    /**
     * @param player Player to heal
     * @param heal   Heal amount
     *               <br>
     *               Negative values are just ignored
     */
    public static void heal(@NotNull LivingEntity player, double heal) {
        heal(player, heal, false);
    }

    /**
     * @param player         Player to heal
     * @param heal           Heal amount
     * @param allowNegatives If passing a negative health value will damage the entity x)
     *                       <br>
     *                       If <code>false</code>, negative values are just ignored
     */
    public static void heal(@NotNull LivingEntity player, double heal, boolean allowNegatives) {
        if (heal > 0 || allowNegatives)
            player.setHealth(Math.min(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), player.getHealth() + heal));
    }
    //endregion

    private static final String[] romanChars = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
    private static final int[] romanValues = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};

    /**
     * @param input Integer from 1 to 3999
     * @return Roman display of given int
     */
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

    /**
     * @param v   Vector to rotate
     * @param loc The position is not actually being used here, only the pitch and yaw
     * @return Vector facing direction given by location
     */
    public static Vector rotateFunc(Vector v, Location loc) {
        double yaw = loc.getYaw() / 180 * Math.PI;
        double pitch = loc.getPitch() / 180 * Math.PI;
        v = rotAxisX(v, pitch);
        v = rotAxisY(v, -yaw);
        return v;
    }

    private static Vector rotAxisX(Vector v, double a) {
        double y = v.getY() * Math.cos(a) - v.getZ() * Math.sin(a);
        double z = v.getY() * Math.sin(a) + v.getZ() * Math.cos(a);
        return v.setY(y).setZ(z);
    }

    private static Vector rotAxisY(Vector v, double b) {
        double x = v.getX() * Math.cos(b) + v.getZ() * Math.sin(b);
        double z = v.getX() * -Math.sin(b) + v.getZ() * Math.cos(b);
        return v.setX(x).setZ(z);
    }

    private static Vector rotAxisZ(Vector v, double c) {
        double x = v.getX() * Math.cos(c) - v.getY() * Math.sin(c);
        double y = v.getX() * Math.sin(c) + v.getY() * Math.cos(c);
        return v.setX(x).setY(y);
    }

    /**
     * @param loc Where we are looking for nearby entities
     * @return List of all entities surrounding a location. This method loops
     *         through the 9 surrounding chunks and collect all entities from
     *         them. This list can be cached and used multiple times in the same
     *         tick for projectile based spells which need to run entity
     *         checkups
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
        return split.length > 1 ? MythicLib.plugin.getVersion().getWrapper().textureItem(material, Integer.parseInt(split[1])) : new ItemStack(material);
    }
}
