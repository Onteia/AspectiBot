package aspectibot;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.chat.events.channel.RaidEvent;
import com.github.twitch4j.common.events.user.PrivateMessageEvent;
import com.github.twitch4j.common.util.CryptoUtils;
import com.github.twitch4j.events.ChannelChangeGameEvent;
import com.github.twitch4j.events.ChannelChangeTitleEvent;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.github.twitch4j.events.ChannelViewerCountUpdateEvent;
import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.Video;
import com.github.twitch4j.pubsub.domain.ChannelPointsRedemption;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import commands.ClipCommand;
import commands.EmotesCommand;
import commands.LeaderboardCommand;
import commands.LogAddCommand;
import commands.LogDeleteCommand;
import commands.LogEditCommand;
import commands.LogShowCommand;
import commands.LurkCommand;
import commands.TwitchEmoteCommand;
import commands.TwitterCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class AspectiBot {
	
	private static final String ASPECTICOR = "aspecticor";
	private static final String CONFIG_FILE = "src/config.properties";

	private static String DISCORD_TOKEN_PATH;
	private static String TWITCH_TOKEN_PATH;
	private static String OPEN_AI_TOKEN_PATH;
	private static String LIVE_ICON_PATH;
	private static String OFFLINE_ICON_PATH;
	public static String COMMAND_LOG_PATH;
	private static String THIS_FOLDER_PATH;
	
	/* Aspecticord settings */
	public static final long SERVER_ID = 864273305330909204L; // Aspecticord Server ID
	private static final long LIVE_CHANNEL_ID = 885705830341697536L; // #aspecticor-is-live channel
	public static final long LOG_CHANNEL_ID = 1016876667509166100L; // #server_logs channel
	public static final long CLIP_CHANNEL_ID = 867258015220236319L;	// #clips channel
	public static final long DEFAULT_ROLE = 885698882695229500L; // Aspecticord default role
	private static final String PING_ROLE = "882772072475017258"; // Aspecticord @TWITCH_PINGS	
	//*/
	
	/* Test Server settings 
	public static final long SERVER_ID = 264217465305825281L;
	public static final long LIVE_CHANNEL_ID = 1022422500161900634L;
	public static final long LOG_CHANNEL_ID = 1022427876609495100L;
	public static final long CLIP_CHANNEL_ID = 1024597131488665601L;
	public static final long DEFAULT_ROLE = 1053423521604309062L;
	public static final String PING_ROLE = "853934165077393458";
	//*/

	private static String token; // discord token
	public static String oAuth; // twitch OAuth
	public static String opnAI; // OpenAI token
	public static Icon liveIcon;
	public static Icon offlineIcon;
	
	public enum StreamStatus {
		LIVE,
		OFFLINE;
	}

	private static String[] modArray = {"aspectibot", "atlae99", "b00kitten", "botspecticor", "brenroarn", "bunnyga", "evan_gao", "fourthwallhq", "fu5ha", "isto_inc", "jhortplays", "katiegrayx3", "kittyzea", "linkus7", "mattyro1", "me_jd", "mracres", "negnegtm", "nightbot", "onteia", "scriptdesk", "seek_", "serkian", "skelly57", "stanz", "streamelements", "streamlabs", "sumneer","theandershour", "thomasthegremlin", "vezlaye", "voidmakesvids", "xemdo"};
	private static StreamStatus streamStatus = StreamStatus.OFFLINE;
	private static final Logger LOG = LoggerFactory.getLogger(AspectiBot.class);
	
	public static String aspecticorId;
	public static TwitchClient twitchClient;
	public static JDA jda;
	private static String notificationMessageId = "";

	public static final Random R = new Random();

	public static void main(String[] args) throws Exception {

        LOG.info("Starting AspectiBot...");

		loadConfig();
		readSaveFile();
		
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

		// join Aspecticor's chat
		OAuth2Credential credential = new OAuth2Credential("twitch", oAuth);

		twitchClient = TwitchClientBuilder.builder()
				.withEnableHelix(true)
				.withDefaultAuthToken(credential)
				.withEnableChat(true)
				.withChatAccount(credential)
				.withEnablePubSub(true)
				.build();

		// join Aspect's stream
		twitchClient.getChat().joinChannel(ASPECTICOR);	

		// Listen to aspecticor's stream
		twitchClient.getClientHelper().enableStreamEventListener(ASPECTICOR);
		aspecticorId = twitchClient.getChat().getChannelNameToChannelId().get(ASPECTICOR); // "275302146"
		twitchClient.getPubSub().listenForChannelPointsRedemptionEvents(credential, aspecticorId);
		
		// if Aspecticor is live change activity and status
		// also change server icon
		goLive(twitchClient, jda);

		// if Aspect turns stream off, change icon back and set status to idle.
		goOffline(twitchClient, jda);

		whisper(twitchClient);

		Map<String, TwitchCommand> commands = new HashMap<>();

		commands.put("!emotes", new EmotesCommand());
		commands.put("!leaderboards", new LeaderboardCommand());
		commands.put("!lurk", new LurkCommand());
		commands.put("!twitter", new TwitterCommand());
		commands.put("!addcom", new LogAddCommand());
		commands.put("!showcom", new LogShowCommand());
		commands.put("!delcom", new LogDeleteCommand());
		commands.put("!editcom", new LogEditCommand());
		commands.put("!clip", new ClipCommand());
		commands.put("!twitchemote", new TwitchEmoteCommand());

		// executing commands
		twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, event -> {

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

		// channel point stuff
		twitchClient.getEventManager().onEvent(RewardRedeemedEvent.class, event -> {
			
			ChannelPointsRedemption redeem = event.getRedemption();
			String rewardName = redeem.getReward().getTitle();
			LOG.info("{} redeemed {}!", redeem.getUser().getDisplayName(), rewardName);

			// ASK THE AI
			if(rewardName.equalsIgnoreCase("ASK THE AI")) { 
				
				String prompt = redeem.getUserInput();
				String user = redeem.getUser().getDisplayName();
				String answer = "";

                LOG.info("AI question asked by {}: {}", user, prompt);
				
				while (answer.equalsIgnoreCase("")) {
					try {
						// Generate a GPT3.5 response from twitch chat question
						List<ChatMessage> messages = Arrays.asList(new ChatMessage("user", prompt, user));
						OpenAiService service = new OpenAiService(opnAI);
						ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
								.messages(messages)
								.model("gpt-3.5-turbo")
								.maxTokens(300)
								.build();
						List<ChatCompletionChoice> choices = service.createChatCompletion(chatCompletionRequest).getChoices();
						answer = choices.get(AspectiBot.R.nextInt(choices.size())).getMessage().getContent();
						String chatResponse = "@" + user + ": " + answer;

						LOG.info("AI response: {}", chatResponse);

						if(chatResponse.length() >= 500) {
							twitchClient.getChat().sendMessage(ASPECTICOR, chatResponse.substring(0,495) + "...");
						} else {
							twitchClient.getChat().sendMessage(ASPECTICOR, chatResponse);
						}	            
					} catch(Exception e) {
						//do nothing
						LOG.error("AI error: {}", e.getMessage());
						e.printStackTrace();
						break;
					}
				}
				
			}
			
		});

		// on raid
		twitchClient.getEventManager().onEvent(RaidEvent.class, event -> {
			if (event.getViewers() < 10) {
				twitchClient.getChat()
							.sendMessage(ASPECTICOR, "!so " + event.getRaider().getName());
				return;
			}
			String raiderId = event.getRaider().getId();
			// send shoutout event
			try {
				twitchClient.getHelix()
							.sendShoutout(oAuth, aspecticorId, raiderId, aspecticorId)
							.execute();
			} catch (Exception e) {
				LOG.error("Unable to send shoutout to {}!", event.getRaider().getName());
				e.printStackTrace();
			}
			twitchClient.getChat()
						.sendMessage(ASPECTICOR, "!so " + event.getRaider().getName());
		});

        LOG.info("AspectiBot Started!");
		
	} // end of main method

	public static void goLive(TwitchClient twitchClient, JDA jda) {

        NewsChannel newsChannel = jda.getNewsChannelById(AspectiBot.LIVE_CHANNEL_ID);
        if (newsChannel == null){
            LOG.error("goLive: Unable to get news channel! Channel ID: " + AspectiBot.LIVE_CHANNEL_ID);
            return;
        }

		twitchClient.getEventManager().onEvent(ChannelGoLiveEvent.class, event -> {
			if (streamStatus == StreamStatus.OFFLINE) {
				streamStatus = StreamStatus.LIVE;
				jda.getPresence().setStatus(OnlineStatus.ONLINE);
				jda.getPresence().setActivity(Activity.watching("Aspecticor's Stream"));
				
				// change icon to Live version
                Guild server = jda.getGuildById(SERVER_ID);

                if (server == null)
                    // ngl if you somehow throw this error and newsChannel isn't null, I'm impressed
                    LOG.error("goLive: Unable to get server! Server ID: " + SERVER_ID);
                else
                    server.getManager().setIcon(liveIcon).queue();
				
				EmbedBuilder goLiveEmbed = formatEmbed(event.getStream());
                Message streamNotificationMessage = newsChannel.sendMessage("<@&"+ PING_ROLE +"> HE'S LIVE!!!")
                        .addEmbeds(goLiveEmbed.build())
                        .complete();

                notificationMessageId = streamNotificationMessage.getId();
                File idFile = new File(AspectiBot.THIS_FOLDER_PATH + "notifID.sav");
                
                try {
                    if(idFile.createNewFile()) {
                        FileWriter fw = new FileWriter(idFile);
                        fw.write(notificationMessageId);
                        fw.close();
                    }
                } catch (IOException e) {
                    LOG.error("goLive: Unable to create save file for the message ID");
                    e.printStackTrace();
                }
                LOG.info(ASPECTICOR + " went live!");				
			}
		});
		// Update stream info when title is changed
		twitchClient.getEventManager().onEvent(ChannelChangeTitleEvent.class, event -> {
			EmbedBuilder newEmbed = formatEmbed(event.getStream());	
			newsChannel.editMessageEmbedsById(notificationMessageId, newEmbed.build()).complete();
		});
		// Update stream info when game/category is changed
		twitchClient.getEventManager().onEvent(ChannelChangeGameEvent.class, event -> {
			EmbedBuilder newEmbed = formatEmbed(event.getStream());
			newsChannel.editMessageEmbedsById(notificationMessageId, newEmbed.build()).complete();
		});
		// Update stream info when viewercount changes
		twitchClient.getEventManager().onEvent(ChannelViewerCountUpdateEvent.class, event -> {
			EmbedBuilder newEmbed = formatEmbed(event.getStream());
			newsChannel.editMessageEmbedsById(notificationMessageId, newEmbed.build()).complete();
		});

	}

    public static void goOffline(TwitchClient twitchClient, JDA jda) {

		twitchClient.getEventManager().onEvent(ChannelGoOfflineEvent.class, event -> {
			streamStatus = StreamStatus.OFFLINE;
			jda.getPresence().setStatus(OnlineStatus.IDLE);
			
			// change icon to Offline version
            Guild server = jda.getGuildById(SERVER_ID);
            if (server == null) {
                LOG.error("goOffline: Unable to get server! Server ID: " + SERVER_ID);
                return;
            } else {
                server.getManager().setIcon(offlineIcon).queue();
			}

			//credit: https://whaa.dev/how-to-generate-random-characters-in-java
			StringBuilder randomKey = new StringBuilder();
			for (int i = 0; i < 30; i++) {
				char randomCharacter = (char)((R.nextBoolean() ? 'a' : 'A') + R.nextInt(26));
				randomKey.append(randomCharacter);
			}
			
			String fakeKey = "live_" + R.nextInt(1000000000) + "_" + randomKey.toString();
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
			String response = randResponses[R.nextInt(randResponses.length)];
			// null safety
			if(response != null) {
				jda.getPresence().setActivity(Activity.watching(response));
			} else {
				LOG.error("goOffline: Offline status response is null!");
			}
			
			List<Video> vodList = twitchClient.getHelix()
                    .getVideos(
                        oAuth, null, aspecticorId, null, null, Video.SearchPeriod.DAY, 
                        Video.SearchOrder.TIME, Video.Type.ARCHIVE, 1, null, null)
                    .execute()
                    .getVideos();
			Video latestVod = vodList.get(0);
			
			createVodThumbnail(latestVod);
			
			// delete messageId value from the save file
			// and set id to ""
			File notifIdFile = new File(AspectiBot.THIS_FOLDER_PATH + "notifID.sav");
            try {
                Files.delete(notifIdFile.toPath());
            } catch (IOException e) {
                // but like how tho, you just created it
                e.printStackTrace();
            } finally {
                notificationMessageId = "";
            }
			
			LOG.info(ASPECTICOR + " went offline!");
		});

	} // end of goOffline method

	private static void createVodThumbnail(Video latestVod) {
        try (InputStream in = new URL(latestVod.getThumbnailUrl(1920, 1080)).openStream()) {
            // credit: https://www.techiedelight.com/download-file-from-url-java/
            // get a local version of the thumbnail to set up for adding overlay
            
            int width = 1920;
            int height = 1080;
            
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
            if(streamDuration == null) streamDuration = "0";
            String streamViewCount = latestVod.getViewCount().toString();
            if(streamViewCount == null) streamViewCount = "0";
            
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

            NewsChannel newsChannel = jda.getNewsChannelById(AspectiBot.LIVE_CHANNEL_ID);

            if (newsChannel == null) {
                LOG.error("goOffline: Could not find the go-live channel! Check the channel ID!");
            } else {
                newsChannel.editMessageEmbedsById(
                        notificationMessageId, 
                        offlineEmbed.build())
                    .setFiles(files)
                    .complete();
            }
                
            vodThumbnail.delete();
            combinedImage.delete();
            
        } catch(Exception e) {
            LOG.error("goOffline: Error creating the VOD thumbnail!");
            e.printStackTrace();
        }
    } // end of createVodThumbnail method

public static void whisper(TwitchClient twitchClient) {
		// if a mod in twitch channel whispers bot, send chat to that twitch channel
		twitchClient.getEventManager().onEvent(PrivateMessageEvent.class, event -> {
			List<String> mods = Arrays.asList(modArray);
			if (mods != null && mods.contains(event.getUser().getName())) {
				twitchClient.getChat().sendMessage(ASPECTICOR, event.getMessage());
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
		if(streamViewCount == null) streamViewCount = "0";
		
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
		
	public static void readSaveFile() {
		File saveFile = new File(AspectiBot.THIS_FOLDER_PATH + "notifID.sav");
		try (BufferedReader br = new BufferedReader(new FileReader(saveFile))) {
            AspectiBot.notificationMessageId = br.readLine();
            LOG.info("readSaveFile: Save file successfully read!");
        } catch (FileNotFoundException e) {
            // file not found
            AspectiBot.notificationMessageId = "";
            LOG.info("readSaveFile: File not found because previous stream ended before this program restarted!");
        } catch (IOException e) {
            LOG.error("readSaveFile: Unable to read the save file!");
            e.printStackTrace();
        }
	}
	
	public static void loadConfig() {
	    // https://niruhan.medium.com/how-to-add-a-config-file-to-a-java-project-99fd9b6cebca
        try (FileInputStream config = new FileInputStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            prop.load(config);
            DISCORD_TOKEN_PATH = prop.getProperty("DISCORD_TOKEN_PATH");
            TWITCH_TOKEN_PATH = prop.getProperty("TWITCH_TOKEN_PATH");
            OPEN_AI_TOKEN_PATH = prop.getProperty("OPEN_AI_TOKEN_PATH");
            LIVE_ICON_PATH = prop.getProperty("LIVE_ICON_PATH");
            OFFLINE_ICON_PATH = prop.getProperty("OFFLINE_ICON_PATH");
            COMMAND_LOG_PATH = prop.getProperty("COMMAND_LOG_PATH");
            THIS_FOLDER_PATH = prop.getProperty("THIS_FOLDER_PATH");
        } catch (FileNotFoundException e) {
            //no config file
            DISCORD_TOKEN_PATH = "/home/orangepi/jars/persistent/discordToken.txt";
            TWITCH_TOKEN_PATH = "/home/orangepi/jars/persistent/twitchOAuth.txt";
            OPEN_AI_TOKEN_PATH = "/home/orangepi/jars/persistent/openAiToken.txt";
            LIVE_ICON_PATH = "/home/orangepi/jars/persistent/Aspecticor_Live.png";
            OFFLINE_ICON_PATH = "/home/orangepi/jars/persistent/Aspecticor_Offline.png";
            COMMAND_LOG_PATH = "/home/orangepi/jars/AspectiBot/src/commands/commands.json";
            THIS_FOLDER_PATH = "/home/orangepi/jars/AspectiBot/";
        } catch (IOException e1) {
            LOG.error("loadConfig: IOException on loading config file!");
        } finally {
            //load credentials
            loadCredentials();
        }
	}
	
	public static void loadCredentials() {
		try {
			// get the files
			File discordToken = new File(DISCORD_TOKEN_PATH);
			File twitchToken = new File(TWITCH_TOKEN_PATH);
			File openAiToken = new File(OPEN_AI_TOKEN_PATH);
			
			// read the files
			// https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
			try (
				BufferedReader br1 = new BufferedReader(new FileReader(discordToken));
				BufferedReader br2 = new BufferedReader(new FileReader(twitchToken));
				BufferedReader br3 = new BufferedReader(new FileReader(openAiToken));
			) {
				token = br1.readLine();
				oAuth = br2.readLine();
				opnAI = br3.readLine();
			}
		} catch (Exception e) {
			LOG.error("loadCredentials: Authentication Failed!");
		}
		
	}

}
