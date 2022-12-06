package commands;

import java.util.List;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;

import aspectibot.AspectiBot;
import aspectibot.TwitchCommand;

public class ArtificialIntelligenceCommand implements TwitchCommand {

    @Override
    public String response(ChannelMessageEvent event) {
        // Generate a GPT3 response to twitch chat question 
        OpenAiService service = new OpenAiService(AspectiBot.opnAI);
        CompletionRequest completionRequest = CompletionRequest.builder()
                .prompt(event.getMessage())
                .model("text-davinci-003")
                .maxTokens(500)
                .echo(false)
                .build();
        List<CompletionChoice> choices = service.createCompletion(completionRequest).getChoices();
        String answer = choices.get(AspectiBot.R.nextInt(choices.size())).getText();
        return answer.length() < 490 ? answer : answer.substring(0, 490) + "...";
    }
    
}
