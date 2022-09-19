package commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;

import aspectibot.TwitchCommand;

public class LogShowCommand implements TwitchCommand {

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
				return "You must provide a command name as an argument!";
			}
			try {	
				File showFile = new File("/home/orangepi/jars/persistent/command_log/" + command_name + ".txt"); //"/home/orangepi/jars/persistent/command_log/"
				Scanner showScanner = new Scanner(showFile);
				
				String output = "";
				while(showScanner.hasNextLine()) {
					output += showScanner.nextLine();
				}
				showScanner.close();
				return output;
				
			} catch(FileNotFoundException e) {
				return "Command doesn't exist!";
			}
		}
		
		return ""; //if user isn't mod
	}

}
