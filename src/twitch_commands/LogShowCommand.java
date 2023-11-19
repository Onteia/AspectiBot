package twitch_commands;

import java.io.IOException;
import java.util.Set;

import org.json.JSONException;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;

import aspectibot.AspectiBot;
import aspectibot.TwitchCommand;
import utils.JSONUtils;

public class LogShowCommand implements TwitchCommand {

	public String response(ChannelMessageEvent event) {
		String[] message = event.getMessage().split(" ");
		Set<CommandPermission> perms = event.getPermissions();
		
		if(perms.contains(CommandPermission.BROADCASTER) || perms.contains(CommandPermission.MODERATOR)) {
			if(message.length <= 1) 
			    return "You must provide a command name as an argument!";
			
			String commandName = message[1];
			try {	
				return JSONUtils.get(commandName, AspectiBot.COMMAND_LOG_PATH);
			} catch (IOException e) {
                return "";
            } catch (JSONException e) {
                return "Command doesn't exist!";
            }
		}
		return ""; //if user isn't mod
	}

}
