package net.Indyuce.mmoitems.api.crafting;

import io.lumine.mythic.lib.api.util.PostLoadObject;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.IngredientInventory;
import net.Indyuce.mmoitems.api.crafting.recipe.CheckedRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.Recipe;
import net.Indyuce.mmoitems.api.crafting.recipe.Recipe.RecipeOption;
import net.Indyuce.mmoitems.api.crafting.recipe.UpgradingRecipe;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public class CraftingStation extends PostLoadObject {
    private final String id;
    private final String name;
    private final Layout layout;
    private final Sound sound;
    private final StationItemOptions itemOptions;
    private final int maxQueueSize;

    /**
     * This is not all the recipes of that crafting station. It only
     * contains the recipes that are specific to that station, relative
     * to the position of that station in the station inheritance tree.
     * <p>
     * In other words that map doesn't contain the crafting recipes of
     * the parent crafting station saved in {@link #parent}
     */
    private final Map<String, Recipe> recipes = new LinkedHashMap<>();

    private CraftingStation parent;

    public CraftingStation(String id, FileConfiguration config) {
        super(config);

        this.id = id.toLowerCase().replace("_", "-").replace(" ", "-");
        this.name = config.getString("name", "Unnamed");
        this.layout = MMOItems.plugin.getLayouts().getLayout(config.getString("layout", "default"));
        this.sound = Sound.valueOf(config.getString("sound", "ENTITY_EXPERIENCE_ORB_PICKUP").toUpperCase());

        for (String key : config.getConfigurationSection("recipes").getKeys(false))
            try {
                registerRecipe(loadRecipe(config.getConfigurationSection("recipes." + key)));
            } catch (IllegalArgumentException exception) {
                MMOItems.plugin.getLogger().log(Level.INFO,
                        "An issue occurred registering recipe '" + key + "' from crafting station '" + id + "': " + exception.getMessage());
            }

        itemOptions = new StationItemOptions(config.getConfigurationSection("items"));
        maxQueueSize = Math.max(1, Math.min(config.getInt("max-queue-size"), 64));
    }

    public CraftingStation(String id, String name, Layout layout, Sound sound, StationItemOptions itemOptions, int maxQueueSize, CraftingStation parent) {
        super(null);

        Validate.notNull(id, "Crafting station ID must not be null");
        Validate.notNull(name, "Crafting station name must not be null");
        Validate.notNull(sound, "Crafting station sound must not be null");

        this.id = id.toLowerCase().replace("_", "-").replace(" ", "-");
        this.name = name;
        this.layout = layout;
        this.sound = sound;
        this.itemOptions = itemOptions;
        this.maxQueueSize = maxQueueSize;
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    @Deprecated
    public String getName() {
        return name;
    }

    public Layout getLayout() {
        return layout;
    }

    public Sound getSound() {
        return sound;
    }

    @Nullable
    public CraftingStation getParent() {
        return parent;
    }

    /**
     * @return Recursively collects all recipes from that station and from
     * its parent station.
     */
    public Collection<Recipe> getRecipes() {
        if (parent == null)
            return recipes.values();

        // Collect recipes from station inheritance tree
        List<Recipe> collected = new ArrayList<>(recipes.values());
        CraftingStation next = parent;
        while (next != null) {
            collected.addAll(next.recipes.values());
            next = next.parent;
        }

        return collected;
    }

    /**
     * @param id Recipe identifier
     * @return Recursively checks if that station has the provided recipe.
     */
    public boolean hasRecipe(String id) {
        return recipes.containsKey(id) || (parent != null && parent.hasRecipe(id));
    }

    /**
     * @param id Recipe identifier
     * @return Recursively finds the corresponding recipe
     */
    public Recipe getRecipe(String id) {
        Recipe found = recipes.get(id);
        return found == null && parent != null ? parent.getRecipe(id) : found;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public List<CheckedRecipe> getAvailableRecipes(PlayerData data, IngredientInventory inv) {
        List<CheckedRecipe> infos = new ArrayList<>();

        for (Recipe recipe : getRecipes()) {
            CheckedRecipe info = recipe.evaluateRecipe(data, inv);
            if ((info.areConditionsMet() || !info.getRecipe().hasOption(RecipeOption.HIDE_WHEN_LOCKED))
                    && (info.allIngredientsHad() || !info.getRecipe().hasOption(RecipeOption.HIDE_WHEN_NO_INGREDIENTS)))
                infos.add(info);
        }

        return infos;
    }

    public StationItemOptions getItemOptions() {
        return itemOptions;
    }

    /**
     * Keep in mind this method also has the effect of register a recipe
     * inside any crafting station that has the current station as child.
     *
     * @param recipe Recipe being registered
     * @see {@link #hasRecipe(String)}
     */
    public void registerRecipe(Recipe recipe) {
        recipes.put(recipe.getId(), recipe);
    }

    public int getMaxPage() {
        int recipes = getRecipes().size();
        return Math.max(1, (int) Math.ceil((double) recipes / getLayout().getRecipeSlots().size()));
    }

    @Override
    protected void whenPostLoaded(ConfigurationSection config) {
        if (config.contains("parent")) {
            String id = config.getString("parent").toLowerCase().replace(" ", "-").replace("_", "-");
            Validate.isTrue(!id.equals(this.id), "Station cannot use itself as parent");
            Validate.isTrue(MMOItems.plugin.getCrafting().hasStation(id), "Could not find parent station with ID '" + id + "'");
            parent = MMOItems.plugin.getCrafting().getStation(id);
        }
    }

    /*
     * find type of crafting recipe based on section. there is no 'type' recipe
     * parameter because old files would be out of date, instead just looks for
     * a parameter of the crafting recipe which is 'output'
     */
    private Recipe loadRecipe(ConfigurationSection config) throws IllegalArgumentException {
        return config.contains("output") ? new CraftingRecipe(config) : new UpgradingRecipe(config);
    }
}
