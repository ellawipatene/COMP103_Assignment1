// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2021T2, Assignment 1
 * Name: Ella Wipatene
 * Username: wipateella
 * ID: 300558005
 */

import ecs100.*;
import java.awt.Color;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * DeShredder allows a user to sort fragments of a shredded document ("shreds") into strips, and
 * then sort the strips into the original document.
 * The program shows
 *   - a list of all the shreds along the top of the window, 
 *   - the working strip (which the user is constructing) just below it.
 *   - the list of completed strips below the working strip.
 * The "rotate" button moves the first shred on the list to the end of the list to let the
 *  user see the shreds that have disappeared over the edge of the window.
 * The "shuffle" button reorders the shreds in the list randomly
 * The user can use the mouse to drag shreds between the list at the top and the working strip,
 *  and move shreds around in the working strip to get them in order.
 * When the user has the working strip complete, they can move
 *  the working strip down into the list of completed strips, and reorder the completed strips
 *
 */
public class DeShredder {

    // Fields to store the lists of Shreds and strips.  These should never be null.
    private List<Shred> allShreds = new ArrayList<Shred>();    //  List of all shreds
    private List<Shred> workingStrip = new ArrayList<Shred>(); // Current strip of shreds
    private List<List<Shred>> completedStrips = new ArrayList<List<Shred>>();

    // Constants for the display and the mouse
    public static final double LEFT = 20;       // left side of the display
    public static final double TOP_ALL = 20;    // top of list of all shreds 
    public static final double GAP = 5;         // gap between strips
    public static final double SIZE = Shred.SIZE; // size of the shreds

    public static final double TOP_WORKING = TOP_ALL+SIZE+GAP;
    public static final double TOP_STRIPS = TOP_WORKING+(SIZE+GAP);

    //Fields for recording where the mouse was pressed  (which list/strip and position in list)
    // note, the position may be past the end of the list!
    private List<Shred> fromStrip;   // The strip (List of Shreds) that the user pressed on
    private int fromPosition = -1;   // index of shred in the strip

    private double initial_y = 0;    // The initial mouse y cords
    private double initial_x = 0;    // The initial mouse x cords

    private boolean text_method = false;  // Determines which method is called 

    /**
     * Initialises the UI window, and sets up the buttons. 
     */
    public void setupGUI() {
        UI.addButton("Load library",   this::loadLibrary);
        UI.addButton("Rotate",         this::rotateList);
        UI.addButton("Shuffle",        this::shuffleList);
        UI.addButton("Complete Strip", this::completeStrip);
        UI.addButton("Save Completed Image", this::saveShred); 
        UI.addButton("Change recommended method", this::changeMethod); 
        UI.addButton("Quit",           UI::quit);

        UI.setMouseListener(this::doMouse);
        UI.setWindowSize(1000,800);
        UI.setDivider(0);
    }

    /**
     * Asks user for a library of shreds, loads it, and redisplays.
     * Uses UIFileChooser to let user select library
     * and finds out how many images are in the library
     * Calls load(...) to construct the List of all the Shreds
     */
    public void loadLibrary(){
        Path filePath = Path.of(UIFileChooser.open("Choose first shred in directory"));
        Path directory = filePath.getParent(); //subPath(0, filePath.getNameCount()-1);
        int count=1;
        while(Files.exists(directory.resolve(count+".png"))){ count++; }
        //loop stops when count.png doesn't exist
        count = count-1;
        load(directory, count);   // YOU HAVE TO COMPLETE THE load METHOD
        display();
    }

    /**
     * Empties out all the current lists (the list of all shreds,
     *  the working strip, and the completed strips).
     * Loads the library of shreds into the allShreds list.
     * Parameters are the directory containing the shred images and the number of shreds.
     * Each new Shred needs the directory and the number/id of the shred.
     */
    public void load(Path dir, int count) {
        /*# YOUR CODE HERE */
        allShreds.clear(); 
        workingStrip.clear();
        completedStrips.clear(); 
        int id = 1; 
        for (int i = 0; i < count; i++){
            allShreds.add(new Shred(dir, id)); 
            id++; 
        }

    }

