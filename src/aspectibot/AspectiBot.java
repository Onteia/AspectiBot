package aspectibot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.user.PrivateMessageEvent;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.StreamList;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;

import commands.BrainpowerCommand;
import commands.EmotesCommand;
import commands.LeaderboardCommand;
import commands.LogAddCommand;
import commands.LogDeleteCommand;
import commands.LogEditCommand;
import commands.LogShowCommand;
import commands.LurkCommand;
import commands.PbCommand;
import commands.TwitterCommand;
import commands.UptimeCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class AspectiBot extends ListenerAdapter {

	private static String token; // discord token
	public static String oAuth; // twitch OAuth
	final public static long serverID = 864273305330909204l; // Aspecticor Discord Server
	private static String channelID = "885705830341697536"; // #aspecticor-is-live channel
	public static String logChannelID = "1016876667509166100"; // #server_logs channel
	private long defaultRole = 885698882695229500l; // Aspecticor default role

	public static Icon liveIcon;
	public static Icon offlineIcon;

	public static Stream aspectStream;

	// final public static long serverID = 264217465305825281l; // SELF Discord server
	// final public static String channelID = "488854205637984266"; // #bot channel
	// public static long defaultRole = 889224665002836019l;
	//public static String logChannelID = "488854205637984266"; // #bot channel


	// used for !gay command
	public enum GayMode {

		IDLE, // waiting for stream; default
		GAY, // fill up gay bar; default once stream starts
		OFF; // used to test if not active

	}
	
	public enum StreamStatus {
		LIVE,
		OFFLINE;
	}

	public static int gayCounter = 0;
	public static GayMode gayMode = GayMode.IDLE;
	public static StreamStatus streamStatus = StreamStatus.OFFLINE;
	public static String aspecticorId;

	public static TwitchClient twitchClient;

	public static void main(String[] args) throws Exception {

		//load credentials
		loadCredentials();
		
		
		// set up JDA
		JDA jda = JDABuilder.createDefault(token).setChunkingFilter(ChunkingFilter.ALL)
				.setMemberCachePolicy(MemberCachePolicy.ALL).enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT).build();
		jda.getPresence().setStatus(OnlineStatus.IDLE);
		
		jda.addEventListener(new DiscordServerListener());

		// load offline and live icons
		File liveFile = new File("/home/orangepi/jars/persistent/Aspecticor_Live.png"); 
		File offlineFile = new File("/home/orangepi/jars/persistent/Aspecticor_Offline.png");
		liveIcon = Icon.from(liveFile);
		offlineIcon = Icon.from(offlineFile);

		// set up Twitch4J
		EventManager eventManager = new EventManager();
		eventManager.autoDiscovery();
		eventManager.setDefaultEventHandler(SimpleEventHandler.class);

		// join Aspecticor's chat
		OAuth2Credential credential = new OAuth2Credential("twitch", oAuth);

		TwitchClient twitchClient = TwitchClientBuilder.builder()
				.withEnableHelix(true)
				.withDefaultAuthToken(credential)
				.withEventManager(eventManager)
				.withDefaultEventHandler(SimpleEventHandler.class)
				.withEnableChat(true)
				.withChatAccount(credential)
				.withEnablePubSub(true)
				.build();

		// join Aspect's stream
		twitchClient.getChat().joinChannel("aspecticor");

		// Listen to aspecticor's stream
		twitchClient.getClientHelper().enableStreamEventListener("aspecticor"); // aspecticor stream listener
		aspecticorId = twitchClient.getChat().getChannelNameToChannelId().get("aspecticor");

		// twitchClient.getClientHelper().enableStreamEventListener("onteia"); // onteia
		// stream listener
		twitchClient.getPubSub().listenForChannelPointsRedemptionEvents(credential, aspecticorId);


		// gaybar fills up a gay bar, at 100% makes Aspect take his shirt off
		gayCommand(eventManager, twitchClient, aspecticorId);

		// if Aspecticor is live change activity and status; also change server icon
		goLive(eventManager, twitchClient, jda);

		// if Aspect turns stream off, change icon back and set status to invisible.
		goOffline(eventManager, twitchClient, jda);

		whisper(eventManager, twitchClient, aspecticorId);

		Map<String, TwitchCommand> commands = new HashMap<>();

		commands.put("!brainpower", new BrainpowerCommand());
		commands.put("!emotes", new EmotesCommand());
		commands.put("!leaderboard", new LeaderboardCommand());
		commands.put("!lurk", new LurkCommand());
		commands.put("!pb", new PbCommand());
		commands.put("!twitter", new TwitterCommand());
		commands.put("!uptime", new UptimeCommand());
		commands.put("!addcom", new LogAddCommand());
		commands.put("!showcom", new LogShowCommand());
		commands.put("!delcom", new LogDeleteCommand());
		commands.put("!editcom", new LogEditCommand());
		
		eventManager.onEvent(ChannelMessageEvent.class, event -> {

			String cmd = event.getMessage().toLowerCase().split(" ")[0];

			TwitchCommand command;
			
			if ((command = commands.get(cmd)) != null) { // if the input is in the hashmap
				
				twitchClient.getChat().sendMessage("aspecticor", command.response(event), "", event.getMessageEvent().getMessageId().get()); // post the proper response
				
			}

		});


	} // end of main method

	public void onGuildMemberJoin(GuildMemberJoinEvent event) {

		// get the member who joined
		Member member = event.getMember();
		// give member who joined the default role
		event.getJDA().getGuildById(serverID).addRoleToMember(member, event.getJDA().getRoleById(defaultRole)).queue();

	} // end of onGuildMemberJoin method

	

	public static void gayCommand(EventManager eventManager, TwitchClient twitchClient, String aspecticorId) {

		eventManager.onEvent(RewardRedeemedEvent.class, event -> {

			// System.out.println("[" + event.getChannel().getName() + "] " +
			// event.getUser().getName() + ": " + event.getMessage());

			// String user = event.getRedemption().getUser().getDisplayName();
			String reward = event.getRedemption().getReward().getTitle();

			if (reward.equalsIgnoreCase("GAI")) {

				//String rewardId = event.getRedemption().getReward().getId();
				String redeemId = event.getRedemption().getId();

				ArrayList<String> redeemList = new ArrayList<String>();
				redeemList.add(redeemId);

				gayCounter++;

				if (gayCounter >= 100 && gayMode == GayMode.GAY) {

					twitchClient.getChat().sendMessage("aspecticor",
							"@Aspecticor GayBar is FULL!!! TAKE KappaPride OFF KappaPride SHIRT KappaPride @Aspecticor");
					gayMode = GayMode.OFF;

				} else {

					if (gayCounter % 10 == 0) {

						twitchClient.getChat().sendMessage("aspecticor", "GayBar: " + gayCounter + "%");

					}
				}

				// twitchClient.getHelix().updateRedemptionStatus(oAuth, aspecticorId, rewardId, redeemId, RedemptionStatus.FULFILLED);

			}

		});

	}

	public static void goLive(EventManager eventManager, TwitchClient twitchClient, JDA jda) {

		eventManager.onEvent(ChannelGoLiveEvent.class, event -> {
			if(streamStatus == StreamStatus.OFFLINE) {
				streamStatus = StreamStatus.LIVE;
				jda.getPresence().setStatus(OnlineStatus.ONLINE);
				jda.getPresence().setActivity(Activity.watching("Aspecticor's Stream"));
				String streamTitle = event.getStream().getTitle();
				jda.getTextChannelById(channelID).sendMessage("<@&882772072475017258> We are live playing \"" + streamTitle
						+ "\" ***right now!***\nhttps://www.twitch.tv/aspecticor").queue();
	
				// change icon to Live version
				jda.getGuildById(serverID).getManager().setIcon(liveIcon).queue();
	
				// enable !gay and reset it
				gayCounter = 0;
				gayMode = GayMode.GAY;
	
				// get aspect's stream
				ArrayList<String> stringList = new ArrayList<String>();
				stringList.add(aspecticorId);
				StreamList streams = twitchClient.getHelix().getStreams(oAuth, "", "", 1, null, null, stringList, null)
						.execute();
				aspectStream = streams.getStreams().get(0);
			}
		});

	}

	public static void goOffline(EventManager eventManager, TwitchClient twitchClient, JDA jda) {

		eventManager.onEvent(ChannelGoOfflineEvent.class, event -> {
			streamStatus = StreamStatus.OFFLINE;
			jda.getPresence().setStatus(OnlineStatus.IDLE);

			// change icon to Offline version
			jda.getGuildById(serverID).getManager().setIcon(offlineIcon).queue();

			// reset and disable !gay
			gayCounter = 0;
			gayMode = GayMode.IDLE;

		});

	} // end of goOffline method

	public static void whisper(EventManager eventManager, TwitchClient twitchClient, String aspecticorId) {

		eventManager.onEvent(PrivateMessageEvent.class, event -> {

			System.out.println(event.getUser().getName() + " sent " + event.getMessage());
			if (event.getUser().getName().equalsIgnoreCase("onteia")) {
				System.out.println(event.getMessage());
				twitchClient.getChat().sendMessage("aspecticor", event.getMessage());				
			}

		});
	
	} // end of onWhisper method
	
	public static void loadCredentials() {
		
		try {

			// get the files
			File discordToken = new File("/home/orangepi/jars/persistent/discordToken.txt");	//"C:\\Users\\ASUS\\DarkJudas\\persistent\\discordToken.txt" ; "/home/orangepi/jars/persistent/discordToken.txt"
			File twitchToken = new File("/home/orangepi/jars/persistent/twitchOAuth.txt");		//"C:\\Users\\ASUS\\DarkJudas\\persistent\\twitchOAuth.txt" ; "/home/orangepi/jars/persistent/twitchOAuth.txt"

			// read the files
			BufferedReader br1 = new BufferedReader(new FileReader(discordToken));
			BufferedReader br2 = new BufferedReader(new FileReader(twitchToken));
			token = br1.readLine();
			oAuth = br2.readLine();
			br1.close();
			br2.close();

		} catch (Exception e) {

			System.err.println("Authentication Failed");

		}
		
	}

}
