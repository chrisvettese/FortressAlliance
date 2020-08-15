# Fortress Alliance
Fortress Alliance is a 2D multiplayer platform game written in Java with [libGDX](https://libgdx.badlogicgames.com/). The game consists of two teams that fight each other and aim to get the most kills before the timer runs out. It has a `server`, which hosts the game, a `map maker` program, which can be used to design levels, and a `client` that is used to play the game.

## Setup
### From the release
 1. Go to the release ([https://github.com/chrisvettese/FortressAlliance/releases](https://github.com/chrisvettese/FortressAlliance/releases)), and download demo.zip.
 2. Extract the files into a folder.
 ### From the source code
 1. Clone the repository.
 2. Inside the top level folder, run `gradlew desktop:dist`. This can be done in IntelliJ: open the project by selecting the build.gradle file, located in the top level folder, and then run the command.
 3. The output jar will be located in `desktop/build/libs`.
 ## Running the game
 If you downloaded the demo.zip release, there are batch/shell files that can run the game in `server`, `map maker`, or `client mode`.
 
 The game can also be run from the command line:
 To run the client: `java -jar jarname.jar`
 To run the server: `java -jar jarname.jar 1`
 To run the map maker: `java -jar jarname.jar 2`
## The Map Maker
Before you can play the game, you need a game map. The demo.zip release contains a pre-made game map, but you can also make your own, or edit the existing one.

First run the map maker. You can open an existing map file, or create a new one. Note that map files are loaded and saved from the same folder as the JAR file. The map maker has instructions on how to control it. Two spawn points (one for each team) must be placed before your map can be saved. Remember to place some weapons!

## The Server
Once you have a game map, it's time to start up the server. Run the server according to the instructions above, and enter the name of the map file you created or downloaded. It must be in the same folder as the JAR file. After selecting a map, the server will start. Now players on the same network can join!

## The Client
At least two people on different computers are needed to properly play the game (or many more), but it can still be tested with a single player. Run the client, enter a name, and enter the server IP. "localhost" is a valid server IP if the server is on the same network. When enough players have joined, one player can click start. This will start the timer and load the world.
### Controls:
A: move left
D: move right
S: drop down (if player is on a platform)
Space: jump
Scroll: switch weapon (if player has both weapons)
Click: use weapon

There are two weapons in the game, a sword and a gun. The player initially starts out with no weapons, and must find the weapons in the game. When the player dies, the other team gets a point, and the player is moved back to the spawn point.

![A scene in the example map.](https://raw.githubusercontent.com/chrisvettese/FortressAlliance/master/i1.png)
A scene in the example map.

![The waiting screen.](https://raw.githubusercontent.com/chrisvettese/FortressAlliance/master/i2.png)
The waiting screen.

![A fight between two players.](https://raw.githubusercontent.com/chrisvettese/FortressAlliance/master/i3.png)
A fight between two players.

![When a player dies.](https://raw.githubusercontent.com/chrisvettese/FortressAlliance/master/i4.png)
When a player dies.

![Editing the map with the map maker.](https://raw.githubusercontent.com/chrisvettese/FortressAlliance/master/i5.png)
Editing the map with the map maker.
