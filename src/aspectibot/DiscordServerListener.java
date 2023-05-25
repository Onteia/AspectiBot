package aspectibot;

import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public class DiscordServerListener extends ListenerAdapter {

    private final Logger LOG = LoggerFactory.getLogger(DiscordServerListener.class);
    
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Message message = event.getMessage();
		Member m = event.getMember();
		if(m == null) {
            LOG.error("onMessageReceived: Member is null!");
            return;
        }
		GuildChannel channel;
		
		if(event.getChannelType().isAudio()) {
			channel = event.getChannel().asVoiceChannel();
		} else {
			channel = event.getChannel().asGuildMessageChannel();
		}
		
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
			TextChannel logChannel = guild.getTextChannelById(AspectiBot.LOG_CHANNEL_ID);
			if(logChannel != null) {
			    logChannel.sendMessageEmbeds(logMessage.build()).complete();
			} else {
			    LOG.error("onMessageReceived: logChannel is null!");
			}
			logMessage.clear();
		
		}
		
	}
	
	@Override
	public void onMessageUpdate(MessageUpdateEvent event) {
		
		Message message = event.getMessage();
		Member m = event.getMember();
		if(m == null) {
		    LOG.error("onMessageUpdate: Member is null!");
		    return;
		}
		GuildChannel channel;
		
		if(event.getChannelType().isAudio()) {
			channel = event.getChannel().asVoiceChannel();
		} else {
			channel = event.getChannel().asGuildMessageChannel();
		}
		
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
			
			TextChannel logChannel = guild.getTextChannelById(AspectiBot.LOG_CHANNEL_ID);
			if(logChannel != null) {
			    logChannel.sendMessageEmbeds(logMessage.build()).complete();
			} else {
			    LOG.error("onMessageUpdate: logChannel is null!");			    
			}
			logMessage.clear();
		}
	}

	@Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        
        // get the member who joined
        Member member = event.getMember();
        
        // give member who joined the default role
        Role defaultRole = event.getJDA().getRoleById(AspectiBot.DEFAULT_ROLE);
        if(defaultRole != null) {
            event.getMember().getGuild().addRoleToMember(member, defaultRole).queue();
        } else {
            LOG.error("onGuildMemberJoin: Default role not configured or invalid!");
        }

    } // end of onGuildMemberJoin method
}
