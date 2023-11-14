package discord_commands;

import aspectibot.DiscordCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class BirthdayCommand implements DiscordCommand {

    @Override
    public CommandData register() {
       return null; 
    }

    @Override
    public MessageCreateData reply() {
        MessageCreateBuilder message = new MessageCreateBuilder();

        return message.build();
    }
    
}
