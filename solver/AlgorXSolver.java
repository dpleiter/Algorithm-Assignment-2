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

        xMatrix.printToFile();

        // placeholder
        return false;
    } // end of solve()

    private class algXMatrix {
        private boolean[][] matrix;
        private boolean[] colInclusion;
        private int[] colSums;

        int numRows;
        int numCols;

        public algXMatrix(int dimensions) {
            numRows = dimensions * dimensions * dimensions;
            numCols = 4 * dimensions * dimensions;

            matrix = new boolean[numRows][numCols];

            colInclusion = new boolean[numCols];
            colSums = new int[numCols];

            double offset;

            for (int colNum = 0; colNum < numCols; colNum++) {
                colInclusion[colNum] = true;
                colSums[colNum] = dimensions;
            }

            for (int rowNum = 0; rowNum < numRows; rowNum++) {
                // cell constraint
                int cellCol = Math.floorDiv(rowNum, dimensions);
                matrix[rowNum][cellCol] = true;

                // row constraint
                offset = dimensions * Math.floorDiv(rowNum, dimensions * dimensions) + rowNum % dimensions;
                int rowCol = (dimensions * dimensions) + (int) offset;
                matrix[rowNum][rowCol] = true;

                // col constraint
                offset = rowNum % (dimensions * dimensions);
                int colCol = 2 * dimensions * dimensions + (int) offset;
                matrix[rowNum][colCol] = true;

                // box constraint
                offset = Math.pow(dimensions, 1.5) * Math.floorDiv(rowNum, (int) Math.pow(dimensions, 2.5))
                        + dimensions
                                * Math.floorDiv(rowNum % (dimensions * dimensions), (int) Math.pow(dimensions, 1.5))
                        + rowNum % dimensions;

                int boxCol = 3 * dimensions * dimensions + (int) offset;

                matrix[rowNum][boxCol] = true;
            }
        }

        // For testing
        public void printToFile() {
            try {
                BufferedWriter file = new BufferedWriter(new FileWriter("out/matrix_out.csv"));

                for (int row = 0; row < numRows; row++) {
                    for (int col = 0; col < numCols; col++) {
                        if (matrix[row][col]) {
                            file.write("1");
                        }

                        file.write(",");
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
