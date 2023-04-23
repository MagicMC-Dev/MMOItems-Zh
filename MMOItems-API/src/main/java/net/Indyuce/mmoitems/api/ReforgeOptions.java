package net.Indyuce.mmoitems.api;

import net.Indyuce.mmoitems.api.util.MMOItemReforger;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ReforgeOptions {
    public static boolean dropRestoredGems;

    // MMOItems stuff
    private final boolean keepName;
    private final boolean keepLore;
    private final boolean keepEnchantments;
    private final boolean keepSkins;
    private final boolean keepUpgrades;
    private final boolean keepGemStones;
    private final boolean keepSoulBind;
    private final boolean keepExternalSH;
    private final boolean keepModifications;
    @Nullable
    private final Boolean keepTier;

    private final boolean reRoll;

    // Third Party Stuff
    private final boolean keepAdvancedEnchantments;

    @NotNull
    String keepCase = ChatColor.GRAY.toString();

    public void setKeepCase(@NotNull String kc) {
        keepCase = kc;
    }

    @NotNull
    public String getKeepCase() {
        return keepCase;
    }

    @NotNull
    ArrayList<String> blacklistedItems = new ArrayList<>();

    /**
     * Apparently, people like to use MMOItems for quests. This
     * will make items NEVER update with RevID
     *
     * @param mmoitemID Item ID. Listen, including MMOItem Type as
     *                  well is unnecessary hassle, complicates the
     *                  implementation.
     *                  <p>
     *                  People who name all their items "1", "2", ...
     *                  can learn to not use magic numbers ffs.
     * @return If this item should not update with RevID (when passing
     * these options, of course).
     */
    public boolean isBlacklisted(@NotNull String mmoitemID) {
        return blacklistedItems.contains(mmoitemID);
    }

    /**
     * Apparently, people like to use MMOItems for quests. This
     * will make items NEVER update with RevID
     *
     * @param mmoitemID Item ID. Listen, including MMOItem Type as
     *                  well is unnecessary hassle, complicates the
     *                  implementation.
     *                  <p>
     *                  People who name all their items "1", "2", ...
     *                  can learn to not use magic numbers ffs.
     */
    public void addToBlacklist(@NotNull String mmoitemID) {
        blacklistedItems.add(mmoitemID);
    }

    /**
     * No MMOItem-ID restrictions on RevID.
     */
    public void clearBlacklist() {
        blacklistedItems.clear();
    }

    public ReforgeOptions(ConfigurationSection config) {
        keepName = config.getBoolean("display-name");
        keepLore = config.getBoolean("lore");
        keepEnchantments = config.getBoolean("enchantments");
        keepUpgrades = config.getBoolean("upgrades");
        keepGemStones = config.getBoolean("gemstones", false) || config.getBoolean("gems", false);
        keepSkins = config.getBoolean("skins", false);
        keepSoulBind = config.getBoolean("soulbound");
        keepCase = config.getString("kept-lore-prefix", ChatColor.GRAY.toString());
        keepExternalSH = config.getBoolean("external-sh", true);
        keepModifications = config.getBoolean("modifications");
        reRoll = config.getBoolean("reroll");
        keepAdvancedEnchantments = config.getBoolean("advanced-enchantments");
        keepTier = config.contains("tier") ? config.getBoolean("tier", true) : null;
    }

    public ReforgeOptions(boolean... values) {
        keepName = arr(values, 0);
        keepLore = arr(values, 1);
        keepEnchantments = arr(values, 2);
        keepUpgrades = arr(values, 3);
        keepGemStones = arr(values, 4);
        keepSoulBind = arr(values, 5);
        keepExternalSH = arr(values, 6);
        reRoll = arr(values, 7);
        keepModifications = arr(values, 8);
        keepAdvancedEnchantments = arr(values, 9);
        keepSkins = arr(values, 10);
        keepTier = arr(values, 11);
    }

    boolean arr(@NotNull boolean[] booleans, int idx) {
        return booleans.length > idx && booleans[idx];
    }

    /**
     * If the item should be rerolled when updated. In the contrary,
     * the previous RNG will be transferred onto the newest item.
     */
    public boolean shouldReRoll() {
        return reRoll;
    }

    /**
     * Keeps the display name of the item.
     */
    public boolean shouldKeepName() {
        return keepName;
    }

    /**
     * Keeps the modifiers of the item.
     */
    public boolean shouldKeepMods() {
        return keepModifications;
    }

    /**
     * Keeps all lore lines that begin with {@link org.bukkit.ChatColor#GRAY}
     */
    public boolean shouldKeepLore() {
        return keepLore;
    }

    /**
     * Keeps skins
     */
    public boolean shouldKeepSkins() {
        return keepSkins;
    }

    /**
     * Should keep the tier? defaults to {@link MMOItemReforger#keepTiersWhenReroll}
     */
    public boolean shouldKeepTier() {
        return keepTier == null ? MMOItemReforger.keepTiersWhenReroll : keepTier;
    }

    /**
     * Should this keep the enchantments the player
     * manually cast onto this item? (Not from gem
     * stones nor upgrades).
     */
    public boolean shouldKeepEnchantments() {
        return keepEnchantments;
    }

    /**
     * Should this keep the enchantments the player
     * manually cast onto this item? (Not from gem
     * stones nor upgrades).
     */
    public boolean shouldKeepAdvancedEnchants() {
        return keepAdvancedEnchantments;
    }

    /**
     * Keep 'extraneous' data registered onto the Stat History
     */
    public boolean shouldKeepExternalSH() {
        return keepExternalSH;
    }

    /**
     * Retains the upgrade level of the item.
     */
    public boolean shouldKeepUpgrades() {
        return keepUpgrades;
    }

    /**
     * Retains all gem stones if there are any, removing
     * one gem socket for every gemstone kept.
     * <p></p>
     * Gemstones remember at what upgrade level they were inserted.
     */
    public boolean shouldKeepGemStones() {
        return keepGemStones;
    }

    /**
     * Retains the soulbind if it has any.
     */
    public boolean shouldKeepSoulBind() {
        return keepSoulBind;
    }
}
