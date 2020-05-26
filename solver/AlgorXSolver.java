/*
 * @author Jeffrey Chan & Minyi Li, RMIT 2020
 */
package solver;

import grid.SudokuGrid;

/**
 * Algorithm X solver for standard Sudoku.
 */
public class AlgorXSolver extends StdSudokuSolver {
    private int gridSize = -1;
    algXMatrix xMatrix;

    @Override
    public boolean solve(SudokuGrid grid) {
        gridSize = grid.getSize();
        xMatrix = new algXMatrix(gridSize);

        xMatrix.init(grid);

        return performCalcs(grid);
    } // end of solve()

    private boolean performCalcs(SudokuGrid grid) {
        int minCol = findMinCol();

        if (minCol == -1) {
            // All constraints have been satisfied
            return true;
        }

        if (xMatrix.colSums[minCol] == 0) {
            // Not possible to fulfill this constraint
            return false;
        }

        for (int mRow = 0; mRow < xMatrix.numRows; mRow++) {
            if (xMatrix.matrix[mRow][minCol]) {
                int digit = mRow % grid.getSize();
                int addInRowNum = Math.floorDiv(mRow, gridSize * gridSize);
                int addInColNum = Math.floorDiv(mRow % (gridSize * gridSize), gridSize);

                xMatrix.removeConstraints(mRow, grid.getSize());

                grid.setCell(addInRowNum, addInColNum, digit);

                if (performCalcs(grid)) {
                    return true;
                } else {
                    grid.setCell(addInRowNum, addInColNum, -1);
                    xMatrix.resetConstraintsByRow(mRow, grid.getSize());
                }
            }
        }

        return false;
    }

    private int findMinCol() {
        int minCol = -1;

        for (int i = 0; i < xMatrix.numCols; i++) {
            if (xMatrix.colInclusion[i]) {
                if (xMatrix.colSums[i] == 0) {
                    return i;
                }

                if (minCol == -1 || (xMatrix.colSums[i] < xMatrix.colSums[minCol])) {
                    minCol = i;
                }
            }
        }

        return minCol;
    }

    private class algXMatrix {
        public boolean[][] matrix;

        public boolean[] colInclusion;
        public int[] colSums;

        public boolean[] rowInclusion;

        public int numRows;
        public int numCols;

        public algXMatrix(int dimensions) {
            numRows = dimensions * dimensions * dimensions;
            numCols = 4 * dimensions * dimensions;

            matrix = new boolean[numRows][numCols];

            rowInclusion = new boolean[numRows];

            colInclusion = new boolean[numCols];
            colSums = new int[numCols];

            for (int colNum = 0; colNum < numCols; colNum++) {
                colInclusion[colNum] = true;
                colSums[colNum] = dimensions;
            }

            for (int rowNum = 0; rowNum < numRows; rowNum++) {
                rowInclusion[rowNum] = true;

                // cell constraint
                matrix[rowNum][cellConstraintByRow(rowNum, dimensions)] = true;

                // row constraint
                matrix[rowNum][rowConstraintByRow(rowNum, dimensions)] = true;

                // col constraint
                matrix[rowNum][colConstraintByRow(rowNum, dimensions)] = true;

                // box constraint
                matrix[rowNum][boxConstraintByRow(rowNum, dimensions)] = true;
            }
        }

