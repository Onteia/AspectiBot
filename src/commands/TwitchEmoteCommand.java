package commands;

import java.util.List;
import java.util.stream.Stream;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.helix.domain.Emote;
import aspectibot.AspectiBot;
import aspectibot.TwitchCommand;

public class TwitchEmoteCommand implements TwitchCommand {

    @Override
    public String response(ChannelMessageEvent event) {
        // returns a random Twitch Global or Channel emote using Helix API
        List<Emote> globals = AspectiBot.twitchClient.getHelix()
                                    .getGlobalEmotes(AspectiBot.oAuth)
                                    .execute()
                                    .getEmotes();
        List<Emote> channels = AspectiBot.twitchClient.getHelix()
                                    .getChannelEmotes(AspectiBot.oAuth, 
                                                    AspectiBot.aspecticorId)
                                    .execute()
                                    .getEmotes();
        List<String> emoteNames = Stream.concat(globals.stream(), 
                                                channels.stream())
                                        .map(Emote::getName)
                                        .toList();
        return emoteNames.get(AspectiBot.R.nextInt(emoteNames.size()));
    }
    
}
