## Introduction
This project solves the [15 puzzle](https://en.wikipedia.org/wiki/15_Puzzle) game in Java. It takes two arguments, an input file containing the board and an output file containing the solution indicated by direction and tile number. The goal is to solve boards of size 3x3 to 9x9 and output the moves taken to solve each board.
## Progress and Methods
I started with the baseline of simply reading, storing, and displaying the board input. I originally saved the boards in two- dimensional (2-d) integer arrays but found it cumbersome to manipulate the data as I would need two nested loops to perform any operation on the board. Later, I decided to create the boards from the input file provided in simple one-dimensional (1-d) arrays of integers. The createBoard method is responsible for that. It first creates a File object from the input file and a Scanner object to read from the file. It reads the size of the board and iterates through each row of the board, appending it to a StringBuilder. I used StringBuilder to save time because I had previously written this code. I assumed it would not make much of a difference in terms of efficiency since this operation is only performed once. It transforms the blank/empty spot to a '0' and removes extra spaces in the concatenated string. The method then creates an integer array to represent the board, splits the concatenated string into an array of strings using spaces as the delimiter, and parses each element of the string array as an integer, storing it in the integer array representation of the board. This is also where I initialize the blank variable with the location of element ‘0’.
Throughout this project, I had difficulties producing efficient code that did not run out of memory. I went in blind and started the process with a breath-first search (BFS) algorithm. This proved to work perfectly while also giving me the most optimal solution. However, the caveat being, my implementation could not solve past most 4x4. I wrote a bash script to test all boards, automatically go to next board after 30 seconds, and record which tests failed. This script and the addition of a runtime counter saved me lots of time by allowing me to quickly test the program on all boards, all at once, using different algorithms.
<p align="center">
  <img src="https://github.com/MurkyPuma/FifteenPuzzleSolver/assets/74885743/f03ab45f-ff8d-45db-b4af-6a0f6add2955" />
</p>

After doing research I started implementing the A* algorithm with the Manhattan heuristic. This proved to also work well, solving all 4x4 and even some 5x5. However, I wanted better. Manhattan distance finds the most optimal solution, and as a result it was very memory intensive as it must go through all iterations of the board. After having some casual algorithm discussions with a classmate, he asked if I tried adding a weighted factor to the Manhattan distance, this way it considers that moving a tile sometimes involves sliding through several adjacent tiles. I played around with this weighted factor, finding ~26 to be the most efficient at solving the puzzle. To my surprise, my A* algorithm went from barely solving 5x5 to solving all 6x6 and some 7x7. I was extremely happy with this progress but thrived for a little more.

I then experimented with different ways of making my code more efficient. I tried using Parallelization, since I assumed it would allow the code to run faster by using multiple cores at once. This implementation was not complete as I realize the problem (at the time) revolved around running out of memory rather than not being able to solve the board fast enough. Linear conflict was also experimented with, where the program detects two tiles are both in the correct row or column but are in the wrong order. By summing all number of linear conflicts, an estimate of total number of moves required to solve the puzzle can be obtained. I’m sure that one or more of these heuristics/implementations would have proved to be more efficient than my current algorithm, however with time being of essence, I had to allocate it to documenting and producing a more robust code that runs.

Finally, I ended up using IDA* with the Manhattan distance heuristic and an alternating weighted factor based on the size of board. This approach does not result in the most optimal solution of each board, but it results in an efficient one. In terms of the alternating weighted
 
factor, I found that for puzzles of size 3x3 and 4x4 (maybe even 5x5), using no factor was perfectly fine and gave the most optimal solution within 30 seconds for all boards. To be safe however, I decided to implement a factor of (SIZE/2). For puzzles 5x5 and larger, I opted for the weighted factor of (SIZE*SIZE/2). There is no particular reason for these factors except that it allowed me to solve all 7x7 puzzles (and even some 8x8 puzzles). I am sure if I looked into it a bit more I could have found something a little more optimal. As a result of all this, the algorithm was able to solve 32 of the 40 boards provided on my two machines (Ubuntu/Intel 6700k/8gb ram, and, MAC OS/M2/8gb ram).
## Algorithm
The program uses IDA* (iterative deepening A* search) algorithm to solve the board and output the solution to a text file. IDA* algorithm is a variation of the A* algorithm that tries to find the most optimal solution using a depth-first search with iterative deepening and a heuristic function to reduce the search space. It explores the search space by using a stack and applies the heuristic function to estimate the cost of reaching the solved state. This can therefore be more memory- efficient than other search algorithms as it only stores one path at a time rather than the whole tree or graph.

The IDASolver is an inner static class that contains the methods needed to solve the board. The algorithm sets a threshold, which is the maximum f-value (g-value + heuristic value) of the states explored. The heuristic value (h-value) is an estimated cost, and the algorithm uses it to guide the search towards the goal state. The g-value is the sum of possible moves required to reach the goal state from the initial state.

The algorithm then performs a depth-first search with iterative deepening from the initial state, exploring states with f-values less than or equal to the threshold. If the f-value is less than or equal to the threshold, it adds the state to the visitedStates set and generates the children of the state. For each child, it calculates the g-value, h-value, and f-value, and pushes it onto the stack. If the child is a goal state, it returns the path from the initial state to the goal state and writes it to file. If a goal state is not found, the threshold is updated to the smallest f-value that was greater than the previous threshold. Then the search is repeated until a solution is found or the program runs out of memory.
## Heuristic
For heuristics, I decided to use the Manhattan distance. It sums up the horizontal and vertical distances between each tile and their respective goal position on the board. This heuristic is both consistent and admissible, meaning it never overestimates the cost of reaching the goal state and the estimated cost from one state to another is always less than or equal to the actual cost. As a result, this heuristic is optimal and gives you the shortest distance to the goal state. However, it's very memory intensive. Therefore, I opted to add a weighted factor to make the solver less optimal but more efficient, particularly when solving larger puzzles.
## Helper functions and additional information
pathNum and path are used to keep track of the solution path. The pathNum list stores the numbers in the solution path, and the path list stores the directions taken to reach the solution. The directions array stores the string representation of the directions: UP, DOWN, LEFT, and RIGHT. The visitedStates hash set is used to keep track of the visited states. The hash table stores the hash code of each state which is calculated based on the state’s board configuration. To prevent the algorithm from revisiting previous states, a hash set was employed, it was also a choice made because searching a hash set typically has an average time complexity of O(1) and a worst-case scenario of O(n).

The program contains another inner static class called State. This class represents each state of the puzzle as an object with the following properties.
- board: an array of integers representing the current state of the puzzle.
- blank: an integer representing the index of the blank tile.
- g: an integer representing the cost of the path from the initial state to the current state.
- h: an integer representing the heuristic value of the current state.
- f: an integer representing the sum of g and h.
- parentBoard: a reference to the parent state.
The algorithm uses the stack to explore the states in a depth-first manner. The states are pushed onto the stack in the order in which they are generated and popped in the reverse order. This allows the algorithm to explore the states with the smallest f-values first, which is important for the efficiency of the algorithm. When a state is popped from the stack, if its f-value is greater than the threshold value, then the algorithm does not expand this state and updates the next threshold value to be the minimum f-value of such states. This essentially prunes nodes that are deemed unlikely to led to a solution based on their heuristic estimate to the goal state.

In summary, the algorithm uses a stack to keep track of the states to be explored, a set to keep track of the states visited, and a hash map to store the g-values of the states. The algorithm generates successor states by moving the blank tile in all possible directions, and it updates the g- values and the f-values of the successor states if a shorter path is found. If a state has already been visited or has an f-value greater than the current threshold, it is not added to the stack. This way, with the addition of a weighted factor on the Manhattan distance, the algorithm can solve all 7x7 boards provided and a couple of the 8x8 boards as well.
## Conclusion
The development of the 15 puzzle solver was a challenging yet rewarding experience. It started with the basic task of reading and displaying the input board then evolved to include various algorithms and heuristics to solve puzzles of sizes 3x3 to 8x8. The use of breath-first search (BFS) and A* algorithm with Manhattan distance heuristic were explored. However, the addition of a weighted factor to the Manhattan distance and the iterative deepening proved to be the most effective choice. Through this process, parallelization and linear conflict were also experimented with but did not provide me with desirable results. Finally, IDA* with the Manhattan distance heuristic and an alternating weighted factor based on board size was used to efficiently solve 32 of the 40 boards provided. The project showcased the importance of algorithmic design and the trade-off between optimality and efficiency in solving complex problems.
