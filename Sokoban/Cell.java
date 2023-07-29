// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2021T2, Assignment 1
 * Name: Ella Wipatene
 * Username: Wipateella
 * ID: 300558005
 */

import ecs100.*;

/** 
 * A Cell is a single square in a Sokoban game.
 * It can be one of five things:
 *  an "empty" cell
 *  a "wall" 
 *  a cell with a "box" 
 *  a cell with a "shelf" 
 *  a cell with a "box on a shelf"
 * It has several useful methods 
 */

public class Cell {
    private String type;
    

    /**
     * Constructor. The type must be one of the valid types of cells. 
     */
    public Cell(String t){
        if (t.equals("empty") || t.equals("shelf") || t.equals("wall") ||
        t.equals("box") || t.equals("boxOnShelf")){
            type = t;
        }
        else {
            throw new RuntimeException("Invalid Cell type!");
        }
    }
    
    /**
     * returns the type of cell
     */
    public String getType(){
        return type; 
    }

    /**
     * Is this cell a shelf without a box? 
     */
    public boolean isEmptyShelf() {
        return type.equals("shelf");
    }

    /**
     * Does this cell have a box in it? 
     */
    public boolean hasBox() {
        return (type.contains("box"));
    }

    /**
     * Is the cell is free to move onto (not a wall or a box)
     */
    public boolean isFree() {
        return (type.equals("empty") || type.equals("shelf"));
    }

    /**
     * Change the type of the cell to not have a box in it  
     */
    public void removeBox() {
        if (type.equals("box")) { type = "empty"; }
        else if (type.equals("boxOnShelf")) { type = "shelf"; }
    }

    /**
     * Change the type of the cell to contain a box
     */
    public void addBox() {
        if (type.equals("empty")) { type = "box"; }
        else if (type.equals("shelf")) { type = "boxOnShelf"; }
    }

    /**
     * Draw the cell at the specified position and size
     */
    public void draw(double left, double top, double size) {
        UI.drawImage((type +".gif"), left, top, size, size);
    }

}
