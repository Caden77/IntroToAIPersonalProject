
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.*;
import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Vector;

import javax.lang.model.type.NullType;
import javax.swing.JComponent;
import javax.swing.JFrame;


// This class draws the probability map and value iteration map that you create to the window
// You need only call updateProbs() and updateValues() from your theRobot class to update these maps
class mySmartMap extends JComponent implements KeyListener {
    public static final int NORTH = 0;
    public static final int SOUTH = 1;
    public static final int EAST = 2;
    public static final int WEST = 3;
    public static final int STAY = 4;

    int currentKey;

    int winWidth, winHeight;
    double sqrWdth, sqrHght;
    Color gris = new Color(170,170,170);
    Color myWhite = new Color(220, 220, 220);
    World mundo;
    
    int gameStatus;

    double[][] probs;
    double[][] vals;
    
    public mySmartMap(int w, int h, World wld) {
        mundo = wld;
        probs = new double[mundo.width][mundo.height];
        vals = new double[mundo.width][mundo.height];
        winWidth = w;
        winHeight = h;
        
        sqrWdth = (double)w / mundo.width;
        sqrHght = (double)h / mundo.height;
        currentKey = -1;
        
        addKeyListener(this);
        
        gameStatus = 0;
    }
    
    public void addNotify() {
        super.addNotify();
        requestFocus();
    }
    
    public void setWin() {
        gameStatus = 1;
        repaint();
    }
    
    public void setLoss() {
        gameStatus = 2;
        repaint();
    }
    
    public void updateProbs(double[][] _probs) {
        for (int y = 0; y < mundo.height; y++) {
            for (int x = 0; x < mundo.width; x++) {
                probs[x][y] = _probs[x][y];
            }
        }
        
        repaint();
    }
    
    public void updateValues(double[][] _vals) {
        for (int y = 0; y < mundo.height; y++) {
            for (int x = 0; x < mundo.width; x++) {
                vals[x][y] = _vals[x][y];
            }
        }
        
        repaint();
    }

    public void paint(Graphics g) {
        paintProbs(g);
        //paintValues(g);
    }

    public void paintProbs(Graphics g) {
        double maxProbs = 0.0;
        int mx = 0, my = 0;
        for (int y = 0; y < mundo.height; y++) {
            for (int x = 0; x < mundo.width; x++) {
                if (probs[x][y] > maxProbs) {
                    maxProbs = probs[x][y];
                    mx = x;
                    my = y;
                }
                if (mundo.grid[x][y] == 1) {
                    g.setColor(Color.black);
                    g.fillRect((int)(x * sqrWdth), (int)(y * sqrHght), (int)sqrWdth, (int)sqrHght);
                }
                else if (mundo.grid[x][y] == 0) {
                    //g.setColor(myWhite);
                    
                    int col = (int)(255 * Math.sqrt(probs[x][y]));
                    if (col > 255)
                        col = 255;
                    g.setColor(new Color(255-col, 255-col, 255));
                    g.fillRect((int)(x * sqrWdth), (int)(y * sqrHght), (int)sqrWdth, (int)sqrHght);
                }
                else if (mundo.grid[x][y] == 2) {
                    g.setColor(Color.red);
                    g.fillRect((int)(x * sqrWdth), (int)(y * sqrHght), (int)sqrWdth, (int)sqrHght);
                }
                else if (mundo.grid[x][y] == 3) {
                    g.setColor(Color.green);
                    g.fillRect((int)(x * sqrWdth), (int)(y * sqrHght), (int)sqrWdth, (int)sqrHght);
                }
            
            }
            if (y != 0) {
                g.setColor(gris);
                g.drawLine(0, (int)(y * sqrHght), (int)winWidth, (int)(y * sqrHght));
            }
        }
        for (int x = 0; x < mundo.width; x++) {
                g.setColor(gris);
                g.drawLine((int)(x * sqrWdth), 0, (int)(x * sqrWdth), (int)winHeight);
        }
        
        //System.out.println("repaint maxProb: " + maxProbs + "; " + mx + ", " + my);
        
        g.setColor(Color.green);
        g.drawOval((int)(mx * sqrWdth)+1, (int)(my * sqrHght)+1, (int)(sqrWdth-1.4), (int)(sqrHght-1.4));
        
        if (gameStatus == 1) {
            g.setColor(Color.green);
            g.drawString("You Won!", 8, 25);
        }
        else if (gameStatus == 2) {
            g.setColor(Color.red);
            g.drawString("You're a Loser!", 8, 25);
        }
    }
    
