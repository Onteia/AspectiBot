package twitch_commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import aspectibot.TwitchCommand;

public class TwitterCommand implements TwitchCommand {

	public String response(ChannelMessageEvent event) {
		
		return "Aspect's twitter: https://twitter.com/aspecticor";
		
	}
	
}
