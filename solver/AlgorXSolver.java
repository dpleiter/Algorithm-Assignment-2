/*
 * @author Jeffrey Chan & Minyi Li, RMIT 2020
 */
package solver;

import grid.SudokuGrid;
import java.io.*;

/**
 * Algorithm X solver for standard Sudoku.
 */
public class AlgorXSolver extends StdSudokuSolver {
    private int gridSize = -1;

    public AlgorXSolver() {

    } // end of AlgorXSolver()

    @Override
    public boolean solve(SudokuGrid grid) {
        gridSize = grid.getSize();
        algXMatrix xMatrix = new algXMatrix(gridSize);

        xMatrix.printToFile();

        xMatrix.init(grid);

        return performCalcs(grid, xMatrix);
    } // end of solve()

    private boolean performCalcs(SudokuGrid grid, algXMatrix matrix) {
        int minCol = -1;

        for (int i = 0; i < matrix.numCols; i++) {
            if (matrix.colInclusion[i]) {
                if (matrix.colSums[i] == 0) {
                    // Not possible to fulfill this constraint
                    System.out.println("Failed2");
                    return false;
                }

                if (minCol == -1 || (matrix.colSums[i] < matrix.colSums[minCol])) {
                    minCol = i;
                }
            }
        }

        if (minCol == -1) {
            // All constraints have been satisfied
            return true;
        }

        int count = 0;

        for (int mRow = 0; mRow < matrix.numRows; mRow++) {
            if (matrix.rowInclusion[mRow] && matrix.matrix[mRow][minCol]) {
                int digit = mRow % grid.getSize();
                int addInRowNum = Math.floorDiv(mRow, gridSize * gridSize);
                int addInColNum = Math.floorDiv(mRow % (gridSize * gridSize), gridSize);

                if (!matrix.removeConstraintsByRow(mRow, grid.getSize())) {
                    count++;
                    continue;
                }

                grid.setCell(addInRowNum, addInColNum, digit);

                if (performCalcs(grid, matrix)) {
                    return true;
                } else {
                    grid.setCell(addInRowNum, addInColNum, -1);
                    matrix.resetConstraintsByRow(mRow, grid.getSize());
                }

                if (++count == matrix.colSums[minCol]) {
                    break;
                }
            }
        }

        return false;
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
                        removeConstraintsByRow(gridSize * gridSize * row + gridSize * col
                                + grid.getDigitPosition(grid.getCellValue(row, col)), gridSize);
                    }
                }
            }
        }

        public boolean removeConstraintsByRow(int rowNum, int gridSize) {
            int cellCol = cellConstraintByRow(rowNum, gridSize);
            int rowCol = rowConstraintByRow(rowNum, gridSize);
            int colCol = colConstraintByRow(rowNum, gridSize);
            int boxCol = boxConstraintByRow(rowNum, gridSize);

            if (colInclusion[cellCol] && colInclusion[rowCol] && colInclusion[colCol] && colInclusion[boxCol]) {
                colInclusion[cellCol] = false;
                colInclusion[rowCol] = false;
                colInclusion[colCol] = false;
                colInclusion[boxCol] = false;

                for (int i = 0; i < numRows; i++) {
                    if (rowInclusion[i]) {
                        if (matrix[i][cellCol]) {
                            colSums[cellConstraintByRow(i, gridSize)]--;
                        }

                        if (matrix[i][rowCol]) {
                            colSums[rowConstraintByRow(i, gridSize)]--;
                        }

                        if (matrix[i][colCol]) {
                            colSums[colConstraintByRow(i, gridSize)]--;
                        }

                        if (matrix[i][boxCol]) {
                            colSums[boxConstraintByRow(i, gridSize)]--;
                        }
                    }
                }
            } else {
                return false;
            }

            int mStartRow = rowNum - (rowNum % gridSize);

            for (int i = 0; i < gridSize; i++) {
                rowInclusion[mStartRow + i] = false;

                int cellConstraintCol = cellConstraintByRow(mStartRow + i, gridSize);
                int rowConstraintCol = rowConstraintByRow(mStartRow + i, gridSize);
                int colConstraintCol = colConstraintByRow(mStartRow + i, gridSize);
                int boxConstraintCol = boxConstraintByRow(mStartRow + i, gridSize);

                colSums[cellConstraintCol] = colSums[cellConstraintCol] - 1;
                colSums[rowConstraintCol] = colSums[rowConstraintCol] - 1;
                colSums[colConstraintCol] = colSums[colConstraintCol] - 1;
                colSums[boxConstraintCol] = colSums[boxConstraintCol] - 1;
            }

            return true;
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

            int mStartRow = rowNum - (rowNum % gridSize);

            for (int i = 0; i < gridSize; i++) {
                rowInclusion[mStartRow + i] = true;

                int cellConstraintCol = cellConstraintByRow(mStartRow + i, gridSize);
                int rowConstraintCol = rowConstraintByRow(mStartRow + i, gridSize);
                int colConstraintCol = colConstraintByRow(mStartRow + i, gridSize);
                int boxConstraintCol = boxConstraintByRow(mStartRow + i, gridSize);

                colSums[cellConstraintCol] = colSums[cellConstraintCol] + 1;
                colSums[rowConstraintCol] = colSums[rowConstraintCol] + 1;
                colSums[colConstraintCol] = colSums[colConstraintCol] + 1;
                colSums[boxConstraintCol] = colSums[boxConstraintCol] + 1;
            }

            for (int i = 0; i < numRows; i++) {
                if (rowInclusion[i]) {
                    if (matrix[i][cellCol]) {
                        colSums[cellConstraintByRow(i, gridSize)]++;
                    }

                    if (matrix[i][rowCol]) {
                        colSums[rowConstraintByRow(i, gridSize)]++;
                    }

                    if (matrix[i][colCol]) {
                        colSums[colConstraintByRow(i, gridSize)]++;
                    }

                    if (matrix[i][boxCol]) {
                        colSums[boxConstraintByRow(i, gridSize)]++;
                    }
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

        // For testing
        @SuppressWarnings("unused")
        public void printToFile() {
            try {
                BufferedWriter file = new BufferedWriter(new FileWriter("out/matrix_out.csv"));

                for (int row = 0; row < numRows; row++) {
                    file.write(Integer.toString(cellConstraintByRow(row, gridSize)));
                    file.write(",");

                    file.write(Integer.toString(rowConstraintByRow(row, gridSize)));
                    file.write(",");

                    file.write(Integer.toString(colConstraintByRow(row, gridSize)));
                    file.write(",");

                    file.write(Integer.toString(boxConstraintByRow(row, gridSize)));
                    file.write("\n");
                }
                file.close();
            } catch (Exception e) {
                System.out.println("Failed to write");
            }
        }
    }

} // end of class AlgorXSolver
