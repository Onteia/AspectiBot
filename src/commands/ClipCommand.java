package commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.helix.domain.CreateClipList;

import aspectibot.AspectiBot;
import aspectibot.TwitchCommand;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class ClipCommand implements TwitchCommand {

	public String response(ChannelMessageEvent event) {
		
		String[] messageArray = event.getMessage().split(" ");
		
		String clipName = "";
		// get the message data without the command itself
		for(int i = 1; i < messageArray.length; i++) {
			clipName += messageArray[i] + " ";
		}
		
		// credit: https://twitch4j.github.io/rest-helix/clips-create
		CreateClipList clipData = AspectiBot.twitchClient.getHelix()
											.createClip(AspectiBot.oAuth, 
														AspectiBot.aspecticorId,
														false)
											.execute();
		
		String clipId = clipData.getData().get(0).getId();

		String clipURL = "https://clips.twitch.tv/" + clipId;
		
		
		
		// send a message in the discord of the clip
		TextChannel clipChannel = AspectiBot.jda.getTextChannelById(AspectiBot.CLIP_CHANNEL_ID);
		if(clipChannel != null) {
		    if(clipName.equalsIgnoreCase("")) {
		        
		    }
		    String clipMessage = "***" + clipName + "*** clipped by " + event.getUser().getName() + "\n" + clipURL; 
		    
		    clipChannel.sendMessage(clipMessage).submit();
		} else {
		    System.err.println("Clip Channel ID not configured or invalid");
		}
		
		// send a message in Twitch Chat that the clip was made
		return "here's your clip: " + clipURL;
		// return "";
	}
	
}
