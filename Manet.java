import java.util.Random;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.LinkedList;
import java.util.Iterator;
import java.lang.Math;
import java.awt.geom.Point2D;

public class Manet implements Iterable<Node>{

    // Maximum size of the "world" of the MANET
    final double WORLD_LIMIT = 10.0;
    // Internal seed used when selecting random nodes to ping
    final long PING_PRNG_SEED = 123456789;
    // Keep this at 1.0 to make a "disk graph"
    final double NODE_COMM_RANGE = 1.0;

    // Node generation methods
    final int SPREAD = 0;
    final int GROWTH = 1;

    private Random prng = null;
    private HashSet<Node> graph;


    public Manet(long prng_seed) {
        this.prng = new Random(prng_seed);
        this.graph = new HashSet<Node>();
    }


    // Convenience Layer Functions
    public void generateNode() { generateNodeNaive(); }

    /**
     * Generates a new node using the internal PRNG to generate coordinates.
     * Currently, the PRNG uses a normal distribution. If the node is outside
     * the allowed communication range, it will "walk" towards the closest 
     * node in the graph. This is done to simulate a device "approaching" the
     * network physically.
     *
     * Neighbor connections are then established. If the node "walked" in
     * to the graph's range, there will probably be only one neighbor.
     *
     * TODO: 2D poisson, controlled "honeycomb"-like?
     **/
    private void generateNodeNaive() {
        // Generate an x and y within the world coordinates
        double x = prng.nextDouble();
        double y = prng.nextDouble();

         // Translate that with respect to the total range of the network
        x = (x * WORLD_LIMIT) - (WORLD_LIMIT / 2);
        y = (y * WORLD_LIMIT) - (WORLD_LIMIT / 2);

        Node new_node = new Node(x, y, NODE_COMM_RANGE);

        // Keep track of the closest just in case we don't find one in range
        double minimum_distance = Double.MAX_VALUE;
        Node closest_node = null;

        // If this is the first node added, we're done here
        if (graph.isEmpty()) {
            graph.add(new_node);
            return;
        }

        // Go through each node in the graph. If it's within range, connect
        for (Node node : graph) {
            double distance = Point2D.distance(x, y, node.getX(), node.getY());
            
            if (distance < minimum_distance) {
                minimum_distance = distance;
                closest_node = node;
            }

            if (node.canCommunicate(new_node)) {
                node.addNeighbor(new_node);
                new_node.addNeighbor(node);
            }
        }

        // If we didn't find one within range, attach to the closest
        if (new_node.getNeighbors().isEmpty()) {
            // Right-angled triangles with the same points are similar
            // So, get the ratio, and scale the difference in coordinates
            double ratio = NODE_COMM_RANGE / minimum_distance;
            
            double walk_x = 0;
            double walk_y = 0;

            // Do checks to make sure the line orientation is l->r top->bot
            if (new_node.getX() >= closest_node.getX()) {
                walk_x = closest_node.getX() + ((new_node.getX() - closest_node.getX()) * ratio);
            } else {
                walk_x = closest_node.getX() - ((closest_node.getX() - new_node.getX()) * ratio);
            }

            if (new_node.getY() >= closest_node.getY()) {
                walk_y = closest_node.getY() + ((new_node.getY() - closest_node.getY()) * ratio);
            } else {
                walk_y = closest_node.getY() - ((closest_node.getY() - new_node.getY()) * ratio);
            }            
            
            Node orig = new_node;
            new_node = new Node(walk_x, walk_y, NODE_COMM_RANGE);

            if(!new_node.canCommunicate(closest_node)){
                double new_distance = Point2D.distance(walk_x, walk_y, closest_node.getX(), closest_node.getY());
                System.err.println("Logical Error in Node Generation: Incorrect Computation.");
                System.err.println("  New Node: (" + orig.getX() + ", " + orig.getY() + ")");
                System.err.println("  Closest : (" + closest_node.getX() + ", " + closest_node.getY() + ")");
                System.err.println("  Adjusted: (" + walk_x + ", " + walk_y + ")");
                System.err.println("  Distance: " + new_distance);
                System.exit(1);
            }

            new_node.addNeighbor(closest_node);
            closest_node.addNeighbor(new_node);
        }

        graph.add(new_node);
    }


    // This doesn't actually work.
    private void generateNodeGrowth() {
        // Generate an x and y within the world coordinates
        double x = 0;
        double y = 0;

        if (!graph.isEmpty()) {
            double theta = Math.toRadians((double)prng.nextInt(360));
            x = Math.sin(theta) * (WORLD_LIMIT / 2);
            y = Math.cos(theta) * (WORLD_LIMIT / 2);
        }
        return;
    }


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