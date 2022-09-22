package aspectibot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

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

	// CHANGE THESE VARIABLES
	
	private static final String ASPECTICOR = "aspecticor";
	
	private static String DISCORD_TOKEN_PATH = "/home/orangepi/jars/persistent/discordToken.txt";
	private static String TWITCH_TOKEN_PATH = "/home/orangepi/jars/persistent/twitchOAuth.txt";
	
	/* Aspecticord settings */
	final public static long SERVER_ID = 864273305330909204L; // Aspecticord Server ID
	private static long LIVE_CHANNEL_ID = 885705830341697536L; // #aspecticor-is-live channel
	public static long LOG_CHANNEL_ID = 1016876667509166100L; // #server_logs channel
	private long DEFAULT_ROLE = 885698882695229500L; // Aspecticord default role
	final private static long PING_ROLE = 882772072475017258L; // Aspecticord @TWITCH_PINGS	
	
	private static String token; // discord token
	public static String oAuth; // twitch OAuth
	public static Icon liveIcon;
	public static Icon offlineIcon;

	public static String LIVE_ICON_PATH = "/home/orangepi/jars/persistent/Aspecticor_Live.png";
	public static String OFFLINE_ICON_PATH = "/home/orangepi/jars/persistent/Aspecticor_Offline.png";
	
	public static Stream aspectStream;
	
	public enum StreamStatus {
		LIVE,
		OFFLINE;
	}

	private static StreamStatus streamStatus = StreamStatus.OFFLINE;
	
	public static String aspecticorId;
	public static TwitchClient twitchClient;

	private static Random r = new Random();

	public static void main(String[] args) throws Exception {

		// https://niruhan.medium.com/how-to-add-a-config-file-to-a-java-project-99fd9b6cebca
		try (
			FileInputStream config = new FileInputStream("src/config.properties")
		) {
			Properties prop = new Properties();
			prop.load(config);
			DISCORD_TOKEN_PATH = prop.getProperty("DISCORD_TOKEN_PATH");
			TWITCH_TOKEN_PATH = prop.getProperty("TWITCH_TOKEN_PATH");
			LIVE_ICON_PATH = prop.getProperty("LIVE_ICON_PATH");
			OFFLINE_ICON_PATH = prop.getProperty("OFFLINE_ICON_PATH");
		} catch (FileNotFoundException e) {
			//no config file
		} finally {
			//load credentials
			loadCredentials();
		}
		
		// set up JDA
		JDA jda = JDABuilder.createDefault(token)
				.setChunkingFilter(ChunkingFilter.ALL)
				.setMemberCachePolicy(MemberCachePolicy.ALL)
				.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
				.build();
		jda.getPresence().setStatus(OnlineStatus.IDLE);	
		jda.addEventListener(new DiscordServerListener());

		// load offline and live icons
		File liveFile = new File(LIVE_ICON_PATH); 
		File offlineFile = new File(OFFLINE_ICON_PATH);
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
				.withEnableTMI(true)
				.build();

		// join Aspect's stream
		twitchClient.getChat().joinChannel(ASPECTICOR);	

		// Listen to aspecticor's stream
		twitchClient.getClientHelper().enableStreamEventListener(ASPECTICOR); // aspecticor stream listener
		aspecticorId = twitchClient.getChat().getChannelNameToChannelId().get(ASPECTICOR);

		// if Aspecticor is live change activity and status; also change server icon
		goLive(eventManager, twitchClient, jda);

		// if Aspect turns stream off, change icon back and set status to idle.
		goOffline(eventManager, twitchClient, jda);

		whisper(eventManager, twitchClient, aspecticorId);

		Map<String, TwitchCommand> commands = new HashMap<>();

		commands.put("!brainpower", new BrainpowerCommand());
		commands.put("!emotes", new EmotesCommand());
		commands.put("!leaderboards", new LeaderboardCommand());
		commands.put("!lurk", new LurkCommand());
		commands.put("!pb", new PbCommand());
		commands.put("!twitter", new TwitterCommand());
		commands.put("!addcom", new LogAddCommand());
		commands.put("!showcom", new LogShowCommand());
		commands.put("!delcom", new LogDeleteCommand());
		commands.put("!editcom", new LogEditCommand());
		
		eventManager.onEvent(ChannelMessageEvent.class, event -> {

			String cmd = event.getMessage().toLowerCase().split(" ")[0];

			TwitchCommand command;
			
			if ((command = commands.get(cmd)) != null) { // if the input is in the hashmap
				
				twitchClient.getChat().sendMessage(ASPECTICOR, command.response(event), "", event.getMessageEvent().getMessageId().get()); // post the proper response
				
			}

		});


	} // end of main method

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {

		// get the member who joined
		Member member = event.getMember();
		// give member who joined the default role
		event.getJDA().getGuildById(SERVER_ID).addRoleToMember(member, event.getJDA().getRoleById(DEFAULT_ROLE)).queue();

	} // end of onGuildMemberJoin method

	public static void goLive(EventManager eventManager, TwitchClient twitchClient, JDA jda) {

		eventManager.onEvent(ChannelGoLiveEvent.class, event -> {
			if(streamStatus == StreamStatus.OFFLINE) {
				streamStatus = StreamStatus.LIVE;
				jda.getPresence().setStatus(OnlineStatus.ONLINE);
				jda.getPresence().setActivity(Activity.watching("Aspecticor's Stream"));
				String streamTitle = event.getStream().getTitle();
				jda.getTextChannelById(LIVE_CHANNEL_ID).sendMessage("<@&"+ PING_ROLE +"> We are live playing \"" + streamTitle
						+ "\" ***right now!***\nhttps://www.twitch.tv/aspecticor").queue();
	
				// change icon to Live version
				jda.getGuildById(SERVER_ID).getManager().setIcon(liveIcon).queue();
	
				// get aspect's stream
				ArrayList<String> stringList = new ArrayList<>();
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
			
			//credit: https://whaa.dev/how-to-generate-random-characters-in-java
			StringBuilder randomKey = new StringBuilder();
			for(int i = 0; i < 30; i++) {
				char randomCharacter = (char)((r.nextBoolean() ? 'a' : 'A') + r.nextInt(26));
				randomKey.append(randomCharacter);
			}
			
			String fakeKey = "live_" + r.nextInt(1000000000) + "_" + randomKey.toString();
			String[] randResponses = {"Aspecticor's VODS", "Aspecticor's Clips",
					"Aspecticor's YT Videos", "Aspecticor's TikToks", 
					"Aspecticor get cancelled on Twitter",
					"Aspecticor die of liver failure", "Aspecticor's TED Talk", 
					"Aspecticor's Brentwood College School Musical Performance",
					"Aspecticor's WACK ASS NAILS grow", "Aspecticor do a flip", 
					"Aspecticor falling into the toilet yet again", 
					"chaos ensue on Aspecticor's dying Subreddit", 
					"Aspecticor going back to Paris",
					"Aspecticor's mouse get stolen by Hitman again", 
					"an NPC catch Aspecticor's heinous crimes",
					"Aspecticor's bi flag get torn down by Mercs", 
					"Aspecticor and Katie's DNA tests being a direct match",
					"Aspecticor go live without looking at his DMs", 
					"Soylent Splive believe in yet another conspiracy theory",
					"Aspecticor watch 'cutscenes'", "Mercs assault the top of Aspecticor's chair", 
					"Mercs do a WICKED jump", "Aspecticor leak his stream key: " + fakeKey};
			jda.getPresence().setActivity(Activity.watching(randResponses[r.nextInt(randResponses.length)]));
			
			// change icon to Offline version
			jda.getGuildById(SERVER_ID).getManager().setIcon(offlineIcon).queue();
		});

	} // end of goOffline method

	public static void whisper(EventManager eventManager, TwitchClient twitchClient, String aspecticorId) {
		// if a mod in twitch channel whispers bot, send chat to that twitch channel
		eventManager.onEvent(PrivateMessageEvent.class, event -> {
			List<String> mods = null;
			System.out.print(event.getUser().getName() + " sent " + event.getMessage());
			try {
				mods = twitchClient.getMessagingInterface()
								   .getChatters(ASPECTICOR)
								   .execute()
								   .getModerators();
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				System.err.println(sw.toString());
			} finally {
				System.out.println(" " + mods);
				if (mods.contains(event.getUser().getName())) {

					twitchClient.getChat().sendMessage(ASPECTICOR, event.getMessage());

				}
			}
		});
	
	} // end of onWhisper method
	

	public static void loadCredentials() {
		
		try {
			
			// get the files
			File discordToken = new File(DISCORD_TOKEN_PATH);
			File twitchToken = new File(TWITCH_TOKEN_PATH);
			
			// read the files
			// https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
			try (
				BufferedReader br1 = new BufferedReader(new FileReader(discordToken));
				BufferedReader br2 = new BufferedReader(new FileReader(twitchToken));
			) {
				token = br1.readLine();
				oAuth = br2.readLine();
			}
		} catch (Exception e) {

			System.err.println("Authentication Failed");

		}
		
	}

}
