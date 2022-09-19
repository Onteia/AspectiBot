package commands;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;

import aspectibot.TwitchCommand;

public class LogEditCommand implements TwitchCommand {

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
				File editFile = new File("/home/orangepi/jars/persistent/command_log/" + command_name + ".txt"); 
				
				editFile.delete();
				if(editFile.createNewFile()) {
					String fullMessage = "";
					for(int i = 0; i < message.length; i++) {
						fullMessage += message[i] + " ";
					}
					FileWriter editWriter = new FileWriter(editFile);
					editWriter.write(fullMessage);
					editWriter.close();
				}
				
				return "";
				
			} catch(IOException e) {
				return "@Onteia you done fucked up again! Madge";
			}
		}
		
		return "";
	}

}
