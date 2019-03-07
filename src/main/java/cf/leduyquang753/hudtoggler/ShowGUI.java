package cf.leduyquang753.hudtoggler;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class ShowGUI extends CommandBase {

	@Override
	public String getCommandName() {
		return "hud";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "Opens HUD settings.";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		Events.shouldOpenGui = true;
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}
	
}
