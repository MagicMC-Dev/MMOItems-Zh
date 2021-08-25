package net.Indyuce.mmoitems;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackMessage;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.version.SpigotPlugin;
import io.lumine.mythic.utils.plugin.LuminePlugin;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.SoulboundInfo;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.crafting.MMOItemUIFilter;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.MMOItemReforger;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.command.MMOItemsCommandTreeRoot;
import net.Indyuce.mmoitems.comp.*;
import net.Indyuce.mmoitems.comp.denizen.DenizenHook;
import net.Indyuce.mmoitems.comp.eco.VaultSupport;
import net.Indyuce.mmoitems.comp.enchants.advanced_enchants.AdvancedEnchantmentsHook;
import net.Indyuce.mmoitems.comp.enchants.CrazyEnchantsStat;
import net.Indyuce.mmoitems.comp.enchants.EnchantPlugin;
import net.Indyuce.mmoitems.comp.enchants.MythicEnchantsSupport;
import net.Indyuce.mmoitems.comp.flags.DefaultFlags;
import net.Indyuce.mmoitems.comp.flags.FlagPlugin;
import net.Indyuce.mmoitems.comp.flags.ResidenceFlags;
import net.Indyuce.mmoitems.comp.flags.WorldGuardFlags;
import net.Indyuce.mmoitems.comp.inventory.*;
import net.Indyuce.mmoitems.comp.itemglow.ItemGlowListener;
import net.Indyuce.mmoitems.comp.itemglow.NoGlowListener;
import net.Indyuce.mmoitems.comp.mmocore.MMOCoreMMOLoader;
import net.Indyuce.mmoitems.comp.mmoinventory.MMOInventorySupport;
import net.Indyuce.mmoitems.comp.mythicmobs.LootsplosionListener;
import net.Indyuce.mmoitems.comp.mythicmobs.MythicMobsLoader;
import net.Indyuce.mmoitems.comp.parse.StringInputParser;
import net.Indyuce.mmoitems.comp.parse.placeholders.DefaultPlaceholderParser;
import net.Indyuce.mmoitems.comp.parse.placeholders.PlaceholderAPIParser;
import net.Indyuce.mmoitems.comp.parse.placeholders.PlaceholderParser;
import net.Indyuce.mmoitems.comp.rpg.DefaultHook;
import net.Indyuce.mmoitems.comp.rpg.McMMOHook;
import net.Indyuce.mmoitems.comp.rpg.RPGHandler;
import net.Indyuce.mmoitems.gui.PluginInventory;
import net.Indyuce.mmoitems.gui.edition.recipe.RecipeBrowserGUI;
import net.Indyuce.mmoitems.gui.listener.GuiListener;
import net.Indyuce.mmoitems.listener.*;
import net.Indyuce.mmoitems.manager.*;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class MMOItems extends LuminePlugin {
    public static MMOItems plugin;

    // Increment this when making breaking changes to items.
    public static final int INTERNAL_REVISION_ID = 1;

    private final PluginUpdateManager pluginUpdateManager = new PluginUpdateManager();
    private final CraftingManager stationRecipeManager = new CraftingManager();
    private final LoreFormatManager formatManager = new LoreFormatManager();
    private final TemplateManager templateManager = new TemplateManager();
    private final AbilityManager abilityManager = new AbilityManager();
    private final EntityManager entityManager = new EntityManager();
    private final RecipeManager recipeManager = new RecipeManager();
    private final LayoutManager layoutManager = new LayoutManager();
    private final TypeManager typeManager = new TypeManager();
    private final ItemManager itemManager = new ItemManager();
    private final PlayerInventoryHandler inventory = new PlayerInventoryHandler();
    private final List<StringInputParser> stringInputParsers = new ArrayList<>();
    private final List<EnchantPlugin> enchantPlugins = new ArrayList<>();

    private DropTableManager dropTableManager;
    private WorldGenManager worldGenManager;
    private UpgradeManager upgradeManager;
    private ConfigManager configManager;
    private BlockManager blockManager;
    private TierManager tierManager;
    private StatManager statManager;
    private SetManager setManager;
    private EquipListener equipListener;

    private PlaceholderParser placeholderParser = new DefaultPlaceholderParser();
    private FlagPlugin flagPlugin = new DefaultFlags();
    private VaultSupport vaultSupport;
    private RPGHandler rpgPlugin;

    @Override
    public void load() {
        plugin = this;

        if (getServer().getPluginManager().getPlugin("WorldGuard") != null)
            try {
                flagPlugin = new WorldGuardFlags();
                getLogger().log(Level.INFO, "Hooked onto WorldGuard");
            } catch (Exception exception) {
                getLogger().log(Level.WARNING, "Could not initialize support with WorldGuard 7: " + exception.getMessage());
            }

        if (getServer().getPluginManager().getPlugin("WorldEdit") != null)
            try {
                new WorldEditSupport();
                getLogger().log(Level.INFO, "Hooked onto WorldEdit");
            } catch (Exception exception) {
                getLogger().log(Level.WARNING, "Could not initialize support with WorldEdit 7: " + exception.getMessage());
            }

        saveDefaultConfig();

        /*
         * stat manager must be initialized before MMOCore compatibility
         * initializes so that MMOCore can register its stats
         */
        statManager = new StatManager();
        typeManager.reload();
        templateManager.preloadTemplates();

        if (Bukkit.getPluginManager().getPlugin("MMOCore") != null)
            new MMOCoreMMOLoader();

        if (Bukkit.getPluginManager().getPlugin("mcMMO") != null)
            //statManager.register(McMMOHook.MCMMO_SUPER_TOOL);
            statManager.register(McMMOHook.disableMcMMORepair);

        if (Bukkit.getPluginManager().getPlugin("AdvancedEnchantments") != null) {
            statManager.register(AdvancedEnchantmentsHook.ADVANCED_ENCHANTMENTS);
            statManager.register(AdvancedEnchantmentsHook.DISABLE_ADVANCED_ENCHANTMENTS);
        }

        if (Bukkit.getPluginManager().getPlugin("MythicEnchants") != null)
            enchantPlugins.add(new MythicEnchantsSupport());

        if (Bukkit.getPluginManager().getPlugin("Depenizen") != null) {
            new DenizenHook();
            getLogger().log(Level.INFO, "Hooked onto Denizen");
        }
    }

    @Override
    public void enable() {

        new SpigotPlugin(39267, this).checkForUpdate();
        new MMOItemsMetrics();

        RecipeBrowserGUI.registerNativeRecipes();
        abilityManager.loadPluginAbilities();
        configManager = new ConfigManager();

        final int configVersion = getConfig().contains("config-version", true) ? getConfig().getInt("config-version") : -1;
        final int defConfigVersion = getConfig().getDefaults().getInt("config-version");
        if (configVersion != defConfigVersion) {
            getLogger().warning("You may be using an outdated config.yml!");
            getLogger().warning("(Your config version: '" + configVersion + "' | Expected config version: '" + defConfigVersion + "')");
        }

        // registering here so the stats will load with the templates
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            new MythicMobsLoader();
            if (getConfig().getBoolean("lootsplosion.enabled"))
                Bukkit.getPluginManager().registerEvents(new LootsplosionListener(), this);
            getLogger().log(Level.INFO, "Hooked onto MythicMobs");
        }

        if (Bukkit.getPluginManager().getPlugin("MMOInventory") != null) {
            new MMOInventorySupport();
            getLogger().log(Level.INFO, "Hooked onto MMOInventory");
        }

        findRpgPlugin();

        /*
         * After tiers, sets and upgrade templates are loaded, MI template data
         * can be fully loaded
         */
        formatManager.reload();
        tierManager = new TierManager();
        setManager = new SetManager();
        upgradeManager = new UpgradeManager();
        templateManager.postloadTemplates();

        dropTableManager = new DropTableManager();
        worldGenManager = new WorldGenManager();
        blockManager = new BlockManager();
        equipListener = new EquipListener();

        if (Bukkit.getPluginManager().getPlugin("Vault") != null)
            vaultSupport = new VaultSupport();

        getLogger().log(Level.INFO, "Loading crafting stations, please wait..");
        layoutManager.reload();
        stationRecipeManager.reload();

        // This ones are not implementing Reloadable
        NumericStatFormula.reload();
        MMOItemReforger.reload();

        Bukkit.getPluginManager().registerEvents(entityManager, this);
        Bukkit.getPluginManager().registerEvents(dropTableManager, this);
        Bukkit.getPluginManager().registerEvents(new ItemUse(), this);
        Bukkit.getPluginManager().registerEvents(new ItemListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new CustomSoundListener(), this);
        Bukkit.getPluginManager().registerEvents(new DurabilityListener(), this);
        Bukkit.getPluginManager().registerEvents(new DisableInteractions(), this);
        Bukkit.getPluginManager().registerEvents(new GuiListener(), this);
        Bukkit.getPluginManager().registerEvents(new ElementListener(), this);
        Bukkit.getPluginManager().registerEvents(new CustomBlockListener(), this);
        if (Bukkit.getPluginManager().getPlugin("PhatLoots") != null) {
            Bukkit.getPluginManager().registerEvents(new PhatLootsHook(), this);
        }
        MMOItemUIFilter.register();

        Bukkit.getScheduler().runTaskTimer(this, () -> Bukkit.getOnlinePlayers().forEach(player -> PlayerData.get(player).updateStats()), 100, 20);

        /*
         * This tasks updates twice a second player inventories on the server.
         * allows now to use a glitchy itemEquipEvent. Must be called after
         * loading the config since it checks for a config option
         */
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers())
                PlayerData.get(player).getInventory().updateCheck();
        }, 100, getConfig().getInt("inventory-update-delay"));

        if (Bukkit.getPluginManager().getPlugin("Residence") != null) {
            flagPlugin = new ResidenceFlags();
            getLogger().log(Level.INFO, "Hooked onto Residence");
        }

        if (Bukkit.getPluginManager().getPlugin("mcMMO") != null)
            Bukkit.getPluginManager().registerEvents(new McMMONonRPGHook(), this);

        /*
         * Registers Player Inventories. Each of these add locations of items to search for
         * when doing inventory updates.
         */
        getInventory().register(new DefaultPlayerInventory());
        if (Bukkit.getPluginManager().getPlugin("RPGInventory") != null) {
            getInventory().register(new RPGInventoryHook());
            getLogger().log(Level.INFO, "Hooked onto RPGInventory");
        }
        if (MMOItems.plugin.getConfig().getBoolean("iterate-whole-inventory"))
            getInventory().register(new OrnamentPlayerInventory());

        if (Bukkit.getPluginManager().getPlugin("CrazyEnchantments") != null) {
            getStats().register(new CrazyEnchantsStat());
            getLogger().log(Level.INFO, "Hooked onto CrazyEnchantments");
        }

        if (Bukkit.getPluginManager().getPlugin("AdvancedEnchantments") != null) {
            Bukkit.getPluginManager().registerEvents(new AdvancedEnchantmentsHook(), this);
            getLogger().log(Level.INFO, "Hooked onto AdvancedEnchantments");
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().log(Level.INFO, "Hooked onto PlaceholderAPI");
            placeholderParser = new PlaceholderAPIParser();
        }

        if (getConfig().getBoolean("item-glow")) {
            if (Bukkit.getPluginManager().getPlugin("GlowAPI") != null && Bukkit.getPluginManager().getPlugin("PacketListenerApi") != null) {
                Bukkit.getPluginManager().registerEvents(new ItemGlowListener(), this);
                getLogger().log(Level.INFO, "Hooked onto GlowAPI (Item Glow)");
            } else
                Bukkit.getPluginManager().registerEvents(new NoGlowListener(), this);
        }

        if (Bukkit.getPluginManager().getPlugin("RealDualWield") != null) {
            Bukkit.getPluginManager().registerEvents(new RealDualWieldHook(), this);
            getLogger().log(Level.INFO, "Hooked onto RealDualWield");
        }

        if (Bukkit.getPluginManager().getPlugin("BossShopPro") != null) {
            getLogger().log(Level.INFO, "Hooked onto BossShopPro");
            (new BukkitRunnable() {
                public void run() {
                    //noinspection ProhibitedExceptionCaught
                    try {
                        new MMOItemsRewardTypes().register();
                    } catch (NullPointerException ignored) {
                        getLogger().log(Level.INFO, "Could not Hook onto BossShopPro");
                    }
                }
            }).runTaskLater(this, 1L);
        }

        // compatibility with /reload
        Bukkit.getScheduler().runTask(this, () -> Bukkit.getOnlinePlayers().forEach(PlayerData::load));

        boolean book = getConfig().getBoolean("recipes.use-recipe-book");
        boolean amounts = getConfig().getBoolean("recipes.recipe-amounts");

        if (book && amounts) {
            getLogger().warning("Tried to enable recipe book while amounts are active!");
            getLogger().warning("Please use only ONE of these options!");
            getLogger().warning("Disabling both options for now...");
            book = false;
            amounts = false;
        }

        recipeManager.load(book, amounts);
        if (amounts)
            Bukkit.getPluginManager().registerEvents(new CraftingListener(), this);

        // amount and bukkit recipes
        getLogger().log(Level.INFO, "Loading recipes, please wait...");
        recipeManager.loadRecipes();

        // main command
        MMOItemsCommandTreeRoot mmoitemsCommand = new MMOItemsCommandTreeRoot();
        getCommand("mmoitems").setExecutor(mmoitemsCommand);
        getCommand("mmoitems").setTabCompleter(mmoitemsCommand);

        // update item command DISABLED
        //getCommand("updateitem").setExecutor(new UpdateItemCommand());
        //getCommand("updateitem").setTabCompleter(new UpdateItemCompletion());
    }

    @Override
    public void disable() {

        // save player data
        PlayerData.getLoaded().forEach(PlayerData::save);

        // save item updater data
        ConfigFile updater = new ConfigFile("/dynamic", "updater");
        updater.getConfig().getKeys(false).forEach(key -> updater.getConfig().set(key, null));
        updater.save();

        // drop abandonned soulbound items
        SoulboundInfo.getAbandonnedInfo().forEach(SoulboundInfo::dropItems);

        // close inventories
        for (Player player : Bukkit.getOnlinePlayers())
            if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory().getHolder() instanceof PluginInventory)
                player.closeInventory();
    }

    public String getPrefix() {
        return ChatColor.DARK_GRAY + "[" + ChatColor.YELLOW + "MMOItems" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
    }

    public File getJarFile() {
        return plugin.getFile();
    }

    public CraftingManager getCrafting() {
        return stationRecipeManager;
    }

    public LayoutManager getLayouts() {
        return layoutManager;
    }

    public SetManager getSets() {
        return setManager;
    }

    public FlagPlugin getFlags() {
        return flagPlugin;
    }

    public void setFlags(FlagPlugin value) {
        flagPlugin = value;
    }

    public RPGHandler getRPG() {
        return rpgPlugin;
    }

    /**
     * The RPGHandler interface lets MMOItems fetch and manipulate RPG data like
     * player level, class, resources like mana and stamina for item or skill
     * costs, item restrictions, etc.
     *
     * @param handler Your RPGHandler instance
     */
    public void setRPG(RPGHandler handler) {
        Validate.notNull(handler, "RPGHandler cannot be null");

        // unregister events from current rpgPlugin instance
        if (rpgPlugin != null && rpgPlugin instanceof Listener && isEnabled())
            HandlerList.unregisterAll((Listener) rpgPlugin);

        rpgPlugin = handler;
        if (handler instanceof Listener && isEnabled())
            Bukkit.getPluginManager().registerEvents((Listener) handler, this);
    }

    public PluginUpdateManager getUpdates() {
        return pluginUpdateManager;
    }

    public PlayerInventoryHandler getInventory() {
        return inventory;
    }

    /**
     * The PlayerInventory interface lets MMOItems knows what items to look for
     * in player inventories whe doing inventory updates. By default, it only
     * checks held items + armor slots. However other plugins like MMOInv do
     * implement custom slots and therefore must register a custom
     * PlayerInventory instance that tells of additional items to look for.
     */
    public void registerPlayerInventory(PlayerInventory value) {

        // Registers in the Inventory Handler
        getInventory().register(value);
    }

    /**
     * The PlayerInventory interface lets MMOItems knows what items to look for
     * in player inventories whe doing inventory updates. By default, it only
     * checks held items + armor slots. However other plugins like MMOInv do
     * implement custom slots and therefore must register a custom
     * PlayerInventory instance.
     * <p>
     * Default instance is DefaultPlayerInventory in comp.inventory
     *
     * @param value The player inventory subclass
     * @deprecated Rather than setting this to the only inventory MMOItems will
     *         search equipment within, you must add your inventory to the
     *         handler with <code>getInventory().register()</code>. This method
     *         will clear all other PlayerInventories for now, as to keep
     *         backwards compatibility.
     */
    @Deprecated
    public void setPlayerInventory(PlayerInventory value) {

        // Unregisters those previously registered
        getInventory().unregisterAll();

        // Registers this as the only
        getInventory().register(value);
    }

    /**
     * Plugins like MythicEnchants which utilize the Bukkit
     * class Enchantment by extending it don't use any ItemStat
     * to store their enchants and therefore need to be called
     * to update the item lore when any item is built.
     *
     * @param enchantPlugin Enchantment plugin
     */
    public void registerEnchantPlugin(EnchantPlugin enchantPlugin) {
        Validate.notNull(enchantPlugin, "Enchant plugin cannot be null");
        enchantPlugins.add(enchantPlugin);
    }

    public StatManager getStats() {
        return statManager;
    }

    public TierManager getTiers() {
        return tierManager;
    }

    public EntityManager getEntities() {
        return entityManager;
    }

    public DropTableManager getDropTables() {
        return dropTableManager;
    }

    public AbilityManager getAbilities() {
        return abilityManager;
    }

    public BlockManager getCustomBlocks() {
        return blockManager;
    }

    public WorldGenManager getWorldGen() {
        return worldGenManager;
    }

    public RecipeManager getRecipes() {
        return recipeManager;
    }

    public ConfigManager getLanguage() {
        return configManager;
    }

    public TypeManager getTypes() {
        return typeManager;
    }

    public UpgradeManager getUpgrades() {
        return upgradeManager;
    }

    public PlaceholderParser getPlaceholderParser() {
        return placeholderParser;
    }

    public EquipListener getEquipListener() {
        return equipListener;
    }

    public TemplateManager getTemplates() {
        return templateManager;
    }

    public LoreFormatManager getFormats() {
        return formatManager;
    }

    @Deprecated
    public ItemManager getItems() {
        return itemManager;
    }

    /*
     * External API's
     */
    public boolean hasPermissions() {
        return vaultSupport != null && vaultSupport.getPermissions() != null;
    }

    public List<EnchantPlugin> getEnchantPlugins() {
        return enchantPlugins;
    }

    public boolean hasEconomy() {
        return vaultSupport != null && vaultSupport.getEconomy() != null;
    }

    public VaultSupport getVault() {
        return vaultSupport;
    }

    public List<StringInputParser> getStringInputParsers() {
        return stringInputParsers;
    }

    //region Easy-Access API

    /**
     * Decide by which system will the RPG Requirements of the player will be checked.
     * <p></p>
     * For example, required level, is that vanilla XP levels, MMOCore levels, McMMO Leves or what?
     */
    public void findRpgPlugin() {
        if (rpgPlugin != null)
            return;

        String preferred = plugin.getConfig().getString("preferred-rpg-provider", null);
        if (preferred != null) {

            try {
                RPGHandler.PluginEnum preferredRPG = RPGHandler.PluginEnum.valueOf(preferred.toUpperCase());
                // Found the plugin?
                if (Bukkit.getPluginManager().getPlugin(preferredRPG.getName()) != null) {

                    // Load that one
                    setRPG(preferredRPG.load());

                    // Mention it
                    print(null, "Using $s{0}$b as RPGPlayer provider", "RPG Provider", preferredRPG.getName());
                    return;
                } else

                    print(null, "Preferred RPGPlayer provider $r{0}$b is not installed!", "RPG Provider", preferred);

            } catch (IllegalArgumentException ignored) {

                // Log error
                FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());
                ffp.activatePrefix(true, "RPG Provider");
                ffp.log(FriendlyFeedbackCategory.ERROR, "Invalid RPG Provider '$u{0}$b' --- These are the supported ones:", preferred);
                for (RPGHandler.PluginEnum pgrep : RPGHandler.PluginEnum.values()) {
                    ffp.log(FriendlyFeedbackCategory.ERROR, " $r+ $b{0}", pgrep.getName());
                }
                ffp.sendTo(FriendlyFeedbackCategory.ERROR, getConsole());
            }
        }

        // For each supported plugin
        for (RPGHandler.PluginEnum pluginEnum : RPGHandler.PluginEnum.values()) {

            // Found the plugin?
            if (Bukkit.getPluginManager().getPlugin(pluginEnum.getName()) != null) {

                // Load that one
                setRPG(pluginEnum.load());

                // Mention it
                print(null, "Using $s{0}$b as RPGPlayer provider", "RPG Provider", pluginEnum.getName());
                return;
            }
        }

        // Just use the default
        setRPG(new DefaultHook());
    }

    /**
     * @return Generates an item given an item template. The item level will
     *         scale according to the player RPG level if the template has the
     *         'level-item' option. The item will pick a random tier if the
     *         template has the 'tiered' option
     */
    @Nullable
    public MMOItem getMMOItem(@Nullable Type type, @Nullable String id, @Nullable PlayerData player) {

        // Valid template?
        MMOItemTemplate found = getTemplates().getTemplate(type, id);
        if (found == null)
            return null;

        // Build if found
        return found.newBuilder(player).build();
    }

    /**
     * @return Generates an item given an item template. The item level will
     *         scale according to the player RPG level if the template has the
     *         'level-item' option. The item will pick a random tier if the
     *         template has the 'tiered' option
     */
    @Nullable
    public ItemStack getItem(@NotNull Type type, @NotNull String id, @NotNull PlayerData player) {
        Validate.notNull(type, "Type cannot be null");
        Validate.notNull(id, "ID cannot be null");

        // Valid MMOItem?
        MMOItem m = getMMOItem(type, id, player);
        if (m == null)
            return null;

        // Build if found
        return m.newBuilder().build();
    }

    /**
     * @param itemLevel The desired item level
     * @param itemTier  The desired item tier, can be null
     * @return Generates an item given an item template with a
     *         specific item level and item tier
     */
    @Nullable
    public MMOItem getMMOItem(@NotNull Type type, @NotNull String id, int itemLevel, @Nullable ItemTier itemTier) {
        Validate.notNull(type, "Type cannot be null");
        Validate.notNull(id, "ID cannot be null");

        // Valid template?
        MMOItemTemplate found = getTemplates().getTemplate(type, id);
        if (found == null)
            return null;

        // Build if found
        return found.newBuilder(itemLevel, itemTier).build();
    }

    /**
     * @param itemLevel The desired item level
     * @param itemTier  The desired item tier, can be null
     * @return Generates an item given an item template with a
     *         specific item level and item tier
     */
    @Nullable
    public ItemStack getItem(@NotNull Type type, @NotNull String id, int itemLevel, @Nullable ItemTier itemTier) {
        Validate.notNull(type, "Type cannot be null");
        Validate.notNull(id, "ID cannot be null");

        // Valid MMOItem?
        MMOItem m = getMMOItem(type, id, itemLevel, itemTier);
        if (m == null)
            return null;

        // Build if found
        return m.newBuilder().build();
    }

    /**
     * @return Generates an item given an item template. The item level will be
     *         0 and the item will have no item tier unless one is specified in
     *         the base item data.
     *         <p></p>
     *         Will return <code>null</code> if such MMOItem does not exist.
     */
    @Nullable
    public MMOItem getMMOItem(@NotNull Type type, @NotNull String id) {
        Validate.notNull(type, "Type cannot be null");
        Validate.notNull(id, "ID cannot be null");

        // Valid template?
        MMOItemTemplate found = getTemplates().getTemplate(type, id);
        if (found == null) return null;

        // Build if found
        return found.newBuilder(0, null).build();
    }

    /**
     * @return Generates an item given an item template. The item level will be
     *         0 and the item will have no item tier unless one is specified in
     *         the base item data.
     *         <p></p>
     *         Will return <code>null</code> if such MMOItem does not exist.
     */

    @Nullable
    public ItemStack getItem(@Nullable String type, @Nullable String id) {
        if (type == null || id == null) {
            return null;
        }
        return getItem(getTypes().get(type), id);
    }

    /**
     * @return Generates an item given an item template. The item level will be
     *         0 and the item will have no item tier unless one is specified in
     *         the base item data.
     *         <p></p>
     *         Will return <code>null</code> if such MMOItem does not exist.
     */
    @Nullable
    public ItemStack getItem(@Nullable Type type, @Nullable String id) {
        if (type == null || id == null) {
            return null;
        }

        // Valid MMOItem?
        MMOItem m = getMMOItem(type, id);
        if (m == null) {
            return null;
        }

        // Build if found
        return m.newBuilder().build();
    }

    //region Reading MMOItems from ItemStacks

    /**
     * @param stack The stack you trying to read
     * @return The MMOItems type of this stack, if it has one
     * @see #getType(NBTItem)
     */
    @Nullable
    public static Type getType(@Nullable ItemStack stack) {

        // Get from nbt
        return getType(NBTItem.get(stack));
    }

    /**
     * @param nbt The NBTItem you trying to read
     * @return The MMOItems type of this nbt, if it has one
     */
    @Nullable
    public static Type getType(@Nullable NBTItem nbt) {

        // That's it
        return plugin.getTypes().get(getTypeName(nbt));
    }

    /**
     * @param stack The stack you trying to read
     * @return The MMOItems type of this stack, if it has one
     * @see #getTypeName(NBTItem)
     */
    @Nullable
    public static String getTypeName(@Nullable ItemStack stack) {

        // Get from nbt
        return getTypeName(NBTItem.get(stack));
    }

    /**
     * @param nbt The NBTItem you trying to read
     * @return The MMOItems type of this nbt, if it has one
     */
    @Nullable
    public static String getTypeName(@Nullable NBTItem nbt) {

        // Straight up no
        if (nbt == null) {
            return null;
        }

        // Get from nbt
        if (!nbt.hasType()) {
            return null;
        }

        // That's it
        return nbt.getType();
    }

    /**
     * @param nbt The ItemStack you trying to read
     * @return The MMOItems ID of this stack, if it has one
     * @see #getID(NBTItem)
     */
    @Nullable
    public static String getID(@Nullable ItemStack nbt) {

        // That's it
        return getID(NBTItem.get(nbt));
    }

    /**
     * @param nbt The NBTItem you trying to read
     * @return The MMOItems ID of this nbt, if it has one
     */
    @Nullable
    public static String getID(@Nullable NBTItem nbt) {

        // Straight up no
        if (nbt == null) {
            return null;
        }

        // That's it
        return nbt.getString("MMOITEMS_ITEM_ID");
    }
    //endregion

    /**
     * Logs something into the console with a cool [MMOItems] prefix :)
     * <p></p>
     * Parses color codes. <b>Mostly for DEV testing</b>. these may removed any release.
     *
     * @author Gunging
     */
    public static void log(@Nullable String message) {
        if (message == null) {
            message = "< null >";
        }
        //String prefix = "\u00a78[" + ChatColor.YELLOW + "MMOItems\u00a78] \u00a77";
        String prefix = "";
        plugin.getServer().getConsoleSender().sendMessage(prefix + message);
    }

    /**
     * Easily log something using the FriendlyFeedbackProvider, nice!
     * <p></p>
     * Use a null level to use the normal console sender.
     *
     * @author Gunging
     */
    public static void print(@Nullable Level level, @Nullable String message, @Nullable String prefix, @NotNull String... replaces) {
        if (message == null) {
            message = "< null >";
        }
        if (level != null) {
            plugin.getLogger().log(level, FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), message, replaces));
        } else {
            FriendlyFeedbackMessage p = new FriendlyFeedbackMessage("", prefix);
            FriendlyFeedbackMessage r = FriendlyFeedbackProvider.generateMessage(p, message, replaces);
            getConsole().sendMessage(r.forConsole(FFPMMOItems.get()));
        }
    }

    /**
     * @return The server's console sender.
     * @author Gunging
     */
    @NotNull
    public static ConsoleCommandSender getConsole() {
        return plugin.getServer().getConsoleSender();
    }
    //endregion
}