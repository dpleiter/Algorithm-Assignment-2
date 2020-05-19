/**
 * @author Jeffrey Chan & Minyi Li, RMIT 2020
 */
package grid;

import java.io.*;
import java.util.*;

/**
 * Class implementing the grid for standard Sudoku. Extends SudokuGrid (hence
 * implements all abstract methods in that abstract class). You will need to
 * complete the implementation for this for task A and subsequently use it to
 * complete the other classes. See the comments in SudokuGrid to understand what
 * each overriden method is aiming to do (and hence what you should aim for in
 * your implementation).
 */
public class StdSudokuGrid extends SudokuGrid {
    // TODO: Add your own attributes
    private int[][] grid;
    private Set<Integer> digits = new HashSet<Integer>();
    private int gridDimensions;

    public StdSudokuGrid() {
        super();
    } // end of StdSudokuGrid()

    /* ********************************************************* */

    @Override
    public void initGrid(String filename) throws FileNotFoundException, IOException {
        // TODO: Throw exceptions when neccesary
        BufferedReader file = new BufferedReader(new FileReader(filename));
        String inputLine;
        String[] cellDetail = new String[2];
        String[] cellCoords;
        String cellValue;

        // dimesnion of grid
        inputLine = file.readLine();
        gridDimensions = Integer.parseInt(inputLine);

        grid = new int[gridDimensions][gridDimensions];

        inputLine = file.readLine();

        for (String digit : inputLine.split(" ")) {
            digits.add(Integer.parseInt(digit));
        }

        inputLine = file.readLine();

        while (inputLine != null) {
            cellDetail = inputLine.split(" ");

            cellCoords = cellDetail[0].split(",");
            cellValue = cellDetail[1];

            grid[Integer.parseInt(cellCoords[0])][Integer.parseInt(cellCoords[1])] = Integer.parseInt(cellValue);

            inputLine = file.readLine();
        }

        file.close();

        validate();
    } // end of initBoard()

    @Override
    public void outputGrid(String filename) throws FileNotFoundException, IOException {
        BufferedWriter outfile = new BufferedWriter(new FileWriter(filename));

        outfile.write(toString());

        outfile.close();
    } // end of outputBoard()

    @Override
    public String toString() {
        String outString = "";
        int cellValue;

        for (int row = 0; row < gridDimensions; row++) {
            for (int col = 0; col < gridDimensions; col++) {
                cellValue = grid[row][col];

                if (cellValue == 0) {
                    outString += " ";
                } else {
                    outString += Integer.toString(cellValue);
                }

                if (col == gridDimensions - 1) {
                    outString += "\n";
                } else {
                    outString += ",";
                }
            }
        }

        return outString;
    } // end of toString()

    @Override
    public boolean validate() {
        int boxSize = (int) Math.sqrt(gridDimensions);
        int cellValue;

        HashSet<Integer> checker = new HashSet<Integer>();

        // Check rows
        for (int row = 0; row < gridDimensions; row++) {
            for (int col = 0; col < gridDimensions; col++) {
                cellValue = grid[row][col];

                if (cellValue == 0)
                    continue;

                if (!checker.contains(cellValue)) {
                    checker.add(cellValue);
                } else {
                    System.err.println("Error in row");
                    return false;
                }
            }

            checker.clear();
        }

        // Check columns
        for (int col = 0; col < gridDimensions; col++) {
            for (int row = 0; row < gridDimensions; row++) {
                cellValue = grid[row][col];

                if (cellValue == 0)
                    continue;

                if (!checker.contains(cellValue)) {
                    checker.add(cellValue);
                } else {
                    System.err.println("Error in column");
                    return false;
                }
            }

            checker.clear();
        }

        // Check boxes
        for (int boxStartRow = 0; boxStartRow < gridDimensions; boxStartRow += 2) {
            for (int boxStartCol = 0; boxStartCol < gridDimensions; boxStartCol += 2) {
                for (int c = 0; c < boxSize; c++) {
                    for (int d = 0; d < boxSize; d++) {
                        cellValue = grid[boxStartRow + c][boxStartCol + d];

                        if (cellValue == 0)
                            continue;

                        if (!checker.contains(cellValue)) {
                            checker.add(cellValue);
                        } else {
                            System.err.println("Error in box");
                            return false;
                        }
                    }
                }

                checker.clear();
            }
        }

        System.out.println("VALID GRID");

        return true;
    } // end of validate()

} // end of class StdSudokuGrid
