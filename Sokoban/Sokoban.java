// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2021T2, Assignment 1
 * Name: Ella Wipatene
 * Username: wipateella
 * ID: 300558005
 */

/*# To do list
   - Fix the challenge code!
   */

import ecs100.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;

/** 
 * Sokoban
 */

public class Sokoban {

    private Cell[][] cells;             // the array representing the warehouse
    private int rows;                   // the height of the warehouse
    private int cols;                   // the width of the warehouse
    private int level = 1;              // current level 

    private Position workerPos;         // the position of the worker
    private String workerDir = "left";  // the direction the worker is facing
    
    private Stack<ActionRecord> action_record = new Stack<ActionRecord>();  // Stack of all the previous actions
    private Stack<ActionRecord> action_redo = new Stack<ActionRecord>();  // Stack of all of the redo actions
    
    private int target_x, target_y;  // the target position that the user clicked. 


    /** 
     *  Constructor: load the 0th level.
     */
    public Sokoban() {
        doLoad();
    }

    /** 
     *  Moves the worker in the given direction, if possible.
     *  If there is box in front of the Worker and a space in front of the box,
     *  then push the box.
     *  Otherwise, if the worker can't move, do nothing.
     */
    public void moveOrPush(String direction) {
        workerDir = direction;                       // turn worker to face in this direction

        Position nextP = workerPos.next(direction);  // where the worker would move to
        Position nextNextP = nextP.next(direction);  // where a box would be pushed to

        // is there a box in that direction which can be pushed?
        if ( cells[nextP.row][nextP.col].hasBox() &&
        cells[nextNextP.row][nextNextP.col].isFree() ) { 
            push(direction);
            action_record.push(new ActionRecord("push", direction)); 
            if (isSolved()) { reportWin(); }
        }
        // is the next cell free for the worker to move into?
        else if ( cells[nextP.row][nextP.col].isFree() ) { 
            move(direction);
            action_record.push(new ActionRecord("", direction)); 
        }
    }

    /**
     * Moves the worker into the new position (guaranteed to be empty) 
     * @param direction the direction the worker is heading
     */
    public void move(String direction) {
        drawCell(workerPos);                   // redisplay cell under worker
        workerPos = workerPos.next(direction); // put worker in new position
        drawWorker();                          // display worker at new position

        Trace.println("Move " + direction);    // for debugging
    }

    /**
     * Push: Moves the Worker, pushing the box one step 
     *  @param direction the direction the worker is heading
     */
    public void push(String direction) {
        Position boxPos = workerPos.next(direction);   // where box is
        Position newBoxPos = boxPos.next(direction);   // where box will go

        cells[boxPos.row][boxPos.col].removeBox();     // remove box from current cell
        cells[newBoxPos.row][newBoxPos.col].addBox();  // place box in its new position

        drawCell(workerPos);                           // redisplay cell under worker
        drawCell(boxPos);                              // redisplay cell without the box
        drawCell(newBoxPos);                           // redisplay cell with the box

        workerPos = boxPos;                            // put worker in new position
        drawWorker();                                  // display worker at new position

        Trace.println("Push " + direction);   // for debugging
    }

    /**
     * Pull: (could be useful for undoing a push)
     *  move the Worker in the direction,
     *  pull the box into the Worker's old position
     */
    public void pull(String direction) {
        /*# YOUR CODE HERE */
        String opp_record = opposite(direction); 
        Position boxPos = workerPos.next(opp_record); // position of the box
        Position newBoxPos = workerPos;
        
        cells[boxPos.row][boxPos.col].removeBox();     // remove box from current cell
        cells[newBoxPos.row][newBoxPos.col].addBox();  // place box in its new position
        
        drawCell(workerPos);                           // redisplay cell under worker
        drawCell(boxPos);                              // redisplay cell without the box
        drawCell(newBoxPos);                           // redisplay cell with the box

        Trace.println("Push " + direction);   // for debugging
    }
    
    
    /**
     * Undo: undos the previous move by moving
     * the worker (and the box if previously moved)
     * back in the opp dirrection. 
     */
    public void undo(){
        if(!action_record.isEmpty()){
            ActionRecord action = action_record.pop(); // removes the top item
            action_redo.push(action); 
            // reverse action 
            String opp_action = opposite(action.direction()); 
            if (action.isPush() == true){
                pull(opp_action); 
                move(opp_action); 
            }else{
                move(opp_action); 
            }
        }
    }
    