    /**
     * Rotate the list of all shreds by one step to the left
     * and redisplay;
     * Should not have an error if the list is empty
     * (Called by the "Rotate" button)
     */
    public void rotateList(){
        /*# YOUR CODE HERE */
        if (allShreds.size() != 0){
            Shred temp = allShreds.get(0); 
            allShreds.remove(0); 
            allShreds.add(temp); 
        }else{
            UI.println("Please load in the shreds."); 
        }
        display(); 
    }

    /**
     * Shuffle the list of all shreds into a random order
     * and redisplay;
     */
    public void shuffleList(){
        /*# YOUR CODE HERE */
        if (allShreds.size() != 0){
            Collections.shuffle(allShreds);
        }else{
            UI.println("Please load in the shreds."); 
        }
        display(); 
    }

    /**
     * Move the current working strip to the end of the list of completed strips.
     * (Called by the "Complete Strip" button)
     */
    public void completeStrip(){
        /*# YOUR CODE HERE */
        if (workingStrip.size() != 0){
            completedStrips.add(workingStrip); 
            workingStrip = new ArrayList<Shred>(); 
            if (text_method == false){
                neighbours();
            } else{
                text_neighbours();
            }
            display(); 
        } else{
            UI.println("Working strip is empty"); 
        }
    }

    /**
     * Simple Mouse actions to move shreds and strips
     *  User can
     *  - move a Shred from allShreds to a position in the working strip
     *  - move a Shred from the working strip back into allShreds
     *  - move a Shred around within the working strip.
     *  - move a completed Strip around within the list of completed strips
     *  - move a completed Strip back to become the working strip
     *    (but only if the working strip is currently empty)
     * Moving a shred to a position past the end of a List should put it at the end.
     * You should create additional methods to do the different actions - do not attempt
     *  to put all the code inside the doMouse method - you will lose style points for this.
     * Attempting an invalid action should have no effect.
     * Note: doMouse uses getStrip and getColumn, which are written for you (at the end).
     * You should not change them.
     */
    public void doMouse(String action, double x, double y){
        if (action.equals("pressed")){
            fromStrip = getStrip(y);      // the List of shreds to move from (possibly null)
            fromPosition = getColumn(x);  // the index of the shred to move (may be off the end
            initial_y = y; 
            initial_x = x; 
        }
        if (action.equals("released")){
            boolean valid = click_valid(initial_x, initial_y, 0); 
            boolean valid2 = click_valid(x, y, 1); 
            //UI.println("valid one " + valid);
            //UI.println("valid two " + valid2); 
            if(valid == true && valid2 == true){
                List<Shred> toStrip = getStrip(y); // the List of shreds to move to (possibly null)
                int toPosition = getColumn(x);     // the index to move the shred to (may be off the end)
                // perform the correct action, depending on the from/to strips/positions

                // for testing purposes
                //for (Shred s: toStrip){
                //    UI.println("to strip: " + s);
                //}
                //UI.println("new x postition: " + toPosition);     

                if (initial_y > 110.0 && initial_y < completedStrips.size() * 45 + 110){ // if they want to move around the completed strips. 
                    if (y > 63.0 && y < 110.0){ // if they are trying to move it back to the work strip. 
                        if (workingStrip.size() == 0){
                            workingStrip = fromStrip;   
                            completedStrips.remove(fromStrip); 
                        }else{
                            UI.println("Sorry, the working stip is not empty."); 
                        }
                    } else{ // if they are trying to shuffle the completed stips 
                        int index_one = findStripIndex(initial_y);  // the from strip index 
                        int index_two = findStripIndex(y);  // the to strip index

                        List<Shred> temp = completedStrips.get(index_one);
                        completedStrips.set(index_one, completedStrips.get(index_two)); 
                        completedStrips.set(index_two, temp); 
                    }
                } else if (workingStrip.size() == 0 && initial_y > 63.0){ // if they are trying to move something from the working strip when it is empty. 
                    UI.println("Working strip is empty."); 
                }else{
                    Shred temp = fromStrip.get(fromPosition); 
                    fromStrip.remove(fromPosition); 
                    toStrip.add(toPosition, temp);
                }
                if (text_method == false){
                    neighbours();
                } else{
                    text_neighbours();
                } 
                display();    
            } 
        }
    }