        public void init(SudokuGrid grid) {
            int gridSize = grid.getSize();

            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < gridSize; col++) {
                    if (grid.getCellValue(row, col) != 0) {
                        removeConstraints(gridSize * gridSize * row + gridSize * col
                                + grid.getDigitPosition(grid.getCellValue(row, col)), gridSize);
                    }
                }
            }
        }

        public void removeConstraints(int rowNum, int gridSize) {
            int cellCol = cellConstraintByRow(rowNum, gridSize);
            int rowCol = rowConstraintByRow(rowNum, gridSize);
            int colCol = colConstraintByRow(rowNum, gridSize);
            int boxCol = boxConstraintByRow(rowNum, gridSize);

            colInclusion[cellCol] = false;
            colInclusion[rowCol] = false;
            colInclusion[colCol] = false;
            colInclusion[boxCol] = false;

            for (int i = 0; i < numRows; i++) {
                if (matrix[i][cellCol]) {
                    int tempCol1 = rowConstraintByRow(i, gridSize);
                    int tempCol2 = colConstraintByRow(i, gridSize);
                    int tempCol3 = boxConstraintByRow(i, gridSize);

                    colSums[tempCol1]--;
                    colSums[tempCol2]--;
                    colSums[tempCol3]--;

                    matrix[i][tempCol1] = false;
                    matrix[i][tempCol2] = false;
                    matrix[i][tempCol3] = false;
                }

                if (matrix[i][rowCol]) {
                    int tempCol1 = cellConstraintByRow(i, gridSize);
                    int tempCol2 = colConstraintByRow(i, gridSize);
                    int tempCol3 = boxConstraintByRow(i, gridSize);

                    colSums[tempCol1]--;
                    colSums[tempCol2]--;
                    colSums[tempCol3]--;

                    matrix[i][tempCol1] = false;
                    matrix[i][tempCol2] = false;
                    matrix[i][tempCol3] = false;
                }

                if (matrix[i][colCol]) {
                    int tempCol1 = cellConstraintByRow(i, gridSize);
                    int tempCol2 = rowConstraintByRow(i, gridSize);
                    int tempCol3 = boxConstraintByRow(i, gridSize);

                    colSums[tempCol1]--;
                    colSums[tempCol2]--;
                    colSums[tempCol3]--;

                    matrix[i][tempCol1] = false;
                    matrix[i][tempCol2] = false;
                    matrix[i][tempCol3] = false;
                }

                if (matrix[i][boxCol]) {
                    int tempCol1 = cellConstraintByRow(i, gridSize);
                    int tempCol2 = rowConstraintByRow(i, gridSize);
                    int tempCol3 = colConstraintByRow(i, gridSize);

                    colSums[tempCol1]--;
                    colSums[tempCol2]--;
                    colSums[tempCol3]--;

                    matrix[i][tempCol1] = false;
                    matrix[i][tempCol2] = false;
                    matrix[i][tempCol3] = false;
                }
            }
        }

        public void resetConstraintsByRow(int rowNum, int gridSize) {
            int cellCol = cellConstraintByRow(rowNum, gridSize);
            int rowCol = rowConstraintByRow(rowNum, gridSize);
            int colCol = colConstraintByRow(rowNum, gridSize);
            int boxCol = boxConstraintByRow(rowNum, gridSize);

            colInclusion[cellCol] = true;
            colInclusion[rowCol] = true;
            colInclusion[colCol] = true;
            colInclusion[boxCol] = true;

            for (int i = 0; i < numRows; i++) {
                if (matrix[i][cellCol]) {
                    int tempCol1 = rowConstraintByRow(i, gridSize);
                    int tempCol2 = colConstraintByRow(i, gridSize);
                    int tempCol3 = boxConstraintByRow(i, gridSize);

                    colSums[tempCol1]++;
                    colSums[tempCol2]++;
                    colSums[tempCol3]++;

                    matrix[i][tempCol1] = true;
                    matrix[i][tempCol2] = true;
                    matrix[i][tempCol3] = true;
                }

                if (matrix[i][rowCol]) {
                    int tempCol1 = cellConstraintByRow(i, gridSize);
                    int tempCol2 = colConstraintByRow(i, gridSize);
                    int tempCol3 = boxConstraintByRow(i, gridSize);

                    colSums[tempCol1]++;
                    colSums[tempCol2]++;
                    colSums[tempCol3]++;

                    matrix[i][tempCol1] = true;
                    matrix[i][tempCol2] = true;
                    matrix[i][tempCol3] = true;
                }

                if (matrix[i][colCol]) {
                    int tempCol1 = cellConstraintByRow(i, gridSize);
                    int tempCol2 = rowConstraintByRow(i, gridSize);
                    int tempCol3 = boxConstraintByRow(i, gridSize);

                    colSums[tempCol1]++;
                    colSums[tempCol2]++;
                    colSums[tempCol3]++;

                    matrix[i][tempCol1] = true;
                    matrix[i][tempCol2] = true;
                    matrix[i][tempCol3] = true;
                }

                if (matrix[i][boxCol]) {
                    int tempCol1 = cellConstraintByRow(i, gridSize);
                    int tempCol2 = rowConstraintByRow(i, gridSize);
                    int tempCol3 = colConstraintByRow(i, gridSize);

                    colSums[tempCol1]++;
                    colSums[tempCol2]++;
                    colSums[tempCol3]++;

                    matrix[i][tempCol1] = true;
                    matrix[i][tempCol2] = true;
                    matrix[i][tempCol3] = true;
                }
            }
        }

        private int cellConstraintByRow(int rowNum, int dimensions) {
            return Math.floorDiv(rowNum, dimensions);
        }

        private int rowConstraintByRow(int rowNum, int dimensions) {
            return (dimensions * dimensions) + dimensions * Math.floorDiv(rowNum, dimensions * dimensions)
                    + rowNum % dimensions;
        }

        private int colConstraintByRow(int rowNum, int dimensions) {
            return 2 * dimensions * dimensions + rowNum % (dimensions * dimensions);
        }

        private int boxConstraintByRow(int rowNum, int dimensions) {
            double offset = Math.pow(dimensions, 1.5) * Math.floorDiv(rowNum, (int) Math.pow(dimensions, 2.5))
                    + dimensions * Math.floorDiv(rowNum % (dimensions * dimensions), (int) Math.pow(dimensions, 1.5))
                    + rowNum % dimensions;

            return 3 * dimensions * dimensions + (int) offset;
        }
    }

} // end of class AlgorXSolver
