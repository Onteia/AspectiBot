package commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import aspectibot.TwitchCommand;

public class LurkCommand implements TwitchCommand {

	public String response(ChannelMessageEvent event) {
		
		return "Thank you for lurking, " + event.getUser().getName() + "! Don't forget to mute the tab instead of the stream peepoLove";
		
	}
	
}