    /**
     * Redo: undos the undo method
     */
    public void redo(){
        if(!action_redo.isEmpty()){
            ActionRecord redo_action = action_redo.pop(); 
            if (redo_action.isPush() == true){
                push(redo_action.direction()); 
            }else{
                move(redo_action.direction());
            }
            action_record.push(redo_action); 
        }
    }
    
    /**
     * Report a win by flickering the cells with boxes
     */
    public void reportWin(){
        for (int i=0; i<12; i++) {
            for (int row=0; row<cells.length; row++)
                for (int column=0; column<cells[row].length; column++) {
                    Cell cell=cells[row][column];

                    // toggle shelf cells
                    if (cell.hasBox()) {
                        cell.removeBox();
                        drawCell(row, column);
                    }
                    else if (cell.isEmptyShelf()) {
                        cell.addBox();
                        drawCell(row, column);
                    }
                }

            UI.sleep(100);
        }
    }

    /** 
     *  Returns true if the warehouse is solved, 
     *  i.e., all the shelves have boxes on them 
     */
    public boolean isSolved() {
        for(int row = 0; row<cells.length; row++) {
            for(int col = 0; col<cells[row].length; col++)
                if(cells[row][col].isEmptyShelf())
                    return  false;
        }

        return true;
    }

    /** 
     * Returns the direction that is opposite of the parameter
     * useful for undoing!
     */
    public String opposite(String direction) {
        if ( direction.equals("right")) return "left";
        if ( direction.equals("left"))  return "right";
        if ( direction.equals("up"))    return "down";
        if ( direction.equals("down"))  return "up";
        throw new RuntimeException("Invalid  direction");
    }


    // Drawing the warehouse
    private static final int LEFT_MARGIN = 40;
    private static final int TOP_MARGIN = 40;
    private static final int CELL_SIZE = 25;

    /**
     * Draw the grid of cells on the screen, and the Worker 
     */
    public void drawWarehouse() {
        UI.clearGraphics();
        // draw cells
        for(int row = 0; row<cells.length; row++)
            for(int col = 0; col<cells[row].length; col++)
                drawCell(row, col);

        drawWorker();
    }

    /**
     * Draw the cell at a given position
     */
    private void drawCell(Position pos) {
        drawCell(pos.row, pos.col);
    }

    /**
     * Draw the cell at a given row,col
     */
    private void drawCell(int row, int col) {
        double left = LEFT_MARGIN+(CELL_SIZE* col);
        double top = TOP_MARGIN+(CELL_SIZE* row);
        cells[row][col].draw(left, top, CELL_SIZE);
    }

    /**
     * Draw the worker at its current position.
     */
    private void drawWorker() {
        double left = LEFT_MARGIN+(CELL_SIZE* workerPos.col);
        double top = TOP_MARGIN+(CELL_SIZE* workerPos.row);
        UI.drawImage("worker-"+workerDir+".gif",
            left, top, CELL_SIZE,CELL_SIZE);
    }

    /**
     * Load a grid of cells (and Worker position) for the current level from a file
     */
    public void doLoad() {
        Path path = Path.of("warehouse" + level + ".txt");

        if (! Files.exists(path)) {
            UI.printMessage("Run out of levels!");
            level--;
        }
        else {
            List<String> lines = new ArrayList<String>();
            try {
                Scanner sc = new Scanner(path);
                while (sc.hasNext()){
                    lines.add(sc.nextLine());
                }
                sc.close();
            } catch(IOException e) {UI.println("File error: " + e);}
            
            rows = lines.size();
            cells = new Cell[rows][];

            for(int row = 0; row < rows; row++) {
                String line = lines.get(row);
                cols = line.length();
                cells[row]= new Cell[cols];
                for(int col = 0; col < cols; col++) {
                    char ch = line.charAt(col);
                    if (ch=='w'){
                        cells[row][col] = new Cell("empty");
                        workerPos = new Position(row,col);
                    }
                    else if (ch=='.') cells[row][col] = new Cell("empty");
                    else if (ch=='#') cells[row][col] = new Cell("wall");
                    else if (ch=='s') cells[row][col] = new Cell("shelf");
                    else if (ch=='b') cells[row][col] = new Cell("box");
                    else {
                        throw new RuntimeException("Invalid char at "+row+","+col+"="+ch);
                    }
                }
            }
            drawWarehouse();
            UI.printMessage("Level "+level+": Push the boxes to their target positions. Use buttons or put mouse over warehouse and use keys (arrows, wasd, ijkl, u)");
        }
    }
    
