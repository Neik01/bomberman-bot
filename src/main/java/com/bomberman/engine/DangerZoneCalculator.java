package com.bomberman.engine;

public class DangerZoneCalculator {

    // Build a danger zone map for a hypothetical bomb placed at (bombRow, bombCol)
    public boolean[][] buildBombDangerZone(String[][] map, int bombRow, int bombCol, int range) {
        if (map == null || map.length == 0) return null;
        int rows = map.length;
        int cols = map[0].length;
        boolean[][] dangerZone = new boolean[rows][cols];

        // Mark center
        if (bombRow >= 0 && bombRow < rows && bombCol >= 0 && bombCol < cols) {
            dangerZone[bombRow][bombCol] = true;
        }

        // Mark 4 directions from the bomb
        markDangerInDirection(dangerZone, map, bombRow, bombCol, -1, 0, range); // UP
        markDangerInDirection(dangerZone, map, bombRow, bombCol, 1, 0, range);  // DOWN
        markDangerInDirection(dangerZone, map, bombRow, bombCol, 0, -1, range); // LEFT
        markDangerInDirection(dangerZone, map, bombRow, bombCol, 0, 1, range);  // RIGHT

        return dangerZone;
    }

    private void markDangerInDirection(boolean[][] dangerZone, String[][] map,
                                       int startRow, int startCol, int deltaRow, int deltaCol, int range) {
        for (int i = 1; i <= range; i++) {
            int row = startRow + (deltaRow * i);
            int col = startCol + (deltaCol * i);

            if (row < 0 || row >= map.length || col < 0 || col >= map[0].length) break;

            String cell = map[row][col];
            if ("W".equals(cell)) break; // Walls block explosion completely

            dangerZone[row][col] = true;

            if ("C".equals(cell)) break; // Chests stop further propagation
        }
    }
}
