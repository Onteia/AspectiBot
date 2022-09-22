package aspectibot;

import java.time.format.DateTimeFormatter;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public class DiscordServerListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Message message = event.getMessage();
		Member m = event.getMember();
		TextChannel channel = event.getChannel().asTextChannel(); //$ should change type to MessageChannel
		Guild guild = channel.getGuild();
		
		if(Long.valueOf(guild.getId()) == AspectiBot.SERVER_ID
				&& !event.getAuthor().isBot()) {
			
			EmbedBuilder logMessage = new EmbedBuilder();
			
			logMessage.setTitle(m.getEffectiveName(), message.getJumpUrl());
			logMessage.setDescription("#" + channel.getName());
			if(message.getContentDisplay().length() >= 1024) {
				logMessage.addField(
					"posted:", 
					message.getContentDisplay().substring(0, 1021) + "...", 
					true);
			} else {
				logMessage.addField(
					"posted:", 
					message.getContentDisplay(), 
					true);
			}
			
			logMessage.setTimestamp(message.getTimeCreated());
			
			try {
				if(message.getAttachments().get(0).isImage()) {
					logMessage.addField(
						"uploaded image:", 
						"", 
						false);
					logMessage.setImage(message.getAttachments().get(0).getUrl());
				} else if(message.getAttachments().get(0).isVideo()) {
					logMessage.addField(
						"uploaded video:", 
						message.getAttachments().get(0).getUrl(), 
						false);
				}
			} catch(Exception e) {
				//no attachments
			}
			
			logMessage.setColor(0xf705ff);
			guild.getTextChannelById(AspectiBot.LOG_CHANNEL_ID).sendMessageEmbeds(logMessage.build()).complete();
			logMessage.clear();
		
		}
	}
	
	@Override
	public void onMessageUpdate(MessageUpdateEvent event) {
		
		Message message = event.getMessage();
		
		Member m = event.getMember();
		TextChannel channel = event.getChannel().asTextChannel();
		Guild guild = channel.getGuild();
		
		if(Long.valueOf(guild.getId()) == AspectiBot.SERVER_ID
				&& !event.getAuthor().isBot()) {
			
			EmbedBuilder logMessage = new EmbedBuilder();
			
			logMessage.setTitle(m.getEffectiveName(), message.getJumpUrl());
			logMessage.setDescription("#" + channel.getName());
			
			if(message.getContentDisplay().length() >= 1024) {
				logMessage.addField(
					"edited:", 
					message.getContentDisplay().substring(0, 1021) + "...", 
					true);
			} else {
				logMessage.addField(
					"edited:", 
					message.getContentDisplay(), 
					true);
			}
			
			logMessage.setTimestamp(message.getTimeEdited());
			DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
			logMessage.setFooter("original message posted: " + message.getTimeCreated().format(formatter));
			
			try {
				if(message.getAttachments().get(0).isImage()) {
					logMessage.setImage(message.getAttachments().get(0).getUrl());
				}
			} catch(Exception e) {
				//no attachments
			}
			
			logMessage.setColor(0xf705ff);
			guild.getTextChannelById(AspectiBot.LOG_CHANNEL_ID).sendMessageEmbeds(logMessage.build()).complete();
			logMessage.clear();
		
		
		
		}
	}

	// when refactoring I took a crack at these two methods
	// I'll figure them out later -Atlae (Clueless)
	
	// public void onGuildMessageReactionAdd(MessageReactionAddEvent event) {
		
	// 	Member m = event.getMember();
	// 	MessageReaction emote = event.getReaction();
	// 	TextChannel channel = (TextChannel) event.getChannel();
	// 	String messageId = event.getMessageId();
	// 	String messageUrl = event.getChannel().getHistory().getMessageById(messageId).getJumpUrl();
	// 	Guild guild = event.getGuild();
		
	// 	if(Long.valueOf(guild.getId()) == AspectiBot.SERVER_ID
	// 			&& !event.getUser().isBot()) {	
	// 		EmbedBuilder logMessage = new EmbedBuilder();
			
	// 		logMessage.setTitle(m.getEffectiveName(), messageUrl);
	// 		logMessage.setDescription("#" + channel.getName());	
	// 		logMessage.addField("reacted:", "", true);

	// 		if (emote.getEmoji())

	// 		logMessage.setImage(emote.getReactionEmote().getImageUrl());
	// 		logMessage.setColor(0xf705ff);
	// 		channel.getGuild().getTextChannelById(AspectiBot.LOG_CHANNEL_ID).sendMessage(logMessage.build()).complete();
	// 		logMessage.clear();
	
		
	// 	}
		
	// } 
	
	
	
	// public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
	// 	Member m = event.getMember();
	// 	MessageReaction.ReactionEmote emote = event.getReactionEmote();
	// 	TextChannel channel = event.getChannel();
	// 	String messageId = event.getMessageId();
	// 	String messageUrl = event.getChannel().getHistory().getMessageById(messageId).getJumpUrl();
		
	// 	if(channel.getGuild().getName().equalsIgnoreCase("Aspecticord")) {
			
	// 		if(!m.getEffectiveName().equalsIgnoreCase("AspectiBot") && !m.getEffectiveName().equalsIgnoreCase("Pingcord") && !m.getEffectiveName().equalsIgnoreCase("Pingcord") && !m.getEffectiveName().equalsIgnoreCase("YAGPDB.xyz")) {
				
	// 			EmbedBuilder logMessage = new EmbedBuilder();
				
	// 			logMessage.setTitle(m.getEffectiveName(), messageUrl);
	// 			logMessage.setDescription("#" + channel.getName());	
	// 			logMessage.addField("unreacted:", "", true);
	// 			logMessage.setImage(emote.getEmote().getImageUrl());
	// 			logMessage.setColor(0xf705ff);
	// 			channel.getGuild().getTextChannelById(AspectiBot.LOG_CHANNEL_ID).sendMessage(logMessage.build()).complete();
	// 			logMessage.clear();
		
	// 		}
	// 	}
	// }
	
}
