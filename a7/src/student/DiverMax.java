package student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import game.GetOutState;
import game.Tile;
import game.FindState;
import game.SewerDiver;
import game.Node;
import game.NodeStatus;
import game.Edge;

public class DiverMax extends SewerDiver {


    /** Get to the ring in as few steps as possible. Once you get there, 
     * you must return from this function in order to pick
     * it up. If you continue to move after finding the ring rather 
     * than returning, it will not count.
     * If you return from this function while not standing on top of the ring, 
     * it will count as a failure.
     * 
     * There is no limit to how many steps you can take, but you will receive
     * a score bonus multiplier for finding the ring in fewer steps.
     * 
     * At every step, you know only your current tile's ID and the ID of all 
     * open neighbor tiles, as well as the distance to the ring at each of these tiles
     * (ignoring walls and obstacles). 
     * 
     * In order to get information about the current state, use functions
     * currentLocation(), neighbors(), and distanceToRing() in FindState.
     * You know you are standing on the ring when distanceToRing() is 0.
     * 
     * Use function moveTo(long id) in FindState to move to a neighboring 
     * tile by its ID. Doing this will change state to reflect your new position.
     * 
     * A suggested first implementation that will always find the ring, but likely won't
     * receive a large bonus multiplier, is a depth-first walk. Some
     * modification is necessary to make the search better, in general.*/
    @Override public void findRing(FindState state) {
        //TODO : Find the ring and return.
        // DO NOT WRITE ALL THE CODE HERE. DO NOT MAKE THIS METHOD RECURSIVE.
        // Instead, write your method elsewhere, with a good specification,
        // and call it from this one.
    	HashSet<Long> visitedNodes = new HashSet<>();
    	dfs(state, visitedNodes);
    }
    
    
    
    /**
     * DFS algorithm - depth first search
     * visit every node reachable along a path of unvisited nodes from node u.
     * Precondition: u has not been visited
     * 
     * Long pos is the current position of the node (state.currentNode())
     * FindState state is the current state of the game (Find state)
     * HashSet<Long> visitedNodes is the hashset containing all nodes - 
     * that the miner has visited
     */
    public static void dfs(FindState state, HashSet<Long> visitedNodes) {
    	// visit current position
    	long currentPos = state.currentLocation();
    	visitedNodes.add(currentPos);
    	 
    	Heap<Long> closestNeighbor = moveToSmallest(state, state.neighbors());
    	
    	while(closestNeighbor.size()>0) {
	    	long nextMove = (long) closestNeighbor.poll();
	    	if(!visitedNodes.contains(nextMove)) {
	    		if(state.distanceToRing() != 0) {
	    			state.moveTo(nextMove);
	    			visitedNodes.add(nextMove);}
	    		if(state.distanceToRing() == 0){
	    			return; 
	    		}
	    		dfs(state, visitedNodes);
	    		if(state.distanceToRing()!= 0) {
	    			state.moveTo(currentPos);
	    		}
	    	}
    	}
    }
    
    
    /**
     * Returns the neighboring tile that is closest to the ring
     * @param neighbors
     */
    public static Heap<Long> moveToSmallest(FindState state, Collection<NodeStatus> neighbors) {
    	
    	Heap<Long> orderedNodes = new Heap<Long>();
    	ArrayList<NodeStatus> array = new ArrayList<>();
    	//(NodeStatus n : neighbors)(int i = neighbors.size()-1; i>=0; i--)
    	for (NodeStatus n : neighbors) {
    		array.add(n);
    	}
    	for (int i = neighbors.size()-1; i>=0; i--) {
    		NodeStatus n = array.get(i);
    		orderedNodes.add(n.getId(), n.getDistanceToTarget());
    	}
    	return orderedNodes;
    }
    

