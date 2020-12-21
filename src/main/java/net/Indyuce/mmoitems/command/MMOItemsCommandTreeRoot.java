package net.Indyuce.mmoitems.command;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.command.item.ItemCommandTreeNode;
import net.Indyuce.mmoitems.command.mmoitems.AbilityCommandTreeNode;
import net.Indyuce.mmoitems.command.mmoitems.AllItemsCommandTreeNode;
import net.Indyuce.mmoitems.command.mmoitems.BrowseCommandTreeNode;
import net.Indyuce.mmoitems.command.mmoitems.CopyCommandTreeNode;
import net.Indyuce.mmoitems.command.mmoitems.CreateCommandTreeNode;
import net.Indyuce.mmoitems.command.mmoitems.DeleteCommandTreeNode;
import net.Indyuce.mmoitems.command.mmoitems.DropCommandTreeNode;
import net.Indyuce.mmoitems.command.mmoitems.EditCommandTreeNode;
import net.Indyuce.mmoitems.command.mmoitems.GenerateCommandTreeNode;
import net.Indyuce.mmoitems.command.mmoitems.GiveAllCommandTreeNode;
import net.Indyuce.mmoitems.command.mmoitems.GiveCommandTreeNode;
import net.Indyuce.mmoitems.command.mmoitems.ItemListCommandTreeNode;
import net.Indyuce.mmoitems.command.mmoitems.ReloadCommandTreeNode;
import net.Indyuce.mmoitems.command.mmoitems.debug.DebugCommandTreeNode;
import net.Indyuce.mmoitems.command.mmoitems.list.ListCommandTreeNode;
import net.Indyuce.mmoitems.command.mmoitems.revid.RevisionIDCommandTreeNode;
import net.Indyuce.mmoitems.command.mmoitems.stations.StationsCommandTreeNode;
import net.Indyuce.mmoitems.command.mmoitems.update.UpdateCommandTreeNode;
import net.mmogroup.mmolib.command.api.CommandTreeRoot;
import net.mmogroup.mmolib.command.api.Parameter;

public class MMOItemsCommandTreeRoot extends CommandTreeRoot {
	public static final Parameter TYPE = new Parameter("<type>",
			(explorer, list) -> MMOItems.plugin.getTypes().getAll().forEach(type -> list.add(type.getId())));
	public static final Parameter ID_2 = new Parameter("<id>", (explorer, list) -> {
		try {
			Type type = Type.get(explorer.getArguments()[1]);
			MMOItems.plugin.getTemplates().getTemplates(type).forEach(template -> list.add(template.getId()));
		} catch (Exception ignored) {
		}
	});

	public MMOItemsCommandTreeRoot() {
		super("mmoitems", "mmoitems.admin");

		addChild(new CreateCommandTreeNode(this));
		addChild(new DeleteCommandTreeNode(this));
		addChild(new EditCommandTreeNode(this));
		addChild(new CopyCommandTreeNode(this));
		addChild(new GiveCommandTreeNode(this));

		addChild(new GenerateCommandTreeNode(this));
		// addChild(new HelpCommandTreeNode(this));
		addChild(new BrowseCommandTreeNode(this));
		addChild(new UpdateCommandTreeNode(this));
		addChild(new DebugCommandTreeNode(this));
		addChild(new ReloadCommandTreeNode(this));
		addChild(new StationsCommandTreeNode(this));
		addChild(new AllItemsCommandTreeNode(this));
		addChild(new ListCommandTreeNode(this));
		addChild(new DropCommandTreeNode(this));
		addChild(new AbilityCommandTreeNode(this));
		addChild(new GiveAllCommandTreeNode(this));
		addChild(new ItemListCommandTreeNode(this));
		addChild(new RevisionIDCommandTreeNode(this));
		
		addChild(new ItemCommandTreeNode(this));
	}
}
