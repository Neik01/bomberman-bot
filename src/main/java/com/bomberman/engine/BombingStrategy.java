package com.bomberman.engine;

import com.bomberman.model.*;

import java.util.*;

public class BombingStrategy {
    private static final int[] ROW_DIRS = {-1, 1, 0, 0}; // UP, DOWN, LEFT, RIGHT
    private static final int[] COL_DIRS = {0, 0, -1, 1};
    
    private GameState gameState;
    
    public BombingStrategy(GameState gameState) {
        this.gameState = gameState;
    }
    
    // Find the best position to place a bomb with safe retreat - O(map_size * range)
    public BombTarget findBestBombingPosition() {
        Bomber myBot = gameState.getMyBomber();
        if (myBot == null || !myBot.isAlive()) return null;
        
        String[][] map = gameState.getGameMap().getMap();
        if (map == null) return null;
        
        int[] myCell = gameState.coordToCell(myBot.getX(), myBot.getY());
        int myRow = myCell[0];
        int myCol = myCell[1];
        
        BombTarget bestTarget = null;
        int bestScore = 0;
        
        // Search nearby cells for bombing positions (within reasonable distance)
        int searchRadius = 5; // Search 5 cells in each direction
        
        for (int r = Math.max(0, myRow - searchRadius); r < Math.min(map.length, myRow + searchRadius + 1); r++) {
            for (int c = Math.max(0, myCol - searchRadius); c < Math.min(map[0].length, myCol + searchRadius + 1); c++) {
                // Skip non-walkable cells
                if (map[r][c] != null) continue;
                
                // Evaluate this position
                BombTarget target = evaluateBombPosition(r, c, myBot.getExplosionRange());
                
                if (target != null && target.isHasRetreat() && target.getScore() > bestScore) {
                    // Calculate path to this position
                    List<String> pathToTarget = findPath(myRow, myCol, r, c);
                    if (pathToTarget != null && !pathToTarget.isEmpty()) {
                        target.setPathToTarget(pathToTarget);
                        bestTarget = target;
                        bestScore = target.getScore();
                    }
                }
            }
        }
        
        return bestTarget;
    }
    
    // Find path using A* algorithm - O(map_size * log(map_size))
    private List<String> findPath(int startRow, int startCol, int goalRow, int goalCol) {
        String[][] map = gameState.getGameMap().getMap();
        if (map == null) return null;
        
        // If already at goal
        if (startRow == goalRow && startCol == goalCol) {
            return new ArrayList<>();
        }
        
        PriorityQueue<PathNode> openSet = new PriorityQueue<>();
        boolean[][] visited = new boolean[map.length][map[0].length];
        
        PathNode start = new PathNode(startRow, startCol, 0, heuristic(startRow, startCol, goalRow, goalCol), null, null);
        openSet.offer(start);
        
        while (!openSet.isEmpty()) {
            PathNode current = openSet.poll();
            
            if (current.getRow() == goalRow && current.getCol() == goalCol) {
                // Reconstruct path
                return reconstructPath(current);
            }
            
            if (visited[current.getRow()][current.getCol()]) continue;
            visited[current.getRow()][current.getCol()] = true;
            
            // Explore neighbors
            String[] directions = {"UP", "DOWN", "LEFT", "RIGHT"};
            for (int i = 0; i < 4; i++) {
                int newRow = current.getRow() + ROW_DIRS[i];
                int newCol = current.getCol() + COL_DIRS[i];
                
                if (newRow < 0 || newRow >= map.length || newCol < 0 || newCol >= map[0].length) continue;
                if (visited[newRow][newCol]) continue;
                if (map[newRow][newCol] != null) continue; // Can't walk through walls/chests
                
                int newG = current.getG() + 1;
                int newH = heuristic(newRow, newCol, goalRow, goalCol);
                int newF = newG + newH;
                
                PathNode neighbor = new PathNode(newRow, newCol, newG, newF, current, directions[i]);
                openSet.offer(neighbor);
            }
        }
        
        return null; // No path found
    }
    
