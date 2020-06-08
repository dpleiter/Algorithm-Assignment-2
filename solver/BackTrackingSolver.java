/*
 * @author Jeffrey Chan & Minyi Li, RMIT 2020
 */

package solver;

import java.util.*;
import grid.SudokuGrid;
import grid.StdSudokuGrid;

/**
 * Backtracking solver for standard Sudoku.
 */
public class BackTrackingSolver extends StdSudokuSolver {
    private StdSudokuGrid grid;
    private int gridDimensions;

    public BackTrackingSolver() {

    } // end of BackTrackingSolver()

    @Override
    public boolean solve(SudokuGrid grid) {
        // Iterator<Integer> digits = grid.getDigits().iterator();
        // int digit;
        this.grid = (StdSudokuGrid) grid;

        gridDimensions = this.grid.getSize();

        for (int i = 0; i < gridDimensions * gridDimensions; i++) {
            int row = i / gridDimensions;
            int col = i % gridDimensions;

            if (grid.getCellValue(row, col) == 0) {
                for (int digit = 0; digit < gridDimensions; digit++) {
                    this.grid.setCell(row, col, digit);

                    if (checkInsertion(row, col)) {
                        if (this.grid.checkComplete()) {
                            return true;
                        } else if (solve(this.grid)) {
                            return true;
                        }
                    }
                }

                this.grid.setCell(row, col, -1);
                return false;
            }
        }

        return false;
    } // end of solve()

    private boolean checkInsertion(int rowNum, int colNum) {
        int boxSize = (int) Math.sqrt(gridDimensions);
        int cellValue;

        HashSet<Integer> checker = new HashSet<Integer>();

        // Check rows
        for (int col = 0; col < gridDimensions; col++) {
            cellValue = grid.getCellValue(rowNum, col);

            if (cellValue == 0)
                continue;

            if (!checker.contains(cellValue)) {
                checker.add(cellValue);
            } else {
                return false;
            }
        }

        checker.clear();

        // Check columns
        for (int row = 0; row < gridDimensions; row++) {
            cellValue = grid.getCellValue(row, colNum);

            if (cellValue == 0)
                continue;

            if (!checker.contains(cellValue)) {
                checker.add(cellValue);
            } else {
                return false;
            }
        }

        checker.clear();

        // Check boxes
        int boxStartRow = boxSize * Math.floorDiv(rowNum, boxSize);
        int boxStartCol = boxSize * Math.floorDiv(colNum, boxSize);

        for (int c = 0; c < boxSize; c++) {
            for (int d = 0; d < boxSize; d++) {
                cellValue = grid.getCellValue(boxStartRow + c, boxStartCol + d);

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

        return true;
    }
} // end of class BackTrackingSolver()
