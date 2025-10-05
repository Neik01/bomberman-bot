package com.bomberman;

import com.bomberman.engine.*;
import com.bomberman.model.*;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;

public class BombermanBot {
    private Socket socket;
    private GameState gameState;
    private BombingStrategy strategy;
    private String myUid;
    private boolean isGameStarted = false;
    private ScheduledExecutorService scheduler;

    public BombermanBot(String serverUrl, String token) throws URISyntaxException {
        gameState = new GameState();
        strategy = new BombingStrategy(gameState);
        scheduler = Executors.newScheduledThreadPool(1);

        IO.Options options = new IO.Options();
        options.auth = Collections.singletonMap("token", token);
        options.reconnection = true;
        options.reconnectionDelay = 1000;
        options.timeout = 10000;
        
        socket = IO.socket(serverUrl, options);
        setupEventHandlers();
        
        System.out.println("Bot initialized with pathfinding (BFS & A*)");
    }

    private void setupEventHandlers() {
        socket.on(Socket.EVENT_CONNECT, args -> {
            System.out.println("Connected to server");
            joinRoom();
        });

        socket.on(Socket.EVENT_DISCONNECT, args -> {
            System.out.println("Disconnected from server");
        });

        socket.on("user", onUserJoined);
        socket.on("start", onGameStart);
        socket.on("player_move", onPlayerMove);
        socket.on("new_bomb", onNewBomb);
        socket.on("bomb_explode", onBombExplode);
        socket.on("map_update", onMapUpdate);
        socket.on("user_die_update", onUserDie);
        socket.on("chest_destroyed", onChestDestroyed);
        socket.on("item_collected", onItemCollected);
        socket.on("new_enemy", onNewEnemy);
        socket.on("finish", onGameFinish);
    }

    private void joinRoom() {
        socket.emit("join", new JSONObject());
        System.out.println("Joined room");
    }

