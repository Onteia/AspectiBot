package commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import aspectibot.TwitchCommand;

public class EmotesCommand implements TwitchCommand {

	public String response(ChannelMessageEvent event) {
		
		return "If you're not able to see all emotes, get the BTTV, FFZ, and 7TV extensions!";
		
	}
	
}