    // Manhattan distance heuristic
    private int heuristic(int row1, int col1, int row2, int col2) {
        return Math.abs(row1 - row2) + Math.abs(col1 - col2);
    }
    
    // Reconstruct path from goal to start
    private List<String> reconstructPath(PathNode goal) {
        List<String> path = new ArrayList<>();
        PathNode current = goal;
        
        while (current.getParent() != null) {
            path.add(0, current.getDirection()); // Add to front
            current = current.getParent();
        }
        
        return path;
    }
    
    // Evaluate a bombing position - O(range)
    private BombTarget evaluateBombPosition(int row, int col, int explosionRange) {
        String[][] map = gameState.getGameMap().getMap();
        if (map == null || row < 0 || row >= map.length || col < 0 || col >= map[0].length) {
            return null;
        }
        
        // Can't place bomb on non-walkable cell
        if (map[row][col] != null) return null;
        
        int chestsHit = 0;
        int enemiesHit = 0;
        
        // Count targets in 4 directions
        chestsHit += countTargetsInDirection(row, col, -1, 0, explosionRange, true);  // UP
        chestsHit += countTargetsInDirection(row, col, 1, 0, explosionRange, true);   // DOWN
        chestsHit += countTargetsInDirection(row, col, 0, -1, explosionRange, true);  // LEFT
        chestsHit += countTargetsInDirection(row, col, 0, 1, explosionRange, true);   // RIGHT
        
        enemiesHit += countTargetsInDirection(row, col, -1, 0, explosionRange, false); // UP
        enemiesHit += countTargetsInDirection(row, col, 1, 0, explosionRange, false);  // DOWN
        enemiesHit += countTargetsInDirection(row, col, 0, -1, explosionRange, false); // LEFT
        enemiesHit += countTargetsInDirection(row, col, 0, 1, explosionRange, false);  // RIGHT
        
        // Calculate score (enemies worth more than chests)
        int score = chestsHit + (enemiesHit * 3);
        
        if (score == 0) return null; // No targets, don't bomb
        
        // Check for retreat path
        boolean hasRetreat = findRetreatPath(row, col, explosionRange);
        
        BombTarget target = new BombTarget();
        target.setRow(row);
        target.setCol(col);
        target.setChestsHit(chestsHit);
        target.setEnemiesHit(enemiesHit);
        target.setScore(score);
        target.setHasRetreat(hasRetreat);
        
        return target;
    }
    
    // Count targets in one direction - O(range)
    private int countTargetsInDirection(int startRow, int startCol, int deltaRow, int deltaCol, 
                                        int range, boolean countChests) {
        String[][] map = gameState.getGameMap().getMap();
        int count = 0;
        
        for (int i = 1; i <= range; i++) {
            int row = startRow + (deltaRow * i);
            int col = startCol + (deltaCol * i);
            
            if (row < 0 || row >= map.length || col < 0 || col >= map[0].length) break;
            
            String cell = map[row][col];
            
            // Wall blocks explosion completely
            if ("W".equals(cell)) break;
            
            // Chest blocks explosion - count it if counting chests, then stop
            if ("C".equals(cell)) {
                if (countChests) {
                    count++;
                }
                break;
            }
            
            // Count enemies only if not counting chests
            if (!countChests) {
                int[] coord = gameState.cellToCoord(row, col);
                if (isEnemyAtPosition(coord[0], coord[1])) {
                    count++;
                }
            }
        }
        
        return count;
    }
    
    // Check if enemy is at position - O(bombers)
    private boolean isEnemyAtPosition(int x, int y) {
        Bomber myBot = gameState.getMyBomber();
        if (myBot == null) return false;
        
        for (Bomber bomber : gameState.getEnemies()) {
            if (!bomber.isAlive()) continue;
            
            // Check if bomber is in this cell (using cell-based comparison)
            int[] bomberCell = gameState.coordToCell(bomber.getX(), bomber.getY());
            int[] targetCell = gameState.coordToCell(x, y);
            
            if (bomberCell[0] == targetCell[0] && bomberCell[1] == targetCell[1]) {
                return true;
            }
        }
        return false;
    }
    
