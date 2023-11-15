package discord_commands;

import aspectibot.DiscordCommand;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class BirthdayCommand implements DiscordCommand {

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
    public MessageCreateData reply() {
        
        MessageCreateBuilder message = new MessageCreateBuilder();
        message.setContent("added your birthday!");
        return message.build();
    }
    
}
