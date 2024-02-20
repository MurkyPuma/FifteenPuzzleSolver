package fifteenpuzzle;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashSet;





public class Solver {

	static int[] board;
	static int SIZE;
	static int[] boardSolved;
	static int blank;
	public final static int UP = 0;
	public final static int DOWN = 1;
	public final static int LEFT = 2;
	public final static int RIGHT = 3;


	static class BadBoardException extends Exception {
		public BadBoardException(String message) {
			super(message);
		}
	}

	public static void boardInSolvedState() {
		int n = SIZE * SIZE;
		boardSolved = new int[SIZE*SIZE];
		int num = 1;
		for (int i = 0; i < SIZE*SIZE; i++) {
			if (num == n) {
				boardSolved[i] = 0;
			} else {
				boardSolved[i] = num;
			}
			num += 1;
		}
//		System.out.println("boardSolved: " + Arrays.toString(boardSolved));
	}


	public void createBoard(String inputFile) throws IOException, BadBoardException {
		// This method creates a board from an input file. It first creates a File object from the input file and a Scanner object to read from the file.
		File file = new File(inputFile);
		Scanner scanner = new Scanner(file);
		// Create a StringBuilder to concatenate the rows of the board into a single string
		StringBuilder stringArray = new StringBuilder();

		// Read in the size of the board and print it to the console
		SIZE = scanner.nextInt();
//		System.out.println(SIZE + " SIZE");

		// Consume the newline character after reading the size
		scanner.nextLine();

		// Iterate through each row of the board and append it to the StringBuilder, adding a space after each row to separate them
		for (int i = 0; i < SIZE; i++) {
			if (scanner.hasNext()) {
				stringArray.append(scanner.nextLine()).append(" ");
			}
		}

		// Transform black/empty spots to '0' and remove extra spaces in the concatenated string
		stringArray = new StringBuilder(stringArray.toString().replaceAll("\\s{3,}", " 0 "));
		stringArray = new StringBuilder(stringArray.toString().replaceAll("\\s{2,}", " "));

		// Remove any extra space at the beginning of the concatenated string
		if(Character.isWhitespace(stringArray.charAt(0))) {
			stringArray = new StringBuilder(stringArray.toString().replaceFirst(" ", ""));
		}

		// Create an integer array to represent the board
		board = new int[SIZE*SIZE];

		// Split the concatenated string into an array of strings using spaces as the delimiter
		String[] parts = stringArray.toString().split(" ");

		// If the number of elements in the string array does not match the expected number of elements, throw a BadBoardException
		if (parts.length != SIZE*SIZE) {
			throw new BadBoardException("Expected "+SIZE*SIZE+" elements, but found "+parts.length);
		}

		// Iterate through each element of the string array and parse it as an integer, storing it in the integer array representation of the board.
		// If an element is '0', initialize the blank variable to that index.
		int index = 0;
		for (int i = 0; i < SIZE*SIZE; i++) {
			String current = parts[index++].trim();
			if (!current.isEmpty()) {
				board[i] = Integer.parseInt(current);
				if (board[i] == 0) { //initialize blank
					blank = board[i];
				}
			}
		}

		// Print the integer array representation of the board to the console
//		System.out.println("board: " + Arrays.toString(board));

		// Close the Scanner object
		scanner.close();
	}

	public static class IDASolver {
		// A list to keep track of the numbers in the solution path
		static List<Integer> pathNum = new ArrayList<>();
		// An array that stores the string representation of the directions
		private final static String[] directions = {"U", "D", "L", "R"};

		// A list that stores the directions taken to reach the solution
		private static final List<String> path = new ArrayList<>();

		// A set to keep track of the visited states
		private static final HashSet<State> visitedStates = new HashSet<>();