    // Find retreat path using BFS - O(map_size) worst case
    private boolean findRetreatPath(int bombRow, int bombCol, int explosionRange) {
        String[][] map = gameState.getGameMap().getMap();
        int rows = map.length;
        int cols = map[0].length;
        
        // Create danger zone for this hypothetical bomb
        boolean[][] dangerZone = new boolean[rows][cols];
        markDangerFromBomb(dangerZone, bombRow, bombCol, explosionRange);
        
        // BFS to find safe cell within 5 seconds (assuming speed=1, ~5 cells away)
        Queue<int[]> queue = new LinkedList<>();
        boolean[][] visited = new boolean[rows][cols];
        
        queue.offer(new int[]{bombRow, bombCol, 0}); // row, col, distance
        visited[bombRow][bombCol] = true;
        
        Bomber myBot = gameState.getMyBomber();
        int maxDistance = 5 + (myBot != null ? myBot.getSpeedCount() : 0); // Can move further with speed boost
        
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int row = current[0];
            int col = current[1];
            int dist = current[2];
            
            // Found safe cell within reachable distance
            if (!dangerZone[row][col] && dist > 0) {
                return true;
            }
            
            if (dist >= maxDistance) continue;
            
            // Explore 4 directions
            for (int i = 0; i < 4; i++) {
                int newRow = row + ROW_DIRS[i];
                int newCol = col + COL_DIRS[i];
                
                if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols) continue;
                if (visited[newRow][newCol]) continue;
                if (map[newRow][newCol] != null) continue; // Can't walk through walls/chests
                
                visited[newRow][newCol] = true;
                queue.offer(new int[]{newRow, newCol, dist + 1});
            }
        }
        
        return false; // No safe retreat found
    }
    
    // Mark danger zone for a hypothetical bomb - O(range)
    private void markDangerFromBomb(boolean[][] dangerZone, int bombRow, int bombCol, int range) {
        String[][] map = gameState.getGameMap().getMap();
        dangerZone[bombRow][bombCol] = true;
        
        // Mark 4 directions
        markDangerInDirection(dangerZone, map, bombRow, bombCol, -1, 0, range); // UP
        markDangerInDirection(dangerZone, map, bombRow, bombCol, 1, 0, range);  // DOWN
        markDangerInDirection(dangerZone, map, bombRow, bombCol, 0, -1, range); // LEFT
        markDangerInDirection(dangerZone, map, bombRow, bombCol, 0, 1, range);  // RIGHT
    }
    
    private void markDangerInDirection(boolean[][] dangerZone, String[][] map, 
                                       int startRow, int startCol, int deltaRow, int deltaCol, int range) {
        for (int i = 1; i <= range; i++) {
            int row = startRow + (deltaRow * i);
            int col = startCol + (deltaCol * i);
            
            if (row < 0 || row >= map.length || col < 0 || col >= map[0].length) break;
            
            String cell = map[row][col];
            if ("W".equals(cell)) break;
            
            dangerZone[row][col] = true;
            
            if ("C".equals(cell)) break;
        }
    }
    
    // Check if target cell is in bomb's blast path - O(1)
    private boolean isInBombBlastPath(int bombRow, int bombCol, int targetRow, int targetCol, int range) {
        // Same row (horizontal blast)
        if (bombRow == targetRow) {
            int distance = Math.abs(bombCol - targetCol);
            return distance <= range;
        }
        
        // Same column (vertical blast)
        if (bombCol == targetCol) {
            int distance = Math.abs(bombRow - targetRow);
            return distance <= range;
        }
        
        return false; // Not in blast path
    }
}



