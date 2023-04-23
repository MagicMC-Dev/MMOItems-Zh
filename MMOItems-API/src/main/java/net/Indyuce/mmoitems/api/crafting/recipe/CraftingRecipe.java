package net.Indyuce.mmoitems.api.crafting.recipe;

import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import io.lumine.mythic.lib.api.crafting.uimanager.UIFilterManager;
import io.lumine.mythic.lib.api.util.SmartGive;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackMessage;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ConfigMMOItem;
import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.crafting.CraftingStatus.CraftingQueue;
import net.Indyuce.mmoitems.api.crafting.MMOItemUIFilter;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.IngredientInventory;
import net.Indyuce.mmoitems.api.event.PlayerUseCraftingStationEvent;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.item.util.ConfigItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.api.util.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CraftingRecipe extends Recipe {
    @NotNull
    public static final String UNSPECIFIED = "N/A";

    public CraftingRecipe(@NotNull ConfigurationSection config) throws IllegalArgumentException {
        super(config);

        craftingTime = config.getDouble("crafting-time");

        // Legacy loading
        String uiFilter = config.getString("output.item", UNSPECIFIED);
        String miType = config.getString("output.type", UNSPECIFIED).toUpperCase().replace("-", "_").replace(" ", "_");
        String miID = config.getString("output.id", UNSPECIFIED).toUpperCase().replace("-", "_").replace(" ", "_");

        // Yes
        FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());

        // Both legacy specified?
        if (!UNSPECIFIED.equals(miType) && !UNSPECIFIED.equals(miID)) {

            // Generate filter
            ProvidedUIFilter sweetOutput = UIFilterManager.getUIFilter("m", miType, miID, config.getString("output.amount", "1"), ffp);

            // Is it null?
            if (sweetOutput == null) {

                // Throw message
                throw new IllegalArgumentException(SilentNumbers.collapseList(SilentNumbers.transcribeList(ffp.getFeedbackOf(FriendlyFeedbackCategory.ERROR), message -> {
                    if (message instanceof FriendlyFeedbackMessage) {
                        return ((FriendlyFeedbackMessage) message).forConsole(FFPMMOItems.get());
                    }
                    return "";
                }), ""));
            }

            // Accept
            output = sweetOutput;

            // New method specified?
        } else if (!UNSPECIFIED.equals(uiFilter)) {

            // Generate filter
            ProvidedUIFilter sweetOutput = UIFilterManager.getUIFilter(uiFilter, ffp);

            // Is it null?
            if (sweetOutput == null) {

                // Throw message
                throw new IllegalArgumentException(SilentNumbers.collapseList(SilentNumbers.transcribeList(ffp.getFeedbackOf(FriendlyFeedbackCategory.ERROR), message -> {
                    if (message instanceof FriendlyFeedbackMessage) {
                        return ((FriendlyFeedbackMessage) message).forConsole(FFPMMOItems.get());
                    }
                    return "";
                }), ""));
            }

            // Accept
            output = sweetOutput;

            // Invalid filter
        } else {

            // Throw message
            throw new IllegalArgumentException(FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "Config must contain a valid Type and ID, or a valid UIFilter. "));
        }

        // Valid UIFilter?
        if (!output.isValid(ffp)) {

            // Throw message
            throw new IllegalArgumentException(SilentNumbers.collapseList(SilentNumbers.transcribeList(ffp.getFeedbackOf(FriendlyFeedbackCategory.ERROR), message -> {
                if (message instanceof FriendlyFeedbackMessage) {
                    return ((FriendlyFeedbackMessage) message).forConsole(FFPMMOItems.get());
                }
                return "";
            }), ""));
        }

        // Valid UIFilter?
        if (output.getItemStack(ffp) == null) {

            // Throw message
            throw new IllegalArgumentException(SilentNumbers.collapseList(SilentNumbers.transcribeList(ffp.getFeedbackOf(FriendlyFeedbackCategory.ERROR), message -> {
                if (message instanceof FriendlyFeedbackMessage) {
                    return ((FriendlyFeedbackMessage) message).forConsole(FFPMMOItems.get());
                }
                return "";
            }), ""));
        }

        // Its a MMOItem UIFilter, then?
        if (output.getParent() instanceof MMOItemUIFilter) {

            // Find template
            MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(MMOItems.plugin.getTypes().get(output.getArgument()), output.getData());

            // Not possible tho
            if (template == null) {

                // Throw message
                throw new IllegalArgumentException(FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "This should be impossible, please contact $egunging$b: $fThe ProvidedUIFilter was flagged as 'valid' but clearly is not. $enet.Indyuce.mmoitems.api.crafting.recipe$b. "));
            }

            // Identify MMOItems operation
            identifiedMMO = new ConfigMMOItem(template, output.getAmount(1));
        }
    }

    /*
     * There can't be any crafting time for upgrading recipes since there is no
     * way to save an MMOItem in the config file TODO save as ItemStack
     */
    private final double craftingTime;

    public double getCraftingTime() {
        return craftingTime;
    }

    public boolean isInstant() {
        return craftingTime <= 0;
    }

    /**
     * @return The item specified by the player that will be produced by this recipe.
     */
    @NotNull
    public ProvidedUIFilter getOutput() {
        return output;
    }

    @NotNull
    private final ProvidedUIFilter output;

    @Nullable
    ConfigMMOItem identifiedMMO;

    /**
     * @return The output ItemStack from this
     */
    @SuppressWarnings("ConstantConditions")
    @NotNull
    public ItemStack getOutputItemStack(@Nullable RPGPlayer rpg) {

        // Generate as MMOItem
        if (identifiedMMO != null && rpg != null) {

            /*
             * Generate in the legacy way. I do this way to preserve
             * backwards compatibility, since this is how it used to
             * be done. Don't want to break that without good reason.
             */
            return identifiedMMO.generate(rpg);
        }

        // Generate from ProvidedUIFilter, guaranteed to not be null don't listen to the inspection.
        return output.getItemStack(null);
    }

    /**
     * @return The preview ItemStack from this
     */
    @NotNull
    public ItemStack getPreviewItemStack() {

        // Generate as MMOItem
        if (identifiedMMO != null) {

            /*
             * Generate in the legacy way. I do this way to preserve
             * backwards compatibility, since this is how it used to
             * be done. Don't want to break that without good reason.
             */
            return identifiedMMO.getPreview();
        }

        // Generate from ProvidedUIFilter, guaranteed to not be null don't listen to the inspection.
        //return output.getParent().getDisplayStack(output.getArgument(), output.getData(), null);
        //return output.getDisplayStack(null);
        ItemStack gen = output.getParent().getDisplayStack(output.getArgument(), output.getData(), null);
        gen.setAmount(output.getAmount(1));
        ItemMeta itemMeta = gen.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(SilentNumbers.getItemName(gen, false) + "\u00a7\u02ab");
            gen.setItemMeta(itemMeta);
        }
        return gen;
    }

    public int getOutputAmount() {
        return output.getAmount(1);
    }

    @Override
    public boolean whenUsed(PlayerData data, IngredientInventory inv, CheckedRecipe recipe, CraftingStation station) {
        if (!data.isOnline())
            return false;

        /*
         * If the recipe is instant, take the ingredients off
         * and directly add the output to the player's inventory
         */
        if (isInstant()) {
            ItemStack result = hasOption(RecipeOption.OUTPUT_ITEM) ? getOutputItemStack(data.getRPG()) : null;
            PlayerUseCraftingStationEvent event = new PlayerUseCraftingStationEvent(data, station, recipe, result);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled())
                return false;

            /*
             * Since instant recipes bypass the crafting queue MI still needs
             * to apply the trigger list when using an instant recipe
             */
            recipe.getRecipe().whenClaimed().forEach(trigger -> trigger.whenCrafting(data));

            if (result != null)
                new SmartGive(data.getPlayer()).give(result);

            // Play sound
            if (!hasOption(RecipeOption.SILENT_CRAFT))
                data.getPlayer().playSound(data.getPlayer().getLocation(), station.getSound(), 1, 1);

            // Recipe was successfully used
            return true;

            /*
             * If the recipe is not instant, add the item to the crafting queue
             */
        }

        PlayerUseCraftingStationEvent called = new PlayerUseCraftingStationEvent(data, station, recipe);
        Bukkit.getPluginManager().callEvent(called);
        if (called.isCancelled())
            return false;

        // Play sound
        if (!hasOption(RecipeOption.SILENT_CRAFT))
            data.getPlayer().playSound(data.getPlayer().getLocation(), station.getSound(), 1, 1);

        data.getCrafting().getQueue(station).add(this);

        // Recipe was successfully used
        return true;
    }

    @Override
    public boolean canUse(PlayerData data, IngredientInventory inv, CheckedRecipe recipe, CraftingStation station) {
        if (isInstant())
            return true;

        CraftingQueue queue = data.getCrafting().getQueue(station);
        if (queue.isFull(station)) {
            if (!data.isOnline())
                return false;

            Message.CRAFTING_QUEUE_FULL.format(ChatColor.RED).send(data.getPlayer());
            data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return false;
        }
        return true;
    }

    @Override
    public ItemStack display(CheckedRecipe recipe) {
        return ConfigItems.CRAFTING_RECIPE_DISPLAY.newBuilder(recipe).build();
    }

    @Override
    public CheckedRecipe evaluateRecipe(PlayerData data, IngredientInventory inv) {
        return new CheckedRecipe(this, data, inv);
    }
}
