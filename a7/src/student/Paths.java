
package student;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import game.Edge;
import game.Node;

/** This class contains the shortest-path algorithm and other methods
 * Author: gries. */
public class Paths {

    /** Return the shortest path from node v to node end ---or the empty list
     * if a path does not exist.
     * Note: The empty list is NOT "null"; it is a list with 0 elements. */
    public static List<Node> minPath(Node v , Node end) {
        /* TODO Read Piazza note Assignment A6 for ALL details. */
        Heap<Node> F = new Heap<Node>(); // As in abstract algorithm in @700.

        // info contains an entry for each node in F or S. Thus, |info| = |F| + |S|.
        // For each such node, the value part in info contains the shortest known
        // distance to the node and the node's backpointer on that shortest path.
        HashMap<Node, DistBack> info= new HashMap<Node, DistBack>();

        F.add(v, 0);
        info.put(v, new DistBack(null, 0));
        // inv: See Piazza note Assignment A6 (Fall 2018), 
        //      together with def of F and info
        while (F.size() != 0) {
            Node f= F.poll();
            if (f == end) return getPath(info, end);
            int fDist= info.get(f).distance;
            
            for (Edge e : f.getExits()) {// for each neighbor w of f
                Node w= e.getOther(f);
                int newWdist= fDist + e.length;
                DistBack wInfo= info.get(w);
                if (wInfo == null) { //if w not in F or S
                    info.put(w, new DistBack(f, newWdist));
                    F.add(w, newWdist);
                } else if (newWdist < wInfo.distance) {
                    wInfo.distance= newWdist;
                    wInfo.bkptr= f;
                    F.changePriority(w, newWdist);
                }
            }
        }

        // no path from v to end
        return new LinkedList<Node>();
    }


    /** Return the path from the beginning node to node end.
     *  Precondition: info contains all the necessary information about
     *  the path. */
    public static List<Node> getPath(HashMap<Node, DistBack> info, Node end) {
        List<Node> path= new LinkedList<Node>();
        Node p= end;
        // invariant: All the nodes from p's successor to the end are in
        //            path, in reverse order.
        while (p != null) {
            path.add(0, p);
            p= info.get(p).bkptr;
        }
        return path;
    }

    /** Return the sum of the weights of the edges on path pa.
     * Precondition: pa contains at least 1 node. If 1, it's a path of length 0,
     * i.e. with no edges. */
    public static int sumPath(List<Node> pa) {
        synchronized(pa) {
            Iterator<Node> iter= pa.iterator();
            Node node= iter.next();  // First node on path
            int sum= 0;
            // invariant: s = sum of weights of edges from start to v
            while (iter.hasNext()) {
                Node q= iter.next();
                sum= sum + node.getEdge(q).length;
                node= q;
            }
            return sum;
        }
    }

    /** An instance contains information about a node: the previous node
     *  on a shortest path from the start node to this node and the distance
     *  of this node from the start node. */
    private static class DistBack {
        private int distance; // distance from start node to this one
        private Node bkptr; // backpointer on path from start node to this one

        /** Constructor: an instance with backpointer p and
         * distance d from the start node.*/
        private DistBack(Node p, int d) {
            distance= d;     // Distance from start node to this one.
            bkptr= p;  // Backpointer on the path (null if start node)
        }

        /** return a representation of this instance. */
        public String toString() {
            return "dist " + distance + ", bckptr " + bkptr;
        }
    }
    
    
}
