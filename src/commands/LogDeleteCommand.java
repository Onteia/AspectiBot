package commands;

import java.io.File;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;

import aspectibot.TwitchCommand;

public class LogDeleteCommand implements TwitchCommand {

    private final Logger LOG = LoggerFactory.getLogger(LogDeleteCommand.class);
    
	public String response(ChannelMessageEvent event) {
		String[] message = event.getMessage().split(" ");
		Set<CommandPermission> perms = event.getPermissions();
		
		if(perms.contains(CommandPermission.BROADCASTER) || perms.contains(CommandPermission.MODERATOR)) {
			String command_name = "INIT";
			try {
				command_name = message[1];
				if(!message[1].substring(0,1).equals("!")) {
					return "Stare stop what you're doing NOW";
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				return "";
			}
			
			try {	
				File delFile = new File("/home/orangepi/jars/persistent/command_log/" + command_name + ".txt");
				
				delFile.delete();
				
				return "";
				
			} catch(Exception e) {
			    LOG.error("response: Unable to delete " + command_name + ". Either " + command_name + ".txt "
			            + "doesn't exist or you don't have permissions to delete the file!");
				return "";
			}
		}
		
		return "";
	}

}
