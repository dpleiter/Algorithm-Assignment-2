/*
 * @author Jeffrey Chan & Minyi Li, RMIT 2020
 */

package solver;

import grid.SudokuGrid;
import java.util.*;

/**
 * Backtracking solver for standard Sudoku.
 */
public class BackTrackingSolver extends StdSudokuSolver {
    public BackTrackingSolver() {
        // TODO: any initialisation you want to implement.
    } // end of BackTrackingSolver()

    @Override
    public boolean solve(SudokuGrid grid) {
        Iterator<Integer> digits = grid.getDigits().iterator();
        int digit;
        int gridSize = grid.getSize();

        for (int i = 0; i < gridSize * gridSize; i++) {
            int row = i / gridSize;
            int col = i % gridSize;

            if (grid.getCellValue(row, col) == 0) {
                while (digits.hasNext()) {
                    digit = digits.next();
                    grid.setCell(row, col, digit);

                    if (grid.validate()) {
                        if (grid.checkComplete()) {
                            return true;
                        } else if (solve(grid)) {
                            return true;
                        }
                    }
                }

                grid.setCell(row, col, 0);
                return false;
            }
        }

        return false;
    } // end of solve()

} // end of class BackTrackingSolver()
