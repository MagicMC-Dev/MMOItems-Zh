package net.Indyuce.mmoitems.api.recipe.workbench;

import io.lumine.mythic.lib.api.crafting.ingredients.MythicRecipeIngredient;
import io.lumine.mythic.lib.api.crafting.ingredients.ShapedIngredient;
import io.lumine.mythic.lib.api.crafting.outputs.MRORecipe;
import io.lumine.mythic.lib.api.crafting.outputs.MythicRecipeOutput;
import io.lumine.mythic.lib.api.crafting.recipes.MythicRecipeBlueprint;
import io.lumine.mythic.lib.api.crafting.recipes.ShapedRecipe;
import io.lumine.mythic.lib.api.crafting.recipes.ShapelessRecipe;
import io.lumine.mythic.lib.api.crafting.uifilters.VanillaUIFilter;
import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import io.lumine.mythic.lib.api.crafting.uimanager.UIFilterManager;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.crafting.recipe.CustomSmithingRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.UpgradeCombinationType;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.recipe.MMOItemUIFilter;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.AirIngredient;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.WorkbenchIngredient;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CustomRecipe implements Comparable<CustomRecipe> {
	private final Type type;
	private final String id;
	private final boolean shapeless;
	private final Map<Integer, WorkbenchIngredient> ingredients = new HashMap<>(9);
	private Permission permission;

	public CustomRecipe(Type type, String id, List<String> recipe, boolean isShapeless) {
		this.shapeless = isShapeless;
		this.type = type;
		this.id = id;

		if (shapeless) {
			Validate.isTrue(recipe.size() == 9, "Invalid shapeless recipe");
			for (int i = 0; i < 9; i++) {
				WorkbenchIngredient ingredient = MMOItems.plugin.getRecipes().getWorkbenchIngredient(recipe.get(i));
				// Only add AirIngredients if the amount system is enabled
				if (MMOItems.plugin.getRecipes().isAmounts() || !(ingredient instanceof AirIngredient))
					ingredients.put(i, ingredient);
			}
			return;
		}

		Validate.isTrue(recipe.size() == 3, "Invalid shaped recipe");
		for (int i = 0; i < 9; i++) {
			List<String> line = Arrays.asList(recipe.get(i / 3).split(" "));
			while (line.size() < 3)
				line.add("AIR");

			WorkbenchIngredient ingredient = MMOItems.plugin.getRecipes().getWorkbenchIngredient(line.get(i % 3));
			if (!(ingredient instanceof AirIngredient))
				ingredients.put(i, ingredient);
		}
	}

	public Set<Entry<Integer, WorkbenchIngredient>> getIngredients() {
		return ingredients.entrySet();
	}

	public boolean isOneRow() {
		for (int value : ingredients.keySet())
			if (value > 2)
				return false;
		return true;
	}

	public boolean isTwoRows() {
		for (int value : ingredients.keySet())
			if (value > 5)
				return false;
		return true;
	}

	public boolean fitsPlayerCrafting() {
		for (int value : ingredients.keySet())
			if (value > 4 || value == 2)
				return false;
		return true;
	}

	public boolean isEmpty() {
		return ingredients.isEmpty();
	}

	public boolean isShapeless() {
		return shapeless;
	}

	public Type getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	public boolean checkPermission(Player player) {
		return permission == null || player.hasPermission(permission);
	}

	public ItemStack getResult(@Nullable Player p) {
		MMOItem mmo = p == null ? MMOItems.plugin.getMMOItem(type, id)
				: MMOItems.plugin.getMMOItem(type, id, PlayerData.get(p));
		ItemStack stack = mmo.newBuilder().build();
		if (mmo.hasData(ItemStats.CRAFT_AMOUNT))
			stack.setAmount((int) ((DoubleData) mmo.getData(ItemStats.CRAFT_AMOUNT)).getValue());
		if (mmo.hasData(ItemStats.CRAFT_PERMISSION))
			permission = new Permission(mmo.getData(ItemStats.CRAFT_PERMISSION).toString(), PermissionDefault.FALSE);
		return stack;
	}

	@Override
	public int compareTo(CustomRecipe o) {
		return Boolean.compare(shapeless, o.shapeless);
	}

	public Recipe asBukkit(NamespacedKey key) {
		Recipe recipe;
		if (shapeless) {
			org.bukkit.inventory.ShapelessRecipe r = new org.bukkit.inventory.ShapelessRecipe(key, getResult(null));
			for (WorkbenchIngredient ingredient : ingredients.values())
				if (!(ingredient instanceof AirIngredient))
					r.addIngredient(ingredient.toBukkit());
			recipe = r;
		} else {
			org.bukkit.inventory.ShapedRecipe r = new org.bukkit.inventory.ShapedRecipe(key, getResult(null));
			char[] characters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};
			List<Integer> list = new ArrayList<>(ingredients.keySet());
			StringBuilder firstRow = new StringBuilder();
			firstRow.append(list.contains(0) ? "A" : " ");
			firstRow.append(list.contains(1) ? "B" : " ");
			firstRow.append(list.contains(2) ? "C" : " ");
			if(!isOneRow()) {
				StringBuilder secondRow = new StringBuilder();
				secondRow.append(list.contains(3) ? "D" : " ");
				secondRow.append(list.contains(4) ? "E" : " ");
				secondRow.append(list.contains(5) ? "F" : " ");
				if(!isTwoRows()) {
					r.shape(firstRow.toString(), secondRow.toString(),
						(list.contains(6) ? "G" : " ") +
						(list.contains(7) ? "H" : " ") +
						(list.contains(8) ? "I" : " "));
				}
				else r.shape(firstRow.toString(), secondRow.toString());
			}
			else r.shape(firstRow.toString());

			for (Entry<Integer, WorkbenchIngredient> ingredient : getIngredients()) {
				if (ingredient.getValue() instanceof AirIngredient) continue;
				char c = characters[ingredient.getKey()];
				r.setIngredient(c, ingredient.getValue().toBukkit());
			}
			
			recipe = r;
		}
		return recipe;
	}


	static int recipeCount = 0;

	/**
	 * Reads this list of strings as a Shapeless Recipe
	 * to craft this MMOItem.
	 *
	 * @param type The TYPE of the crafted MMOItem
	 * @param id The ID of the crafted MMOItem
	 * @param recipe The compactly-stored recipe information
	 * @return A baked recipe, ready to deploy.
	 * @throws IllegalArgumentException If the recipe is in incorrect format
	 */
	@NotNull public static MythicRecipeBlueprint generateShapeless(@NotNull Type type, @NotNull String id, @NotNull List<String> recipe) throws IllegalArgumentException {

		// Get it
		MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(type, id);
		Validate.isTrue(template != null, "Unexpected Error Occurred: Template does not exist.");

		// Identify the Provided UIFilters
		ArrayList<MythicRecipeIngredient> poofs = new ArrayList<>();

		// Error yes
		FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());
		ffp.activatePrefix(true, "Recipe of $u" + type + " " + id);

		// Read from the recipe
		boolean nonAirFound = false;
		for (String str : recipe) {

			// Null is a sleeper
			if (str == null || str.equals("AIR")) { continue; }

			// Add
			ProvidedUIFilter p = readIngredientFrom(str, ffp);
			nonAirFound = true;
			poofs.add(new MythicRecipeIngredient(p));
		}
		if (!nonAirFound) { throw new IllegalArgumentException(FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "Shapeless recipe containing only AIR, $fignored$b.")); }
		String recipeName = type + "." + id + "." + recipeCount++;

		// Build Main
		ShapedRecipe shapedRecipe = ShapedRecipe.single(recipeName,  new ProvidedUIFilter(MMOItemUIFilter.get(), type.getId(), id, Math.max(template.getCraftedAmount(), 1)));

		// Make ingredients
		ShapelessRecipe inputRecipe = new ShapelessRecipe(recipeName, poofs);

		// Create Output
		MythicRecipeOutput outputRecipe = new MRORecipe(shapedRecipe);

		// That's our blueprint :)
		return new MythicRecipeBlueprint(inputRecipe, outputRecipe);
	}

	/**
	 * Reads this list of strings as a Shaped Recipe
	 * to craft this MMOItem.
	 *
	 * @param type The TYPE of the crafted MMOItem
	 * @param id The ID of the crafted MMOItem
	 * @param recipe The compactly-stored recipe information
	 * @return A baked recipe, ready to deploy.
	 * @throws IllegalArgumentException If the recipe is in incorrect format
	 */
	@NotNull public static MythicRecipeBlueprint generateShaped(@NotNull Type type, @NotNull String id, @NotNull List<String> recipe) throws IllegalArgumentException {

		// Get it
		MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(type, id);
		Validate.isTrue(template != null, "Unexpected Error Occurred: Template does not exist.");

		// Identify the Provided UIFilters
		ArrayList<ShapedIngredient> poofs = new ArrayList<>();

		// Error yes
		FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());
		ffp.activatePrefix(true, "Recipe of $u" + type + " " + id);
		int rowNumber = 0;

		// All right lets read them
		boolean nonAirFound = false;
		for (String row : recipe) {

			/*
			 * This row could be in either legacy or new format, and we will assume no combination of them.
			 *
			 * Either:
			 *  ANYTHING ANY.THING ANYTHING
			 *
			 * or
			 *  A NYT THIN G|A NYT THING|A NYT THIN G
			 */

			// What are the three ingredients encoded in this row?
			String[] positions;

			if (row.contains("|")) {

				// Split by |s
				positions = row.split("\\|");

			// Is legacy
			} else {

				// Split by spaces
				positions = row.split(" ");
			}

			// Size not 3? BRUH
			if (positions.length != 3) { throw new IllegalArgumentException("Invalid crafting table row $u" + row + "$b ($fNot exactly 3 ingredients wide$b)."); }

			// Identify
			ProvidedUIFilter left = readIngredientFrom(positions[0], ffp);
			ProvidedUIFilter center = readIngredientFrom(positions[1], ffp);
			ProvidedUIFilter right = readIngredientFrom(positions[2], ffp);
			if (!left.isAir()) { nonAirFound = true; }
			if (!center.isAir()) { nonAirFound = true; }
			if (!right.isAir()) { nonAirFound = true; }

			/*
			 * To detect if a recipe can be crafted in the survival inventory (and remove extra AIR),
			 * we must see that a whole row AND a whole column be air. Not any column or row though,
			 * but any of those that do not cross the center.
			 *
			 * If a single left item is not air, LEFT is no longer an unsharped column.
			 * If a single right item is not air, RIGHT is no longer an unsharped column.
			 *
			 * All items must be air in TOP or BOTTOM for they to be unsharped.
			 */

			// Bake
			ShapedIngredient leftIngredient = new ShapedIngredient(left, 0, -rowNumber);
			ShapedIngredient centerIngredient = new ShapedIngredient(center, 1, -rowNumber);
			ShapedIngredient rightIngredient = new ShapedIngredient(right, 2, -rowNumber);

			// Parse and add
			poofs.add(leftIngredient);
			poofs.add(centerIngredient);
			poofs.add(rightIngredient);

			// Prepare for next row
			rowNumber++;
		}
		if (!nonAirFound) { throw new IllegalArgumentException(FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "Shaped recipe containing only AIR, $fignored$b.")); }
		String recipeName = type + "." + id + "." + recipeCount++;

		// Build Main
		ShapedRecipe shapedRecipe = ShapedRecipe.single(recipeName,  new ProvidedUIFilter(MMOItemUIFilter.get(), type.getId(), id, Math.max(template.getCraftedAmount(), 1)));

		// Make ingredients
		ShapedRecipe inputRecipe = ShapedRecipe.unsharpen((new ShapedRecipe(recipeName, poofs)));

		// Create Output
		MythicRecipeOutput outputRecipe = new MRORecipe(shapedRecipe);

		// That's our blueprint :)
		return new MythicRecipeBlueprint(inputRecipe, outputRecipe);
	}

	/**
	 * Reads this list of strings as a Smithing Recipe to craft this MMOItem.
	 *
	 * @param type The TYPE of the crafted MMOItem
	 * @param id The ID of the crafted MMOItem
	 * @param item First item you need to put in the Smithing Station
	 * @param ingot Second item to put in the smithing station
	 * @return A baked recipe, ready to deploy.
	 * @throws IllegalArgumentException If the recipe is in incorrect format
	 */
	@NotNull public static MythicRecipeBlueprint generateSmithing(@NotNull Type type, @NotNull String id, @NotNull String item, @NotNull String ingot, boolean dropGems, @NotNull String upgradeBehaviour) throws IllegalArgumentException {

		// Get it
		MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(type, id);
		Validate.isTrue(template != null, "Unexpected Error Occurred: Template does not exist.");
		UpgradeCombinationType upgradeEffect = UpgradeCombinationType.valueOf(upgradeBehaviour.toUpperCase());

		// Identify the Provided UIFilters
		ArrayList<ShapedIngredient> poofs = new ArrayList<>();

		// Error yes
		FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());
		ffp.activatePrefix(true, "Recipe of $u" + type + " " + id);
		int rowNumber = 0;

		// All right lets read them
		ProvidedUIFilter itemPoof = readIngredientFrom(item, ffp);
		ProvidedUIFilter ingotPoof = readIngredientFrom(ingot, ffp);
		if (itemPoof.isAir() || ingotPoof.isAir()) { throw new IllegalArgumentException(FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "Smithing recipe containing AIR, $fignored$b.")); }

		MythicRecipeIngredient itemIngredient = new MythicRecipeIngredient(itemPoof);
		MythicRecipeIngredient ingotIngredient = new MythicRecipeIngredient(ingotPoof);
		String recipeName = type + "." + id + "." + recipeCount++;

		// Build Main
		ShapedRecipe shapedRecipe = ShapedRecipe.single(recipeName,  new ProvidedUIFilter(MMOItemUIFilter.get(), type.getId(), id, Math.max(template.getCraftedAmount(), 1)));

		// Make ingredients
		ShapelessRecipe inputItem = new ShapelessRecipe(recipeName, itemIngredient);
		ShapelessRecipe inputIngot = new ShapelessRecipe(recipeName, ingotIngredient);

		// Create Output
		MythicRecipeOutput outputRecipe = new CustomSmithingRecipe(template, dropGems, upgradeEffect);

		MythicRecipeBlueprint recipe = new MythicRecipeBlueprint(inputItem, outputRecipe);
		recipe.addSideCheck("ingot", inputIngot);

		// That's our blueprint :)
		return recipe;
	}

	/**
	 * To support legacy formats, at least for now, we use this method
	 * to read individual ingredients.
	 * <p></p>
	 * It supports the formats:
	 * <p><code>MATERIAL</code> (legacy vanilla material)
	 * </p><code>TYPE.ID</code> (legacy MMOItem)
	 * <p><code>KEY ARGUMENT DATA AMOUNT</code> (current)
	 * </p>
	 *
	 * @param str String that's should be in one of the formats above.
	 * @param ffp To tell what happened
	 *
	 * @throws IllegalArgumentException If not in the correct format.
	 *
	 * @return An ingredient read from this string.
	 */
	@NotNull public static ProvidedUIFilter readIngredientFrom(@NotNull String str, @NotNull FriendlyFeedbackProvider ffp) throws IllegalArgumentException {

		/*
		 * This entry, is it a vanilla material?
		 *
		 * Then build it as material.
		 */
		Material asMaterial = null;
		try { asMaterial = Material.valueOf(str.toUpperCase().replace(" ", "_").replace("-", "_")); } catch (IllegalArgumentException ignored) {}
		if (asMaterial != null) {

			// Is it AIR?
			if (asMaterial.isAir()) { return new ProvidedUIFilter(VanillaUIFilter.get(), "AIR", "0", 1); }

			// We snooze if its AIR or such
			if (!asMaterial.isItem()) { throw new IllegalArgumentException("Invalid Ingredient $u" + str + "$b ($fNot an Item$b)."); }

			// All right create filter and go
			ProvidedUIFilter poof = UIFilterManager.getUIFilter("v", asMaterial.toString(), "", "1..", ffp);

			// Valid?
			if (poof != null) {

				// Add
				return poof;

			} else {

				// Send all I guess
				ffp.sendTo(FriendlyFeedbackCategory.ERROR, MMOItems.getConsole());
				ffp.sendTo(FriendlyFeedbackCategory.FAILURE, MMOItems.getConsole());

				// Ew
				throw new IllegalArgumentException("Invalid Ingredient $u" + str);
			}
		}

		/*
		 * Not a vanilla material, lets try to read it as a Legacy MMOItem thing.
		 *
		 * It must have a dot, and no spaces.
		 */
		if (str.contains(".") && !str.contains(" ")) {

			// Split by dot
			String[] split = str.split("\\.");

			// Exactly two?
			if (split.length == 2) {

				// Well
				String iType = split[0];
				String iID = split[1];

				// All right create filter and go
				ProvidedUIFilter poof = UIFilterManager.getUIFilter("m", iType, iID, "1..", ffp);

				// Valid?
				if (poof != null) {

					// Add
					return poof;

				} else {

					// Send all I guess
					ffp.sendAllTo(MMOItems.getConsole());

					// Ew
					throw new IllegalArgumentException("Invalid Ingredient $u" + str);
				}
			}
		}

		/*
		 * Not a vanilla Material, but what about a UIFilter itself?
		 */
		ProvidedUIFilter poof = UIFilterManager.getUIFilter(str, ffp);

		// Valid?
		if (poof != null) {

			// Add
			return poof;

		} else {

			// Send all I guess
			ffp.sendAllTo(MMOItems.getConsole());

			// Ew
			throw new IllegalArgumentException("Invalid Ingredient $u" + str);
		}
	}
}
