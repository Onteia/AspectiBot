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

public class LogDeleteCommand implements TwitchCommand {

    private final Logger LOG = LoggerFactory.getLogger(LogDeleteCommand.class);
    
	public String response(ChannelMessageEvent event) {
		String[] message = event.getMessage().split(" ");
		Set<CommandPermission> perms = event.getPermissions();
		
		if(perms.contains(CommandPermission.BROADCASTER) || perms.contains(CommandPermission.MODERATOR)) {
			if(message.length <= 1)
			    return "";
			try {	
				JSONUtils.delete(message[1], AspectiBot.COMMAND_LOG_PATH);
				return "";
			} catch(IOException e) {
			    LOG.error("response: Unable to delete " + message[1] + " from the json!");
				return "";
			}
		}
		return "";
	}

}