    // Additional methods to perform the different actions, called by doMouse

    /*# YOUR CODE HERE */
    /**
     * Checks the location of the mouse clicks
     * Returns a boolean value on if the location is valid or not
     * Location is valid if it is on one of the existing shreds
     * (For the initial mouse click, 'from location')
     */
    public boolean click_valid(double x, double y, int from){
        boolean valid = false; 
        if (y > TOP_ALL && y < TOP_ALL + SIZE){ // For the all shreds strip
            if(x > LEFT && x < LEFT + (allShreds.size() + from) * SIZE){
                valid = true; 
            }
        } else if(y > TOP_ALL + GAP + SIZE && y < TOP_ALL + GAP + 2 * SIZE){ // For the working strip
            if(x > LEFT && x < (workingStrip.size() + from) * SIZE + LEFT){
                valid = true; 
            }
        } else if(y > TOP_ALL + GAP * 2 + SIZE * 2 && y < TOP_ALL + GAP * (2 + completedStrips.size()) + SIZE * (2 + completedStrips.size())){ // For completed strips
            for (int i = 0; i < completedStrips.size(); i++){
                if (x > LEFT && x < LEFT + completedStrips.get(i).size() * SIZE){
                    valid = true; 
                }
            }
        }

        return valid; 
    }

    /**
     * Returns the index of the completed strip
     * Based on the location of where the mouse was clicked
     */
    public int findStripIndex(double y){
        int index = 0; 
        y = y - 110; 
        index = (int) (y / SIZE);
        UI.println(index); 

        return index; 
    }

    /**
     * Lets the user change the neighbours method
     * based on if it is a picture or text
     */
    public void changeMethod(){
        if (text_method == true){
            text_method = false;
            neighbours(); 
        }else{
            text_method = true; 
            text_neighbours(); 
        }

    }

    /**
     * Finds possible matches for the last shed in the working station
     * Records the colours of the last shred in the working station 
     * Records the colours of all of the shreds in the allSheds array
     * Compares the colours of the two shreds
     * Highlights the shreds with the same colours
     * NOTE: This mainly only works for the coloured tiles, not so much the written text
     */  
    public void neighbours(){ 
        if (workingStrip.size() != 0){
            for (Shred s: allShreds){ // resets the neighbours 
                s.neighboursFalse(); 
            }

            int index = workingStrip.size() - 1; 
            Color[][] last_shred_image = loadImage(workingStrip.get(index).getFileName()); 

            HashSet<Color> last_shred_colours = new HashSet<Color>(); // HashSet so that there are no duplicates. 

            for (int i = 0; i < last_shred_image.length; i++){ 
                for (int j = 0; j < last_shred_image[i].length; j++){
                    last_shred_colours.add(last_shred_image[i][j]); 
                }
            } 

            // get the colours out off all of the left over shreds           
            for (Shred s: allShreds){
                Color[][] all_shred_images = loadImage(s.getFileName()); 
                HashSet<Color> shred_colours = new HashSet<Color>(); 

                for (int i = 0; i < all_shred_images.length; i++){
                    for (int j = 0; j < all_shred_images[i].length; j++){
                        shred_colours.add(all_shred_images[i][j]); 
                    }
                }

                // if the shred has more than one of the same colours (excluding white) it will be considered a match
                int match_counter = 0; 
                for (Color c : shred_colours){
                    if (last_shred_colours.contains(c)){  
                        match_counter++; 
                    }                
                }
                if (match_counter > 1){
                    s.neighboursTrue();
                }
            }
        }
        display(); 
    } 

