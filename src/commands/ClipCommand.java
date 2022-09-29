package commands;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.graphql.TwitchGraphQL;
import com.github.twitch4j.graphql.TwitchGraphQLBuilder;
import com.github.twitch4j.helix.domain.CreateClipList;

import aspectibot.AspectiBot;
import aspectibot.TwitchCommand;

public class ClipCommand implements TwitchCommand {

	public String response(ChannelMessageEvent event) {
		
		String[] messageArray = event.getMessage().split(" ");
		
		String clipName = "";
		// get the message data without the command itself
		for(int i = 1; i < messageArray.length; i++) {
			clipName += messageArray[i] + " ";
		}
		
		// credit: https://twitch4j.github.io/rest-helix/clips-create
		
		//CreateClipList clipData = AspectiBot.twitchClient.getHelix().createClip(AspectiBot.oAuth, AspectiBot.aspecticorId, false);
		
		//String clipId = clipData.getData().get(0).getId();
		
		
		
	    AspectiBot.twitchClient.getGraphQL();

	    
		
		
		
		//String clipURL = "https://clips.twitch.tv/" + clipId;
		
		
		
		// send a message in the discord of the clip
		//AspectiBot.jda.getGuildById(AspectiBot.SERVER_ID).getTextChannelById(AspectiBot.CLIP_CHANNEL_ID).sendMessage(clipURL).submit();
		
		// send a message in Twitch Chat that the clip was made
		//return "here's your clip: " + clipURL;
		return "";
	}
	
}
