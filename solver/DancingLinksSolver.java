/*
 * @author Jeffrey Chan & Minyi Li, RMIT 2020
 */

package solver;

import grid.SudokuGrid;

/**
 * Dancing links solver for standard Sudoku.
 */
public class DancingLinksSolver extends StdSudokuSolver {
    matrixCol[] colHeaders;

    public DancingLinksSolver() {

    } // end of DancingLinksSolver()

    @Override
    public boolean solve(SudokuGrid grid) {
        initMatrix(grid);

        return performCalcs(grid);
    } // end of solve()

    private void initMatrix(SudokuGrid grid) {
        int gridDimensions = grid.getSize();

        int matrixRows = gridDimensions * gridDimensions * gridDimensions;
        int matrixCols = 4 * gridDimensions * gridDimensions;

        colHeaders = new matrixCol[matrixCols];

        for (int col = 0; col < matrixCols; col++) {
            colHeaders[col] = new matrixCol(gridDimensions);
        }

        for (int row = 0; row < matrixRows; row++) {
            Node cellConstraint = new Constraint(row);
            Node rowConstraint = new Constraint(row);
            Node colConstraint = new Constraint(row);
            Node boxConstraint = new Constraint(row);

            cellConstraint.setLeft(boxConstraint);
            cellConstraint.setRight(rowConstraint);

            rowConstraint.setLeft(cellConstraint);
            rowConstraint.setRight(colConstraint);

            colConstraint.setLeft(rowConstraint);
            colConstraint.setRight(boxConstraint);

            boxConstraint.setLeft(colConstraint);
            boxConstraint.setRight(cellConstraint);

            int cellCol = cellConstraintByRow(row, gridDimensions);
            int rowCol = rowConstraintByRow(row, gridDimensions);
            int colCol = colConstraintByRow(row, gridDimensions);
            int boxCol = boxConstraintByRow(row, gridDimensions);

            colHeaders[cellCol].addVertical(cellConstraint);
            colHeaders[rowCol].addVertical(rowConstraint);
            colHeaders[colCol].addVertical(colConstraint);
            colHeaders[boxCol].addVertical(boxConstraint);
        }

        // INIT WITH GRID
        for (int row = 0; row < gridDimensions; row++) {
            for (int col = 0; col < gridDimensions; col++) {
                if (grid.getCellValue(row, col) != 0) {
                    // Remove from matrix
                    removeConstraintsByRow(row * gridDimensions * gridDimensions + gridDimensions * col
                            + grid.getDigitPosition(grid.getCellValue(row, col)), gridDimensions);
                }
            }
        }
    }

    private boolean performCalcs(SudokuGrid grid) {
        matrixCol minCol = findMinCol();

        if (minCol == null) {
            return true;
        }

        if (minCol.getColSum() == 0) {
            return false;
        }

        Node activeConstraint = minCol.getBelow();

        while (activeConstraint instanceof Constraint) {
            Constraint constraint = (Constraint) activeConstraint;

            int gridRow = constraint.getGridRow(grid.getSize());
            int gridCol = constraint.getGridCol(grid.getSize());
            int gridDigit = constraint.getGridDigit(grid.getSize());

            grid.setCell(gridRow, gridCol, gridDigit);

            removeConstraintsByRow(constraint.getMatrixRow(), grid.getSize());

            if (performCalcs(grid)) {
                return true;
            } else {
                resetConstraintsByRow(constraint.getMatrixRow(), grid.getSize());
                grid.setCell(gridRow, gridCol, -1);
            }

            activeConstraint = activeConstraint.getBelow();
        }

        return false;
    }

    private matrixCol findMinCol() {
        matrixCol minCol = null;
        matrixCol activeCol;

        for (int col = 0; col < colHeaders.length; col++) {
            activeCol = colHeaders[col];

            if (activeCol.isActive()) {
                if (minCol == null || activeCol.getColSum() < minCol.getColSum()) {
                    minCol = activeCol;
                }
            }
        }

        return minCol;
    }

    private void removeConstraintsByRow(int rowNum, int gridSize) {
        matrixCol cellConstraint = colHeaders[cellConstraintByRow(rowNum, gridSize)];
        matrixCol rowConstraint = colHeaders[rowConstraintByRow(rowNum, gridSize)];
        matrixCol colConstraint = colHeaders[colConstraintByRow(rowNum, gridSize)];
        matrixCol boxConstraint = colHeaders[boxConstraintByRow(rowNum, gridSize)];

        cellConstraint.setStatus(false);
        rowConstraint.setStatus(false);
        colConstraint.setStatus(false);
        boxConstraint.setStatus(false);

        removeConstraintsByCol(cellConstraint);
        removeConstraintsByCol(rowConstraint);
        removeConstraintsByCol(colConstraint);
        removeConstraintsByCol(boxConstraint);
    }

