import java.util.Random;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.LinkedList;
import java.util.Iterator;
import java.lang.Math;
import java.awt.geom.Point2D;

public abstract class Manet implements Iterable<Node>{

    // Maximum size of the "world" of the MANET
    final double WORLD_LIMIT = 10.0;
    // Internal seed used when selecting random nodes to ping
    final long PING_PRNG_SEED = 123456789;
    // Keep this at 1.0 to make a "disk graph"
    final double NODE_COMM_RANGE = 1.0;

    protected Random prng = null;
    protected HashSet<Node> graph;


    public Manet(long prng_seed) {
        this.prng = new Random(prng_seed);
        this.graph = new HashSet<Node>();
    }


    // Convenience Layer Functions
    public abstract void generateNode();


    public int floodBFS() { return floodBFS(false); }
    public int floodBFS(boolean verbose) {

        if (graph.isEmpty() ) {
            return 0;
        }

        Random selector = new Random(PING_PRNG_SEED);

        double x = selector.nextDouble();
        double y = selector.nextDouble();
        x = (x * WORLD_LIMIT) - (WORLD_LIMIT / 2);
        y = (y * WORLD_LIMIT) - (WORLD_LIMIT / 2);

        double minimum_distance = Double.MAX_VALUE;
        Node closest_node = null;

        for (Node node : graph) {
            double distance = Point2D.distance(x, y, node.getX(), node.getY());
            
            if (distance < minimum_distance) {
                minimum_distance = distance;
                closest_node = node;
            }
        }

        LinkedList<Node> queue = new LinkedList<Node>();
        HashSet<Node> marked = new HashSet<Node>();

        HashSet<Node> current_layer = new HashSet<Node>();
        HashSet<Node> next_layer = new HashSet<Node>();
        int num_layers = 1;

        if(verbose) {
            System.out.println("Layer: " + num_layers + " --> " + current_layer.size());
        }

        queue.offer(closest_node);
        marked.add(closest_node);

        current_layer.add(closest_node);

        while (!queue.isEmpty()) {
            Node visit = queue.poll();

            boolean add_to_next = false;
            if (current_layer.contains(visit)) {
                current_layer.remove(visit);
                add_to_next = true;
            }

            for (Node neighbor : visit.getNeighbors()) {
                if (!marked.contains(neighbor)) {
                    marked.add(neighbor);
                    queue.offer(neighbor);

                    if(add_to_next) {
                        next_layer.add(neighbor);
                    }
                }
            }

            if (current_layer.isEmpty()) {
                current_layer = next_layer;
                next_layer = new HashSet<Node>();

                if (!current_layer.isEmpty()) {
                    num_layers ++;
                    if(verbose) {
                        System.out.println("Layer: " + num_layers + " --> " + current_layer.size());
                    }
                }
            }
        }

        if(verbose) { System.out.println("Number of BFS Layers: " + num_layers); }

        // Sanity check that the BFS covers all nodes
        if( marked.size() != graph.size() ) {
            System.err.println("Logical Error in Flood BFS: Incorrect Visited.");
            System.err.println("  The number of nodes visited in the BFS is not the size of the graph.");
            System.exit(1);
        }
        return num_layers;
    }

    
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
}