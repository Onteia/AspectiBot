package aspectibot;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.imageio.ImageIO;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.user.PrivateMessageEvent;
import com.github.twitch4j.common.util.CryptoUtils;
import com.github.twitch4j.events.ChannelChangeGameEvent;
import com.github.twitch4j.events.ChannelChangeTitleEvent;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.github.twitch4j.events.ChannelViewerCountUpdateEvent;
import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.Video;

import commands.BrainpowerCommand;
import commands.ClipCommand;
import commands.EmotesCommand;
import commands.LeaderboardCommand;
import commands.LogAddCommand;
import commands.LogDeleteCommand;
import commands.LogEditCommand;
import commands.LogShowCommand;
import commands.LurkCommand;
import commands.PbCommand;
import commands.TwitterCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class AspectiBot extends ListenerAdapter {
	
	private static final String ASPECTICOR = "aspecticor";
	private static final String CONFIG_FILE = "src/config.properties";

	private static String DISCORD_TOKEN_PATH;
	private static String TWITCH_TOKEN_PATH;
	private static String LIVE_ICON_PATH;
	private static String OFFLINE_ICON_PATH;
	private static String THIS_FOLDER_PATH;
	
	/* Aspecticord settings */
	final public static long SERVER_ID = 864273305330909204L; // Aspecticord Server ID
	private static long LIVE_CHANNEL_ID = 885705830341697536L; // #aspecticor-is-live channel
	public static final long LOG_CHANNEL_ID = 1016876667509166100L; // #server_logs channel
	public static final long CLIP_CHANNEL_ID = 867258015220236319L;	// #clips channel
	private long DEFAULT_ROLE = 885698882695229500L; // Aspecticord default role
	final private static String PING_ROLE = "882772072475017258"; // Aspecticord @TWITCH_PINGS	
	
	 /*
	public static final long SERVER_ID = 264217465305825281L; // SELF Discord server
	public static final long LIVE_CHANNEL_ID = 1022422500161900634L; // #bot channel
	public static final long LOG_CHANNEL_ID = 1022427876609495100L; // #bot channel
	public static final long CLIP_CHANNEL_ID = 1024597131488665601L; // #clips channel
	public static final long DEFAULT_ROLE = 963139708655919145L;
	 */

	private static String token; // discord token
	public static String oAuth; // twitch OAuth
	public static Icon liveIcon;
	public static Icon offlineIcon;
	
	public enum StreamStatus {
		LIVE,
		OFFLINE;
	}

	private static StreamStatus streamStatus = StreamStatus.OFFLINE;
	
	public static String aspecticorId;
	public static TwitchClient twitchClient;
	public static JDA jda;
	public static Message streamNotificationMessage = null;

	private static Random r = new Random();

	public static void main(String[] args) throws Exception {

		// https://niruhan.medium.com/how-to-add-a-config-file-to-a-java-project-99fd9b6cebca
		try (
			FileInputStream config = new FileInputStream(CONFIG_FILE);
		) {
			Properties prop = new Properties();
			prop.load(config);
			DISCORD_TOKEN_PATH = prop.getProperty("DISCORD_TOKEN_PATH");
			TWITCH_TOKEN_PATH = prop.getProperty("TWITCH_TOKEN_PATH");
			LIVE_ICON_PATH = prop.getProperty("LIVE_ICON_PATH");
			OFFLINE_ICON_PATH = prop.getProperty("OFFLINE_ICON_PATH");
		} catch (FileNotFoundException e) {
			//no config file
			DISCORD_TOKEN_PATH = "/home/orangepi/jars/persistent/discordToken.txt";
			TWITCH_TOKEN_PATH = "/home/orangepi/jars/persistent/twitchOAuth.txt";
			LIVE_ICON_PATH = "/home/orangepi/jars/persistent/Aspecticor_Live.png";
			OFFLINE_ICON_PATH = "/home/orangepi/jars/persistent/Aspecticor_Offline.png";
			THIS_FOLDER_PATH = "/home/orangepi/jars/AspectiBot/";
		} finally {
			//load credentials
			loadCredentials();
		}
		
		// set up JDA
		jda = JDABuilder.createDefault(token)
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

		twitchClient = TwitchClientBuilder.builder()
				.withEnableHelix(true)
				.withDefaultAuthToken(credential)
				.withEventManager(eventManager)
				.withDefaultEventHandler(SimpleEventHandler.class)
				.withEnableChat(true)
				.withChatAccount(credential)
				.withEnablePubSub(true)
				.withEnableTMI(true)
				.withEnableGraphQL(true)
				.build();

		// join Aspect's stream
		twitchClient.getChat().joinChannel(ASPECTICOR);	

		// Listen to aspecticor's stream
		twitchClient.getClientHelper().enableStreamEventListener(ASPECTICOR);
		aspecticorId = twitchClient.getChat().getChannelNameToChannelId().get(ASPECTICOR);

		// if Aspecticor is live change activity and status
		// also change server icon
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
		commands.put("!clip", new ClipCommand());

		eventManager.onEvent(ChannelMessageEvent.class, event -> {

			String cmd = event.getMessage().toLowerCase().split(" ")[0];

			TwitchCommand command;
			
			if ((command = commands.get(cmd)) != null) { 
				// if the input is in the hashmap
				
				twitchClient.getChat().sendMessage(
					ASPECTICOR, command.response(event), 
					"", event.getMessageEvent().getMessageId().get()); 
					// post the proper response
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
				
				EmbedBuilder goLiveEmbed = formatEmbed(event.getStream());	
				streamNotificationMessage = jda.getNewsChannelById(AspectiBot.LIVE_CHANNEL_ID)
						.sendMessage("<@&"+ PING_ROLE +"> HE'S LIVE!!!")
						.addEmbeds(goLiveEmbed.build())
						.complete();
				
				// change icon to Live version
				jda.getGuildById(SERVER_ID).getManager().setIcon(liveIcon).queue();

			}
		});
		// Update stream info when title is changed
		eventManager.onEvent(ChannelChangeTitleEvent.class, event -> {
			EmbedBuilder newEmbed = formatEmbed(event.getStream());
			streamNotificationMessage = streamNotificationMessage.editMessageEmbeds(newEmbed.build()).complete();
		});
		// Update stream info when game/category is changed
		eventManager.onEvent(ChannelChangeGameEvent.class, event -> {
			EmbedBuilder newEmbed = formatEmbed(event.getStream());
			streamNotificationMessage = streamNotificationMessage.editMessageEmbeds(newEmbed.build()).complete();
		});
		// Update stream info when viewercount changes
		eventManager.onEvent(ChannelViewerCountUpdateEvent.class, event -> {
			System.err.println("Change in viewer count");
			EmbedBuilder newEmbed = formatEmbed(event.getStream());
			streamNotificationMessage = streamNotificationMessage.editMessageEmbeds(newEmbed.build()).complete();
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
			
			List<Video> vodList = twitchClient.getHelix().getVideos(oAuth, null, aspecticorId, null, null, "day", "time", "archive", null, null, 1).execute().getVideos();
			Video latestVod = vodList.get(0);
			
			try {
				// credit: https://www.techiedelight.com/download-file-from-url-java/
				// get a local version of the thumbnail to set up for adding overlay
				
				int width = 1920;
				int height = 1080;
				
				URL vodThumbURL = new URL(latestVod.getThumbnailUrl(width, height));
				InputStream in = vodThumbURL.openStream();
				Files.copy(in, Paths.get(THIS_FOLDER_PATH + "vod_thumbnail.png"));
				
				// credit: https://stackoverflow.com/a/2319251
				// adds the vod_overlay on top of the vod_thumbnail
				BufferedImage uploadedThumbnail = ImageIO.read(new File(THIS_FOLDER_PATH + "vod_thumbnail.png"));
				BufferedImage vodOverlay = ImageIO.read(new File(THIS_FOLDER_PATH + "vod_overlay.png"));
				
				BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				Graphics g = combined.getGraphics();
				g.drawImage(uploadedThumbnail, 0, 0, null);
				
				// set offset to center the overlay
				int x_offset = (uploadedThumbnail.getWidth() - vodOverlay.getWidth()) / 2;
				int y_offset = (uploadedThumbnail.getHeight() - vodOverlay.getHeight()) / 2;
				
				g.drawImage(vodOverlay, x_offset, y_offset, null);
				g.dispose();
				// save the image on the system
				ImageIO.write(combined, "PNG", new File(THIS_FOLDER_PATH + "combined.png"));
				
				File combinedImage = new File(THIS_FOLDER_PATH + "combined.png");
				String streamTitle = latestVod.getTitle();
				String vodThumbnailURL = "attachment://combined.png";
				String streamDuration = latestVod.getDuration();
				String streamViewCount = latestVod.getViewCount().toString();
				
				// format date to look like "Wed, Sep 28, 2022"
				SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy");
				Date vodDate = sdf.parse(sdf.format(Date.from(latestVod.getPublishedAtInstant())));
				String[] dateArray = vodDate.toString().split(" ");
				String stringDate = dateArray[0] + ", " + dateArray[1] + " " + dateArray[2] + ", " + dateArray[5];
				
				EmbedBuilder offlineEmbed = new EmbedBuilder();
				offlineEmbed.setTitle("**[VOD]** " + streamTitle, latestVod.getUrl());
				offlineEmbed.setDescription("VOD from " + stringDate);
				offlineEmbed.addField(
						"__VOD View Count__:",
						streamViewCount,
						true);
				offlineEmbed.addBlankField(true);
				offlineEmbed.addField(
						"__VOD Length__:",
						streamDuration,
						true);
				offlineEmbed.setImage(vodThumbnailURL);
				offlineEmbed.setThumbnail("https://i.imgur.com/YfplpoR.png");
				offlineEmbed.setAuthor(
						"Aspecticor",
						"https://www.twitch.tv/aspecticor",
						"https://static-cdn.jtvnw.net/jtv_user_pictures/0dd6cf74-d650-453a-8d18-403409ae5517-profile_image-70x70.png"
						);
				offlineEmbed.setFooter(
						"brought to you by AspectiBot \u2764",
						"https://i.imgur.com/hAOV52i.png");
				offlineEmbed.setColor(0x8045f4);
				
				Collection<FileUpload> files = new LinkedList<FileUpload>();
				files.add(FileUpload.fromData(combinedImage, "combined.png"));
				
				File vodThumbnail = new File(THIS_FOLDER_PATH + "vod_thumbnail.png");
				
				streamNotificationMessage.editMessageEmbeds(offlineEmbed.build()).setFiles(files).complete();
				
				vodThumbnail.delete();
				combinedImage.delete();
				
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			streamNotificationMessage = null;
			
			// change icon to Offline version
			jda.getGuildById(SERVER_ID).getManager().setIcon(offlineIcon).submit();
			
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
	
	public static EmbedBuilder formatEmbed(Stream twitchStream) {
		
		String streamTitle = twitchStream.getTitle();
		String streamGame = twitchStream.getGameName();
		String streamThumbnailURL = twitchStream.getThumbnailUrl(1920, 1080) + "?c=" + CryptoUtils.generateNonce(4);
		Duration streamDuration = twitchStream.getUptime();
		int streamTotalSeconds = (int) streamDuration.getSeconds();
		
		final int SECONDS_TO_HOURS = 3600;
		final int SECONDS_TO_MINUTES = 60;
		
		String streamHours = (streamTotalSeconds / SECONDS_TO_HOURS) + "h ";
		String streamMinutes = (streamTotalSeconds % SECONDS_TO_HOURS) / SECONDS_TO_MINUTES + "m ";
		String streamSeconds = (streamTotalSeconds % SECONDS_TO_HOURS) % SECONDS_TO_MINUTES + "s ";
		
		// only display hours if stream is over an hour long
		if(streamHours.equalsIgnoreCase("0h ")) {
			streamHours = "";
		}
		
		String streamUptime = streamHours + streamMinutes + streamSeconds;
		
		String streamViewCount = twitchStream.getViewerCount().toString();
		
		EmbedBuilder goLiveEmbed = new EmbedBuilder();
		goLiveEmbed.setTitle(streamTitle, "https://www.twitch.tv/" + ASPECTICOR);
		goLiveEmbed.setDescription("Playing **" + streamGame + "**");
		goLiveEmbed.addField(
				"__Viewers__:",
				streamViewCount,
				true);
		goLiveEmbed.addBlankField(true);
		goLiveEmbed.addField(
				"__Uptime__:",
				streamUptime,
				true);
		goLiveEmbed.setImage(streamThumbnailURL);
		goLiveEmbed.setThumbnail("https://i.imgur.com/dimEDm5.png");
		goLiveEmbed.setAuthor(
				"Aspecticor",
				"https://www.twitch.tv/aspecticor",
				"https://static-cdn.jtvnw.net/jtv_user_pictures/0dd6cf74-d650-453a-8d18-403409ae5517-profile_image-70x70.png"
		);
		goLiveEmbed.setFooter("brought to you by AspectiBot \u2764", "https://i.imgur.com/hAOV52i.png");
		goLiveEmbed.setColor(0xf92b75);
		
		return goLiveEmbed;
		
	}
		
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
