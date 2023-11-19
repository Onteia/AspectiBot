package discord_commands;

import java.io.IOException;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;

import javax.management.openmbean.KeyAlreadyExistsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aspectibot.AspectiBot;
import aspectibot.DiscordCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import utils.JSONUtils;

public class BirthdayCommand implements DiscordCommand {

    private static final Logger LOG = LoggerFactory.getLogger(BirthdayCommand.class);
    private final String TIME_ZONE = "Canada/Mountain";
    private final int HOUR = 1;
    private final int MINUTE = 3;
    private final int SECOND = 0;

    public BirthdayCommand() {
        LOG.info("Birthday command initialized!");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));
        calendar.set(Calendar.HOUR_OF_DAY, HOUR);
        calendar.set(Calendar.MINUTE, MINUTE);
        calendar.set(Calendar.SECOND, SECOND);
        Date pingTime = calendar.getTime();
        LOG.info("pinging at: " + pingTime.toString());
        Timer timer = new Timer();
        timer.schedule(new BirthdayScheduler(), pingTime);
    }

    @Override
    public CommandData register() {
        CommandData command = Commands.slash("birthday", "add your birthday to be pinged by AspectiBot!")
            .addOptions(
                new OptionData(OptionType.STRING, "month", "the month you were born in")
                    .addChoice("January", "january")
                    .addChoice("February", "february")
                    .addChoice("March", "march")
                    .addChoice("April", "april")
                    .addChoice("May", "may")
                    .addChoice("June", "june")
                    .addChoice("July", "july")
                    .addChoice("August", "august")
                    .addChoice("September", "september")
                    .addChoice("October", "october")
                    .addChoice("November", "november")
                    .addChoice("December", "december")
                    .setRequired(true),
                new OptionData(OptionType.INTEGER, "day", "the day you were born on")
                    .setRequiredRange(1, 31)                  
                    .setRequired(true)
            );
        return command; 
    }

    @Override
    public MessageCreateData reply(SlashCommandInteractionEvent event) {
        String month;
        int day;
        String userID;
        try {
            month = event.getOption("month").getAsString();
            day = event.getOption("day").getAsInt();
            userID = event.getUser().getId();
        } catch(IllegalArgumentException e) {
            LOG.error("reply: unable to get command option!", e);
            return error();
        }
        
        try {
            // better to have month&day as the key to easily search through all users that 
            // have a birthday then
            // issue: have to somehow make the value an array to append to it instead of overwriting
            JSONUtils.add(day+"-"+month, userID, AspectiBot.BIRTHDAY_LOG_PATH);
        } catch(KeyAlreadyExistsException e) {
            try {
                JSONUtils.edit(userID, month+","+day, AspectiBot.BIRTHDAY_LOG_PATH);
            } catch(IOException e2) {
                LOG.error("reply: unable to edit birthday in json file!", e2);
            }
        } catch(IOException e) {
            LOG.error("reply: unable to add birthday to json file!", e);
            return error();
        }
        
        MessageCreateBuilder message = new MessageCreateBuilder();
        message.setContent("added your birthday!");
        return message.build();
    }
    
    private static MessageCreateData error() {
        MessageCreateBuilder message = new MessageCreateBuilder()
            .setContent("unable to process your request, try again later! (also blame onteia)");
        return message.build();
    }
    
}
