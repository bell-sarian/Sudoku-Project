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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import javax.xml.bind.annotation.XmlElementDecl.GLOBAL;

public class Sudoku {
	// Constants
	final static int UNASSIGNED = 0; // UNASSIGNED is used for empty cells in sudoku grid
	final static int N = 9;// N is used for size of Sudoku grid. Size will be NxN
	static int numBacktracks = 0;
	static int choice;

	// Class-wide variables
	static int variableOrderChoice = -1;
	static int inferenceChoice = -1;
	static HashSet<Integer> domain = new HashSet<Integer>();

	/////////////////////////////////////////////////////////////////////
	// Main function used to test solver.
	public static void main(String[] args) throws FileNotFoundException {
		// Prompt user for test case
		Scanner scan = new Scanner(System.in);
		System.out.println("Please select a test case by entering 1-3: ");

		// Reads in from TestCase.txt (sample sudoku puzzle).
		// 0 means unassigned cells - You can search the internet for more test cases.
		Scanner fileScan = new Scanner(new File("Case3.txt"));

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

		// Prompt user for variable ordering option
		System.out.println("Please select a variable ordering option from the following list: ");
		System.out.println("\t1: Default Static Ordering (top-left to bottom-right)");
		System.out.println("\t2: Your Static Ordering Idea");
		System.out.println("\t3: Random Ordering");
		System.out.println("\t4: MINimum Remaining Values");
		System.out.println("\t5: MAXimum Remaining Values");
		System.out.println("\nYour choice (1-5): ");
		variableOrderChoice = scan.nextInt();

		// Setup timer - Obtain the time before solving
		long stopTime = 0L;
		long startTime = System.currentTimeMillis();

		SudokuCoord variable;
		switch (variableOrderChoice) {
			case 1:
				variable = FindUnassignedVariable(grid);
				break;
			case 2:
				variable = MyOriginalStaticOrderingOpt2(grid);
				break;
			case 3:
				variable = MyOriginalRandomOrderingOpt3(grid);
				break;
			case 4:
				variable = MyMinRemainingValueOrderingOpt4(grid);
				break;
			case 5:
				variable = MyMaxRemainingValueOrderingOpt5(grid);
				break;
			default: {
				variable = FindUnassignedVariable(grid);
				System.err.println("Invalid variable ordering case chosen.");
				System.exit(0);
				break;
			}

		}
		int row = variable.row;
		int col = variable.col;

		String keyRowCol = String.format("%s-%s", row, col);
		HashMap<String, HashSet<Integer>> initialDom = new HashMap<String, HashSet<Integer>>();
		// HashSet<Integer> domain = new HashSet<Integer>();
		for (int d = 1; d < 10; d++) {
			domain.add(d);
		}
		System.out.println(variable.row + ", " + variable.col);

		initialDom.put(keyRowCol, domain);
		//
		// System.out.println(initialDom);
		// Attempts to solve and prints results
		if (SolveSudoku(grid) == true) {
			// if (SolveSudokuWithForwardChecking(grid, initialDom) == true) {
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
		// variable = MyMinRemainingValueOrderingOpt4(grid);

		switch (variableOrderChoice) {
			case 1:
				variable = FindUnassignedVariable(grid);
				break;
			case 2:
				variable = MyOriginalStaticOrderingOpt2(grid);
				break;
			case 3:
				variable = MyOriginalRandomOrderingOpt3(grid);
				break;
			case 4:
				variable = MyMinRemainingValueOrderingOpt4(grid);
				break;
			case 5:
				variable = MyMaxRemainingValueOrderingOpt5(grid);
				break;
			default: {
				variable = FindUnassignedVariable(grid);
				System.err.println("Invalid variable ordering case chosen.");
				System.exit(0);
				break;
			}

		}

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

	// Forward Checking

	////////////////////////////////////////////////////////////////////
	// Takes a partially filled-in grid and attempts to assign values to
	// all unassigned locations using forward checking
	// in such a way to meet the requirements
	// for Sudoku solution (non-duplication across rows, columns, and boxes)
	/////////////////////////////////////////////////////////////////////
	static boolean SolveSudokuWithForwardChecking(int grid[][], HashMap<String, HashSet<Integer>> domains) {
		// Select next unassigned variable
		SudokuCoord variable;

		// TODO: Here, you will create an IF-ELSEIF-ELSE statement to select
		// the next variables using 1 of the 5 orderings selected by the user.
		// By default, it is hardcoded to the method FindUnassignedVariable(),
		// which corresponds to the "1) Default static ordering" option.

		switch (variableOrderChoice) {
			case 1:
				variable = FindUnassignedVariable(grid);
				break;
			case 2:
				variable = MyOriginalStaticOrderingOpt2(grid);
				break;
			case 3:
				variable = MyOriginalRandomOrderingOpt3(grid);
				break;
			case 4:
				variable = MyMinRemainingValueOrderingOpt4(grid);
				break;
			case 5:
				variable = MyMaxRemainingValueOrderingOpt5(grid);
				break;
			default: {
				variable = FindUnassignedVariable(grid);
				System.err.println("Invalid variable ordering case chosen.");
				System.exit(0);
				break;
			}

		}

		// If there is no unassigned location, we are done
		if (variable == null)
			return true; // success!

		int row = variable.row;
		int col = variable.col;

		String keyRowCol = String.format("%s-%s", row, col);

		System.out.println(variable.row + ", " + variable.col + " doms" + domains);
		// System.out.println("domains.get "+ domains.get(variable));
		HashSet<Integer> d = domains.get(keyRowCol);
		System.out.println("d: " + d);
		System.out.println(variable);
		// consider digits in my variable's domain
		for (int num : d) {
			System.out.println("\nHere? 1");
			// Create a clone of domains that we will cut elements from using Forward
			// Checking
			HashMap<String, HashSet<Integer>> domainsForCutting = (HashMap<String, HashSet<Integer>>) copyMyHashMap(
					domains);

			boolean badGuess = false; // Signals if we will need to re-guess

			// if looks promising
			if (isSafe(grid, row, col, num)) {
				// make tentative assignment and remove this coordinate from domainsForCutting
				grid[row][col] = num;
				domainsForCutting.remove(keyRowCol);

				// For any coordinate that is in the same row, col, or box as variable,
				// eliminate any conflicting elements in their domains
				Set<String> set = new HashSet<String>(domainsForCutting.keySet()); // Set used to loop through
																					// domainsforCutting

				for (String coord : set) {
					int cRow = Character.getNumericValue(coord.charAt(0));
					int cCol = Character.getNumericValue(coord.charAt(2));
					if (cRow == row || cCol == col || getBoxIndex(cRow, cCol) == getBoxIndex(row, col)
							&& domainsForCutting.get(coord).contains(num)) {
						// Print statements
						System.out.println(
								"Reducing Domains after assigning (" + row + ", " + col + ") the value of " + num);
						System.out.print("Coordinate (" + cRow + ", " + cCol + ") had domain "
								+ domainsForCutting.get(coord) + ". ");

						// Remove the value from coord's domain
						domainsForCutting.get(coord).remove(num);

						// More print statements
						System.out.println("Updated domain is " + domainsForCutting.get(coord) + ".\n");

						// If there are no more elements in a coordinates domain, update badGuess to be
						// true
						if (domainsForCutting.get(coord).size() == 0) {
							badGuess = true;
						}

					}
				}

				// if there is no need to backtrack immediately, check
				// SolveSudokuWithForwardChecking again but using the set of reduced domains
				// if success, then return true.
				if (!badGuess) {
					if (SolveSudokuWithForwardChecking(grid, domainsForCutting)) {
						return true;
					}
				}

				// failure, un-assign & try again
				grid[row][col] = UNASSIGNED;
			}
		}

		// Increment the number of backtracks
		numBacktracks++;
		return false; // This triggers backtracking
	}

	private static SudokuCoord getBoxIndex(int row, int col) {
		// TODO Auto-generated method stub
		int boxRow;
		int boxCol;
		if (row >= 0 && row <= 2) {
			if (col >= 0 && col <= 2) {
				boxRow = 0;
				boxCol = 0;
				return new SudokuCoord(boxRow, boxCol);
			}
			if (col >= 3 && col <= 5) {
				boxRow = 0;
				boxCol = 3;
				return new SudokuCoord(boxRow, boxCol);

			}
			if (col >= 6 && col <= 8) {
				boxRow = 0;
				boxCol = 6;
				return new SudokuCoord(boxRow, boxCol);
			}
		}
		if (row >= 3 && row <= 5) {
			if (col >= 0 && col <= 2) {
				boxRow = 3;
				boxCol = 0;
				return new SudokuCoord(boxRow, boxCol);
			}
			if (col >= 3 && col <= 5) {
				boxRow = 3;
				boxCol = 3;
				return new SudokuCoord(boxRow, boxCol);
			}
			if (col >= 6 && col <= 8) {
				boxRow = 3;
				boxCol = 6;
				return new SudokuCoord(boxRow, boxCol);
			}
		}
		if (row >= 6 && row <= 8) {
			if (col >= 0 && col <= 2) {
				boxRow = 6;
				boxCol = 0;
				return new SudokuCoord(boxRow, boxCol);
			}
			if (col >= 3 && col <= 5) {
				boxRow = 6;
				boxCol = 3;
				return new SudokuCoord(boxRow, boxCol);
			}
			if (col >= 6 && col <= 8) {
				boxRow = 6;
				boxCol = 6;
				return new SudokuCoord(boxRow, boxCol);
			}
		}
		return null;
	}

	private static HashMap<String, HashSet<Integer>> copyMyHashMap(HashMap<String, HashSet<Integer>> domains) {

		HashMap<String, HashSet<Integer>> copyDomain = new HashMap<String, HashSet<Integer>>();
		for (Map.Entry<String, HashSet<Integer>> entry : domains.entrySet()) {
			copyDomain.put(entry.getKey(), entry.getValue());
			System.out.println(" Key " + entry.getKey());
			System.out.println("Value" + entry.getValue());
		}

		// HashMap copy = (HashMap) domains.clone();
		return copyDomain;
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

		// iterate through the sudoku table
		for (int row = 0; row < N; row++)
			for (int col = 0; col < N; col++)
				// if the location is unnasigned add it to the row and column arraylists
				if (grid[row][col] == UNASSIGNED) {
					rowList.add(row);
					colList.add(col);
				}

		int randomIndex;
		if (!rowList.isEmpty()) {
			randomIndex = rd.nextInt(rowList.size()); // radomize the index in the arraylist
			return new SudokuCoord(rowList.get(randomIndex), colList.get(randomIndex)); // return the coordinates
		}
		System.out.println("Randomized Selection output");
		printGrid(grid);
		if (rowList.isEmpty())
			return null;
		return null;
	}

	static SudokuCoord MyMinRemainingValueOrderingOpt4(int grid[][]) {

		List<Integer> domain = new ArrayList<Integer>(); // stores values from 1 to 9 for ROW
		List<Integer> domain2 = new ArrayList<Integer>(); // stores values from 1 to 9 for COL
		List<Integer> domain3 = new ArrayList<Integer>(); // stores values from 1 to 9 for COL

		int min = 10;
		int minRowIndex = 10;
		int minColIndex = 10;
		for (int d = 1; d < 10; d++) { // populates domain array
			domain.add(d);
		}
		for (int row = 0; row < N; row++) { // Iterate Rows

			// Check values used in row
			for (int d = 1; d < 10; d++) {
				if (UsedInRow(grid, row, d)) {
					int index = domain.indexOf(d);
					domain.remove(index);
				}
			}

			for (int col = 0; col < N; col++) { // Iterate Columns
				// System.out.println("\nBegin Col " + col);
				domain2.clear();
				domain3.clear();
				domain2.addAll(domain);

				for (int d = 1; d < 10; d++) {
					if (UsedInCol(grid, col, d)) {
						int index = domain2.indexOf(d);
						// System.out.println("r u stopping here 3");
						if (index != -1)
							domain2.remove(index);
						// System.out.println("r u stopping here 4");
					}

					if (domain2.size() < min && grid[row][col] == UNASSIGNED) {
						// System.out.println("\t\t***ADDING NEW MIN***");
						min = domain2.size();
						minRowIndex = row;
						minColIndex = col;
					}
				}
				// System.out.println("END Domain 2: " +domain2);
				domain3.addAll(domain2);
				// System.out.println("Domain3: " + domain3);
				int boxRow = 0;
				int boxCol = 0;
				for (int d = 1; d < 10; d++) {
					// System.out.println("r u stopping here BOX 1");
					if (row >= 0 && row <= 2) {
						if (col >= 0 && col <= 2) {
							boxRow = 0;
							boxCol = 0;
						}
						if (col >= 3 && col <= 5) {
							boxRow = 0;
							boxCol = 3;
						}
						if (col >= 6 && col <= 8) {
							boxRow = 0;
							boxCol = 6;
						}
					}
					if (row >= 3 && row <= 5) {
						if (col >= 0 && col <= 2) {
							boxRow = 3;
							boxCol = 0;
						}
						if (col >= 3 && col <= 5) {
							boxRow = 3;
							boxCol = 3;
						}
						if (col >= 6 && col <= 8) {
							boxRow = 3;
							boxCol = 6;
						}
					}
					if (row >= 6 && row <= 8) {
						if (col >= 0 && col <= 2) {
							boxRow = 6;
							boxCol = 0;
						}
						if (col >= 3 && col <= 5) {
							boxRow = 6;
							boxCol = 3;
						}
						if (col >= 6 && col <= 8) {
							boxRow = 6;
							boxCol = 6;
						}
					}
					if (UsedInBox(grid, boxRow, boxCol, d)) {
						// System.out.println("r u stopping here BOX 2");
						int index = domain3.indexOf(d);
						// System.out.println("r u stopping here BOX 3");
						if (index != -1)
							domain3.remove(index);
						// System.out.println("r u stopping here BOX 4");
					}

					if (domain3.size() < min && grid[row][col] == UNASSIGNED) {
						// System.out.println("\t\t***ADDING NEW MIN***");
						min = domain3.size();
						minRowIndex = row;
						minColIndex = col;
					}
				}
			}

			domain.clear();
			domain2.clear();
			domain3.clear();

			for (int d = 1; d < 10; d++) { // populates domain array
				domain.add(d);
				// System.out.println("Domain at index " + d + ": " + domain.get(d));
			}

		}
		// printGrid(grid);
		if (min < 10) {
			// System.out.println("Reached");
			return new SudokuCoord(minRowIndex, minColIndex);
		}
		return null;
	}

	static SudokuCoord MyMaxRemainingValueOrderingOpt5(int grid[][]) {
		List<Integer> domain = new ArrayList<Integer>(); // stores values from 1 to 9 for ROW
		List<Integer> domain2 = new ArrayList<Integer>(); // stores values from 1 to 9 for COL
		List<Integer> domain3 = new ArrayList<Integer>(); // stores values from 1 to 9 for COL

		int max = 0;
		int maxRowIndex = 10;
		int maxColIndex = 10;
		for (int d = 1; d < 10; d++) { // populates domain array
			domain.add(d);
		}

		for (int row = 0; row < N; row++) { // Iterate Rows

			// Check values used in row
			for (int d = 1; d < 10; d++) {
				if (UsedInRow(grid, row, d)) {
					int index = domain.indexOf(d);
					domain.remove(index);
				}
			}

			for (int col = 0; col < N; col++) { // Iterate Columns
				// System.out.println("\nBegin Col " + col);
				domain2.clear();
				domain3.clear();
				domain2.addAll(domain);

				for (int d = 1; d < 10; d++) {
					if (UsedInCol(grid, col, d)) {
						int index = domain2.indexOf(d);
						// System.out.println("r u stopping here 3");
						if (index != -1)
							domain2.remove(index);
						// System.out.println("r u stopping here 4");
					}

					if (domain2.size() > max && grid[row][col] == UNASSIGNED) {
						// System.out.println("\t\t***ADDING NEW MIN***");
						max = domain2.size();
						maxRowIndex = row;
						maxColIndex = col;
					}
				}
				// System.out.println("END Domain 2: " +domain2);
				domain3.addAll(domain2);
				// System.out.println("Domain3: " + domain3);
				int boxRow = 0;
				int boxCol = 0;
				for (int d = 1; d < 10; d++) {
					// System.out.println("r u stopping here BOX 1");
					if (row >= 0 && row <= 2) {
						if (col >= 0 && col <= 2) {
							boxRow = 0;
							boxCol = 0;
						}
						if (col >= 3 && col <= 5) {
							boxRow = 0;
							boxCol = 3;
						}
						if (col >= 6 && col <= 8) {
							boxRow = 0;
							boxCol = 6;
						}
					}
					if (row >= 3 && row <= 5) {
						if (col >= 0 && col <= 2) {
							boxRow = 3;
							boxCol = 0;
						}
						if (col >= 3 && col <= 5) {
							boxRow = 3;
							boxCol = 3;
						}
						if (col >= 6 && col <= 8) {
							boxRow = 3;
							boxCol = 6;
						}
					}
					if (row >= 6 && row <= 8) {
						if (col >= 0 && col <= 2) {
							boxRow = 6;
							boxCol = 0;
						}
						if (col >= 3 && col <= 5) {
							boxRow = 6;
							boxCol = 3;
						}
						if (col >= 6 && col <= 8) {
							boxRow = 6;
							boxCol = 6;
						}
					}
					if (UsedInBox(grid, boxRow, boxCol, d)) {
						// System.out.println("r u stopping here BOX 2");
						int index = domain3.indexOf(d);
						// System.out.println("r u stopping here BOX 3");
						if (index != -1)
							domain3.remove(index);
						// System.out.println("r u stopping here BOX 4");
					}

					if (domain3.size() > max && grid[row][col] == UNASSIGNED) {
						// System.out.println("\t\t***ADDING NEW MIN***");
						max = domain3.size();
						maxRowIndex = row;
						maxColIndex = col;
					}
				}
			}

			domain.clear();
			domain2.clear();
			domain3.clear();

			for (int d = 1; d < 10; d++) { // populates domain array
				domain.add(d);

			}

		}
		// printGrid(grid);
		if (max > 0) {
			// System.out.println("Reached");
			return new SudokuCoord(maxRowIndex, maxColIndex);
		}
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