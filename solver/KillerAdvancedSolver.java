/*
 * @author Jeffrey Chan & Minyi Li, RMIT 2020
 */

package solver;

import grid.KillerSudokuGrid;
import grid.SudokuGrid;

/**
 * Your advanced solver for Killer Sudoku.
 */
public class KillerAdvancedSolver extends KillerSudokuSolver {
    private MatrixCol[] colHeaders;
    private MatrixRow[] rowHeaders;

    private KillerSudokuGrid grid;
    private int gridDimensions;

    public KillerAdvancedSolver() {

    } // end of KillerAdvancedSolver()

    @Override
    public boolean solve(SudokuGrid grid) {
        this.grid = (KillerSudokuGrid) grid;
        gridDimensions = grid.getSize();

        initMatrix(this.grid);

        return performCalcs();
    }

    private void initMatrix(KillerSudokuGrid grid) {
        int matrixRows = gridDimensions * gridDimensions * gridDimensions;
        int matrixCols = 4 * gridDimensions * gridDimensions;

        colHeaders = new MatrixCol[matrixCols];
        rowHeaders = new MatrixRow[matrixRows];

        for (int col = 0; col < matrixCols; col++) {
            colHeaders[col] = new MatrixCol(gridDimensions);
        }

        for (int row = 0; row < matrixRows; row++) {
            MatrixRow newRow = new MatrixRow(row);
            rowHeaders[row] = newRow;

            int cellCol = cellConstraintByRow(row);
            int rowCol = rowConstraintByRow(row);
            int colCol = colConstraintByRow(row);
            int boxCol = boxConstraintByRow(row);

            Node cellConstraint = new Constraint(newRow, colHeaders[cellCol]);
            Node rowConstraint = new Constraint(newRow, colHeaders[rowCol]);
            Node colConstraint = new Constraint(newRow, colHeaders[colCol]);
            Node boxConstraint = new Constraint(newRow, colHeaders[boxCol]);

            newRow.setRight(cellConstraint);
            cellConstraint.setRight(rowConstraint);
            rowConstraint.setRight(colConstraint);
            colConstraint.setRight(boxConstraint);
            boxConstraint.setRight(cellConstraint);

            colHeaders[cellCol].addVertical(cellConstraint);
            colHeaders[rowCol].addVertical(rowConstraint);
            colHeaders[colCol].addVertical(colConstraint);
            colHeaders[boxCol].addVertical(boxConstraint);
        }

        for (KillerSudokuGrid.Cage cage : grid.getCages()) {
            for (int digit : grid.getDigits()) {
                if (!cage.getPossibleDigits().contains(digit)) {
                    // remove row
                    for (KillerSudokuGrid.Cell cell : cage.getCells()) {
                        int xRowNum = cell.getRow() * gridDimensions * gridDimensions + cell.getCol() * gridDimensions
                                + grid.getDigitPosition(digit);

                        MatrixRow xRow = rowHeaders[xRowNum];

                        Constraint constraint = (Constraint) xRow.getRight();

                        for (int i = 0; i < 4; i++) {
                            constraint.detachNode();

                            constraint = (Constraint) constraint.getRight();
                        }

                        xRow.setCageLock(true);
                    }
                }
            }
        }
    }

