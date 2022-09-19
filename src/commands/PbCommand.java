package commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import aspectibot.TwitchCommand;

public class PbCommand implements TwitchCommand {

	public String response(ChannelMessageEvent event) {
		
		try {
			
			//get the file
			File pbFile = new File("/home/orangepi/jars/AspectiBot/pb.txt");
			
			//read the file
			BufferedReader br = new BufferedReader(new FileReader(pbFile));
			String output = br.readLine();
			br.close();
			
			return output;
			
		} catch(Exception e) {
			
			return "@Onteia you done fucked up! Madge";
			
		}
		
		
	}
	
}
