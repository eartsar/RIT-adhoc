import java.util.HashSet;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Random;
import java.lang.Math;
import java.awt.geom.Point2D;

public abstract class ManetWrapper implements ManetListener{

    // Internal seed used when selecting random nodes to ping
    final long PING_PRNG_SEED = 123456789;
    private Random selector;

    protected Manet network;


    public ManetWrapper(Manet network) {
        this.network = network;
        this.network.setListener(this);
        selector = new Random(PING_PRNG_SEED);
    }


    public Node getRandomNode(Random prng) {
        double x = prng.nextDouble();
        double y = prng.nextDouble();
        x = (x * network.WORLD_LIMIT) - (network.WORLD_LIMIT / 2);
        y = (y * network.WORLD_LIMIT) - (network.WORLD_LIMIT / 2);

        double minimum_distance = Double.MAX_VALUE;
        Node closest_node = null;

        for (Node node : network.getGraph()) {
            double distance = Point2D.distance(x, y, node.getX(), node.getY());
            
            if (distance < minimum_distance) {
                minimum_distance = distance;
                closest_node = node;
            }
        }

        return closest_node;
    }


    public int floodBFS() { return floodBFS(false); }
    public int floodBFS(boolean verbose) {

        if (network.getGraph().isEmpty() ) {
            return 0;
        }

        Node closest_node = getRandomNode(selector);

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
        if( marked.size() != network.getGraph().size() ) {
            System.err.println("Logical Error in Flood BFS: Incorrect Visited.");
            System.err.println("  The number of nodes visited in the BFS is not the size of the graph.");
            System.exit(1);
        }
        return num_layers;
    }


    public void show() { this.network.show(); }
    public Iterator<Node> iterator() { return this.network.iterator(); }

    public void pruneNode() { this.network.removeLastNode(); }

    public abstract void floodPing();
    public abstract void addNodeCallback(Node node);
    public abstract void removeNodeCallback(Node node);
}