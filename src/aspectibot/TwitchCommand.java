package aspectibot;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

public interface TwitchCommand {

	public String response(ChannelMessageEvent event);
	
}
