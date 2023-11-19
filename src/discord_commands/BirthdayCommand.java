package discord_commands;

import java.io.IOException;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;

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
    protected static final String TIME_ZONE = "Canada/Mountain";
    private final int HOUR = 2;
    private final int MINUTE = 33;
    private final int SECOND = 0;

    public BirthdayCommand() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));
        calendar.set(Calendar.HOUR_OF_DAY, HOUR);
        calendar.set(Calendar.MINUTE, MINUTE);
        calendar.set(Calendar.SECOND, SECOND);
        Date pingTime = calendar.getTime();
        Timer timer = new Timer();
        timer.schedule(new BirthdayScheduler(), pingTime);
        LOG.info("pinging at: " + pingTime.toString());
    }

    @Override
    public CommandData register() {
        CommandData command = Commands.slash("birthday", "add your birthday to be pinged by AspectiBot!")
            .addOptions(
                new OptionData(OptionType.STRING, "month", "the month you were born in")
                    .addChoice("January", "1")
                    .addChoice("February", "2")
                    .addChoice("March", "3")
                    .addChoice("April", "4")
                    .addChoice("May", "5")
                    .addChoice("June", "6")
                    .addChoice("July", "7")
                    .addChoice("August", "8")
                    .addChoice("September", "9")
                    .addChoice("October", "10")
                    .addChoice("November", "11")
                    .addChoice("December", "12")
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
        String day;
        String userID;
        try {
            month = event.getOption("month").getAsString();
            day = event.getOption("day").getAsString();
            userID = event.getUser().getId();
        } catch(IllegalArgumentException e) {
            LOG.error("reply: unable to get command option!", e);
            return error();
        }
        
        try {
            // better to have month&day as the key to easily search through all users that 
            // have a birthday then
            // issue: have to somehow make the value an array to append to it instead of overwriting
            JSONUtils.append(month+"-"+day, userID, AspectiBot.BIRTHDAY_LOG_PATH);
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
