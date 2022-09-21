package commands;

import java.util.ArrayList;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.helix.domain.StreamList;

import aspectibot.AspectiBot;
import aspectibot.TwitchCommand;

public class UptimeCommand implements TwitchCommand {

	public String response(ChannelMessageEvent event) {
		
		try {
			
			ArrayList<String> stringList = new ArrayList<>();
			stringList.add(AspectiBot.aspecticorId);
			StreamList streams = AspectiBot.twitchClient.getHelix().getStreams(AspectiBot.oAuth, "", "", 1, null, null, stringList, null)
					.execute();
			AspectiBot.aspectStream = streams.getStreams().get(0);
			
			long seconds = AspectiBot.aspectStream.getUptime().getSeconds();
			int hours = (int) (seconds/3600);
			int minutes = (int) (seconds/60) - (hours * 60);
			seconds = seconds - (hours * 3600) - (minutes * 60);
			
			return "Ignore Nightbot, stream uptime is: " + hours + "h " + minutes + "m " + (int) seconds + "s";
		
		} catch(Exception e) {
			
			return "";
			
		}
	}

}
