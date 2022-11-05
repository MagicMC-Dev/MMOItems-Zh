package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import io.lumine.mythic.lib.api.crafting.uimanager.UIFilterManager;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.ui.QuickNumberRange;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.gui.edition.recipe.RecipeBrowserGUI;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeMakerGUI;
import net.Indyuce.mmoitems.gui.edition.recipe.interpreter.RMG_RecipeInterpreter;
import net.Indyuce.mmoitems.gui.edition.recipe.rba.RecipeButtonAction;
import net.Indyuce.mmoitems.stat.data.StringData;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Crafting extends ItemStat<RandomStatData<StatData>, StatData> {
	public Crafting() {
		super("CRAFTING", VersionMaterial.CRAFTING_TABLE.toMaterial(), "Crafting",
				new String[] { "The crafting recipes of your item.", "Changing a recipe requires &o/mi reload recipes&7." }, new String[] { "all" });
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new RecipeBrowserGUI(inv.getPlayer(), inv.getEdited()).open(inv.getPage());

		else if (event.getAction() == InventoryAction.PICKUP_HALF && inv.getEditedSection().contains("crafting")) {
			inv.getEditedSection().set("crafting", null);
			inv.registerTemplateEdition();
			inv.getPlayer()
					.sendMessage(MMOItems.plugin.getPrefix() + "Crafting recipes successfully removed. Make sure you reload active recipes using "
							+ ChatColor.RED + "/mi reload recipes" + ChatColor.GRAY + ".");
		}
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData<StatData>> statData) {
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to access the crafting edition menu.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove all crafting recipes.");
	}

	/**
	 * This stat is not saved within the item. This method returns a StringData with its value as <code>null</code>,
	 * though it is just a placeholder, for this method truly has no data associated to it.
	 */
	@NotNull
	@Override
	public StatData getClearStatData() {
		return new StringData(null);
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {

		/*
		 * #1 Type - Is it input, output, or a button being pressed?
		 */
		int type = (int) info[0];

		switch (type) {
			case RecipeMakerGUI.INPUT:
			case RecipeMakerGUI.OUTPUT:

				//region Transcribe from old format to new
				int spc = message.indexOf(' ');
				QuickNumberRange qnr = null;
				if (spc > 0) {

					// Any space? attempt to parse that as a number
					String qnrp = message.substring(spc + 1);

					// Is it just a number 'X' ?
					if (SilentNumbers.DoubleTryParse(qnrp)) {

						/*
						 * In technical QNR jargon, X means "requires exactly this",
						 * however, many times when crafting, specifying X means that
						 * crafting it once requires that many ingredients.
						 *
						 * Translating the crafting intention into QNR outputs X..
						 *
						 * If anyone truly means that the recipe can only be crafted
						 * having X in the same slot of the crafting table, they will
						 * have to write X..X
						 */
						qnrp += "..";
					}

					// Parse QNR
					qnr = QuickNumberRange.getFromString(qnrp);
				}

				/*
				 * Changes easy MMOItems input into MythicLib NBT Filter.
				 */
				if (spc <= 0 || qnr != null) {

					// No amount specified=
					if (qnr == null) {

						// Default is one and onward, 1..
						qnr = new QuickNumberRange(1D, null);

					// Amount was specified
					} else {

						// Crop from message
						message = message.substring(0, spc);
					}

					// MMOItem?
					if (message.contains(".")) {

						// Split
						String[] midSplit = message.split("\\.");

						// MMOItem UIFilter
						message = "m " + midSplit[0] + " " + midSplit[1] + " " + qnr;

						// Vanilla material
					} else {

						// Vanilla UIFilter
						message = "v " + message + " - " + qnr;
					}
				}
				//endregion

				/*
				 * #2 Recipe Interpreter - Correctly edits the configuration section in the files,
				 *                         depending on how the recipe is supposed to be saved.
				 */
				RMG_RecipeInterpreter interpreter = (RMG_RecipeInterpreter) info[1];

				/*
				 * #3 Slot - Which slot was pressed?
				 */
				int slot = (int) info[2];

				// Attempt to get
				ProvidedUIFilter read = UIFilterManager.getUIFilter(message, inv.getFFP());

				// Null? Cancel
				if (read == null) { throw new IllegalArgumentException(""); }
				if (!read.isValid(inv.getFFP())) { throw new IllegalArgumentException(""); }

				// Find section
				ConfigurationSection section = RecipeMakerGUI.getSection(inv.getEditedSection(), "crafting");
				section = RecipeMakerGUI.getSection(section, ((RecipeMakerGUI) inv).getRecipeRegistry().getRecipeConfigPath());
				section = RecipeMakerGUI.getSection(section, ((RecipeMakerGUI) inv).getRecipeName());

				// Redirect
				if (type == RecipeMakerGUI.INPUT)  {
					interpreter.editInput(read, slot);

				// It must be output
				} else {
					interpreter.editOutput(read, slot); }

				// Save changes
				inv.registerTemplateEdition();

				break;
			case RecipeMakerGUI.PRIMARY:
			case RecipeMakerGUI.SECONDARY:

				/*
				 * No Button Action? That's the end, and is not necessarily
				 * an error (the button might have done what it had to do
				 * already when pressed, if it needed no user input).
				 */
				if (info.length < 2) { return; }
				if (!(info[1] instanceof RecipeButtonAction)) { return; }

				// Delegate

				if (type == RecipeMakerGUI.PRIMARY)  {
					((RecipeButtonAction) info[1]).primaryProcessInput(message, info);

				} else {
					((RecipeButtonAction) info[1]).secondaryProcessInput(message, info); }

				// Save changes
				inv.registerTemplateEdition();
				break;

			default: inv.registerTemplateEdition(); break;
		}

		/*
		 * Handles burning recipes ie furnace, campfire, smoker and blast
		 * furnace recipes
		 *
			case "item": {
				String[] args = message.split(" ");
				Validate.isTrue(args.length == 3, "Invalid format");
				Validate.notNull(MMOItems.plugin.getRecipes().getWorkbenchIngredient(args[0]), "Invalid ingredient");
				int time = Integer.parseInt(args[1]);
				double exp = MMOUtils.parseDouble(args[2]);

				inv.getEditedSection().set("crafting." + info[1] + ".1.item", args[0]);
				inv.getEditedSection().set("crafting." + info[1] + ".1.time", time);
				inv.getEditedSection().set("crafting." + info[1] + ".1.experience", exp);
				inv.registerTemplateEdition();
				break;
			}

		*/
	}

	@Nullable
	@Override
	public RandomStatData whenInitialized(Object object) {
		return null;
	}

	/**
	 * This stat is not saved within the item. This method is empty.
	 */
	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) { }

	/**
	 * This stat is not saved within the item. This method is always an empty array.
	 */
	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull StatData data) { return new ArrayList<>(); }

	/**
	 * This stat is not saved within the item. This method is empty.
	 */
	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) { }

	/**
	 * This stat is not saved within the item. This method is always null.
	 */
	@Nullable
	@Override
	public StatData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) { return null; }
}
