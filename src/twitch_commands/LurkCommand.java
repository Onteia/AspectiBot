package twitch_commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import aspectibot.TwitchCommand;

public class LurkCommand implements TwitchCommand {

	public String response(ChannelMessageEvent event) {
		
		return "Thank you for lurking, " + event.getUser().getName() + "! Homi";
		
	}
	
}
