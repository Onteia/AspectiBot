# Contributing

Thank you for your interest in contributing to this project! It is an entirely volunteer-based project, and we appreciate every bit of help we can get.

## Table of Contents

- [Contributing](#contributing)
  - [Table of Contents](#table-of-contents)
  - [Code of Conduct](#code-of-conduct)
  - [How to Build and Test](#how-to-build-and-test)
  - [Style](#style)

## Code of Conduct

Read the Code of Conduct [here](CODE_OF_CONDUCT.md). Please be respectful and exercise common courtesy when contributing.

## How to Build and Test

- First, fork and clone the repository.

- Create your own [Discord bot](https://discord.com/developers/applications). You can find instructions on how to do so [here](https://discordpy.readthedocs.io/en/stable/discord.html). Make sure to select the `guild.members.read`, `bot`, `messages.read`, and `applications.commands` scopes. The token for the bot you've created should be copied into a file and kept secret. ***Do not share your bot token!***

- Create (or commandeer) a Discord server where you have the "Manage Server" permission. Using the scopes you have selected, you should be able to generate a URL that will invite your bot to your server. Create test channels and roles and copy the IDs (which are accessible after you turn on `User Settings > Advanced > Developer Mode`) and replace the values in `Aspecticor.java` ***Remember to change these values back before you commit!***

- Create your own Twitch bot. First, create a new account on Twitch. You can then generate an OAuth access token via third-party sites like <https://twitchtokengenerator.com> by selecting desired scopes and clicking "Generate Token!" As far as we can tell, you can add all scopes, and it doesn't break. Similar with the Discord token, you should keep this token private and copy this into a file somewhere.

- Create a config file. By default, we have this set to `src/config.properties`, but you can change this to any file if you change the `CONFIG_FILE` variable in `AspectiBot.java`. In this config file, place the path strings of your files containing the tokens as an escaped URI string. Do the same for two test images that will be the live and offline server icons. An example config file is shown below.

```properties
LIVE_ICON_PATH=C:\\Users\\pokem\\OneDrive\\Pictures\\Flags\\Atlae.png
OFFLINE_ICON_PATH=C:\\Users\\pokem\\OneDrive\\Pictures\\Flags\\AtlaeNightLight.png
DISCORD_TOKEN_PATH=C:\\Users\\pokem\\Documents\\GitHub\\AspectiBot\\discordToken.txt
TWITCH_TOKEN_PATH=C:\\Users\\pokem\\Documents\\GitHub\\AspectiBot\\twitchOAuth.txt
THIS_FOLDER_PATH=C:\\Users\\pokem\\Documents\\GitHub\\Aspectibot\\
```

- Install [Maven](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html), the tool we use to build this Java project. Use `mvn clean` and `mvn compile` as you see fit. Once you are ready to run, export the project as a JAR file and run `java -jar AspectiBot.jar`.

## Style

We're not terribly picky. Consult [existing Java style guides](https://google.github.io/styleguide/javaguide.html) liberally and keep unconventional code styling to a minimum.
