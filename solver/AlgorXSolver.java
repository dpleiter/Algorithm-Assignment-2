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
    public AlgorXSolver() {

    } // end of AlgorXSolver()

    @Override
    public boolean solve(SudokuGrid grid) {
        algXMatrix xMatrix = new algXMatrix(grid.getSize());

        // xMatrix.printToFile();

        xMatrix.init(grid);

        xMatrix.printToFile();

        // placeholder
        return false;
    } // end of solve()

    private class algXMatrix {
        private boolean[][] matrix;
        private boolean[] colInclusion;
        private int[] colSums;

        private boolean[] rowInclusion;

        int numRows;
        int numCols;

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

            int cellConstraintCol;
            int rowConstraintCol;
            int colConstraintCol;
            int boxConstraintCol;

            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < gridSize; col++) {
                    if (grid.getCellValue(row, col) != 0) {
                        int mStartRow = (gridSize * gridSize * row) + (gridSize * col);
                        for (int i = 0; i < gridSize; i++) {
                            rowInclusion[mStartRow + i] = false;

                            cellConstraintCol = cellConstraintByRow(mStartRow + i, gridSize);
                            rowConstraintCol = rowConstraintByRow(mStartRow + i, gridSize);
                            colConstraintCol = colConstraintByRow(mStartRow + i, gridSize);
                            boxConstraintCol = boxConstraintByRow(mStartRow + i, gridSize);

                            colSums[cellConstraintCol] = colSums[cellConstraintCol] - 1;
                            colSums[rowConstraintCol] = colSums[rowConstraintCol] - 1;
                            colSums[colConstraintCol] = colSums[colConstraintCol] - 1;
                            colSums[boxConstraintCol] = colSums[boxConstraintCol] - 1;
                        }

                        colInclusion[cellConstraintByRow(mStartRow + grid.getCellValue(row, col) - 1,
                                gridSize)] = false;
                        colInclusion[rowConstraintByRow(mStartRow + grid.getCellValue(row, col) - 1, gridSize)] = false;
                        colInclusion[colConstraintByRow(mStartRow + grid.getCellValue(row, col) - 1, gridSize)] = false;
                        colInclusion[boxConstraintByRow(mStartRow + grid.getCellValue(row, col) - 1, gridSize)] = false;
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
