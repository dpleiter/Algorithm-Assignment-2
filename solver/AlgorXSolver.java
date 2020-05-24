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

        xMatrix.init(grid);

        xMatrix.printToFile();

        return performCalcs(grid, xMatrix);
    } // end of solve()

    private boolean performCalcs(SudokuGrid grid, algXMatrix matrix) {
        int minCol = -1;

        for (int i = 0; i < matrix.numCols; i++) {
            if (matrix.colInclusion[i]) {
                if (matrix.colSums[i] == 0) {
                    // then not possible to fulfill this constraint
                    return false;
                }

                if (minCol == -1 || (matrix.colSums[i] < matrix.colSums[minCol])) {
                    minCol = i;
                }
            }
        }

        if (minCol == -1) {
            return true;
        }

        int count = 0;

        for (int mRow = 0; mRow < matrix.numRows; mRow++) {
            if (matrix.rowInclusion[mRow] && matrix.matrix[mRow][minCol]) {
                int digit = grid.getDigits().get(mRow % grid.getSize());
                int addInRowNum = Math.floorDiv(mRow, gridSize * gridSize);
                int addInColNum = Math.floorDiv(mRow % (gridSize * gridSize), gridSize);

                System.out.println("Trying digit " + digit + " in cell (" + addInRowNum + ", " + addInColNum + ")");

                if (!matrix.removeConstraintsByRow(mRow, grid.getSize())) {
                    count++;
                    continue;
                }

                grid.setCell(addInRowNum, addInColNum, digit);

                if (performCalcs(grid, matrix)) {
                    return true;
                } else {
                    System.out.println("Resetting cell (" + addInRowNum + ", " + addInColNum + ")");
                    grid.setCell(addInRowNum, addInColNum, 0);
                    matrix.resetConstraintsByRow(mRow, grid.getSize());
                }

                if (++count == matrix.colSums[minCol]) {
                    break;
                }
            }
        }

        System.out.println("FAILED :(");
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
            int mStartRow = rowNum - (rowNum % gridSize);

            int cellCol = cellConstraintByRow(rowNum, gridSize);
            int rowCol = rowConstraintByRow(rowNum, gridSize);
            int colCol = colConstraintByRow(rowNum, gridSize);
            int boxCol = boxConstraintByRow(rowNum, gridSize);

            if (colInclusion[cellCol] && colInclusion[rowCol] && colInclusion[colCol] && colInclusion[boxCol]) {
                colInclusion[cellConstraintByRow(rowNum, gridSize)] = false;
                colInclusion[rowConstraintByRow(rowNum, gridSize)] = false;
                colInclusion[colConstraintByRow(rowNum, gridSize)] = false;
                colInclusion[boxConstraintByRow(rowNum, gridSize)] = false;
            } else {
                System.out.println("FAILED :O");
                return false;
            }

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

            colInclusion[cellConstraintByRow(rowNum, gridSize)] = true;
            colInclusion[rowConstraintByRow(rowNum, gridSize)] = true;
            colInclusion[colConstraintByRow(rowNum, gridSize)] = true;
            colInclusion[boxConstraintByRow(rowNum, gridSize)] = true;
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
        public void printToFile() {
            try {
                BufferedWriter file = new BufferedWriter(new FileWriter("out/matrix_out.csv"));

                for (int row = 0; row < numRows; row++) {
                    if (rowInclusion[row]) {
                        for (int col = 0; col < numCols; col++) {
                            if (colInclusion[col] && matrix[row][col]) {
                                file.write("1");
                            }

                            file.write(",");
                        }
                    }
                    file.write("\n");
                }
                file.close();
            } catch (Exception e) {
                System.out.println("Failed to write");
            }
        }
    }

} // end of class AlgorXSolver