    private Emitter.Listener onUserJoined = args -> {
        try {
            JSONObject data = (JSONObject) args[0];
            System.out.println("Received user data" +data.toString());
            
            // Parse map
            JSONArray mapArray = data.getJSONArray("map");
            gameState.parseMap(mapArray);
            
            // Parse bombers
            JSONArray bombers = data.getJSONArray("bombers");
            gameState.parseBombers(bombers);
            
            // Find my UID
            for (int i = 0; i < bombers.length(); i++) {
                JSONObject bomber = bombers.getJSONObject(i);
                myUid = bomber.getString("uid");
                gameState.setMyUid(myUid);
                break; // First bomber is me
            }
            
            // Parse bombs
            if (data.has("bombs")) {
                JSONArray bombs = data.getJSONArray("bombs");
                gameState.parseBombs(bombs);
            }
            
            // Parse chests
            if (data.has("chests")) {
                JSONArray chests = data.getJSONArray("chests");
                gameState.parseChests(chests);
            }
            
            // Parse items
            if (data.has("items")) {
                JSONArray items = data.getJSONArray("items");
                gameState.parseItems(items);
            }
            
            System.out.println("Game state initialized. My UID: " + myUid);
            
            // Start bot immediately in practice mode (no start event)
            // In competition mode, wait for start event
            isGameStarted = true;
            startBotLogic();
            System.out.println("Bot started moving!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private Emitter.Listener onGameStart = args -> {
        // This event only fires in competition mode
        isGameStarted = true;
        System.out.println("Game started! (Competition mode)");
        // Don't start bot logic here as it's already started in onUserJoined
        // This just confirms the competition has begun
    };

    private Emitter.Listener onPlayerMove = args -> {
        try {
            JSONObject bomber = (JSONObject) args[0];
            gameState.updateBomber(bomber);
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private Emitter.Listener onNewBomb = args -> {
        try {
            JSONObject bomb = (JSONObject) args[0];
            gameState.addBomb(bomb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private Emitter.Listener onBombExplode = args -> {
        try {
            JSONObject data = (JSONObject) args[0];
            int bombId = data.getInt("id");
            gameState.removeBomb(bombId);
            System.out.println("Bomb exploded "+ data.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private Emitter.Listener onMapUpdate = args -> {
        try {
            JSONObject data = (JSONObject) args[0];
            if (data.has("chests")) {
                gameState.parseChests(data.getJSONArray("chests"));
            }
            if (data.has("items")) {
                gameState.parseItems(data.getJSONArray("items"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private Emitter.Listener onUserDie = args -> {
        try {
            JSONObject data = (JSONObject) args[0];
            if (data.has("killed")) {
                JSONObject killed = data.getJSONObject("killed");
                String killedUid = killed.getString("uid");
                gameState.removeBomber(killedUid);
            }
            if (data.has("killer")) {
                gameState.updateBomber(data.getJSONObject("killer"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private Emitter.Listener onChestDestroyed = args -> {
        try {
            JSONObject data = (JSONObject) args[0];
            int x = data.getInt("x");
            int y = data.getInt("y");
            gameState.removeChest(x, y);
            
            if (data.has("item") && !data.isNull("item")) {
                JSONObject item = data.getJSONObject("item");
                gameState.addItem(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private Emitter.Listener onItemCollected = args -> {
        try {
            JSONObject data = (JSONObject) args[0];
            if (data.has("item")) {
                JSONObject item = data.getJSONObject("item");
                int x = item.getInt("x");
                int y = item.getInt("y");
                gameState.removeItem(x, y);
            }
            if (data.has("bomber") && !data.isNull("bomber")) {
                gameState.updateBomber(data.getJSONObject("bomber"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private Emitter.Listener onNewEnemy = args -> {
        try {
            JSONObject data = (JSONObject) args[0];
            if (data.has("bomber")) {
                gameState.updateBomber(data.getJSONObject("bomber"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private Emitter.Listener onGameFinish = args -> {
        System.out.println("Game finished!");
        isGameStarted = false;
        scheduler.shutdown();
    };

    private void startBotLogic() {
        // Prevent starting multiple times
        if (scheduler.isShutdown() || scheduler.isTerminated()) {
            scheduler = Executors.newScheduledThreadPool(1);
        }

        new Thread(() -> {

                try {
                    // Wait for game to start in competition mode
                    if (!gameState.isGameStarted()) {
                        Thread.sleep(100);
                        continue;
                    }

                    makeDecision();
                    Thread.sleep(100); // Decision every 100ms
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

        }).start();
    }

    private enum BotState {
        IDLE,
        MOVING_TO_BOMB_POSITION,
        PLACING_BOMB,
        RETREATING
    }

    private BotState currentState = BotState.IDLE;
    private List<String> currentPath = null;
    private int pathIndex = 0;
    private BombTarget currentTarget = null;

    private void makeDecision() {
        Bomber myBot = gameState.getMyBomber();
        if (myBot == null || !myBot.isAlive()) return;

        // Calculate current danger zones
        boolean[][] dangerZones = gameState.calculateDangerZones();

        // PRIORITY: If we're in danger, retreat immediately (override any state)
        if (gameState.isInDangerZone(myBot.getX(), myBot.getY(), dangerZones)) {
            currentState = BotState.IDLE; // Reset state
            currentPath = null;

            String safeDirection = findSafeDirection(dangerZones);
            if (safeDirection != null) {
                move(safeDirection);
            }
            return;
        }

        // State machine for bombing strategy
        switch (currentState) {
            case IDLE:
                // Look for bombing opportunities
                if (myBot.getBombCount() > 0) {
                    BombTarget target = strategy.findBestBombingPosition();
                    if (target != null && target.hasRetreat && target.score > 0) {
                        currentTarget = target;
                        currentPath = target.pathToTarget;
                        pathIndex = 0;

                        if (currentPath == null || currentPath.isEmpty()) {
                            // Already at bombing position
                            currentState = BotState.PLACING_BOMB;
                        } else {
                            // Need to move to bombing position
                            currentState = BotState.MOVING_TO_BOMB_POSITION;
                        }

                        System.out.println("Found bomb target: " + target);
                    }
                }

                // If no bombing opportunity, explore
                if (currentState == BotState.IDLE) {
                    String exploreDirection = findSafeDirection(dangerZones);
                    if (exploreDirection != null) {
                        move(exploreDirection);
                    }
                }
                break;

            case MOVING_TO_BOMB_POSITION:
                // Follow path to bombing position
                if (currentPath != null && pathIndex < currentPath.size()) {
                    String direction = currentPath.get(pathIndex);
                    move(direction);
                    pathIndex++;
                } else {
                    // Reached bombing position
                    currentState = BotState.PLACING_BOMB;
                }
                break;

            case PLACING_BOMB:
                // Place bomb
                placeBomb();
                System.out.println("Placed bomb at position");

                // Calculate retreat path
                int[] myCell = gameState.coordToCell(myBot.getX(), myBot.getY());
                List<String> retreatPath = strategy.findRetreatPath(
                        myCell[0], myCell[1], myBot.getExplosionRange()
                );

                if (retreatPath != null && !retreatPath.isEmpty()) {
                    currentPath = retreatPath;
                    pathIndex = 0;
                    currentState = BotState.RETREATING;
                    System.out.println("Retreating with path: " + retreatPath);
                } else {
                    // No retreat path, just move to any safe direction
                    currentState = BotState.IDLE;
                    String safeDir = findSafeDirection(dangerZones);
                    if (safeDir != null) {
                        move(safeDir);
                    }
                }
                break;

            case RETREATING:
                // Follow retreat path
                if (currentPath != null && pathIndex < currentPath.size()) {
                    String direction = currentPath.get(pathIndex);
                    move(direction);
                    pathIndex++;
                } else {
                    // Finished retreating
                    System.out.println("Finished retreat, back to IDLE");
                    currentState = BotState.IDLE;
                    currentPath = null;
                    currentTarget = null;
                }
                break;
        }
    }

    private String findSafeDirection(boolean[][] dangerZones) {
        Bomber myBot = gameState.getMyBomber();
        int[] myCell = gameState.coordToCell(myBot.getX(), myBot.getY());

        String[] directions = {"UP", "DOWN", "LEFT", "RIGHT"};
        int[][] deltas = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int i = 0; i < 4; i++) {
            int newRow = myCell[0] + deltas[i][0];
            int newCol = myCell[1] + deltas[i][1];
            int[] newCoord = gameState.cellToCoord(newRow, newCol);

            if (gameState.isPositionWalkable(newCoord[0], newCoord[1]) &&
                    !gameState.isInDangerZone(newCoord[0], newCoord[1], dangerZones)) {
                return directions[i];
            }
        }

        return null;
    }
    private void move(String direction) {
        JSONObject data = new JSONObject();
        data.put("orient", direction);
        socket.emit("move", data);
        System.out.println("Moving: " + direction);
    }

    private void placeBomb() {
        socket.emit("place_bomb", new JSONObject());
        System.out.println("Placing bomb");
    }

    public void connect() {
        socket.connect();
    }

    public void disconnect() {
        socket.disconnect();
        scheduler.shutdown();
    }

    public static void main(String[] args) {
        String serverUrl = System.getenv("SOCKET_SERVER");
        String token = System.getenv("BOT_TOKEN");
        
        if (serverUrl == null || token == null) {
            System.err.println("Missing environment variables!");
            System.exit(1);
        }
        
        try {
            BombermanBot bot = new BombermanBot(serverUrl, token);
            bot.connect();
            
            // Keep the bot running
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}