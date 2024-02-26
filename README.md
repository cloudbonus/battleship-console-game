# Battleship Game

## Overview

The game is played on a 16x16 grid, where the columns are labeled from A to P and the rows from 1 to 16. Players have a fleet consisting of 6 types of ships, ranging in size from 1 to 6 cells.

The game offers two modes: solo play against a bot or multiplayer mode using websockets for real-time interaction. In both modes, players can choose to place their ships on the grid either manually or automatically, following the rule that ships cannot overlap or be adjacent to each other.

During the game, players take turns guessing the coordinates of their opponent's ships. The game provides feedback in the form of "hit," "destroyed," or "miss." If a player misses, the turn shifts to the opponent.

The primary objective is to be the first to destroy all of the opponent's ships. Each move is visually represented on the grid, with a cross denoting a "miss" and a cross inside a box indicating a "hit" or "destroyed" ship.

## Installation

1. **Clone the Repository**
    ```console  
    git clone https://github.com/cloudbonus/senla-project.git 
    ```
2. **Run the Script**
   - For Linux systems, execute `battleship_linux.sh`.
   - For Windows systems, execute `battleship_windows.bat`.

## How to Play

Follow the on-screen prompts to start playing the game.

## Features

### Game

- At the start of the game, the player is prompted to enter their name.
- The game offers a choice between "singleplayer mode" and "multiplayer mode".
- In "singleplayer mode", a shooting algorithm is implemented based on the responses from the game field (without looking at the player's field).
- In "multiplayer mode", the game synchronizes actions and turn distribution via a websocket connection. Additionally, the host connection is specified in the game mode for connecting to an existing player.
- The second player connects if the first player has started the game via websocket.
- If players for a game have already been found, all subsequent connections are made as "observers" who cannot make moves, but can only watch the host game.
- Each turn, the game draws the player's field and the opponent's field with hit markers, clearing the console state.
- An automatic ship filling mode is implemented.

### Bot

- During each turn, the bot is programmed to concentrate on a specific action: either finalizing an attack or searching a ship.
- In the “searching” phase, the bot uses strategic tactics. If the largest ship has not been hit and there are no areas left on the opponent’s field where such a ship could be located, the bot aims for such an area. At the beginning of the game, the bot uses random coordinates.
- The “finalizing an attack” phase means that if a hit resulted in a “hit” ship, the bot must destroy the ship before moving on to the “searching” phase.

## To-Do

- [ ] Refactor the code. There's a significant amount of refactoring to be done to improve the current codebase.
- [ ] Add logging functionality to track the game moves.
- [ ] Implement an Admin mode for enhanced control and management.
- [ ] Improve spectator mode.


## Known Bugs

This section lists the known bugs in the game. They are being worked on and will be fixed in future updates:

1. Windows Encoding: Symbols that work correctly on Linux are displayed incorrectly in the Windows console.

## Contributing

Feel free to explore the code and make improvements. Contributions are welcome!

## License

[MIT](https://choosealicense.com/licenses/mit/)