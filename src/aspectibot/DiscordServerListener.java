package aspectibot;

import java.time.format.DateTimeFormatter;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEmoteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public class DiscordServerListener extends ListenerAdapter {

	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		Message message = event.getMessage();
		Member m = event.getMember();
		TextChannel channel = event.getChannel();
		
		if(channel.getGuild().getName().equalsIgnoreCase("Aspecticord")) {
		
			if(!m.getEffectiveName().equalsIgnoreCase("AspectiBot") && !m.getEffectiveName().equalsIgnoreCase("Pingcord") && !m.getEffectiveName().equalsIgnoreCase("Pingcord") && !m.getEffectiveName().equalsIgnoreCase("YAGPDB.xyz")) {
				
				EmbedBuilder log_message = new EmbedBuilder();
				
				log_message.setTitle(m.getEffectiveName(), message.getJumpUrl());
				log_message.setDescription("#" + channel.getName());
				if(message.getContentDisplay().length() >= 1024) {
					log_message.addField("posted:", message.getContentDisplay().substring(0, 1021) + "...", true);
				} else {
					log_message.addField("posted:", message.getContentDisplay(), true);
				}
				
				log_message.setTimestamp(message.getTimeCreated());
				
				try {
					if(message.getAttachments().get(0).isImage()) {
						log_message.addField("uploaded image:", "", false);
						log_message.setImage(message.getAttachments().get(0).getUrl());
					} else if(message.getAttachments().get(0).isVideo()) {
						log_message.addField("uploaded video:", message.getAttachments().get(0).getUrl(), false);
					}
				} catch(Exception e) {
					//no attachments
				}
				
				log_message.setColor(0xf705ff);
				channel.getGuild().getTextChannelById(AspectiBot.logChannelID).sendMessage(log_message.build()).complete();
				log_message.clear();
			
			}
		
		}
	}
	
	public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
		
		Message message = event.getMessage();
		
		Member m = event.getMember();
		TextChannel channel = event.getChannel();
		
		if(channel.getGuild().getName().equalsIgnoreCase("Aspecticord")) {
			
			if(!m.getEffectiveName().equalsIgnoreCase("AspectiBot")  && !m.getEffectiveName().equalsIgnoreCase("Aspecticor") && !m.getEffectiveName().equalsIgnoreCase("Pingcord") && !m.getEffectiveName().equalsIgnoreCase("Pingcord") && !m.getEffectiveName().equalsIgnoreCase("YAGPDB.xyz")) {
				
				EmbedBuilder log_message = new EmbedBuilder();
				
				log_message.setTitle(m.getEffectiveName(), message.getJumpUrl());
				log_message.setDescription("#" + channel.getName());
				
				if(message.getContentDisplay().length() >= 1024) {
					log_message.addField("edited:", message.getContentDisplay().substring(0, 1021) + "...", true);
				} else {
					log_message.addField("edited:", message.getContentDisplay(), true);
				}
				
				log_message.setTimestamp(message.getTimeEdited());
				DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
				log_message.setFooter("original message posted: " + message.getTimeCreated().format(formatter));
				
				try {
					if(message.getAttachments().get(0).isImage()) {
						log_message.setImage(message.getAttachments().get(0).getUrl());
					}
				} catch(Exception e) {
					//no attachments
				}
				
				log_message.setColor(0xf705ff);
				channel.getGuild().getTextChannelById(AspectiBot.logChannelID).sendMessage(log_message.build()).complete();
				log_message.clear();
			
			}
		
		}
	}

	/*
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		
		Member m = event.getMember();
		MessageReaction.ReactionEmote emote = event.getReactionEmote();
		TextChannel channel = event.getChannel();
		String messageId = event.getMessageId();
		String messageUrl = event.getChannel().getHistory().getMessageById(messageId).getJumpUrl();
		
		if(channel.getGuild().getName().equalsIgnoreCase("Aspecticord")) {
			
			if(!m.getEffectiveName().equalsIgnoreCase("AspectiBot") && !m.getEffectiveName().equalsIgnoreCase("Pingcord") && !m.getEffectiveName().equalsIgnoreCase("Pingcord") && !m.getEffectiveName().equalsIgnoreCase("YAGPDB.xyz")) {
				
				EmbedBuilder log_message = new EmbedBuilder();
				
				log_message.setTitle(m.getEffectiveName(), messageUrl);
				log_message.setDescription("#" + channel.getName());	
				log_message.addField("reacted:", "", true);
				log_message.setImage(emote.getEmote().getImageUrl());
				log_message.setColor(0xf705ff);
				channel.getGuild().getTextChannelById(AspectiBot.logChannelID).sendMessage(log_message.build()).complete();
				log_message.clear();
		
			}
		}
		
	} 
	*/
	
	/*
	public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
		Member m = event.getMember();
		MessageReaction.ReactionEmote emote = event.getReactionEmote();
		TextChannel channel = event.getChannel();
		String messageId = event.getMessageId();
		String messageUrl = event.getChannel().getHistory().getMessageById(messageId).getJumpUrl();
		
		if(channel.getGuild().getName().equalsIgnoreCase("Aspecticord")) {
			
			if(!m.getEffectiveName().equalsIgnoreCase("AspectiBot") && !m.getEffectiveName().equalsIgnoreCase("Pingcord") && !m.getEffectiveName().equalsIgnoreCase("Pingcord") && !m.getEffectiveName().equalsIgnoreCase("YAGPDB.xyz")) {
				
				EmbedBuilder log_message = new EmbedBuilder();
				
				log_message.setTitle(m.getEffectiveName(), messageUrl);
				log_message.setDescription("#" + channel.getName());	
				log_message.addField("unreacted:", "", true);
				log_message.setImage(emote.getEmote().getImageUrl());
				log_message.setColor(0xf705ff);
				channel.getGuild().getTextChannelById(AspectiBot.logChannelID).sendMessage(log_message.build()).complete();
				log_message.clear();
		
			}
		}
	}
	*/
}