    /** Get out of the sewer system before the steps are all used, trying to collect
     * as many coins as possible along the way. Your solution must ALWAYS get out
     * before the steps are all used, and this should be prioritized above
     * collecting coins.
     * 
     * You now have access to the entire underlying graph, which can be accessed
     * through GetOutState. currentNode() and getExit() will return Node objects
     * of interest, and getNodes() will return a collection of all nodes on the graph. 
     * 
     * You have to get out of the sewer system in the number of steps given by
     * getStepsRemaining(); for each move along an edge, this number is decremented
     * by the weight of the edge taken.
     * 
     * Use moveTo(n) to move to a node n that is adjacent to the current node.
     * When n is moved-to, coins on node n are automatically picked up.
     * 
     * You must return from this function while standing at the exit. Failing to
     * do so before steps run out or returning from the wrong node will be
     * considered a failed run.
     * 
     * Initially, there are enough steps to get from the starting point to the
     * exit using the shortest path, although this will not collect many coins.
     * For this reason, a good starting solution is to use the shortest path to
     * the exit. */
    @Override public void getOut(GetOutState state) {
        //TODO: Get out of the sewer system before the steps are used up.
        // DO NOT WRITE ALL THE CODE HERE. Instead, write your method elsewhere,
        //with a good specification, and call it from this one.
    	
    	ArrayList<Node> coinValsSorted = sortedCoinValNodes(state); 
    	getOutOptimalScore(state, coinValsSorted);
    	getOutShortestPath(state);
    }
    
    
    /**
     * This method takes in the game state and a sorted array of the nodes 
     * containing coins
     * it then iterates through the array - if there are enough steps left,
     *  it will walk the miner to that node - starting with high value nodes 
     *  first. Then it checks the next highest values - if there are enough 
     *  steps left it will walk the miner to that next node, and so on, 
     *  until steps run out
     *  
     *  Precondition: coinValsSorted is a sorted ArrayList of Nodes 
     */
    public static void getOutOptimalScore(GetOutState state, ArrayList<Node> coinValsSorted) {
    	for(int i = coinValsSorted.size()-1; i >= 0; i--) {
    		if((stepsToMaxNode(state, coinValsSorted.get(i)) + 
    				shortestPathFromMaxtoExit(state, coinValsSorted.get(i))) <=
    				state.stepsLeft()) {
    			//walk to max node
    	    	Node currentNode = state.currentNode();
    			List<Node> walk = Paths.minPath(currentNode, coinValsSorted.get(i));
    	    	for (int z = 1; z < walk.size(); z++) {
    	    		state.moveTo(walk.get(z));
    	    	}
    		} 
    	}
    }

    /**
     * This method calculates the total weight of the edges to get to the 
     * max value node
     */
    public static int stepsToMaxNode(GetOutState state, Node node) {
    	Node currentNode = state.currentNode();
    	List<Node> path = Paths.minPath(currentNode, node);
    	return Paths.sumPath(path);
    }
    
    /**
     * This method calculates the total weight off the edges to get from a node
     * to the exit
     */
    public static int shortestPathFromMaxtoExit(GetOutState state, Node node) {
    	Node exit = state.getExit();
    	List<Node> path = Paths.minPath(node, exit);
    	return Paths.sumPath(path);
    }
    
    
    /**
     * returns an ArrayList of all of the nodes with coins on them, 
     * sorted from lowest value to highest value
     */
    public static ArrayList<Node> sortedCoinValNodes(GetOutState state) {
    	Collection<Node> allNodes = state.allNodes();
    	ArrayList<Node> coinValsSorted = new ArrayList<>();
    	
    	for (Node q : allNodes) {
    		if (q.getTile().coins() > 0) {
    			coinValsSorted.add(q);
    		}
    	}
    	bubbleSort(coinValsSorted);
    	return coinValsSorted;
    }
    
    
    /**
     * This is the bubbleSort algorithm
     * It will sort an ArrayList of Nodes in order of their coin values
     * low values to high values
     * 
     * Precondition array must be an ArrayList of Nodes
     */
    public static void bubbleSort(ArrayList<Node> array) {
    	for (int i = array.size()-1; i>=0; i--) {
    		for (int j =1; j <=i; j++) {
    			if(array.get(j-1).getTile().coins() > array.get(j).getTile().coins()) {
    				Node temp = array.get(j-1);
    				array.add(j-1, array.get(j));
    				array.remove(j);
    				array.add(j, temp);
    				array.remove(j+1);
    			}
    		}
    	}
    }
    

    /**
     * Finds the shortest path from current node to the end node-
     * using shortest path algorithm - Paths.minPath().
     * Then walks player down that select path until the player is out of the 
     * sewer
     * return nothing
     * 
     */
    public static void getOutShortestPath(GetOutState state) {
    	Node exit = state.getExit();
    	Node currentNode = state.currentNode();    	
    	List<Node> shortestPath = Paths.minPath(currentNode, exit);
    	for (int i = 1; i < shortestPath.size(); i++) {
    		state.moveTo(shortestPath.get(i));
    	}
    }
}


    