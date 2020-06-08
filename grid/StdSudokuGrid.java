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
    public int[][] grid;
    private ArrayList<Integer> digits = new ArrayList<Integer>();
    private int gridDimensions;

    public StdSudokuGrid() {
        super();
    } // end of StdSudokuGrid()

    /* ********************************************************* */

    @Override
    public void initGrid(String filename) throws FileNotFoundException, IOException {
        BufferedReader file = new BufferedReader(new FileReader(filename));
        String inputLine;

        // dimesnion of grid
        inputLine = file.readLine();
        gridDimensions = Integer.parseInt(inputLine);

        grid = new int[gridDimensions][gridDimensions];

        // Unique digits
        inputLine = file.readLine();

        String[] digitStrings = inputLine.split(" ");

        if (digitStrings.length != gridDimensions) {
            file.close();
            throw new IOException("Number of digits must equal grid dimensions");
        }

        for (String digit : digitStrings) {
            int newDigit = Integer.parseInt(digit);

            if (digits.contains(newDigit)) {
                file.close();
                throw new IOException("Duplicate digits detected");
            }

            digits.add(newDigit);
        }

        // Begin reading cell information
        inputLine = file.readLine();

        while (inputLine != null) {
            String[] cellDetail = inputLine.split("[, ]");

            if (cellDetail.length != 3) {
                file.close();
                throw new IOException("Error reading cell data. Make sure lines have 3 arguments only");
            }

            int gridRow = Integer.parseInt(cellDetail[0]);
            int gridCol = Integer.parseInt(cellDetail[1]);
            int cellValue = Integer.parseInt(cellDetail[2]);

            if (gridRow >= gridDimensions || gridCol >= gridDimensions) {
                file.close();
                throw new IOException("Make sure grid cells are within bounds");
            }

            if (!digits.contains(cellValue)) {
                file.close();
                throw new IOException("Cell value does not exist in defined digits");
            }

            grid[gridRow][gridCol] = cellValue;

            inputLine = file.readLine();
        }

        file.close();

        if (!validate()) {
            throw new IOException("Invalid starting grid");
        }
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
    public void setCell(int row, int col, int digitNum) {
        if (digitNum == -1) {
            grid[row][col] = 0;
        } else {
            grid[row][col] = digits.get(digitNum);
        }
    }

    @Override
    public int getDigitPosition(int digit) {
        for (int position = 0; position < gridDimensions; position++) {
            if (digits.get(position) == digit) {
                return position;
            }
        }

        return -1;
    }
} // end of class StdSudokuGrid