    /**
     *  This method works better for text documents
     *  in addition to recording the different colours, it also records the y location 
     *  of the first time that the program experinces a colour other than white, therefore 
     *  locating how far the text is. 
     */
    public void text_neighbours(){
        if (workingStrip.size() != 0){
            for (Shred s: allShreds){ // resets the neighbours 
                s.neighboursFalse(); 
            }

            int index = workingStrip.size() - 1; 
            Color[][] last_shred_image = loadImage(workingStrip.get(index).getFileName()); 

            HashSet<Color> last_shred_colours = new HashSet<Color>(); // HashSet so that there are no duplicates. 
            int first_y = 0; 

            for (int i = 0; i < last_shred_image.length; i++){ 
                for (int j = 0; j < last_shred_image[i].length; j++){
                    if(!last_shred_image[i][j].equals(Color.white) && first_y == 0){
                        first_y = i;
                    }
                    last_shred_colours.add(last_shred_image[i][j]); 
                }
            } 

            // get the colours out off all of the left over shreds           
            for (Shred s: allShreds){
                Color[][] all_shred_images = loadImage(s.getFileName()); 
                HashSet<Color> shred_colours = new HashSet<Color>(); 

                int second_y = 0; 

                for (int i = 0; i < all_shred_images.length; i++){
                    for (int j = 0; j < all_shred_images[i].length; j++){
                        if(!all_shred_images[i][j].equals(Color.white) && second_y == 0){
                            second_y = i;
                        }
                        shred_colours.add(all_shred_images[i][j]); 
                    }
                }

                // if the shred has more than one of the same colours (excluding white) it will be considered a match
                int match_counter = 0; 
                for (Color c : shred_colours){
                    if (last_shred_colours.contains(c)){  
                        match_counter++; 
                    }                
                }

                if(first_y + 4 > second_y && first_y - 4 < second_y){
                    s.neighboursTrue(); 
                }else if (match_counter > 2){
                    //s.neighboursTrue();
                }
            }
        }
        display(); 
    }

    /**
     * Save shreds saves all of the shreds in the completed strips as an image
     */
    public void saveShred(){
        if (!completedStrips.isEmpty()){
            int rows = completedStrips.size(); 
            int cols = get_max_cols(); // maximum amount of colums  
            Color[][] white_square = getWhiteSquare(); // gets a 40 by 40 array full of white pixels 
            Color[][] final_image = new Color[rows*40][cols*40];

            for (int i = 0; i < rows; i++){
                int col_counter = completedStrips.get(i).size(); 
                for(int j = 0; j < cols; j++){
                    if (j >= col_counter){
                        for(int m = 0; m < 40; m++){
                            for(int n = 0; n < 40; n++){
                                final_image[i*40 + m][j*40 + n] = white_square[m][n]; 
                            }
                        }
                    } else{
                        Color[][] shred_array = loadImage(completedStrips.get(i).get(j).getFileName()); 
                        for(int m = 0; m < 40; m++){
                            for(int n = 0; n < 40; n++){
                                final_image[i*40 + m][j*40 + n] = shred_array[m][n];
                            }
                        }
                    }
                }
            }

            String fileName = UI.askString("File Name: "); 
            saveImage(final_image, fileName);
        }
    }

    /**
     * Returns a 40 by 40 array list of white colour values
     * Used in the save image method
     */
    public Color[][] getWhiteSquare(){
        Color[][] white_square = new Color[40][40];
        for(int i = 0; i < 40; i++){
            for(int j = 0; j < 40; j++){
                white_square[i][j] = Color.white; 
            }
        }
        return white_square; 
    }

    /**
     * Finds the maximum amount of columns in the completed strips
     */
    public int get_max_cols(){
        int max = 0; 
        for(int i = 0; i < completedStrips.size(); i++){
            if(completedStrips.get(i).size() > max){
                max = completedStrips.get(i).size(); 
            }
        }
        return max; 
    }

