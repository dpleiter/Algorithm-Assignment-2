/**
 * @author Jeffrey Chan & Minyi Li, RMIT 2020
 */
package grid;

import java.io.*;
import java.util.*;

/**
 * Class implementing the grid for Killer Sudoku. Extends SudokuGrid (hence
 * implements all abstract methods in that abstract class). You will need to
 * complete the implementation for this for task E and subsequently use it to
 * complete the other classes. See the comments in SudokuGrid to understand what
 * each overriden method is aiming to do (and hence what you should aim for in
 * your implementation).
 */
public class KillerSudokuGrid extends SudokuGrid {
    public Cell[][] grid;
    private ArrayList<Integer> digits = new ArrayList<Integer>();
    private int gridDimensions;

    private ArrayList<Cage> cages = new ArrayList<Cage>();

    public KillerSudokuGrid() {
        super();
    } // end of KillerSudokuGrid()

    /* ********************************************************* */

    @Override
    public void initGrid(String filename) throws FileNotFoundException, IOException {
        BufferedReader file = new BufferedReader(new FileReader(filename));

        String inputLine = file.readLine();

        gridDimensions = Integer.parseInt(inputLine);
        grid = new Cell[gridDimensions][gridDimensions];

        inputLine = file.readLine();

        for (String digit : inputLine.split(" ")) {
            digits.add(Integer.parseInt(digit));
        }

        inputLine = file.readLine();
        inputLine = file.readLine();

        while (inputLine != null) {
            String[] strSplit = inputLine.split(" ");

            Cage newCage = new Cage(Integer.parseInt(strSplit[0]));
            cages.add(newCage);

            for (int i = 1; i < strSplit.length; i++) {
                String[] coords = strSplit[i].split(",");

                int row = Integer.parseInt(coords[0]);
                int col = Integer.parseInt(coords[1]);

                if (grid[row][col] == null) {
                    Cell newCell = new Cell(newCage);

                    newCage.addCell(newCell);
                    grid[row][col] = newCell;
                } else {
                    // throw new Exception();
                    System.out.println("EXCEPTION SHOULD BE THROWN HERE");
                }
            }

            System.out.println("Finding combinations for cage total of " + Integer.parseInt(strSplit[0]));
            newCage.findDigits(digits, new ArrayList<Integer>(), 0, 0);
            System.out.println("Finished");
            newCage.printCombinations();

            inputLine = file.readLine();
        }

        file.close();
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
                cellValue = grid[row][col].getValue();

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
                cellValue = grid[row][col].getValue();

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
                cellValue = grid[row][col].getValue();

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
                        cellValue = grid[boxStartRow + c][boxStartCol + d].getValue();

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

        // Check cages
        for (Cage cage : cages) {
            if (!cage.isValid()) {
                return false;
            }
        }

        return true;
    } // end of validate()

    @Override
    public boolean checkComplete() {
        for (Cell[] row : grid) {
            for (Cell cell : row) {
                if (cell.getValue() == 0) {
                    return false;
                }
            }
        }

        for (Cage cage : cages) {
            if (!cage.isComplete()) {
                return false;
            }
        }

        return true;
    }

    public Cell[][] getGrid() {
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
        return this.grid[row][col].getValue();
    }

    @Override
    public void setCell(int row, int col, int value) {
        if (value == -1) {
            this.grid[row][col].setValue(0);
        } else {
            this.grid[row][col].setValue(digits.get(value));
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

    @SuppressWarnings("unused")
    private class Cage {
        private ArrayList<Cell> cells = new ArrayList<Cell>();
        private HashSet<Integer> possibleDigits = new HashSet<Integer>();
        private HashSet<Integer> digitsInCage = new HashSet<Integer>();
        private int targetValue;

        public Cage(int targetValue) {
            this.targetValue = targetValue;
        }

        public void addCell(Cell cell) {
            cells.add(cell);
        }

        public void findDigits(ArrayList<Integer> digits, ArrayList<Integer> partialSol, int sum, int startIndex) {
            possibleDigits.clear();

            findDigitsRecursive(digits, partialSol, sum, startIndex);
        }

        public void findDigitsRecursive(ArrayList<Integer> digits, ArrayList<Integer> partialSol, int sum,
                int startIndex) {
            if (sum == this.targetValue && partialSol.size() == this.cells.size()) {
                for (int i : partialSol) {
                    possibleDigits.add(i);
                }
            }

            for (int i = startIndex; i < digits.size(); i++) {
                int newDigit = digits.get(i);

                if (digitsInCage.contains(newDigit)) {
                    continue;
                }

                partialSol.add(newDigit);

                findDigitsRecursive(digits, partialSol, sum + digits.get(i), i + 1);

                partialSol.remove(partialSol.size() - 1);
            }
        }

        public void printCombinations() {
            System.out.println("Cage sum = " + this.targetValue);
            for (int digit : possibleDigits) {
                System.out.print(digit + " ");
            }
            System.out.println();
        }

        public boolean isValid() {
            int sum = 0;
            HashSet<Integer> digits = new HashSet<Integer>();

            for (Cell cell : cells) {
                if (cell.getValue() == 0) {
                    continue;
                }

                if (digits.contains(cell.getValue())) {
                    return false;
                } else {
                    digits.add(cell.getValue());
                }

                sum += cell.getValue();

                if (sum > this.targetValue) {
                    return false;
                }
            }

            return true;
        }

        public boolean isComplete() {
            int sum = 0;
            HashSet<Integer> digits = new HashSet<Integer>();

            for (Cell cell : cells) {
                if (digits.contains(cell.getValue())) {
                    return false;
                } else {
                    digits.add(cell.getValue());
                }

                sum += cell.getValue();
            }

            if (sum != this.targetValue) {
                return false;
            }

            return true;
        }

        public HashSet<Integer> getDigitsInCage() {
            return this.digitsInCage;
        }
    }

    @SuppressWarnings("unused")
    private class Cell {
        private Cage cage;
        private int value;

        public Cell(Cage cage) {
            this.cage = cage;
        }

        public void setValue(int newVal) {
            if (newVal == 0) {
                this.cage.getDigitsInCage().remove(this.value);
            } else {
                this.cage.getDigitsInCage().add(newVal);
            }

            this.value = newVal;
        }

        public int getValue() {
            return this.value;
        }
    }

} // end of class KillerSudokuGrid
