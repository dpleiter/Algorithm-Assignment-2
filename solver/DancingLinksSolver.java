/*
 * @author Jeffrey Chan & Minyi Li, RMIT 2020
 */

package solver;

import grid.SudokuGrid;

/**
 * Dancing links solver for standard Sudoku.
 */
public class DancingLinksSolver extends StdSudokuSolver {
    private matrixCol[] colHeaders;
    private int gridDimensions;

    @Override
    public boolean solve(SudokuGrid grid) {
        gridDimensions = grid.getSize();
        initMatrix(grid);

        return performCalcs(grid);
    } // end of solve()

    private void initMatrix(SudokuGrid grid) {
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

            cellConstraint.setRight(rowConstraint);
            rowConstraint.setRight(colConstraint);
            colConstraint.setRight(boxConstraint);
            boxConstraint.setRight(cellConstraint);

            int cellCol = cellConstraintByRow(row);
            int rowCol = rowConstraintByRow(row);
            int colCol = colConstraintByRow(row);
            int boxCol = boxConstraintByRow(row);

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
                            + grid.getDigitPosition(grid.getCellValue(row, col)));
                }
            }
        }
    }

    private boolean performCalcs(SudokuGrid grid) {
        // Algorithm works almost exactly the same as algorithm X
        matrixCol minCol = findMinCol();

        if (minCol == null) {
            // All constraints have been satisfied
            return true;
        }

        if (minCol.getColSum() == 0) {
            // Not possible to satisfy constraint
            return false;
        }

        Node activeConstraint = minCol.getBelow();

        while (activeConstraint instanceof Constraint) {
            Constraint constraint = (Constraint) activeConstraint;

            int gridRow = constraint.getGridRow(gridDimensions);
            int gridCol = constraint.getGridCol(gridDimensions);
            int gridDigit = constraint.getGridDigit(gridDimensions);

            grid.setCell(gridRow, gridCol, gridDigit);

            removeConstraintsByRow(constraint.getMatrixRow());

            if (performCalcs(grid)) {
                return true;
            } else {
                resetConstraintsByRow(constraint.getMatrixRow());
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

    private void removeConstraintsByRow(int rowNum) {
        matrixCol cellConstraint = colHeaders[cellConstraintByRow(rowNum)];
        matrixCol rowConstraint = colHeaders[rowConstraintByRow(rowNum)];
        matrixCol colConstraint = colHeaders[colConstraintByRow(rowNum)];
        matrixCol boxConstraint = colHeaders[boxConstraintByRow(rowNum)];

        cellConstraint.setStatus(false);
        rowConstraint.setStatus(false);
        colConstraint.setStatus(false);
        boxConstraint.setStatus(false);

        removeConstraintsByCol(cellConstraint);
        removeConstraintsByCol(rowConstraint);
        removeConstraintsByCol(colConstraint);
        removeConstraintsByCol(boxConstraint);
    }

    private void resetConstraintsByRow(int rowNum) {
        matrixCol cellConstraint = colHeaders[cellConstraintByRow(rowNum)];
        matrixCol rowConstraint = colHeaders[rowConstraintByRow(rowNum)];
        matrixCol colConstraint = colHeaders[colConstraintByRow(rowNum)];
        matrixCol boxConstraint = colHeaders[boxConstraintByRow(rowNum)];

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
                tempConstraint.reattachNode();

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

    private int cellConstraintByRow(int rowNum) {
        return Math.floorDiv(rowNum, gridDimensions);
    }

    private int rowConstraintByRow(int rowNum) {
        return (gridDimensions * gridDimensions)
                + gridDimensions * Math.floorDiv(rowNum, gridDimensions * gridDimensions) + rowNum % gridDimensions;
    }

    private int colConstraintByRow(int rowNum) {
        return 2 * gridDimensions * gridDimensions + rowNum % (gridDimensions * gridDimensions);
    }

    private int boxConstraintByRow(int rowNum) {
        final double EPS = 0.5; // to account for division errors with double

        double offset = Math.pow(gridDimensions, 1.5)
                * Math.floor(((double) rowNum + EPS) / Math.pow(gridDimensions, 2.5))
                + gridDimensions * Math.floor(
                        (double) (rowNum % (gridDimensions * gridDimensions) + EPS) / Math.pow(gridDimensions, 1.5))
                + rowNum % gridDimensions;

        return 3 * gridDimensions * gridDimensions + (int) offset;
    }

    private abstract class Node {
        protected Node right;

        protected Node above;
        protected Node below;

        public Node() {
            this.right = this;

            this.above = this;
            this.below = this;
        }

        public void detachNode() {
            this.above.setBelow(this.below);
            this.below.setAbove(this.above);
        }

        public void reattachNode() {
            this.above.setBelow(this);
            this.below.setAbove(this);
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
        private boolean isActive;

        public matrixCol(int dimensions) {
            this.colSum = dimensions;
            this.isActive = true;
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

        public void setStatus(boolean newStatus) {
            this.isActive = newStatus;
        }

        public boolean isActive() {
            return this.isActive;
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