    private boolean performCalcs() {
        MatrixCol minCol = findMinCol();

        if (minCol == null) {
            return true;
        }

        if (minCol.getColSum() == 0) {
            return false;
        }

        Constraint activeConstraint = (Constraint) minCol.getBelow();

        while (true) {
            MatrixRow constraintRow = activeConstraint.getRowHeader();

            int gridRow = constraintRow.getGridRow(gridDimensions);
            int gridCol = constraintRow.getGridCol(gridDimensions);
            int gridDigit = constraintRow.getGridDigit(gridDimensions);

            grid.setCell(gridRow, gridCol, gridDigit);

            // Determine new digits that can occupy cage and update matrix accordingly
            deleteRowsByCage(this.grid.getCage(gridRow, gridCol), constraintRow.getMatrixRowNum());

            // Now do the same for other constraints
            removeConstraintsByRow(constraintRow.getMatrixRowNum());

            if (performCalcs()) {
                return true;
            } else {
                grid.setCell(gridRow, gridCol, -1);
                resetConstraintsByRow(constraintRow.getMatrixRowNum());
                deleteRowsByCage(this.grid.getCage(gridRow, gridCol), constraintRow.getMatrixRowNum());
            }

            if (activeConstraint.getBelow() instanceof Constraint) {
                activeConstraint = (Constraint) activeConstraint.getBelow();
            } else {
                break;
            }
        }

        return false;
    }