    /**
     *  doMouse: doMouse will find a path for the
     *  worker to the place where the user clicked if
     *  possible. 
     */
    public void doMouse(String action, double x, double y){
        if(action.equals("released")){
            this.target_x = (int)(x - LEFT_MARGIN)/CELL_SIZE;
            this.target_y = (int)(y - TOP_MARGIN)/CELL_SIZE; 
            
            if(target_x <  get_cells_max() && target_y < cells.length){ 
                if(cells[target_y][target_x].isFree()){
                    find_path(target_y, target_x); 
                } 
            }
        }
    }
    
    /**
     * Will work out the path to the location where the user clicked.
     * If it is possible, it will move it to that location. 
    */ 
    public void find_path(int target_y, int target_x){
        UI.println(" x = " + target_x);
        UI.println(" y = " + target_y); 
        
        UI.println(" x worker = " + workerPos.col);
        UI.println(" y worker = " + workerPos.row); 
        
        int x_diff = target_x - workerPos.col; 
        int temp_x = workerPos.col;
        int y_diff = target_y - workerPos.row; 
        int temp_y = workerPos.row; 
        
        while(target_x != workerPos.col || target_y != workerPos.row){
            if(x_diff != 0){
                if(target_x < workerPos.col){
                    temp_x -= 1; 
                }else{
                    temp_x += 1; 
                }
                if(cells[workerPos.row][temp_x].isFree()){
                    Position temp = new Position(workerPos.row, temp_x); 
                    Position old_worker = workerPos; 
                    workerPos = temp; 
                    x_diff = target_x - workerPos.col; 
                    drawWorker();
                    drawCell(old_worker); 
                }
                UI.println(cells[workerPos.row][temp_x].getType()); 
            }else if(y_diff != 0){
                if(target_y < workerPos.row){
                    temp_y -= 1;
                }else{
                    temp_y += 1;
                }
                if(cells[temp_y][workerPos.col].isFree()){
                    Position temp = new Position(temp_y, workerPos.col); 
                    Position old_worker = workerPos; 
                    workerPos = temp;
                    y_diff = target_y - workerPos.row; 
                    drawWorker();
                    drawCell(old_worker); 
                }
                UI.println(cells[temp_y][workerPos.col].getType());
            }
        }
    }
    
    /**
     * Returns the max numbers of cells 
     */
    public int get_cells_max(){
        int max = 0; 
        for (int i = 0; i < cells.length; i++){
            if(cells[i].length > max){
                max = cells[i].length; 
            }
        }
        return max; 
    }
    
    /** 
     * 
     */
    public boolean validCell(double x, double y){
        boolean valid = false; 
        // use rows and cols??? 
        for(int i = 0; i < cells.length; i++){
            for(int j = 0; j < cells[i].length; j++){
                //cells[i][j]; 
            
            }
        }
        return valid; 
    }
    
    /**
     * If possible, will calculate a path from the 
     * workers position to the location where the 
     * user clicked. 
     */
    public void find_target_path(){
    
    
    }
    
    /**
     * Add the buttons and set the key listener.
     */
    public void setupGUI(){
        UI.addButton("New Level", () -> {level++; doLoad();});
        UI.addButton("Restart",   this::doLoad);
        UI.addButton("left",      () -> {moveOrPush("left");});
        UI.addButton("up",        () -> {moveOrPush("up");});
        UI.addButton("down",      () -> {moveOrPush("down");});
        UI.addButton("right",     () -> {moveOrPush("right");});
        UI.addButton("Undo",      this::undo); 
        UI.addButton("Redo",      this::redo); 
        UI.addButton("Quit",      UI::quit);
        
        UI.setMouseListener(this::doMouse); 
        UI.setKeyListener(this::doKey);
        UI.setDivider(0.0);
    }

    /** 
     * Respond to key actions
     */
    public void doKey(String key) {
        key = key.toLowerCase();
        if (key.equals("i")|| key.equals("w") ||key.equals("up")) {
            moveOrPush("up");
        }
        else if (key.equals("k")|| key.equals("s") ||key.equals("down")) {
            moveOrPush("down");
        }
        else if (key.equals("j")|| key.equals("a") ||key.equals("left")) {
            moveOrPush("left");
        }
        else if (key.equals("l")|| key.equals("d") ||key.equals("right")) {
            moveOrPush("right");
        }
    }

    public static void main(String[] args) {
        Sokoban skb = new Sokoban();
        skb.setupGUI();
    }
}
