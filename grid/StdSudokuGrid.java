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
    public int[][] grid;
    private ArrayList<Integer> digits;
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

        // digitArray = digits.toArray();

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

    public boolean validate() {
        // to run after initialisation to ensure a valid starting grid
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
                    return false;
                }
            }

            checker.clear();
        }

        // Check boxes
        for (int boxStartRow = 0; boxStartRow < gridDimensions; boxStartRow += boxSize) {
            for (int boxStartCol = 0; boxStartCol < gridDimensions; boxStartCol += boxSize) {
                for (int c = 0; c < boxSize; c++) {
                    for (int d = 0; d < boxSize; d++) {
                        cellValue = grid[boxStartRow + c][boxStartCol + d];

                        if (cellValue == 0)
                            continue;

                        if (!checker.contains(cellValue)) {
                            checker.add(cellValue);
                        } else {
                            return false;
                        }
                    }
                }

                checker.clear();
            }
        }

        return true;
    } // end of validate()

    public boolean checkComplete() {
        for (int[] row : grid) {
            for (int cell : row) {
                if (cell == 0)
                    return false;
            }
        }

        return true;
    }

    @Override
    public int[][] getGrid() {
        return grid;
    }

    @Override
    public ArrayList<Integer> getDigits() {
        return this.digits;
    }

    @Override
    public int getSize() {
        return this.gridDimensions;
    }

    @Override
    public int getCellValue(int row, int col) {
        return grid[row][col];
    }

    @Override
    public void setCell(int row, int col, int value) {
        grid[row][col] = value;
    }
} // end of class StdSudokuGrid
