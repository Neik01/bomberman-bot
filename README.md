# Bomberman Bot v2

An advanced Java bot for Bomberman-style programming competition using Socket.IO with sophisticated AI decision making.

## Setup

1. **Environment Variables**: Create a `.env` file with the following variables:
   ```
   SOCKET_SERVER=YOUR_SERVER_ADDRESS:PORT
   BOT_TOKEN=YOUR_BOT_TOKEN
   ```

2. **Build and Run with Docker**:
   ```bash
   docker-compose up --build
   ```

3. **Build and Run Locally**:
   ```bash
   mvn clean install
   java -jar target/bomberman-bot-1.0-SNAPSHOT.jar
   ```

## Features

### 🧠 Advanced AI
- **Heuristic Engine**: Sophisticated decision making with weighted scoring system
- **Multi-Action Support**: Move, bomb placement, and move+bomb combinations
- **Safety First**: Advanced escape route calculation and danger assessment
- **Strategic Positioning**: Optimal distance from enemies and center positioning

### 🎯 Smart Targeting
- **Chest Destruction**: Prioritizes destroying chests for points
- **Enemy Elimination**: Calculates bomb placement for maximum enemy damage
- **Item Collection**: Values items based on current stats and needs
- **Escape Planning**: BFS-based escape route calculation

### ⚡ Performance
- **Fast Decision Making**: 100ms decision intervals (vs 200ms in v1)
- **Efficient State Management**: Centralized GameState with optimized data structures
- **Comprehensive Event Handling**: All socket events including user_die_update

## Architecture

### Core Classes
- `BombermanBot`: Main bot class with Socket.IO client and game loop
- `HeuristicEngine`: Advanced AI decision making engine
- `GameState`: Centralized game state management
- `Action`/`ActionType`: Action system supporting complex moves

### Model Classes
- `Bomber`: Player/bot data with enhanced stats tracking
- `Bomb`, `Chest`, `Item`: Game entity data classes
- `Position`: Grid position utilities

### Event System
- Complete socket event handling (user, start, player_move, new_bomb, bomb_explode, map_update, user_die_update, chest_destroyed, item_collected, new_enemy, finish)
- Robust error handling and state synchronization

## AI Strategy

### Decision Making Process
1. **Safety Check**: Immediate escape if in danger
2. **Action Evaluation**: Score all possible actions (move, bomb, move+bomb)
3. **Weighted Scoring**: 
   - Enemy attacks: 100 points
   - Chest destruction: 80 points
   - Item collection: 90 points
   - Safety: 200 points
   - Enemy proximity: -50 points
   - Center positioning: 30 points

### Advanced Features
- **Escape Route Planning**: BFS-based pathfinding to safety
- **Bomb Timing**: Predicts explosion timing and escape routes
- **Item Value Assessment**: Dynamic item prioritization based on current stats
- **Enemy Prediction**: Considers enemy escape capabilities

## Development

The bot uses Maven for dependency management and includes:
- **Java 17**: Modern Java features and performance improvements
- Socket.IO Java client (2.1.0)
- JSON processing
- Advanced heuristic-based AI
- Multi-threaded decision making

## Competition Environment

The bot is designed to work in both practice and competition environments:
- **Practice**: Starts decision making immediately after receiving initial state
- **Competition**: Waits for 'start' event before beginning decision making
- **Robust Error Handling**: Continues operation even with network issues

