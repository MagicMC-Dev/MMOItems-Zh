package net.Indyuce.mmoitems.api.util.message;

import net.Indyuce.mmoitems.MMOItems;

public class AddonMessage extends PlayerMessage {
	public AddonMessage(String path) {
		super(MMOItems.plugin.getLanguage().getMessage(path));
	}
}
