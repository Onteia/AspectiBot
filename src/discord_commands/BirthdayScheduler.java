package discord_commands;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aspectibot.AspectiBot;
import net.dv8tion.jda.api.entities.UserSnowflake;
import utils.JSONUtils;

public class BirthdayScheduler extends TimerTask {

    private static final Logger LOG = LoggerFactory.getLogger(BirthdayScheduler.class);
    // TODO: change this back to the general channel
    private final long pingChannelId = 885775210228359189L;

    public BirthdayScheduler() {
        LOG.info("BirthdayScheduler initialized!");
    }

    public void run() {
        LocalDate today = LocalDate.now(ZoneId.of(BirthdayCommand.TIME_ZONE));
        String month = ""+today.getMonthValue();
        String day = ""+today.getDayOfMonth();

        List<Object> possibleUsers;
        try {
            JSONArray arr = JSONUtils.getArray(month+"-"+day, AspectiBot.BIRTHDAY_LOG_PATH);
            possibleUsers = arr.toList();
        } catch (JSONException e) {
            LOG.error("run: key is not in the birthday json!");
            return;
        } catch (IOException e) {
            LOG.error("run: unable to read birthday json!");
            return;
        }

        ArrayList<String> usersToNotify = new ArrayList<String>();
        possibleUsers.forEach(id -> {
            if(AspectiBot.jda.getGuildById(AspectiBot.SERVER_ID)
                .isMember(UserSnowflake.fromId((String) id)))
            {
                usersToNotify.add((String) id);
            } else {
                try {
                    String birthdate = JSONUtils.get((String) id, AspectiBot.BIRTHDAY_LOG_PATH);
                    JSONUtils.delete((String) id, AspectiBot.BIRTHDAY_LOG_PATH);
                    JSONArray array = JSONUtils.getArray(birthdate, AspectiBot.BIRTHDAY_LOG_PATH);
                    int index = array.toList().indexOf(id);
                    array.remove(index);
                    JSONUtils.addArray(month+"-"+day, array, AspectiBot.BIRTHDAY_LOG_PATH);
                } catch (JSONException e) {
                    LOG.error("run: json entry not found!");
                    return;
                } catch (IOException e) {
                    LOG.error("run: unable to read json file!", e);
                    return;
                }
            }
        });

        String birthdayText = "";
        for(String userID : usersToNotify) {
            birthdayText += "<@" + userID + "> ";
        }
        birthdayText += "happy birthday!! everyone come wish them a happy birthday!!";

        AspectiBot.jda.getGuildById(AspectiBot.SERVER_ID)
            .getTextChannelById(pingChannelId)
            .sendMessage(birthdayText).queue();
    }

}