    //=============================================================================
    // Completed for you. Do not change.
    // loadImage and saveImage may be useful for the challenge.

    /**
     * Displays the remaining Shreds, the working strip, and all completed strips
     */
    public void display(){
        UI.clearGraphics();

        // list of all the remaining shreds that haven't been added to a strip
        double x=LEFT;
        for (Shred shred : allShreds){
            if (shred.neighbours == true){
                UI.setColor(Color.green); 
            }else{
                UI.setColor(Color.black);
            }
            shred.drawWithBorder(x, TOP_ALL);
            x+=SIZE;
        }

        //working strip (the one the user is workingly working on)
        x=LEFT;
        for (Shred shred : workingStrip){
            shred.draw(x, TOP_WORKING);
            x+=SIZE;
        }
        UI.setColor(Color.red);
        UI.drawRect(LEFT-1, TOP_WORKING-1, SIZE*workingStrip.size()+2, SIZE+2);
        UI.setColor(Color.black);

        //completed strips
        double y = TOP_STRIPS;
        for (List<Shred> strip : completedStrips){
            x = LEFT;
            for (Shred shred : strip){
                shred.draw(x, y);
                x+=SIZE;
            }
            UI.drawRect(LEFT-1, y-1, SIZE*strip.size()+2, SIZE+2);
            y+=SIZE+GAP;
        }
    }

    /**
     * Returns which column the mouse position is on.
     * This will be the index in the list of the shred that the mouse is on, 
     * (or the index of the shred that the mouse would be on if the list were long enough)
     */
    public int getColumn(double x){
        return (int) ((x-LEFT)/(SIZE));
    }

    /**
     * Returns the strip that the mouse position is on.
     * This may be the list of all remaining shreds, the working strip, or
     *  one of the completed strips.
     * If it is not on any strip, then it returns null.
     */
    public List<Shred> getStrip(double y){
        int row = (int) ((y-TOP_ALL)/(SIZE+GAP));
        if (row<=0){
            return allShreds;
        }
        else if (row==1){
            return workingStrip;
        }
        else if (row-2<completedStrips.size()){
            return completedStrips.get(row-2);
        }
        else {
            return null;
        }
    }

    public static void main(String[] args) {
        DeShredder ds =new DeShredder();
        ds.setupGUI();

    }

    /**
     * Load an image from a file and return as a two-dimensional array of Color.
     * From COMP 102 assignment 8&9.
     * Maybe useful for the challenge. Not required for the core or completion.
     */
    public Color[][] loadImage(String imageFileName) {
        if (imageFileName==null || !Files.exists(Path.of(imageFileName))){
            return null;
        }
        try {
            BufferedImage img = ImageIO.read(Files.newInputStream(Path.of(imageFileName)));
            int rows = img.getHeight();
            int cols = img.getWidth();
            Color[][] ans = new Color[rows][cols];
            for (int row = 0; row < rows; row++){
                for (int col = 0; col < cols; col++){                 
                    Color c = new Color(img.getRGB(col, row));
                    ans[row][col] = c;
                }
            }
            return ans;
        } catch(IOException e){UI.println("Reading Image from "+imageFileName+" failed: "+e);}
        return null;
    }

    /**
     * Save a 2D array of Color as an image file
     * From COMP 102 assignment 8&9.
     * Maybe useful for the challenge. Not required for the core or completion.
     */
    public  void saveImage(Color[][] imageArray, String imageFileName) {
        int rows = imageArray.length;
        int cols = imageArray[0].length;
        BufferedImage img = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Color c =imageArray[row][col];
                img.setRGB(col, row, c.getRGB());
            }
        }
        try {
            if (imageFileName==null) { return;}
            ImageIO.write(img, "png", Files.newOutputStream(Path.of(imageFileName)));
        } catch(IOException e){UI.println("Image reading failed: "+e);}

    }

}