		// A method that solves the puzzle using the IDA* algorithm
		public static List<String> solve(int[] initial) {
			int threshold = heuristic(initial);
			while (true) {
				int nextThreshold = Integer.MAX_VALUE;
				Stack<State> stack = new Stack<>();
				Map<Integer, Integer> gScores = new HashMap<>();

				// Push the initial state onto the stack
				State initial_state = new State(initial, 0, heuristic(initial));
				stack.push(initial_state);
				gScores.put(initial_state.hashCode(), initial_state.g);

				// Continue until the stack is empty
				while (!stack.isEmpty()) {
					// Pop the state with the lowest f-value from the stack
					State current = stack.pop();

					// Check if the f-value of the current state is greater than the threshold
					if (current.f > threshold) {
						nextThreshold = Math.min(nextThreshold, current.f);
					} else {
						visitedStates.add(current);
						// Check if the current state is the solution
						if (Arrays.equals(current.board, boardSolved)) {
							return reconstructPath(current);
						}

						// Try moving the blank tile in all directions
						for (int direction : new int[] {UP, DOWN, LEFT, RIGHT}) {
							// Calculate the index of the tile that the blank tile will move to
							int newBlank = current.blank + getDelta(direction);

							// Check if the new index is out of bounds or the move cannot be made
							if (isOutOfBounds(newBlank) || !canMove(current.blank, direction)) {
								continue;
							}

							// Create a new board by swapping the blank tile with the tile at the new index
							int[] nextBoard = Arrays.copyOf(current.board, current.board.length);
							swap(nextBoard, current.blank, newBlank);

							// Create a new state for the new board
							State nextState = new State(nextBoard, newBlank, heuristic(nextBoard));

							// Calculate the tentative g-score for the new state
							int tentativeGScore = gScores.getOrDefault(current.hashCode(), Integer.MAX_VALUE) + 1;

							// If the tentative g-score is better than the existing g-score, update the state
							if (tentativeGScore < gScores.getOrDefault(nextState.hashCode(), Integer.MAX_VALUE)) {
								nextState.parentBoard = current;
								gScores.put(nextState.hashCode(), tentativeGScore);
								nextState.f = tentativeGScore + nextState.h;
								if (!visitedStates.contains(nextState)) {
									stack.push(nextState);
								}
							}
						}
					}
				}

				// If the next threshold is the maximum integer value, the puzzle is unsolvable
				if (nextThreshold == Integer.MAX_VALUE) {
					return null;
				}

				// Update the threshold and clear the visited states set
				threshold = nextThreshold;
				visitedStates.clear();
			}
		}

		private static int getDelta(int direction) {
			// Return the delta value for the given direction.
			return direction == UP ? -SIZE :
					direction == DOWN ? SIZE :
							direction == LEFT ? -1 :
									direction == RIGHT ? 1 :
											0; // Default case: no movement
		}
		private static boolean isOutOfBounds(int index) {
			// Check if the index is out of bounds in the board.
			return index < 0 || index >= board.length;
		}
		private static boolean canMove(int blank, int direction) {
			// Check if the blank tile can be moved to the given direction.
			return (direction == UP && blank / SIZE != 0) ||
					(direction == DOWN && blank / SIZE != SIZE - 1) ||
					(direction == LEFT && blank % SIZE != 0) ||
					(direction == RIGHT && blank % SIZE != SIZE - 1);
		}

		private static void swap(int[] board, int i, int j) {
			// Swap the tiles at positions i and j in the board.
			int temp = board[i];
			board[i] = board[j];
			board[j] = temp;
		}


		private static int heuristic(int[] board) {
			// Compute the heuristic value of the given board (i.e., the sum of Manhattan distances).
			int h = 0;
			for (int i = 0; i < board.length; i++) {
				if (board[i] == 0) {
					continue; // Skip the blank tile
				}
				h += manhattanDistance(i, board[i]); // Add the Manhattan distance of the tile to the heuristic value
			}
			return h;
		}

		private static int manhattanDistance(int currentPos, int num) {
			if (num == 0) {
				return 0; // The blank tile has no Manhattan distance
			}
			// Compute the row and column of the current tile and the goal tile
			int row = currentPos / SIZE;
			int col = currentPos % SIZE;
			int goalRow = (num - 1) / SIZE;
			int goalCol = (num - 1) % SIZE;
			if (SIZE <= 4) { //most optimal solution if size is 4 or less
				// Compute the Manhattan distance of the tile and multiply by a factor of SIZE/2
				// This gives a better heuristic for small board sizes
				return (Math.abs(row - goalRow) + Math.abs(col - goalCol)) * (SIZE/2);
			} else {
				// Compute the Manhattan distance of the tile and multiply by a factor of SIZE^2/2
				// This gives a better heuristic for large board sizes
				return (Math.abs(row - goalRow) + Math.abs(col - goalCol)) * (SIZE*SIZE/2);
			}
		}