    public void paintValues(Graphics g) {
        double maxVal = -99999, minVal = 99999;
        int mx = 0, my = 0;
        
        for (int y = 0; y < mundo.height; y++) {
            for (int x = 0; x < mundo.width; x++) {
                if (mundo.grid[x][y] != 0)
                    continue;
                
                if (vals[x][y] > maxVal)
                    maxVal = vals[x][y];
                if (vals[x][y] < minVal)
                    minVal = vals[x][y];
            }
        }
        if (minVal == maxVal) {
            maxVal = minVal+1;
        }

        int offset = winWidth+20;
        for (int y = 0; y < mundo.height; y++) {
            for (int x = 0; x < mundo.width; x++) {
                if (mundo.grid[x][y] == 1) {
                    g.setColor(Color.black);
                    g.fillRect((int)(x * sqrWdth)+offset, (int)(y * sqrHght), (int)sqrWdth, (int)sqrHght);
                }
                else if (mundo.grid[x][y] == 0) {
                    //g.setColor(myWhite);
                    
                    //int col = (int)(255 * Math.sqrt((vals[x][y]-minVal)/(maxVal-minVal)));
                    int col = (int)(255 * (vals[x][y]-minVal)/(maxVal-minVal));
                    if (col > 255)
                        col = 255;
                    g.setColor(new Color(255-col, 255-col, 255));
                    g.fillRect((int)(x * sqrWdth)+offset, (int)(y * sqrHght), (int)sqrWdth, (int)sqrHght);
                }
                else if (mundo.grid[x][y] == 2) {
                    g.setColor(Color.red);
                    g.fillRect((int)(x * sqrWdth)+offset, (int)(y * sqrHght), (int)sqrWdth, (int)sqrHght);
                }
                else if (mundo.grid[x][y] == 3) {
                    g.setColor(Color.green);
                    g.fillRect((int)(x * sqrWdth)+offset, (int)(y * sqrHght), (int)sqrWdth, (int)sqrHght);
                }
            
            }
            if (y != 0) {
                g.setColor(gris);
                g.drawLine(offset, (int)(y * sqrHght), (int)winWidth+offset, (int)(y * sqrHght));
            }
        }
        for (int x = 0; x < mundo.width; x++) {
                g.setColor(gris);
                g.drawLine((int)(x * sqrWdth)+offset, 0, (int)(x * sqrWdth)+offset, (int)winHeight);
        }
    }

    
    public void keyPressed(KeyEvent e) {
        //System.out.println("keyPressed");
    }
    public void keyReleased(KeyEvent e) {
        //System.out.println("keyReleased");
    }
    public void keyTyped(KeyEvent e) {
        char key = e.getKeyChar();
        //System.out.println(key);
        
        switch (key) {
            case 'i':
                currentKey = NORTH;
                break;
            case ',':
                currentKey = SOUTH;
                break;
            case 'j':
                currentKey = WEST;
                break;
            case 'l':
                currentKey = EAST;
                break;
            case 'k':
                currentKey = STAY;
                break;
        }
    }
}


// This is the main class that you will add to in order to complete the lab
public class theRobot extends JFrame {
    // Mapping of actions to integers
    public static final int NORTH = 0;
    public static final int SOUTH = 1;
    public static final int EAST = 2;
    public static final int WEST = 3;
    public static final int STAY = 4;

    Color bkgroundColor = new Color(230,230,230);
    
    static mySmartMap myMaps; // instance of the class that draw everything to the GUI
    String mundoName;
    
    World mundo; // mundo contains all the information about the world.  See World.java
    double moveProb, sensorAccuracy;  // stores probabilies that the robot moves in the intended direction
                                      // and the probability that a sonar reading is correct, respectively
    
    // variables to communicate with the Server via sockets
    public Socket s;
	public BufferedReader sin;
	public PrintWriter sout;
    
    // variables to store information entered through the command-line about the current scenario
    boolean isManual = false; // determines whether you (manual) or the AI (automatic) controls the robots movements
    boolean knownPosition = false;
    int startX = -1, startY = -1;
    int decisionDelay = 250;
    
    // store your probability map (for position of the robot in this array
    double[][] probs;
    
    // store your computed value of being in each state (x, y)
    double[][] Vs;

    //------------------- Personal Project -------------------//

    // Store which path finding algorithm we are using
    String pathFindAlg;

    //Store precomputed actions for the current board if needed
    int[][] compActs; //Compiled actions





    // // store the actions to take if lost (don't know our location)
    // double[][] lostActions;
    
    public theRobot(String _manual, int _decisionDelay, String _pathFindAlg) {
        // initialize variables as specified from the command-line
        if (_manual.equals("automatic"))
            isManual = false;
        else
            isManual = true;
        decisionDelay = _decisionDelay;

        this.pathFindAlg = _pathFindAlg;
        
        // get a connection to the server and get initial information about the world
        initClient();
    
        // Read in the world
        mundo = new World(mundoName);
        
        // set up the GUI that displays the information you compute
        int width = 500;
        int height = 500;
        int bar = 20;
        setSize(width,height+bar);
        getContentPane().setBackground(bkgroundColor);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(0, 0, width, height+bar);
        myMaps = new mySmartMap(width, height, mundo);
        getContentPane().add(myMaps);
        
        setVisible(true);
        setTitle("Probability and Value Maps");
        
        //Gain control of the robot
        startRobotControler(); // Function to have the robot move about its world until it gets to its goal or falls in a stairwell
    }
    
