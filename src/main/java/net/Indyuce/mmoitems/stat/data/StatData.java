package net.Indyuce.mmoitems.stat.data;

import java.util.Random;
import java.util.logging.Level;

import net.Indyuce.mmoitems.api.item.MMOItem;

public abstract class StatData {
	private MMOItem mmoitem;
	private boolean valid = true;

	protected static final Random random = new Random();

	public void setMMOItem(MMOItem mmoitem) {
		this.mmoitem = mmoitem;
	}

	public boolean hasItem() {
		return mmoitem != null;
	}

	public MMOItem getMMOItem() {
		return mmoitem;
	}

	public boolean isValid() {
		return valid;
	}

	public void throwError(String message) {
		if (hasItem())
			mmoitem.log(Level.WARNING, message);
		valid = false;
	}
}
