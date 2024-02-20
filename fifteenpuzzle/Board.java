package fifteenpuzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Board {
    private static final int SIZE = 4;
    private static final int[][] GOAL = new int[][]{{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}, {13, 14, 15, 0}};

    private final int[][] tiles;
    private final int emptyRow;
    private final int emptyCol;
    private final int manhattan;

    public Board(int[][] tiles) {
        this.tiles = tiles;
        int manhattanSum = 0;
        int emptyRow = -1;
        int emptyCol = -1;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (tiles[i][j] == 0) {
                    emptyRow = i;
                    emptyCol = j;
                } else {
                    int value = tiles[i][j];
                    int row = (value - 1) / SIZE;
                    int col = (value - 1) % SIZE;
                    manhattanSum += Math.abs(row - i) + Math.abs(col - j);
                }
            }
        }
        this.emptyRow = emptyRow;
        this.emptyCol = emptyCol;
        this.manhattan = manhattanSum;
    }

    public boolean isGoal() {
        return Arrays.deepEquals(tiles, GOAL);
    }

    public Iterable<Board> neighbors() {
        List<Board> neighbors = new ArrayList<>();
        if (emptyRow > 0) {
            int[][] copy = copyTiles();
            swap(copy, emptyRow, emptyCol, emptyRow - 1, emptyCol);
            neighbors.add(new Board(copy));
        }
        if (emptyRow < SIZE - 1) {
            int[][] copy = copyTiles();
            swap(copy, emptyRow, emptyCol, emptyRow + 1, emptyCol);
            neighbors.add(new Board(copy));
        }
        if (emptyCol > 0) {
            int[][] copy = copyTiles();
            swap(copy, emptyRow, emptyCol, emptyRow, emptyCol - 1);
            neighbors.add(new Board(copy));
        }
        if (emptyCol < SIZE - 1) {
            int[][] copy = copyTiles();
            swap(copy, emptyRow, emptyCol, emptyRow, emptyCol + 1);
            neighbors.add(new Board(copy));
        }
        return neighbors;
    }

    public int manhattan() {
        return manhattan;
    }

    private int[][] copyTiles() {
        int[][] copy = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(tiles[i], 0, copy[i], 0, SIZE);
        }
        return copy;
    }

    private void swap(int[][] tiles, int i, int j, int k, int l) {
        int temp = tiles[i][j];
        tiles[i][j] = tiles[k][l];
        tiles[k][l] = temp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                sb.append(String.format("%2d ", tiles[i][j]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}

