

import java.util.Random;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Iterator;


/**
 * Manet provides an object which holds a collection of Nodes representing a 
 * 	graph topology of Ad-Hoc devices.
 * 
 * Used to simulate the differences of Ad-Hoc Routing protocols.
 * 
 * Manet is an abstract class that is used in ManetWrapper
 */
public abstract class Manet implements Iterable<Node>{

    // Maximum size of the "world" of the MANET
    public final double WORLD_LIMIT = 10.0;
    // Keep this at 1.0 to make a "disk graph"
    final double NODE_COMM_RANGE = 1.0;

    protected Random prng = null;
    protected HashSet<Node> graph;
    protected Stack<Node> remove_stack;

    private LinkedList<ManetListener> listeners;


    public Manet(long prng_seed) {
        this.prng = new Random(prng_seed);
        this.graph = new HashSet<Node>();
        this.remove_stack = new Stack<Node>();
        this.listeners = new LinkedList<ManetListener>();
    }


    // Convenience Layer Functions
    public abstract void generateNode();

    /**
     * show()
     * 
     * Displays the network graph using ManetCanvas
     */
    public void show() {
        ManetCanvas canvas = new ManetCanvas(this);
        canvas.show();
    }


    // Accessors and Mutators
    public HashSet<Node> getGraph() { return this.graph; }


    // Iterable Implementation Code
    public class ManetIterator implements Iterator<Node> {
        private Iterator<Node> internal_iter = null;
        public ManetIterator(Manet manet) {
            internal_iter = manet.getGraph().iterator();
        }
        public boolean hasNext() { return internal_iter.hasNext(); }
        public Node next() { return internal_iter.next(); }
        public void remove() { internal_iter.remove(); }
    }

    public Iterator<Node> iterator() {
        return new ManetIterator(this);
    }

    public void addListener(ManetListener listener) {
        this.listeners.add(listener);
    }


    /**
     * addNode(node)
     * this function adds a Node to the graph represented by a HashSet<Node>
     * 	the node is also added to the helper remove_stack which is used 
     * 	when removing nodes from the graph
     * 
     * addNodeCallback() listener is called for each node added
     * 
     * @param node - Node to be added
     */
    protected void addNode(Node node) {
        this.graph.add(node);
        
        // This is just for removal
        this.remove_stack.push(node);

        for (ManetListener listener : this.listeners) {
            listener.addNodeCallback(node);
        }
    }

    
    /**
     * removeLastNode()
     * This function removes the last node to be added to the graph.  
     * 	Using process to remove nodes guarantees that the graph will not be
     * 	partitioned when a node is removed.
     * 
     * calls removeNode()
     * 
     */
    protected void removeLastNode() {
        Node to_remove = remove_stack.pop();
        removeNode(to_remove);
    }

    
    /**
     * removeNode(node)
     * This function removes a ndoe from the graph.
     * 	Node is removed from all Node.getNeighbors()
     * 	removeNodeCallback() is called for each node removed.
     * 
     * @param node - Node to be removed
     */
    protected void removeNode(Node node) {
        for (Node neighbor : node.getNeighbors()) {
            neighbor.removeNeighbor(node);
        }
        this.graph.remove(node);

        for (ManetListener listener : this.listeners) {
            listener.removeNodeCallback(node);
        }
    }
}