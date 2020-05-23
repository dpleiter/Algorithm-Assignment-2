/**
 * @author Jeffrey Chan & Minyi Li, RMIT 2020
 */
package grid;

import java.io.*;
import java.util.*;

/**
 * Class implementing the grid for Killer Sudoku. Extends SudokuGrid (hence
 * implements all abstract methods in that abstract class). You will need to
 * complete the implementation for this for task E and subsequently use it to
 * complete the other classes. See the comments in SudokuGrid to understand what
 * each overriden method is aiming to do (and hence what you should aim for in
 * your implementation).
 */
public class KillerSudokuGrid extends SudokuGrid {
    public int[][] grid;
    private ArrayList<Integer> digits = new ArrayList<Integer>();
    int gridDimensions;

    public KillerSudokuGrid() {
        super();

        // TODO: any necessary initialisation at the constructor
    } // end of KillerSudokuGrid()

    /* ********************************************************* */

    @Override
    public void initGrid(String filename) throws FileNotFoundException, IOException {
        // TODO
    } // end of initBoard()

    @Override
    public void outputGrid(String filename) throws FileNotFoundException, IOException {
        // TODO
    } // end of outputBoard()

    @Override
    public String toString() {
        // TODO

        // placeholder
        return String.valueOf("");
    } // end of toString()

    @Override
    public boolean validate() {
        // TODO

        // placeholder
        return false;
    } // end of validate()

    @Override
    public boolean checkComplete() {

        return false;
    }

    @Override
    public int[][] getGrid() {
        return grid;
    }

    @Override
    public ArrayList<Integer> getDigits() {
        return this.digits;
    }

    @Override
    public int getSize() {
        return this.gridDimensions;
    }

    @Override
    public int getCellValue(int row, int col) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setCell(int row, int col, int value) {

    }

} // end of class KillerSudokuGrid
