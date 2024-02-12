package net.Indyuce.mmoitems.api.interaction;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.SkullTextureData;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;

public class ItemSkin extends UseItem {
    @Deprecated
    public ItemSkin(Player player, NBTItem item) {
        super(player, item);
    }

    public ItemSkin(PlayerData player, NBTItem item) {
        super(player, item);
    }

    public ApplyResult applyOntoItem(NBTItem target, Type targetType) {
        if (targetType == Type.SKIN)
            return new ApplyResult(ResultType.NONE);

        if (MMOItems.plugin.getConfig().getBoolean("locked-skins") && MMOUtils.isNonEmpty(target.getString(ItemSkin.SKIN_ID_TAG))) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            Message.SKIN_REJECTED.format(ChatColor.RED, "#item#", MMOUtils.getDisplayName(target.getItem())).send(player);
            return new ApplyResult(ResultType.NONE);
        }

        //SKIN//MMOItems.log("\u00a78SKIN \u00a7eCPT\u00a77 Applying onto " + MMOUtils.getDisplayName(target.getItem()));

        // Types compatibility check
        if (mmoitem.hasData(ItemStats.COMPATIBLE_TYPES)) {
            //SKIN//MMOItems.log("\u00a78SKIN \u00a7eCPT\u00a77 Testing that TYPE is compatible: ");
            final List<String> acceptedTypes = ((StringListData) mmoitem.getData(ItemStats.COMPATIBLE_TYPES)).getList();
            if (acceptedTypes.size() > 0 && acceptedTypes.stream().noneMatch(s -> s.equalsIgnoreCase(targetType.getId()))) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
                Message.SKIN_INCOMPATIBLE.format(ChatColor.RED, "#item#", MMOUtils.getDisplayName(target.getItem()))
                        .send(player);
                return new ApplyResult(ResultType.NONE);
            }
        }

        // IDs compatibility check
        if (mmoitem.hasData(ItemStats.COMPATIBLE_IDS)) {
            //SKIN//MMOItems.log("\u00a78SKIN \u00a7eCPT\u00a77 Testing that ID is compatible: ");
            final List<String> acceptedIDs = ((StringListData) mmoitem.getData(ItemStats.COMPATIBLE_IDS)).getList();
            final String targetId = target.getString("MMOITEMS_ITEM_ID");

            if (acceptedIDs.size() > 0 && acceptedIDs.stream()
                    .noneMatch(s -> s.equalsIgnoreCase(targetId))) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
                Message.SKIN_INCOMPATIBLE.format(ChatColor.RED, "#item#", MMOUtils.getDisplayName(target.getItem()))
                        .send(player);
                return new ApplyResult(ResultType.NONE);
            }
        }

        // Material compatibility check
        if (mmoitem.hasData(ItemStats.COMPATIBLE_MATERIALS)) {
            //SKIN//MMOItems.log("\u00a78SKIN \u00a7eCPT\u00a77 Testing that MATERIAL is compatible: ");
            List<String> acceptedMaterials = ((StringListData) mmoitem.getData(ItemStats.COMPATIBLE_MATERIALS)).getList();

            if (acceptedMaterials.size() > 0 && acceptedMaterials.stream()
                    .noneMatch(s -> s.equalsIgnoreCase(target.getItem().getType().name()))) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
                Message.SKIN_INCOMPATIBLE.format(ChatColor.RED, "#item#", MMOUtils.getDisplayName(target.getItem()))
                        .send(player);
                return new ApplyResult(ResultType.NONE);
            }
        }

        // check for success rate
        double successRate = getNBTItem().getStat(ItemStats.SUCCESS_RATE.getId());
        if (successRate != 0)
            if (RANDOM.nextDouble() < 1 - successRate / 100) {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                Message.SKIN_BROKE.format(ChatColor.RED, "#item#", MMOUtils.getDisplayName(target.getItem()))
                        .send(player);
                return new ApplyResult(ResultType.FAILURE);
            }

        // Apply skin
        ItemStack item = applySkin(target, mmoitem);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
        Message.SKIN_APPLIED.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(target.getItem())).send(player);

        return new ApplyResult(item);
    }

    /**
     * When applying a skin to an item, the skin item ID is saved
     * in the target item so that if deskined, it can be retrieved
     * and given back to the player.
     */
    public static final String
            SKIN_ID_TAG = "MMOITEMS_SKIN_ID",
            SKIN_TYPE_TAG = "MMOITEMS_SKIN_TYPE";

    /**
     * Applies the skin information from a skin consumable onto any item.
     * <p>
     * This methods also works if the provided VolatileMMOItem matches an item
     * that already has a skin information stored inside of it, in which case it
     * will fetch the value of the {@link #SKIN_ID_TAG} nbttag.
     *
     * @param target  Target item that the skin has been <b>successfully</b> applied to
     * @param volSkin Skin consumable
     * @return Built ItemStack from the target NBT but with the skin data contained in the skin consumable
     * @deprecated Badly implemented. This handles individual stats and should use some SkinStat interface
     */
    @Deprecated
    @NotNull
    public static ItemStack applySkin(@NotNull NBTItem target, @NotNull VolatileMMOItem volSkin) {
        final NBTItem nbtSkin = volSkin.getNBT();

        // Apply skin ID to new item
        final String appliedSkinId = MMOUtils.requireNonEmptyElse(volSkin.getNBT().getString(SKIN_ID_TAG), nbtSkin.getString("MMOITEMS_ITEM_ID"));
        final String appliedTypeId = MMOUtils.requireNonEmptyElse(volSkin.getNBT().getString(SKIN_TYPE_TAG), nbtSkin.getString("MMOITEMS_ITEM_TYPE"));
        target.addTag(new ItemTag(SKIN_ID_TAG, appliedSkinId));
        target.addTag(new ItemTag(SKIN_TYPE_TAG, appliedTypeId));

        // Custom model data
        if (nbtSkin.getInteger("CustomModelData") != 0)
            target.addTag(new ItemTag("CustomModelData", nbtSkin.getInteger("CustomModelData")));

        // Particles
        if (!nbtSkin.getString("MMOITEMS_ITEM_PARTICLES").isEmpty())
            target.addTag(new ItemTag("MMOITEMS_ITEM_PARTICLES", nbtSkin.getString("MMOITEMS_ITEM_PARTICLES")));

        final ItemStack item = target.toItem();
        if (item.getType() != nbtSkin.getItem().getType())
            item.setType(nbtSkin.getItem().getType());

        final ItemMeta meta = item.getItemMeta();
        final ItemMeta skinMeta = nbtSkin.getItem().getItemMeta();
        if (skinMeta != null && meta != null) {

            // TODO SkinStat interface

            // Unbreakable & durability
            if (skinMeta.isUnbreakable()) {
                meta.setUnbreakable(true);
                if (meta instanceof Damageable && skinMeta instanceof Damageable)
                    ((Damageable) meta).setDamage(((Damageable) skinMeta).getDamage());
            }

            // Leather armor
            if (skinMeta instanceof LeatherArmorMeta && meta instanceof LeatherArmorMeta)
                ((LeatherArmorMeta) meta).setColor(((LeatherArmorMeta) skinMeta).getColor());

            // Skull texture
            if (volSkin.hasData(ItemStats.SKULL_TEXTURE)
                    && item.getType() == VersionMaterial.PLAYER_HEAD.toMaterial()
                    && nbtSkin.getItem().getType() == VersionMaterial.PLAYER_HEAD.toMaterial())
                try {
                    final Field profileField = meta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(meta,
                            ((SkullTextureData) volSkin.getData(ItemStats.SKULL_TEXTURE)).getGameProfile());
                } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                    MMOItems.plugin.getLogger().warning("Could not read skull texture");
                }

            item.setItemMeta(meta);
        }

        return item;
    }

    public static class ApplyResult {
        private final ResultType type;
        private final ItemStack result;

        public ApplyResult(ResultType type) {
            this(null, type);
        }

        public ApplyResult(ItemStack result) {
            this(result, ResultType.SUCCESS);
        }

        public ApplyResult(ItemStack result, ResultType type) {
            this.type = type;
            this.result = result;
        }

        public ResultType getType() {
            return type;
        }

        public ItemStack getResult() {
            return result;
        }
    }

    public enum ResultType {
        FAILURE, NONE, SUCCESS
    }
}
