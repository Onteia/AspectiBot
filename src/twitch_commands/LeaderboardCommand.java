package twitch_commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import aspectibot.TwitchCommand;

public class LeaderboardCommand implements TwitchCommand {

	public String response(ChannelMessageEvent event) {
		
		return "Aspect: https://www.speedrun.com/user/aspecticor | Trilogy: https://www.speedrun.com/hitman_3/full_game#Trilogy_Campaign";
		
	}
	
}
