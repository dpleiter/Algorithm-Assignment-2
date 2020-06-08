/*
 * @author Jeffrey Chan & Minyi Li, RMIT 2020
 */

package solver;

import grid.SudokuGrid;
import grid.KillerSudokuGrid;
import java.util.*;

/**
 * Backtracking solver for Killer Sudoku.
 */
public class KillerBackTrackingSolver extends KillerSudokuSolver {
    private int gridDimensions;
    KillerSudokuGrid grid;

    public KillerBackTrackingSolver() {
        // No constructor required
    } // end of KillerBackTrackingSolver()

    @Override
    public boolean solve(SudokuGrid grid) {
        this.grid = (KillerSudokuGrid) grid;
        gridDimensions = grid.getSize();

        for (int i = 0; i < gridDimensions * gridDimensions; i++) {
            int row = i / gridDimensions;
            int col = i % gridDimensions;

            if (grid.getCellValue(row, col) == 0) {
                for (int digitPosition = 0; digitPosition < gridDimensions; digitPosition++) {
                    // Need to first check if digit is already in cage, as duplicate digits within
                    // cages throw up unique issues
                    if (this.grid.getCage(row, col).checkDuplicates(this.grid.getDigits().get(digitPosition))) {
                        continue;
                    }

                    grid.setCell(row, col, digitPosition);

                    if (checkInsertion(row, col)) {
                        if (grid.checkComplete()) {
                            return true;
                        } else if (solve(grid)) {
                            return true;
                        }
                    }
                }

                grid.setCell(row, col, -1);
                return false;
            }
        }

        return false;
    } // end of solve()

    private boolean checkInsertion(int rowNum, int colNum) {
        /*
         * Check whether the latest insertion introduced a duplicate in row, column, or
         * box. Finally, check whether the insertion puts the cage for the given cell
         * into an invalid state
         */
        int boxSize = (int) Math.sqrt(gridDimensions);

        // To check for duplicates
        HashSet<Integer> checker = new HashSet<Integer>();

        // Check rows
        for (int col = 0; col < gridDimensions; col++) {
            int cellValue = grid.getCellValue(rowNum, col);

            if (cellValue == 0) {
                continue;
            }

            if (!checker.contains(cellValue)) {
                checker.add(cellValue);
            } else {
                return false;
            }
        }

        checker.clear();

        // Check columns
        for (int row = 0; row < gridDimensions; row++) {
            int cellValue = grid.getCellValue(row, colNum);

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
                int cellValue = grid.getCellValue(boxStartRow + c, boxStartCol + d);

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

        // Check cages
        if (!grid.getCage(rowNum, colNum).isValid()) {
            return false;
        }

        return true;
    }
} // end of class KillerBackTrackingSolver()