    // this function establishes a connection with the server and learns
    //   1 -- which world it is in
    //   2 -- it's transition model (specified by moveProb)
    //   3 -- it's sensor model (specified by sensorAccuracy)
    //   4 -- whether it's initial position is known.  if known, its position is stored in (startX, startY)
    public void initClient() {
        int portNumber = 3333;
        String host = "localhost";
        
        try {
			s = new Socket(host, portNumber);
            sout = new PrintWriter(s.getOutputStream(), true);
			sin = new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            mundoName = sin.readLine();
            moveProb = Double.parseDouble(sin.readLine());
            sensorAccuracy = Double.parseDouble(sin.readLine());
            System.out.println("Need to open the mundo: " + mundoName);
            System.out.println("moveProb: " + moveProb);
            System.out.println("sensorAccuracy: " + sensorAccuracy);
            
            // find out of the robots position is know
            String _known = sin.readLine();
            if (_known.equals("known")) {
                knownPosition = true;
                startX = Integer.parseInt(sin.readLine());
                startY = Integer.parseInt(sin.readLine());
                System.out.println("Robot's initial position is known: " + startX + ", " + startY);
            }
            else {
                System.out.println("Robot's initial position is unknown");
            }
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
    }

    // function that gets human-specified actions
    // 'i' specifies the movement up
    // ',' specifies the movement down
    // 'l' specifies the movement right
    // 'j' specifies the movement left
    // 'k' specifies the movement stay
    int getHumanAction() {
        System.out.println("Reading the action selected by the user");
        while (myMaps.currentKey < 0) {
            try {
                Thread.sleep(50);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        int a = myMaps.currentKey;
        myMaps.currentKey = -1;
        
        System.out.println("Action: " + a);
        
        return a;
    }
    
    // initializes the probabilities of where the AI is
    void initializeProbabilities() {
        probs = new double[mundo.width][mundo.height];
        // if the robot's initial position is known, reflect that in the probability map
        if (knownPosition) {
            for (int y = 0; y < mundo.height; y++) {
                for (int x = 0; x < mundo.width; x++) {
                    if ((x == startX) && (y == startY))
                        probs[x][y] = 1.0;
                    else
                        probs[x][y] = 0.0;
                }
            }
        }
        else {  // otherwise, set up a uniform prior over all the positions in the world that are open spaces
            int count = 0;
            
            for (int y = 0; y < mundo.height; y++) {
                for (int x = 0; x < mundo.width; x++) {
                    if (mundo.grid[x][y] == 0)
                        count++;
                }
            }
            
            for (int y = 0; y < mundo.height; y++) {
                for (int x = 0; x < mundo.width; x++) {
                    if (mundo.grid[x][y] == 0)
                        probs[x][y] = 1.0 / count;
                    else
                        probs[x][y] = 0;
                }
            }
        }
        
        myMaps.updateProbs(probs);
    }

    double[][] deepCopyProbabilities(double[][] currProbs) {
        double[][] newProbs = new double[currProbs.length][currProbs[0].length];
        for (int c = 0; c < currProbs.length; c++) {
            for (int r = 0; r < currProbs[0].length; r++) {
                newProbs[c][r] = currProbs[c][r];
            }
        }
        return newProbs;
    }

    double[][] predictTransition(double[][] currProbs, int action) {
        double[][] probsPredicted = deepCopyProbabilities(currProbs);
        //System.out.printf("using a move probability of: %f\n", moveProb);

        //go through the array by column's first to avoid confusion
        for (int c = 0; c < probsPredicted.length; c++) {
            for (int r = 0; r < probsPredicted[0].length; r++) {
                //go through all probabilities
                //System.out.printf("on 'row': %d 'col': %d\n", r, c);

                //if this square is not a space we can occupy then skip it as a movement option (it would be the end of the game otherwise)
                if (mundo.grid[c][r] != 0) {
                    //System.out.println("skipped");
                    continue;
                }
                
                //for each individual tile calculate the probability of landing ON the tile next
                // based upon surrounding spaces
                // there are two movement options for each individual action:
                // * movement from an empty nearby space to the current space
                // * movement on the current space into an ajacent wall, resulting in staying on the current space
                //store direction based on index of what will get on this tile: 
                //coming from action of... north, south, east, west, stay
                int[] surrSpaces = {r + 1, c, r - 1, c, r, c - 1, r, c + 1, r, c};
                double predictionSum = 0;
                for (int spaceI = 0; spaceI < surrSpaces.length / 2; spaceI += 1) {
                    int spaceR = surrSpaces[(spaceI * 2)];
                    int spaceC = surrSpaces[(spaceI * 2) + 1];
                    int rowDif = spaceR - r;
                    int colDif = spaceC - c;
                    int wallR = r - rowDif;
                    int wallC = c - colDif;

                    //check for invalid spaces (i.e out of bounds) and empty spaces (not coming from the finish or death spaces or walls)
                    if ((spaceR < 0 || spaceR >= mundo.height) ||
                            (spaceC < 0 || spaceC >= mundo.width) ||
                            (mundo.grid[spaceC][spaceR] != 0)) {
                        //continue onto next test
                    } else {
                        //else add to the probability sum for this transition from an empty space
                        double transitionProb;
                        if (spaceI == action) {
                            transitionProb = moveProb;
                        } else {
                            transitionProb = (1 - moveProb) / 4;
                        }
                        predictionSum += probs[spaceC][spaceR] * transitionProb;
                    }
                    
                    //consider running into a wall
                    if ((wallR < 0 || wallR >= mundo.height) ||
                    (wallC < 0 || wallC >= mundo.width) ||
                    (mundo.grid[wallC][wallR] != 1)) {
                        //if the next space to move into is not a wall don't do anything
                    } else {
                        //moving into a wall, if in the direction of the wanted action then give it a higher probability
                        double transitionProb;
                        if (spaceI == action) {
                            transitionProb = moveProb;
                        } else {
                            transitionProb = (1 - moveProb) / 4;
                        }
                        predictionSum += probs[c][r] * transitionProb; //go off of the current probability for this square
                        //should look like the previous probability adds to the current probability
                    }
                }

                probsPredicted[c][r] = predictionSum;
            }
        }

        return probsPredicted;
    }

    double[][] observeSensorProbabilities(double[][] currProbs, String sonars) {
        double[][] probsPredicted = currProbs.clone();

        //for every spot (column first)
        for (int c = 0; c < probsPredicted.length; c++) {
            for (int r = 0; r < probsPredicted[0].length; r++) {
                //if not at an empty square (i.e. a wall finish or death space) skip it
                if (mundo.grid[c][r] != 0) {
                    continue;
                }

                double probCorrect = probsPredicted[c][r];
                //go through each sensor
                //multiply the probability that the sonar is correct
                //check for upwards for a wall
                if (((sonars.charAt(0) == '1') && (r - 1 >= 0 && mundo.grid[c][r - 1] == 1)) ||
                        ((sonars.charAt(0) == '0') && (r - 1 >= 0 && mundo.grid[c][r - 1] != 1))) {
                    probCorrect *= sensorAccuracy;
                } else {
                    probCorrect *= (1 - sensorAccuracy);
                }
                //down
                if (((sonars.charAt(1) == '1') && (r + 1 < probsPredicted[0].length && mundo.grid[c][r + 1] == 1)) ||
                        ((sonars.charAt(1) == '0') && (r + 1 < probsPredicted[0].length && mundo.grid[c][r + 1] != 1))) {
                    probCorrect *= sensorAccuracy;
                } else {
                    probCorrect *= (1 - sensorAccuracy);
                }
                //right
                if (((sonars.charAt(2) == '1') && (c + 1 < probsPredicted.length && mundo.grid[c + 1][r] == 1)) ||
                        ((sonars.charAt(2) == '0') && (c + 1 < probsPredicted.length && mundo.grid[c + 1][r] != 1))) {
                    probCorrect *= sensorAccuracy;
                } else {
                    probCorrect *= (1 - sensorAccuracy);
                }
                //left
                if (((sonars.charAt(3) == '1') && (c - 1 >= 0 && mundo.grid[c - 1][r] == 1)) || 
                        ((sonars.charAt(3) == '0') && (c - 1 >= 0 && mundo.grid[c - 1][r] != 1))) {
                    probCorrect *= sensorAccuracy;
                } else {
                    probCorrect *= (1 - sensorAccuracy);
                }

                probsPredicted[c][r] = probCorrect;
                //System.out.println("probCorrect: " + probCorrect);
            }
        }

        return probsPredicted;
    }

    double[][] normalizeProbabilities(double[][] currProb) {
        double[][] probsPredicted = deepCopyProbabilities(currProb);
        //printProbabilityBoard(currProb);
        double sum = 0;
        for (int c = 0; c < currProb.length; c++) {
            for (int r = 0; r < currProb[0].length; r++) {
                sum += currProb[c][r];
            }
        }
        // System.out.println("sum: " + sum);
        for (int c = 0; c < currProb.length; c++) {
            for (int r = 0; r < currProb[0].length; r++) {
                if (probsPredicted[c][r] != 0.0) {
                    probsPredicted[c][r] = probsPredicted[c][r] / sum;
                }
            }
        }
        return probsPredicted;
    }

    void printProbabilityBoard(double[][] board) {
        //print probsPredicted
        //column dominant
        for (int r = 0; r < board[0].length; r++) {
            for (int c = 0; c < board.length; c++) {
                System.out.printf("%2.2f\t ", board[c][r]);
            }
            System.out.println();
        }
    }

    void printActionBoard(int[][] board) {
        for (int r = 0; r < board[0].length; r++) {
            for (int c = 0; c < board.length; c++) {
                System.out.printf("%2d ", board[c][r]);
            }
            System.out.println();
        }
    }

    // TODO: update the probabilities of where the AI thinks it is based on the action selected and the new sonar readings
    //       To do this, you should update the 2D-array "probs"
    // Note: sonars is a bit string with four characters, specifying the sonar reading in the direction of North, South, East, and West
    //       For example, the sonar string 1001, specifies that the sonars found a wall in the North and West directions, but not in the South and East directions
    void updateProbabilities(int action, String sonars) {
        //sensors is up down right left

        // System.out.println("action is " + action);
        // System.out.println("sensing: " + sonars);
        // your code
        // printProbabilityBoard(probs);
        double[][] probsPredicted = deepCopyProbabilities(probs);
        //NOTE!!!!! The probabilities array is flipped! columns are rows and rows are columns

        //Predict
        probs = predictTransition(probsPredicted, action);

        // printProbabilityBoard(probs);

        //Observe (with the new sonar readings)
        probs = observeSensorProbabilities(probs, sonars);

        //Normalize
        probs = normalizeProbabilities(probs);

        myMaps.updateProbs(probs); // call this function after updating your probabilities so that the
                                   //  new probabilities will show up in the probability map on the GUI
    }

    /*
     * Get the expected utility of a certain position on the board, given the direction that
     * the robot will try to move in if in that state.
     */
    double getExpectedUtility(int[] currPos, int action) {
        //TODO:can't get the next position if the current position is a wall

        double currStateUtility = 0;
        double[] transProbs = getTransitionProbs(action);
        for (int i = 0; i < 5; i++) {
            int[] nextPos = getNextPosition(currPos, i);
            currStateUtility += (transProbs[i] * Vs[nextPos[0]][nextPos[1]]);
        }
        return currStateUtility;
    }

    /*
     * Get the expected utility of an action, given the probabilities of the robots location.
     */
    double getExpectedUtility(int action) {
        double expectedUtility = 0;
        for (int c = 0; c < mundo.width; c++) {
            for (int r = 0; r < mundo.height; r++) {
                int[] currPos = {c, r};
                //For every state, that we have a possibility of being on
                if (probs[c][r] != 0) {
                    expectedUtility += probs[c][r] * getExpectedUtility(currPos, action);
                }
            }
        }
        return expectedUtility;
    }

    /*
     * Return the action that gives the max utility
     */
    int selectMaxExpectedUtilityAction() {
        int NUMBER_OF_DIRECTIONS_TO_MOVE = 5;

        //return the action with the maximum expected value
        int maxAction = STAY;
        double maxExpectedUtility = 0;
        for (int a = 0; a < NUMBER_OF_DIRECTIONS_TO_MOVE; a++) {
            //get the expected utility for this action
            double expectedUtility = getExpectedUtility(a);
            if (a == 0) {
                maxAction = 0;
                maxExpectedUtility = expectedUtility;
            } else if (maxExpectedUtility < expectedUtility) {
                maxAction = a;
                maxExpectedUtility = expectedUtility;
            }
        }
        // System.out.println("Max Action: " + maxAction);
        return maxAction;
    }

    int selectActionForLocation() {
        // Randomly keep moving up and left
        // This algorithm depends upon there not being as many
        // corners in the maze. (more risk more reward)

        Random random = new Random();
        int directionFlag = random.nextInt() % 2;
        if (directionFlag == 0) {
            //go horizontally
            return WEST;
        } else {
            //go vertically
            return NORTH;
        }
    }

    int pathFindValItrExplore() {
        int emptyCount = 0;
        for (int c = 0; c < mundo.width; c++) {
            for (int r = 0; r < mundo.height; r++) {
                if (mundo.grid[c][r] == 0) {
                    emptyCount++;
                }
            }
        }
        double probBound = 0.2; //very unlikley to go into a corner
        boolean haveLocation = false;
        for (int c = 0; c < mundo.width; c++) {
            for (int r = 0; r < mundo.height; r++) {
                if (probs[c][r] > probBound) {
                    haveLocation = true;
                }
            }
        }
        if (haveLocation) {
            return selectMaxExpectedUtilityAction();
        } else {
            return selectActionForLocation();
        }
    }
    
    // This is the function you'd need to write to make the robot move using your AI;
    // You do NOT need to write this function for this lab; it can remain as is
    int automaticAction() {
        if (this.pathFindAlg.equals("valItr")) {
            //Approach 1: Value Iteration with going towards max utility
            return selectMaxExpectedUtilityAction();

        } else if (this.pathFindAlg.equals("valItr_explore")) {
            //Approach 2
            //Move Up until a wall is found, then follow the wall to find out where we are currently.
            //  This is similar to navigating a maze in real life, where you always follow a wall.
            //After finding out roughly where we are start moving towards the goal.
            return pathFindValItrExplore();


        //--------------- Personal Project ---------------------//
        } else if (this.pathFindAlg.equals("astr")) {
            //Approach 3: find the direction to go to via A*

            //Count the number of spaces available to us (probability will be spread throughout it)
            int emptyCount = 0;
            for (int c = 0; c < mundo.width; c++) {
                for (int r = 0; r < mundo.height; r++) {
                    if (mundo.grid[c][r] == 0) {
                        emptyCount++;
                    }
                }
            }

            double[] actionProbs = {0, 0, 0, 0, 0};
            //Add up the actions probabilities if the spaces probability is above a percentage
            for (int c = 0; c < mundo.width; c++) {
                for (int r = 0; r < mundo.height; r++) {
                    if (probs[c][r] > (2/emptyCount)) {
                        // System.out.println("looking at prob position: " + c + " " + r);
                        //add the probability for this current action
                        int action = getAStarAction(c, r);
                        actionProbs[action] += probs[c][r];
                    }
                }
            }

            //return the largest probability
            int maxAction = 0;
            boolean isSelected = false;
            for (int i = 0; i < actionProbs.length; i++) {
                System.out.print(actionProbs[i] + " ");
                if (actionProbs[i] >= actionProbs[maxAction]) {
                    maxAction = i;
                }
                if (actionProbs[i] > 0) {
                    isSelected = true;
                }
            }
            System.out.println("");
            if (isSelected) {
                return maxAction;
            } else {
                //return a random direction
                Random rand = new Random();
                int randAct = rand.nextInt() % 5;
                return randAct;
            }

        } else if (this.pathFindAlg.equals("mcts")) {
            //Approach 4: use a variation of mcts to get the probability of making it to the end for a certain action

            return getMctsAction();
        }

        return 0; //By default just go upwards
    }

    double[] getTransitionProbs(int direction) {
        double[] transitionProbs = new double[5];
        //goes up, down, right, left, stay
        for (int i = 0; i < 5; i++) {
            transitionProbs[i] = (1 - moveProb) / 4;
        }
        transitionProbs[direction] = moveProb;
        return transitionProbs;
    }

    /*
     * Returns the next position of the 
     */
    int[] getNextPosition(int[] currPos, int direction) {
        //currPos = column, row pair in that order

        int GRID_WALL = 1;
        int GRID_DEATH = 2;
        int GRID_GOAL = 3;
        int GRID_EMPTY = 0;
        
        int newC = currPos[0];
        int newR = currPos[1];
        if (mundo.grid[currPos[0]][currPos[1]] == GRID_EMPTY || mundo.grid[currPos[0]][currPos[1]] == GRID_GOAL) {
            //dependant on the direction increase/decrease r or c if you can
            //if moving and CAN move there then execute, else return the current position
            if (direction == NORTH && mundo.grid[newC][newR - 1] != GRID_WALL) {
                newR = newR - 1;
            } else if (direction == SOUTH && mundo.grid[newC][newR + 1] != GRID_WALL) {
                newR = newR + 1;
            } else if (direction == EAST && mundo.grid[newC + 1][newR] != GRID_WALL) {
                newC = newC + 1;
            } else if (direction == WEST && mundo.grid[newC - 1][newR] != GRID_WALL) {
                newC = newC - 1;
            }
        } else {
            System.out.println("player unable to reach this position");
        }

        int[] newPair = {newC, newR};
        return newPair;
    }

    double getMaxMoveUtility(int[] currPos) {
        int NUMBER_OF_DIRECTIONS_TO_MOVE = 5;

        //calculate the max utility of going any maximal direction
        double maxUtilVal = 0;
        for (int d = 0; d < NUMBER_OF_DIRECTIONS_TO_MOVE; d++) {
            //Get the utility of choosing to go direction d
            double[] transitionProbs = getTransitionProbs(d);
            double thisUtility = 0;
            for (int probDir = 0; probDir < NUMBER_OF_DIRECTIONS_TO_MOVE; probDir++) {
                int[] newPos = getNextPosition(currPos, d);
                double transVal = Vs[newPos[0]][newPos[1]];
                thisUtility += transitionProbs[probDir] * transVal;
            }

            //compare max utilities
            if (d == 0) {
                maxUtilVal = thisUtility;
            } else if (maxUtilVal < thisUtility) {
                maxUtilVal = thisUtility;
            }
        }
        return maxUtilVal;
    }

    void valueIteration() {
        System.out.println("Starting value Iteration");
        //Initialize:
        //Constants
        double REWARD_EMPTY = -2.0; //the longer a path takes the less we want to traverse it (baseline)
        double REWARD_DEATH = -1000; //always avoid death squares (even if multiplied by a tiny probability)
        double REWARD_GOAL = 20; //prefer to go towards the goals (values weighted towards this)
        double REWARD_WALL = 0.0; //not looked at, used for print out
        int GRID_WALL = 1;
        int GRID_DEATH = 2;
        int GRID_GOAL = 3;
        int GRID_EMPTY = 0;

        double discount_fact = 1.0;
        double converge_bound = 0.01;
        double[][] rewardMatrix = new double[mundo.width][mundo.height];
        Vs = new double[mundo.width][mundo.height];

        //Get probabilities (what direction to go toward (initially always deterministic))
        //For now assume the directions are all to stay put (will move around randomly)
        //Initialize values
        //Initialize rewards
        System.out.println("Initializing rewards and utilities...");
        for (int c = 0; c < mundo.width; c++) {
            for (int r = 0; r < mundo.height; r++) {
                switch (mundo.grid[c][r]) {
                    case 0:
                        //open
                        Vs[c][r] = REWARD_EMPTY;
                        rewardMatrix[c][r] = REWARD_EMPTY;
                        break;
                    case 1:
                        //wall?
                        Vs[c][r] = REWARD_WALL;
                        rewardMatrix[c][r] = REWARD_WALL;
                        break;
                    case 2:
                        //death
                        Vs[c][r] = REWARD_DEATH;
                        rewardMatrix[c][r] = REWARD_DEATH;
                        break;
                    case 3:
                        //goal
                        Vs[c][r] = REWARD_GOAL;
                        rewardMatrix[c][r] = REWARD_GOAL;
                        break;
                    default:
                        //Unknown
                        System.out.println("Error in value iteration rewards");
                        break;
                }
            }
        }
        System.out.println("Finished initializing");
        //Initialize discount factor
        //Initialize convergence factor

        double newConvergeVal = converge_bound + 1;
        while (newConvergeVal > converge_bound) {
            newConvergeVal = 0;
            //1. calculate the new utility values

            //for every state s, calculate it's new utility value (the grid is column first!)
            
            for (int c = 0; c < mundo.width; c++) {
                for (int r = 0; r < mundo.height; r++) {
                    //Don't need to calculate utility values of walls, or end states (death / goal)
                    //Walls are not able to be moved to so they don't have values
                    //End states have fixed values because no actions are executed away from them.
                    if (mundo.grid[c][r] == GRID_DEATH || mundo.grid[c][r] == GRID_GOAL || mundo.grid[c][r] == GRID_WALL) {
                        continue;
                    }

                    //state s == (c, r)
                    int[] currPos = {c, r};
                    double oldUtilityVal = Vs[c][r];
                    double maxUtilVal = getMaxMoveUtility(currPos);
                    //calculate the new value for this state
                    double newUtilityVal = rewardMatrix[c][r] + discount_fact * maxUtilVal;
                    Vs[c][r] = newUtilityVal;

                    //Take maximum convergance amount
                    double currConvergeVal = Math.abs(newUtilityVal - oldUtilityVal);

                    if (c == 0 && r == 0) {
                        //This is the first iteration
                        newConvergeVal = currConvergeVal;
                    } else if (currConvergeVal > newConvergeVal) {
                        newConvergeVal = currConvergeVal;
                    }
                }
            }
            // break; //TODO:take out so its not just one iteration
        }
        System.out.println("Utility:");
        printProbabilityBoard(Vs);
    }

    int getAStarH(int c, int r, int cost, int goalC, int goalR) {
        //Heuristic1: Get cost for each step

        //Heuristic2: Get distance to the goal
        double dist = Math.sqrt(Math.pow((goalC - c), 2) + Math.pow((goalR - r), 2));
        int distRound = (int) Math.round(dist);

        //Get whether or not we are nearby a stairwell
        int stairCost = 0;
        if (mundo.grid[c + 1][r] == 2 ||
            mundo.grid[c][r + 1] == 2 ||
            mundo.grid[c - 1][r] == 2 ||
            mundo.grid[c][r - 1] == 2) {
            stairCost = 50;
        }


        return cost + distRound + stairCost;
    }

    int[] getAStarPos(int c, int r, int action, int prevCost, int goalC, int goalR) {
        int h = getAStarH(c, r, prevCost + 1, goalC, goalR);
        int[] pos = {h, c, r, action, prevCost + 1}; //last one is the action to back
        return pos;
    }

    int getAStarAction(int startC, int startR) {
        compActs = new int[mundo.width][mundo.height];
        for (int c = 0; c < mundo.width; c++) {
            for (int r = 0; r < mundo.height; r++) {
                compActs[c][r] = -1;
            }
        }

        //choose a goal to go towards
        int goalC = -1;
        int goalR = -1;
        boolean goalFound = false;
        for (int c = 0; c < mundo.width; c++) {
            for (int r = 0; r < mundo.height; r++) {
                if (mundo.grid[c][r] == 3) {
                    goalC = c;
                    goalR = r;
                    goalFound = true;
                    break;
                }
            }
            if (goalFound) {
                break;
            }
        }

        PriorityQueue<int[]> queue = new PriorityQueue<int[]>(5, new PositionComparator());
        int[] startPos = getAStarPos(startC, startR, 0, 0, goalC, goalR);
        queue.add(startPos);
        Vector<int[]> visitedPos = new Vector<int[]>();

        boolean isSuccessful = false;
        while (!queue.isEmpty() && !isSuccessful) {
            int[] currNode = queue.remove();
            int currC = currNode[1];
            int currR = currNode[2];
            int prevCost = currNode[4];
            int[] currPos = {currC, currR};

            //If we have already visited this position, skip it
            boolean isVisited = false;
            for (int i = 0; i < visitedPos.size(); i++) {
                if (currC == visitedPos.get(i)[0] && currR == visitedPos.get(i)[1]) {
                    isVisited = true;
                    break;
                }
            }
            if (isVisited) {
                continue;
            }

            //"Explore" this node
            compActs[currC][currR] = currNode[3];
            visitedPos.add(currPos);

            //Print the current state of the board
            // printActionBoard(compActs);

            //check to see if we arrived at a solution
            if (mundo.grid[currC][currR] == 3) {
                //Success!
                isSuccessful = true;
                goalC = currC;
                goalR = currR;
                break;
            }

            //Generate neighbors (may have repeats)
            if (currC >= 1 && currC < mundo.width - 1 && currR >= 1 && currR < mundo.height - 1) {
                if (mundo.grid[currC + 1][currR] != 1 && mundo.grid[currC + 1][currR] != 2) {
                    int[] rightPos = getAStarPos(currC + 1, currR, WEST, prevCost, goalC, goalR);
                    queue.add(rightPos);
                }
                if (mundo.grid[currC - 1][currR] != 1 && mundo.grid[currC - 1][currR] != 2) {
                    int[] leftPos = getAStarPos(currC - 1, currR, EAST, prevCost, goalC, goalR);
                    queue.add(leftPos);
                }
                if (mundo.grid[currC][currR + 1] != 1 && mundo.grid[currC][currR + 1] != 2) {
                    int[] downPos = getAStarPos(currC, currR + 1, NORTH, prevCost, goalC, goalR);
                    queue.add(downPos);
                }
                if (mundo.grid[currC][currR - 1] != 1 && mundo.grid[currC][currR - 1] != 2) {
                    int[] upPos = getAStarPos(currC, currR - 1, SOUTH, prevCost, goalC, goalR);
                    queue.add(upPos);
                }
            }
        }

        //Debug: uncomment for generated action graphs
        // printActionBoard(compActs);
        // System.out.println();

        //follow the path to a goal and return the action to go to that goal (there may be multiple goal but one of us)

        int currC = goalC;
        int currR = goalR;
        int prevC = currC;
        int prevR = currR;
        while (currC != startC || currR != startR) {
            //Go to next node on the path
            prevC = currC;
            prevR = currR;
            int[] currPos = {currC, currR};
            int[] nextPos = getNextPosition(currPos, compActs[currC][currR]);
            currC = nextPos[0];
            currR = nextPos[1];
        }

        //Return the opposite action
        int reverseAction = compActs[prevC][prevR];
        if (reverseAction == NORTH) {
            return SOUTH;
        } else if (reverseAction == SOUTH) {
            return NORTH;
        } else if (reverseAction == EAST) {
            return WEST;
        } else if (reverseAction == WEST) {
            return EAST;
        }
        return 0;
    }

    int getMctsAction() {
        //Once an action is chosen for every location, add up the probabilities of moving a certain direction
        // Calculated off of the first node
        int maxIterations = 1000;
        int[][] visited = new int[mundo.width][mundo.height];
        //initialize to all 0's
        for (int c = 0; c < mundo.width; c++) {
            for (int r = 0; r < mundo.height; r++) {
                visited[c][r] = 0;
            }
        }
        //Add up the probabilities for each location
        double[] actionProbs = {0, 0, 0, 0, 0};
        boolean firstIter = true;
        for (int c = 1; c < mundo.width; c++) {
            for (int r = 1; r < mundo.height; r++) {
                if (firstIter) {
                    firstIter = false;

                    //Run through MCTS for a specific location
                    MCTSNode root = new MCTSNode(this.mundo.grid, c, r, visited);

                    //Select and expand
                    MCTSNode selectedNode = root.select();
                    // Simulate + back propagate
                    selectedNode.simulate();

                    // if (selectedNode.parent != null) {
                    //     selectedNode.parent.backpropogate();
                    // }

                    // for (int i = 0; i < maxIterations; i++) {
                    //     //Select and expand
                    //     MCTSNode selectedNode = root.select();
                    //     //Simulate + back propagate
                    //     selectedNode.simulate();

                    //     if (selectedNode.parent != null) {
                    //         selectedNode.parent.backpropogate();
                    //     }
                    // }

                    root.printTree();
                    System.out.println();
                }

            }
        }
        //Have
        //this.probs //probability of where we are

        return 0;
    }
    
    void startRobotControler() {
        int action;

        if (this.pathFindAlg.equals("valItr") || this.pathFindAlg.equals("valItr_explore")) {
            valueIteration();
        }
        initializeProbabilities();  // Initializes the location (probability) map
        
        while (true) {
            try {
                if (isManual)
                    action = getHumanAction();  // get the action selected by the user (from the keyboard)
                else
                    action = automaticAction(); // TODO: get the action selected by your AI;
                                                // you'll need to write this function for part III
                
                sout.println(action); // send the action to the Server
                
                // get sonar readings after the robot moves
                String sonars = sin.readLine();
                //System.out.println("Sonars: " + sonars);
            
                updateProbabilities(action, sonars); // TODO: this function should update the probabilities of where the AI thinks it is
                
                if (sonars.length() > 4) {  // check to see if the robot has reached its goal or fallen down stairs
                    if (sonars.charAt(4) == 'w') {
                        System.out.println("I won!");
                        myMaps.setWin();
                        break;
                    }
                    else if (sonars.charAt(4) == 'l') {
                        System.out.println("I lost!");
                        myMaps.setLoss();
                        break;
                    }
                }
                else {
                    // here, you'll want to update the position probabilities
                    // since you know that the result of the move as that the robot
                    // was not at the goal or in a stairwell
                }
                Thread.sleep(decisionDelay);  // delay that is useful to see what is happening when the AI selects actions
                                              // decisionDelay is specified by the send command-line argument, which is given in milliseconds
            }
            catch (IOException e) {
                System.out.println(e);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // java theRobot [manual/automatic] [delay]
    public static void main(String[] args) {
        theRobot robot = new theRobot(args[0], Integer.parseInt(args[1]), args[2]);  // starts up the robot, 3ed argument is the type of path finding algorithm used
    }
}


class PositionComparator implements Comparator<int[]> {
    public int compare(int[] n1, int[] n2) {
        if (n1[0] < n2[0]) {
            return -1;
        } else if (n1[0] > n2[0]) {
            return 1;
        }
        return 0;
    }
}


