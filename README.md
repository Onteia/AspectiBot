# aspectibot

AspectiBot: the open-source Discord and Twitch bot for Aspecticor (<https://www.twitch.tv/aspecticor>) maintained by Aspecticor's mods and community, mainly [Onteia](https://www.github.com/Onteia) *(she/her)* and [Atlae](https://www.github.com/Atlae) *(he/him)*.

AspectiBot is written entirely in Java (yeah, yeah, we know) and uses the libraries [Twitch4J](https://github.com/twitch4j/twitch4j) to interface with Twitch, [JDA](https://github.com/DV8FromTheWorld/JDA) to interface with Discord, and [openai-java](https://github.com/TheoKanning/openai-java) to interface with OpenAI.

## Contributing

To build and run this program:
compile with Maven: `mvn clean compile assembly:single`
move the jar to the top-level directory: `mv target/AspectiBot<version>.jar AspectiBot.jar`
execute with: `java -jar AspectiBot.jar`

Please read the [CONTRIBUTING.md](CONTRIBUTING.md) for more comprehensive instructions and if you'd like to help! In addition, you can join our Discord server [here](https://discord.gg/aspecticor).

