package net.Indyuce.mmoitems.api.recipe.workbench;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CachedRecipe {
	private final Map<Integer, Integer> amounts = new HashMap<>();
	private ItemStack stack;

	public boolean isValid(ItemStack[] matrix) {
		boolean check = true;
		for (int i = 0; i < matrix.length; i++) {
			if (matrix[i] == null || matrix[i].getType() == Material.AIR)
				continue;
			if (matrix[i].getAmount() < amounts.get(i))
				check = false;
			if (!check)
				break;
		}
		return check;
	}

	public ItemStack[] generateMatrix(ItemStack[] matrix) {
		ItemStack[] newMatrix = new ItemStack[9];
		for (int i = 0; i < matrix.length; i++) {
			ItemStack stack = matrix[i];
			if (stack == null || stack.getType() == Material.AIR) {
				newMatrix[i] = null;
				continue;
			}
			int amountLeft = stack.getAmount() - amounts.get(i);
			if (amountLeft < 1) {
				newMatrix[i] = null;
				continue;
			}
			stack.setAmount(amountLeft);
			newMatrix[i] = stack;
		}

		return newMatrix;
	}

	public void add(int slot, int amount) {
		amounts.put(slot, amount);
	}

	public void setResult(ItemStack result) {
		stack = result;
	}

	public ItemStack getResult() {
		return stack;
	}

	public void clean() {
		for (int i = 0; i < 9; i++)
			if(!amounts.containsKey(i))
				amounts.put(i, 0);
	}
}
