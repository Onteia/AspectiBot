package discord_commands;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aspectibot.AspectiBot;

public class BirthdayScheduler extends TimerTask {

    private static final Logger LOG = LoggerFactory.getLogger(BirthdayScheduler.class);
    private final long pingChannelId = 885775210228359189L;
    private ArrayList<String> usersToNotify;

    public BirthdayScheduler() {
        LOG.info("BirthdayScheduler initialized!");
    }

    public void run() {
        // get current month and day
        LocalDate today = LocalDate.now(ZoneId.of(BirthdayCommand.TIME_ZONE));
        //today.getMonthValue();
        // get all users whose birthday it is

        // if the object doesn't exist in the json file, return

        // check if the user ids are still in the discord server

        // if not, remove them from the json file

        // have different messages for number of people

        // wish them a happy birthday
        AspectiBot.jda.getGuildById(AspectiBot.SERVER_ID)
            .getTextChannelById(pingChannelId)
            .sendMessage("{user_ids} happy birthday!! everyone come wish them a happy birthday!!").queue();
    }

}
