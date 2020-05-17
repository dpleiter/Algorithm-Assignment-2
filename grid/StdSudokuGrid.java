/**
 * @author Jeffrey Chan & Minyi Li, RMIT 2020
 */
package grid;

import java.io.*;
import java.util.*;

/**
 * Class implementing the grid for standard Sudoku. Extends SudokuGrid (hence
 * implements all abstract methods in that abstract class). You will need to
 * complete the implementation for this for task A and subsequently use it to
 * complete the other classes. See the comments in SudokuGrid to understand what
 * each overriden method is aiming to do (and hence what you should aim for in
 * your implementation).
 */
public class StdSudokuGrid extends SudokuGrid {
    // TODO: Add your own attributes
    BufferedReader file;
    int[][] grid;
    Set<Integer> digits = new HashSet<Integer>();

    public StdSudokuGrid() {
        super();

        // TODO: any necessary initialisation at the constructor
    } // end of StdSudokuGrid()

    /* ********************************************************* */

    @Override
    public void initGrid(String filename) throws FileNotFoundException, IOException {
        // TODO: Throw exceptions when neccesary
        BufferedReader file = new BufferedReader(new FileReader(filename));
        String inputLine;
        String[] cellDetail = new String[2];
        String[] cellCoords;
        String cellValue;

        // dimesnion of grid
        inputLine = file.readLine();
        int gridDimensions = Integer.parseInt(inputLine);

        grid = new int[gridDimensions][gridDimensions];
        System.out.println("Initialised grid with dimensions " + gridDimensions + "x" + gridDimensions);

        inputLine = file.readLine();

        for (String digit : inputLine.split(" ")) {
            System.out.println("Adding digit " + digit + " to set");
            digits.add(Integer.parseInt(digit));
        }

        inputLine = file.readLine();

        while (inputLine != null) {
            cellDetail = inputLine.split(" ");

            cellCoords = cellDetail[0].split(",");
            cellValue = cellDetail[1];

            System.out.println("Cell at coordinate (" + cellCoords[0] + "," + cellCoords[1] + ") = " + cellValue);
            grid[Integer.parseInt(cellCoords[0])][Integer.parseInt(cellCoords[1])] = Integer.parseInt(cellValue);

            inputLine = file.readLine();
        }

        file.close();
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

} // end of class StdSudokuGrid
