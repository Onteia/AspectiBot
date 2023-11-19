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
    // use aspect's timezone
    private final ZoneId TIME_ZONE = ZoneId.of("America/Denver");
    private LocalDate today;
    private ArrayList<String> usersToNotify;

    public BirthdayScheduler() {
        LOG.info("BirthdayScheduler initialized!");
    }

    public void run() {
        // get all users whose birthday it is

        // wish them a happy birthday
        AspectiBot.jda.getGuildById(AspectiBot.SERVER_ID)
            .getTextChannelById(pingChannelId)
            .sendMessage("{user_ids} happy birthday!! everyone come wish them a happy birthday!!").queue();
    }

}