		// Reconstruct the path taken to reach the current state by following the parent board pointers.
		private static List<String> reconstructPath(State current) {
			while (current.parentBoard != null) {
				State prev = current.parentBoard;
				// Get the move made from the previous board to the current board.
				int move = getMove(prev.blank, current.blank);
				// Add the direction of the move to the path.
				path.add(directions[move]);
				// Add the number moved to the pathNum list.
				int moveNum = current.board[prev.blank];
				pathNum.add(moveNum);
				// Move to the parent board.
				current = prev;
			}

// Reverse the path and pathNum lists to get them in the correct order.
			Collections.reverse(pathNum);
			Collections.reverse(path);

			return path;
		}

		// Get the move made from the previous position to the new position.
		private static int getMove(int prevPos, int newPos) {
			if (newPos - prevPos == SIZE) {
				return UP;
			} else if (newPos - prevPos == -SIZE) {
				return DOWN;
			} else if (newPos - prevPos == 1) {
				return LEFT;
			} else {
				return RIGHT;
			}
		}
	}

	// Definition of the State class that represents a state of the puzzle.
	static class State implements Comparable<State> {
		public int[] board; // the current state of the board
		public int g; // the cost of getting to this state
		public int h; // the heuristic cost of this state
		public int f; // the total cost of this state (g + h)
		public int blank; // the index of the blank tile in the board
		public State parentBoard; // the parent of this state

		public State(int[] board, int g, int h) { // constructor of the State class
			this.board = board; // initializes the board
			this.g = g; // initializes the cost of getting to this state
			this.h = h; // initializes the heuristic cost of this state

			// finds the index of the blank tile in the board
			for (int i = 0; i < board.length; i++) {
				if (board[i] == 0) {
					this.blank = i;
					break;
				}
			}
		}

		public int f() { // calculates the total cost of this state
			return g + h;
		}

		public int compareTo(State that) { // compares this state with another state based on their total cost
			return Integer.compare(this.f(), that.f());
		}

		public boolean equals(Object that) { // checks if this state is equal to another state
			if (that == null || that.getClass() != this.getClass()) {
				return false;
			}
			State state = (State) that;
			return Arrays.equals(this.board, state.board);
		}

		public int hashCode() { // computes the hash code of this state
			return Arrays.hashCode(this.board);
		}

		public String toString() { // returns a string representation of this state
			StringBuilder sb = new StringBuilder();
			sb.append("{\n");
			for (int i = 0; i < SIZE; i++) {
				sb.append("  ");
				for (int j = 0; j < SIZE; j++) {
					sb.append(board[i * SIZE + j]);
					sb.append(" ");
				}
				sb.append("\n");
			}
			sb.append("  g: ");
			sb.append(g);
			sb.append(", h: ");
			sb.append(h);
			sb.append("\n}\n");
			return sb.toString();
		}
	}



	public static void main(String[] args) throws IOException, BadBoardException {
		long startTime = System.nanoTime();
		if (args.length != 2) {
			System.err.println("Usage: java Solver inputfile outputfile");
			System.exit(1);
		}

		String inputFile = args[0];
		String outputFile = args[1];
		Solver puzzle = new Solver();

		puzzle.createBoard(inputFile);
		boardInSolvedState();

		System.out.print("Puzzle size: " + SIZE + "x" + SIZE +'\n');
		List<String> path = IDASolver.solve(board);
		if (path != null) {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			for (int i = 0; i < path.size(); i++) {
				String direction = path.get(i);
				writer.write( IDASolver.pathNum.get(i) + " " + direction + '\n');
//				System.out.println(IDASolver.pathNum.get(i) + " " + direction);
			}
			System.out.println("Found a solution in: " + path.size() + " moves");
			writer.close();
		} else {
			System.out.println("No solution found");
		}
		long endTime   = System.nanoTime();
		long totalTime = endTime - startTime;
		System.out.println("Runtime: " + totalTime/1000000000.0 + " seconds\n");
	}
}







