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
                    Cell newCell = new Cell(row, col, newCage);

                    newCage.addCell(newCell);
                    grid[row][col] = newCell;
                } else {
                    // throw new Exception();
                    System.out.println("EXCEPTION SHOULD BE THROWN HERE");
                }
            }

            newCage.findCombinations(digits);

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

    public ArrayList<Cage> getCages() {
        return this.cages;
    }

    public Cell getCell(int row, int col) {
        return this.grid[row][col];
    }

    public Cage getCage(int row, int col) {
        return this.grid[row][col].getCage();
    }

    public class Cage {
        private ArrayList<Cell> cells = new ArrayList<Cell>();
        private HashSet<Integer> possibleDigits = new HashSet<Integer>();
        private HashSet<Integer> digitsInCage = new HashSet<Integer>();
        private int currentValue;
        private int targetValue;

        public Cage(int targetValue) {
            this.targetValue = targetValue;
        }

        public void addCell(Cell cell) {
            cells.add(cell);
        }

        public void findCombinations(ArrayList<Integer> digits) {
            possibleDigits.clear();

            findCombinationsRecursive(digits, new ArrayList<Integer>(), currentValue, 0);
        }

        public void findCombinationsRecursive(ArrayList<Integer> digits, ArrayList<Integer> partialSol, int sum,
                int startIndex) {
            if (sum == this.targetValue && partialSol.size() == (this.cells.size() - digitsInCage.size())) {
                for (int i : partialSol) {
                    possibleDigits.add(i);
                }
            }

            if (!(sum >= targetValue)) {
                for (int i = startIndex; i < digits.size(); i++) {
                    int newDigit = digits.get(i);

                    if (digitsInCage.contains(newDigit)) {
                        continue;
                    }

                    partialSol.add(newDigit);

                    findCombinationsRecursive(digits, partialSol, sum + digits.get(i), i + 1);

                    partialSol.remove(partialSol.size() - 1);
                }
            }
        }

        public void increaseSum(int value) {
            this.currentValue += value;
        }

        public boolean checkDuplicates(int digit) {
            return this.digitsInCage.contains(digit);
        }

        public boolean isValid() {
            HashSet<Integer> digits = new HashSet<Integer>();

            // System.out.println(" Digits in cage = " + digitsInCage.size() + ", cells in
            // cage = " + cells.size());

            if (this.currentValue > this.targetValue
                    || (digitsInCage.size() == cells.size() && this.currentValue != this.targetValue)) {
                return false;
            }

            for (Cell cell : cells) {
                if (cell.getValue() == 0) {
                    continue;
                }

                if (digits.contains(cell.getValue())) {
                    return false;
                } else {
                    digits.add(cell.getValue());
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

        public HashSet<Integer> getPossibleDigits() {
            return this.possibleDigits;
        }

        public ArrayList<Cell> getCells() {
            return this.cells;
        }
    }

    public class Cell {
        private Cage cage;
        private int value;

        private int gridRow;
        private int gridCol;

        public Cell(int row, int col, Cage cage) {
            this.gridRow = row;
            this.gridCol = col;

            this.cage = cage;
        }

        public void setValue(int newVal) {
            if (this.value != 0) {
                this.cage.getDigitsInCage().remove(this.value);
            }

            if (newVal != 0) {
                this.cage.getDigitsInCage().add(newVal);
            }

            // System.out.println("Increase cage total by " + (newVal - this.value));
            this.cage.increaseSum(newVal - this.value);
            this.value = newVal;
        }

        public int getRow() {
            return this.gridRow;
        }

        public int getCol() {
            return this.gridCol;
        }

        public int getValue() {
            return this.value;
        }

        public Cage getCage() {
            return this.cage;
        }
    }

} // end of class KillerSudokuGrid
