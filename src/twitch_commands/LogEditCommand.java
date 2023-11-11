package twitch_commands;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;

import aspectibot.TwitchCommand;
import utils.CommandLog;

public class LogEditCommand implements TwitchCommand {

    private final Logger LOG = LoggerFactory.getLogger(LogEditCommand.class);
    
	public String response(ChannelMessageEvent event) {
		String[] message = event.getMessage().split(" ");
		Set<CommandPermission> perms = event.getPermissions();
		
		if(perms.contains(CommandPermission.BROADCASTER) || perms.contains(CommandPermission.MODERATOR)) {
			String command_name = "INIT";
			
			for(int i = 1; i < message.length; i++) {
				if(message[i].substring(0, 1).equalsIgnoreCase("!")) {
					command_name = message[i];
					break;
				}
			}
			
			try {	
			    CommandLog.edit(command_name, event.getMessage());
				return "";
			} catch(IOException e) {
			    LOG.error("response: Unable to edit " + command_name + "!");
				return "";
			}
		}
		
		return "";
	}

}