    private void resetConstraintsByRow(int rowNum, int gridSize) {
        matrixCol cellConstraint = colHeaders[cellConstraintByRow(rowNum, gridSize)];
        matrixCol rowConstraint = colHeaders[rowConstraintByRow(rowNum, gridSize)];
        matrixCol colConstraint = colHeaders[colConstraintByRow(rowNum, gridSize)];
        matrixCol boxConstraint = colHeaders[boxConstraintByRow(rowNum, gridSize)];

        cellConstraint.setStatus(true);
        rowConstraint.setStatus(true);
        colConstraint.setStatus(true);
        boxConstraint.setStatus(true);

        resetConstraintsByCol(cellConstraint);
        resetConstraintsByCol(rowConstraint);
        resetConstraintsByCol(colConstraint);
        resetConstraintsByCol(boxConstraint);
    }

    private void removeConstraintsByCol(matrixCol colHeader) {
        Node activeConstraint = colHeader.getBelow();

        while (activeConstraint instanceof Constraint) {
            Node tempConstraint = activeConstraint.getRight();

            while (tempConstraint != activeConstraint) {
                tempConstraint.detachNode();

                decrementColumnOfConstraint(tempConstraint);

                tempConstraint = tempConstraint.getRight();
            }

            activeConstraint = activeConstraint.getBelow();
        }
    }

    private void resetConstraintsByCol(matrixCol colHeader) {
        Node activeConstraint = colHeader.getBelow();

        while (activeConstraint instanceof Constraint) {
            Node tempConstraint = activeConstraint.getRight();

            while (tempConstraint != activeConstraint) {
                tempConstraint.reatachNode();

                incrementColumnOfConstraint(tempConstraint);

                tempConstraint = tempConstraint.getRight();
            }

            activeConstraint = activeConstraint.getBelow();
        }
    }

    private void decrementColumnOfConstraint(Node node) {
        while (node instanceof Constraint) {
            node = node.getAbove();
        }

        matrixCol colHeader = (matrixCol) node;

        colHeader.decreaseColSum();
    }

    private void incrementColumnOfConstraint(Node node) {
        while (node instanceof Constraint) {
            node = node.getAbove();
        }

        matrixCol colHeader = (matrixCol) node;

        colHeader.incrementColSum();
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

    private abstract class Node {
        protected boolean isActive;

        protected Node left;
        protected Node right;

        protected Node above;
        protected Node below;

        public Node() {
            this.isActive = true;

            this.left = this;
            this.right = this;

            this.above = this;
            this.below = this;
        }

        public void detachNode() {
            this.above.setBelow(this.below);
            this.below.setAbove(this.above);
        }

        public void reatachNode() {
            this.above.setBelow(this);
            this.below.setAbove(this);
        }

        public boolean isActive() {
            return this.isActive;
        }

        public Node getRight() {
            return this.right;
        }

        public Node getAbove() {
            return this.above;
        }

        public Node getBelow() {
            return this.below;
        }

        public void setStatus(boolean newStatus) {
            this.isActive = newStatus;
        }

        public void setLeft(Node node) {
            this.left = node;
        }

        public void setRight(Node node) {
            this.right = node;
        }

        public void setAbove(Node node) {
            this.above = node;
        }

        public void setBelow(Node node) {
            this.below = node;
        }
    }

    private class matrixCol extends Node {
        private int colSum;

        public matrixCol(int dimensions) {
            this.colSum = dimensions;
        }

        public int getColSum() {
            return this.colSum;
        }

        public void decreaseColSum() {
            this.colSum--;
        }

        public void incrementColSum() {
            this.colSum++;
        }

        public void addVertical(Node node) {
            node.setAbove(this);
            node.setBelow(this.below);

            this.below.setAbove(node);
            this.below = node;
        }
    }

    private class Constraint extends Node {
        private int rowNum;

        public Constraint(int rowNum) {
            super();

            this.rowNum = rowNum;
        }

        public int getGridRow(int gridDimensions) {
            return Math.floorDiv(this.rowNum, gridDimensions * gridDimensions);
        }

        public int getGridCol(int gridDimensions) {
            return Math.floorDiv(this.rowNum % (gridDimensions * gridDimensions), gridDimensions);
        }

        public int getGridDigit(int gridDimensions) {
            return this.rowNum % gridDimensions;
        }

        public int getMatrixRow() {
            return this.rowNum;
        }
    }

} // end of class DancingLinksSolver
