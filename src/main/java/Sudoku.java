/////////////////////////////////////////////////////////////////////////////////
// CS 430 - Artificial Intelligence
// Project 4 - Sudoku Solver w/ Variable Ordering and Forward Checking
// File: Sudoku.java
//
// Group Member Names: Mina Anđelković, Bell'aria Sarian
// Due Date: 11/09/2020
// 
//
// Description: A Backtracking program in Java to solve the Sudoku problem.
// Code derived from a C++ implementation at:
// http://www.geeksforgeeks.org/backtracking-set-7-suduku/
/////////////////////////////////////////////////////////////////////////////////

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Sudoku {
	// Constants
	final static int UNASSIGNED = 0; // UNASSIGNED is used for empty cells in sudoku grid
	final static int N = 9;// N is used for size of Sudoku grid. Size will be NxN
	static int numBacktracks = 0;
	static int[][][] domain;
    static int[][] domainSize;

	/////////////////////////////////////////////////////////////////////
	// Main function used to test solver.
	public static void main(String[] args) throws FileNotFoundException {
		// Reads in from TestCase.txt (sample sudoku puzzle).
		// 0 means unassigned cells - You can search the internet for more test cases.
		Scanner fileScan = new Scanner(new File("TestCase.txt"));

		// Reads case into grid 2D int array
		int grid[][] = new int[9][9];
		for (int r = 0; r < 9; r++) {
			String row = fileScan.nextLine();
			String[] cols = row.split(",");
			for (int c = 0; c < cols.length; c++)
				grid[r][c] = Integer.parseInt(cols[c].trim());
		}

		// Prints out the unsolved sudoku puzzle (as is)
		System.out.println("Unsolved sudoku puzzle:");
		printGrid(grid);

		// Setup timer - Obtain the time before solving
		long stopTime = 0L;
		long startTime = System.currentTimeMillis();

		// Attempts to solve and prints results
		if (SolveSudoku(grid) == true) {
			// Get stop time once the algorithm has completed solving the puzzle
			stopTime = System.currentTimeMillis();
			System.out.println("Algorithmic runtime: " + (stopTime - startTime) + "ms");
			System.out.println("Number of backtracks: " + numBacktracks);

			// Sanity check to make sure the computed solution really IS solved
			if (!isSolved(grid)) {
				System.err.println("An error has been detected in the solution.");
				System.exit(0);
			}
			System.out.println("\n\nSolved sudoku puzzle:");
			printGrid(grid);
		} else
			System.out.println("No solution exists");
	}

	/////////////////////////////////////////////////////////////////////
	// Write code here which returns true if the sudoku puzzle was solved
	// correctly, and false otherwise. In short, it should check that each
	// row, column, and 3x3 square of 9 cells maintain the ALLDIFF constraint.
	private static boolean isSolved(int[][] grid) {
		// System.out.println("TODO: Update the code here to complete the method.");
		// System.out.println("The default test case in TestCase.txt IS valid and this
		// method should return true for it.");
		// System.out.println("It is currently hardcoded to return false just so that it
		// compiles.");

		// 3, 0, 6, 5, 0, 8, 4, 0, 0
		// 5, 2, 0, 0, 0, 0, 0, 0, 0
		// 0, 8, 7, 0, 0, 0, 0, 3, 1
		// 0, 0, 3, 0, 1, 0, 0, 8, 0
		// 9, 0, 0, 8, 6, 3, 0, 0, 5
		// 0, 5, 0, 0, 9, 0, 6, 0, 0
		// 1, 3, 0, 0, 0, 0, 2, 5, 0
		// 0, 0, 0, 0, 0, 0, 0, 7, 4
		// 0, 0, 5, 2, 0, 6, 3, 0, 0

		// boolean array that stores unique numbers in the Sudoku table and returns true
		// if they are
		boolean[] uniqueValue = new boolean[10]; // stores values from 1 to 9
		int RC = 1;
		int CR = 1;
		// Rows
		for (int r = 0; r < 9; r++) {

			Arrays.fill(uniqueValue, false); // fill in all the values w/ false

			// traverse rows of grid
			for (int c = 0; c < 9; c++) {
				RC = grid[r][c]; // captures value at current (Row, Col)
			}

			// check if current (row, col) value in boolean array is true, if not return
			// false
			if (uniqueValue[RC]) {
				return false;
			}
			uniqueValue[RC] = true;
		}

		// Columns
		for (int r = 0; r < 9; r++) {
			Arrays.fill(uniqueValue, false); // fill in all the values w/ false

			for (int c = 0; c < 9; c++) {
				CR = grid[c][r]; // captures value at current (Col, Row)
			}

			// check if current (col, row) value in boolean array is true, if not return
			// false
			if (uniqueValue[CR]) {
				return false;
			}

			uniqueValue[CR] = true;
		}

		// Squares
		for (int r = 0; r < 7; r += 3) {

			for (int c = 0; c < 7; c += 3) {

				Arrays.fill(uniqueValue, false);

				for (int sh = 0; sh < 3; sh++) { // square horizontal

					for (int sv = 0; sv < 3; sv++) { // square vertical

						int SQR = r + sh; // row number of current square

						int SQC = c + sv;

						int SQRSQC = grid[SQR][SQC];

						if (uniqueValue[SQRSQC]) {
							return false;
						}
						uniqueValue[SQRSQC] = true;
					}
				}

			}
		}
		return true;
	}

	/////////////////////////////////////////////////////////////////////
	// Takes a partially filled-in grid and attempts to assign values to
	// all unassigned locations in such a way to meet the requirements
	// for Sudoku solution (non-duplication across rows, columns, and boxes)
	/////////////////////////////////////////////////////////////////////
	static boolean SolveSudoku(int grid[][]) {
		// Select next unassigned variable
		SudokuCoord variable;

		// TODO: Here, you will create an IF-ELSEIF-ELSE statement to select
		// the next variables using 1 of the 5 orderings selected by the user.
		// By default, it is hardcoded to the method FindUnassignedVariable(),
		// which corresponds to the "1) Default static ordering" option.
		variable = MyMinRemainingValueOrderingOpt4(grid);

		// If there is no unassigned location, we are done
		if (variable == null)
			return true; // success!

		int row = variable.row;
		int col = variable.col;

		// consider digits 1 to 9
		for (int num = 1; num <= 9; num++) {
			// if looks promising
			if (isSafe(grid, row, col, num)) {
				// make tentative assignment
				grid[row][col] = num;

				// return, if success, yay!
				if (SolveSudoku(grid))
					return true;

				// failure, un-assign & try again
				grid[row][col] = UNASSIGNED;
			}
		}

		// Increment the number of backtracks
		numBacktracks++;
		return false; // This triggers backtracking
	}

	/////////////////////////////////////////////////////////////////////
	// Searches the grid to find an entry that is still unassigned. If
	// found, the reference parameters row, col will be set the location
	// that is unassigned, and true is returned. If no unassigned entries
	// remain, null is returned.
	/////////////////////////////////////////////////////////////////////
	static SudokuCoord FindUnassignedVariable(int grid[][]) {
		for (int row = 0; row < N; row++)
			for (int col = 0; col < N; col++)
				if (grid[row][col] == UNASSIGNED)
					return new SudokuCoord(row, col);
		return null;
	}

	/////////////////////////////////////////////////////////////////////
	// TODO: Implement the following orderings, as specified in the
	// project description. You MAY feel free to add extra parameters if
	// needed (you shouldn't need to for the first two, but it may prove
	// helpful for the last two methods).
	/////////////////////////////////////////////////////////////////////
	static SudokuCoord MyOriginalStaticOrderingOpt2(int grid[][]) {
		// starts from bottom-left corner and goes to top-right
		for (int row = N - 1; row >= 0; row--)
			for (int col = 0; col < N; col++)
				if (grid[row][col] == UNASSIGNED)
					return new SudokuCoord(row, col);
		return null;
	}

	static SudokuCoord MyOriginalRandomOrderingOpt3(int grid[][]) {
		Random rd = new Random();

		List<Integer> rowList = new ArrayList<Integer>();
		List<Integer> colList = new ArrayList<Integer>();


		for (int row = 0; row < N; row++)
			for (int col = 0; col < N; col++)
				if (grid[row][col] == UNASSIGNED) {
					rowList.add(row);
					colList.add(col);
				}

		int randomIndex;
		if (!rowList.isEmpty()) {
			randomIndex = rd.nextInt(rowList.size()); // 9?
			return new SudokuCoord(rowList.get(randomIndex), colList.get(randomIndex));
		}
		System.out.println("Randomized Selection output");
		printGrid(grid);
		if (rowList.isEmpty())
			return null;
		return null;
	}

	static SudokuCoord MyMinRemainingValueOrderingOpt4(int grid[][]) {

		for(int i = 0; i < 9; i ++) {
            for (int j = 0; j < 9; j ++) {
                int count = 0;
                // if unassigned variable count domain size
                if (grid[i][j] == 0) {
                    for (int k = 0; k < 9; k ++) {
                        if(domain[i][j][k] == 0) {
                            count++;
                        }
                    }
                } else {
                    // else, variable assigned set count to 10
                    // bigger than any unassigned value so will not be picked for assigning
                    count = 10;
                }
                // once counting has finished, set value in domainSize
                domainSize[i][j] = count;
                // if domain size is zero return false so that backtracking can occur
                if (count == 0) {
					break;
                }

			}
		}

		int[] smallest = null;

		// find first value with solution value of 0 (unassigned)
		for(int row = 0; row < N; row ++){
			for (int col = 0 ; col < N; col++) {
				if(grid[row][col] == 0) {
					smallest = new int[] {row, col};
					break;
				}
			}
		}
		if (smallest == null) {
			return null;
		}

		for ( int row = 0; row < N; row++) {
			for (int col = 0; col < N; col++) { // line 370
				if (domainSize[row][col] < domainSize[smallest[0]][smallest[1]]) {
					smallest[0] = row;
					smallest[1] = col;
				}
			}
		}
		return new SudokuCoord(smallest[0], smallest[1]);
	}

	static SudokuCoord MyMaxRemainingValueOrderingOpt5(int grid[][]) {
		return null;
	}

	/////////////////////////////////////////////////////////////////////
	// Returns a boolean which indicates whether any assigned entry
	// in the specified row matches the given number.
	/////////////////////////////////////////////////////////////////////
	static boolean UsedInRow(int grid[][], int row, int num) {
		for (int col = 0; col < N; col++)
			if (grid[row][col] == num)
				return true;
		return false;
	}

	/////////////////////////////////////////////////////////////////////
	// Returns a boolean which indicates whether any assigned entry
	// in the specified column matches the given number.
	/////////////////////////////////////////////////////////////////////
	static boolean UsedInCol(int grid[][], int col, int num) {
		for (int row = 0; row < N; row++)
			if (grid[row][col] == num)
				return true;
		return false;
	}

	/////////////////////////////////////////////////////////////////////
	// Returns a boolean which indicates whether any assigned entry
	// within the specified 3x3 box matches the given number.
	/////////////////////////////////////////////////////////////////////
	static boolean UsedInBox(int grid[][], int boxStartRow, int boxStartCol, int num) {
		for (int row = 0; row < 3; row++)
			for (int col = 0; col < 3; col++)
				if (grid[row + boxStartRow][col + boxStartCol] == num)
					return true;
		return false;
	}

	/////////////////////////////////////////////////////////////////////
	// Returns a boolean which indicates whether it will be legal to assign
	// num to the given row, col location.
	/////////////////////////////////////////////////////////////////////
	static boolean isSafe(int grid[][], int row, int col, int num) {
		// Check if 'num' is not already placed in current row,
		// current column and current 3x3 box
		return !UsedInRow(grid, row, num) && !UsedInCol(grid, col, num)
				&& !UsedInBox(grid, row - row % 3, col - col % 3, num);
	}

	/////////////////////////////////////////////////////////////////////
	// A utility function to print grid
	/////////////////////////////////////////////////////////////////////
	static void printGrid(int grid[][]) {
		for (int row = 0; row < N; row++) {
			for (int col = 0; col < N; col++) {
				if (grid[row][col] == 0)
					System.out.print("- ");
				else
					System.out.print(grid[row][col] + " ");

				if ((col + 1) % 3 == 0)
					System.out.print(" ");
			}
			System.out.print("\n");
			if ((row + 1) % 3 == 0)
				System.out.println();
		}
	}
}