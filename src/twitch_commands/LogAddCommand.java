package twitch_commands;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;

import aspectibot.AspectiBot;
import aspectibot.TwitchCommand;
import utils.JSONUtils;

public class LogAddCommand implements TwitchCommand {

    private final Logger LOG = LoggerFactory.getLogger(LogAddCommand.class);
    
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
			    JSONUtils.add(command_name, event.getMessage(), AspectiBot.COMMAND_LOG_PATH);
			} catch(IOException e) {
			    LOG.error("response: Unable to add " + command_name + " to the json file!");
				return "@Onteia you fucked up again! Madge";
			}
		}
		return "";	//if user isn't mod
	}

}
