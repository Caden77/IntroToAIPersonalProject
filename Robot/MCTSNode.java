import com.sun.source.doctree.SerialFieldTree;
import java.util.Random;
import java.util.Vector;

public class MCTSNode {

    //This nodes variables
    int reward = 0;
    int timesSelected = 0;
    int[][] mundoGrid;
    int currC;
    int currR;
    int[][] visited;
    MCTSNode parent;
    MCTSNode[] children;
    int[][] actions;

    public MCTSNode(int[][] mundoGrid, int c, int r, int[][] visited) {
        reward = 0;
        timesSelected = 0;
        this.mundoGrid = mundoGrid;
        this.currC = c;
        this.currR = r;
        this.visited = visited;
        this.parent = null;
        int[] north = {this.currC, this.currR - 1};
        int[] south = {this.currC, this.currR + 1};
        int[] east = {this.currC + 1, this.currR};
        int[] west = {this.currC - 1, this.currR};
        this.actions = new int[4][];
        this.actions[0] = north;
        this.actions[1] = south;
        this.actions[2] = east;
        this.actions[3] = west;

        //initialize children to none
        this.children = new MCTSNode[5];
        for (int i = 0; i < this.children.length; i++) {
            this.children[i] = null;
        }

        //mark ourselves as visited
        visited[c][r] = 1;
    }

    public MCTSNode(int[][] mundoGrid, int c, int r, int[][] visited, MCTSNode parent) {
        reward = 0;
        timesSelected = 0;
        this.mundoGrid = mundoGrid;
        this.currC = c;
        this.currR = r;
        this.visited = visited;
        this.parent = parent;
        int[] north = {this.currC, this.currR - 1};
        int[] south = {this.currC, this.currR + 1};
        int[] east = {this.currC + 1, this.currR};
        int[] west = {this.currC - 1, this.currR};
        this.actions = new int[4][];
        this.actions[0] = north;
        this.actions[1] = south;
        this.actions[2] = east;
        this.actions[3] = west;

        //initialize children to none
        this.children = new MCTSNode[5];
        for (int i = 0; i < this.children.length; i++) {
            this.children[i] = null;
        }

        //mark ourselves as visited
        visited[c][r] = 1;
    }

    //Select the node just before or at a leaf node (combines selecting and expanding)
    public MCTSNode select() {

        //If at terminal return yourself (stairs or goal)
        if (mundoGrid[currC][currR] == 2 || mundoGrid[currC][currR] == 3) {
            return this;
        }

        //If a possible action is null then stop
        //North, south, east, west
        Vector<Integer> activeChildren = new Vector<Integer>();
        Vector<Integer> inactiveChildren = new Vector<Integer>();

        for (int i = 0; i < actions.length; i++) {
            int[] newPos = this.actions[i];
            if ((newPos[0] >= 0 && newPos[0] <= this.mundoGrid.length) && //in col bounds
                (newPos[1] >= 0 && newPos[1] <= this.mundoGrid[0].length) && // in row bounds
                (this.mundoGrid[newPos[0]][newPos[1]] != 1) &&       // not wall
                (this.visited[newPos[0]][newPos[1]] != 1)) {         // not in current visited
                    if (this.children[i] == null) {
                        inactiveChildren.add(i);
                    } else {
                        activeChildren.add(i);
                    }
            }
        }

        Random rand = new Random();
        if (inactiveChildren.size() > 0) {
            //haven't explored all of this leaf node, return a newly created child
            int inactChildInd = rand.nextInt(0, inactiveChildren.size());
            int dirInd = inactiveChildren.get(inactChildInd);
            MCTSNode newNode = new MCTSNode(this.mundoGrid, this.actions[dirInd][0], this.actions[dirInd][1], this.visited, this);
            this.children[dirInd] = newNode;
            return newNode;
        } else {
            //we have children we need to select from next randomly
            int actChildInd = rand.nextInt(0, activeChildren.size());
            MCTSNode returnedChild = this.children[activeChildren.get(actChildInd)];
            return returnedChild.select();
        }
    }

    //This is either a leaf node or a terminal node. Get the reward for this node
    public void simulate() {
        timesSelected += 1;

        //if terminal
        if (mundoGrid[currC][currR] == 2) {
            //bad, terminal
            reward += -1;
            return;

        } else if (mundoGrid[currC][currR] == 3) {
            //good, terminal
            reward += 5;
            return;
        }
        //else the surroundings are all visited
        boolean hasPotentialChildren = false;
        for (int i = 0; i < actions.length; i++) {
            int[] newPos = this.actions[i];
            if ((newPos[0] >= 0 && newPos[0] <= this.mundoGrid.length) && //in col bounds
                (newPos[1] >= 0 && newPos[1] <= this.mundoGrid[0].length) && // in row bounds
                (this.mundoGrid[newPos[0]][newPos[1]] != 1) &&       // not wall
                (this.visited[newPos[0]][newPos[1]] != 1)) {         // not in current visited
                    hasPotentialChildren = true;
                    break;
            }
        }
        if (!hasPotentialChildren) {
            reward += 1;
            return;
        }

        //the recursive part
        int randIterations = 500;
        int c = currC;
        int r = currR;
        for (int i = 0; i < randIterations; i++) {
            //if at a goal or stairwell return their reward
            //if terminal
            if (mundoGrid[c][r] == 2) {
                //bad, terminal
                reward += -1;
                return;

            } else if (mundoGrid[c][r] == 3) {
                //good, terminal
                reward += 5;
                return;
            }

            //get a list of new destinations
            int[] north = {c, r - 1};
            int[] south = {c, r + 1};
            int[] east = {c + 1, r};
            int[] west = {c - 1, r};
            int[][] newActions = {north, south, east, west};

            Vector<Integer> validActions = new Vector<Integer>();
            //if the action is a wall, don't consider it
            for (int j = 0; j < newActions.length; j++) {
                if (this.mundoGrid[newActions[j][0]][newActions[j][1]] != 1) {
                    //did not hit a wall
                    validActions.add(j);
                }
            }

            //go a random valid direction
            Random rand = new Random();
            int valInd = rand.nextInt(0, validActions.size());
            int[] selPair = newActions[validActions.get(valInd)];
            c = selPair[0];
            r = selPair[1];
        }

        //did not find a goal (probably got stuck)
        reward += 1;
        return;
    }

    //Backpropogate what node and reward we ended up with
    public void backpropogate() {
        if (parent != null) {
            this.timesSelected += 1;
            this.parent.backpropogate();
        }
    }

    public void printTree() {
        System.out.println("c:" + this.currC + " r:" + this.currR + " rew:" + this.reward + " itr:" + this.timesSelected);
        for (int i = 0; i < this.children.length; i++) {
            if (this.children[i] != null) {
                this.children[i].printTree();
            }
        }
    }

}