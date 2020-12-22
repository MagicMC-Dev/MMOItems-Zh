package net.Indyuce.mmoitems.stat;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.recipe.RecipeListEdition;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.util.AltChar;
import net.mmogroup.mmolib.version.VersionMaterial;

public class Crafting extends ItemStat {
	public Crafting() {
		super("CRAFTING", VersionMaterial.CRAFTING_TABLE.toMaterial(), "Crafting",
				new String[] { "The crafting recipes of your item.", "Changing a recipe requires &o/mi reload recipes&7." }, new String[] { "all" });
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new RecipeListEdition(inv.getPlayer(), inv.getEdited()).open(inv.getPage());

		else if (event.getAction() == InventoryAction.PICKUP_HALF && inv.getEditedSection().contains("crafting")) {
			inv.getEditedSection().set("crafting", null);
			inv.registerTemplateEdition();
			inv.getPlayer()
					.sendMessage(MMOItems.plugin.getPrefix() + "Crafting recipes successfully removed. Make sure you reload active recipes using "
							+ ChatColor.RED + "/mi reload recipes" + ChatColor.GRAY + ".");
		}
	}

	@Override
	public void whenDisplayed(List<String> lore, RandomStatData statData) {
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to access the crafting edition menu.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove all crafting recipes.");
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {
		String type = (String) info[0];

		switch (type) {

		/*
		 * Handles shaped and shapeless crafting recipes
		 */
		case "recipe":
			int slot = (int) info[2];
			Validate.notNull(MMOItems.plugin.getRecipes().getWorkbenchIngredient(message), "Invalid ingredient");

			/*
			 * Handles shaped crafting recipes
			 */
			if ((info[1]).equals("shaped")) {
				List<String> newList = inv.getEditedSection().getStringList("crafting.shaped.1");
				String[] newArray = newList.get(slot / 3).split(" ");
				newArray[slot % 3] = message;
				newList.set(slot / 3, (newArray[0] + " " + newArray[1] + " " + newArray[2]));

				for (String s : newList) {
					if (s.equals("AIR AIR AIR"))
						continue;

					inv.getEditedSection().set("crafting.shaped.1", newList);
					inv.registerTemplateEdition();
					break;
				}

				/*
				 * Handles shapeless crafting recipes
				 */
			} else {
				List<String> newList = inv.getEditedSection().getStringList("crafting.shapeless.1");
				newList.set(slot, message);

				for (String s : newList) {
					if (s.equals("AIR"))
						continue;
					inv.getEditedSection().set("crafting.shapeless.1", newList);
					inv.registerTemplateEdition();
					break;
				}
			}

			break;

		/*
		 * Handles burning recipes ie furnace, campfire, smoker and blast
		 * furnace recipes
		 */
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

		/**
		 * Handles smithing recipes
		 */
		case "smithing": {
			String[] args = message.split(" ");
			Validate.isTrue(args.length == 2, "Invalid format");
			Validate.notNull(MMOItems.plugin.getRecipes().getWorkbenchIngredient(args[0]), "Invalid first ingredient");
			Validate.notNull(MMOItems.plugin.getRecipes().getWorkbenchIngredient(args[1]), "Invalid second ingredient");

			inv.getEditedSection().set("crafting.smithing.1.input1", args[0]);
			inv.getEditedSection().set("crafting.smithing.1.input2", args[1]);
			inv.registerTemplateEdition();
			break;
		}
		default:
			throw new IllegalArgumentException("Recipe type not recognized");
		}
	}

	@Override
	public RandomStatData whenInitialized(Object object) {
		return null;
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
	}
}