    private MatrixCol findMinCol() {
        MatrixCol minCol = null;
        MatrixCol activeCol;

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

    private void deleteRowsByCage(KillerSudokuGrid.Cage cage, int forbiddenRow) {
        cage.findCombinations(grid.getDigits());

        for (int digit : grid.getDigits()) {
            // if (!cage.getPossibleDigits().contains(digit)) {
            // remove row
            for (KillerSudokuGrid.Cell cell : cage.getCells()) {
                int xRowNum = cell.getRow() * gridDimensions * gridDimensions + cell.getCol() * gridDimensions
                        + grid.getDigitPosition(digit);

                if (xRowNum == forbiddenRow) {
                    continue;
                }

                MatrixRow xRow = rowHeaders[xRowNum];

                if (!xRow.isConstraintLocked()) {
                    Constraint constraint = (Constraint) xRow.getRight();

                    if (xRow.isCageLocked() && cage.getPossibleDigits().contains(digit)) {
                        // need to reattach these nodes
                        xRow.setCageLock(false);

                        for (int i = 0; i < 4; i++) {
                            constraint.reattachNode();

                            constraint = (Constraint) constraint.getRight();
                        }
                    }

                    if (!xRow.isCageLocked() && !cage.getPossibleDigits().contains(digit)) {
                        xRow.setCageLock(true);

                        for (int i = 0; i < 4; i++) {
                            constraint.detachNode();

                            constraint = (Constraint) constraint.getRight();
                        }
                    }
                }
            }
            // }
        }
    }

    private void removeConstraintsByRow(int rowNum) {
        MatrixCol cellConstraint = colHeaders[cellConstraintByRow(rowNum)];
        MatrixCol rowConstraint = colHeaders[rowConstraintByRow(rowNum)];
        MatrixCol colConstraint = colHeaders[colConstraintByRow(rowNum)];
        MatrixCol boxConstraint = colHeaders[boxConstraintByRow(rowNum)];

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
        MatrixCol cellConstraint = colHeaders[cellConstraintByRow(rowNum)];
        MatrixCol rowConstraint = colHeaders[rowConstraintByRow(rowNum)];
        MatrixCol colConstraint = colHeaders[colConstraintByRow(rowNum)];
        MatrixCol boxConstraint = colHeaders[boxConstraintByRow(rowNum)];

        cellConstraint.setStatus(true);
        rowConstraint.setStatus(true);
        colConstraint.setStatus(true);
        boxConstraint.setStatus(true);

        resetConstraintsByCol(cellConstraint);
        resetConstraintsByCol(rowConstraint);
        resetConstraintsByCol(colConstraint);
        resetConstraintsByCol(boxConstraint);
    }

    private void removeConstraintsByCol(MatrixCol colHeader) {
        Node activeConstraint = colHeader.getBelow();

        while (activeConstraint instanceof Constraint) {
            Constraint constraint = (Constraint) activeConstraint;

            if (!constraint.getRowHeader().isCageLocked()) {
                Constraint tempConstraint = (Constraint) constraint.getRight();

                while (tempConstraint != constraint) {
                    tempConstraint.detachNode();

                    tempConstraint = (Constraint) tempConstraint.getRight();
                }

                constraint.getRowHeader().setConstraintLock(true);
            }

            activeConstraint = activeConstraint.getBelow();
        }
    }

    private void resetConstraintsByCol(MatrixCol colHeader) {
        Node activeConstraint = colHeader.getBelow();

        while (activeConstraint instanceof Constraint) {
            Constraint constraint = (Constraint) activeConstraint;

            if (constraint.getRowHeader().isConstraintLocked()) {
                Constraint tempConstraint = (Constraint) constraint.getRight();

                while (tempConstraint != constraint) {
                    tempConstraint.reattachNode();

                    tempConstraint = (Constraint) tempConstraint.getRight();
                }

                constraint.getRowHeader().setConstraintLock(false);
            }

            activeConstraint = activeConstraint.getBelow();
        }
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

    // PRIVATE CLASSES
    private abstract class Node {
        private boolean isActive;

        protected Node right;
        protected Node above;
        protected Node below;

        public Node() {
            this.isActive = true;

            this.right = this;

            this.above = this;
            this.below = this;
        }

        public boolean isActive() {
            return this.isActive;
        }

        public Node getRight() {
            return this.right;
        }

        public Node getBelow() {
            return this.below;
        }

        public void setStatus(boolean newStatus) {
            this.isActive = newStatus;
        }

        public void setRight(Node newNode) {
            this.right = newNode;
        }

        public void setAbove(Node newNode) {
            this.above = newNode;
        }

        public void setBelow(Node newNode) {
            this.below = newNode;
        }
    }

    private class MatrixCol extends Node {
        private int colSum;

        public MatrixCol(int dimensions) {
            super();

            this.colSum = dimensions;
        }

        public void addVertical(Node node) {
            node.setAbove(this);
            node.setBelow(this.below);

            this.below.setAbove(node);
            this.below = node;
        }

        public int getColSum() {
            return this.colSum;
        }

        public void decrementSum() {
            this.colSum--;
        }

        public void incrementSum() {
            this.colSum++;
        }
    }

    private class MatrixRow extends Node {
        private int matrixRowNum;

        private boolean cageLock;
        private boolean constraintLock;

        public MatrixRow(int rowNum) {
            this.matrixRowNum = rowNum;
        }

        public int getGridRow(int gridDimensions) {
            return Math.floorDiv(this.matrixRowNum, gridDimensions * gridDimensions);
        }

        public int getGridCol(int gridDimensions) {
            return Math.floorDiv(this.matrixRowNum % (gridDimensions * gridDimensions), gridDimensions);
        }

        public int getGridDigit(int gridDimensions) {
            return this.matrixRowNum % gridDimensions;
        }

        public int getMatrixRowNum() {
            return this.matrixRowNum;
        }

        public boolean isCageLocked() {
            return this.cageLock;
        }

        public boolean isConstraintLocked() {
            return this.constraintLock;
        }

        public void setCageLock(boolean newStatus) {
            this.cageLock = newStatus;
        }

        public void setConstraintLock(boolean newStatus) {
            this.constraintLock = newStatus;
        }
    }

    private class Constraint extends Node {
        private MatrixRow rowHeader;
        private MatrixCol colHeader;

        public Constraint(MatrixRow rowHeader, MatrixCol colHeader) {
            this.rowHeader = rowHeader;
            this.colHeader = colHeader;
        }

        public void detachNode() {
            this.above.setBelow(this.below);
            this.below.setAbove(this.above);

            // super.setStatus(false);

            this.colHeader.decrementSum();
        }

        public void reattachNode() {
            this.above.setBelow(this);
            this.below.setAbove(this);

            // super.setStatus(true);

            this.colHeader.incrementSum();
        }

        public MatrixRow getRowHeader() {
            return this.rowHeader;
        }
    }
} // end of class KillerAdvancedSolver
