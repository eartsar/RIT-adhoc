import java.util.Random;
import java.util.HashSet;
import java.util.TreeMap;
import java.awt.geom.Point2D;

public class Manet {

    // Maximum size of the "world" of the MANET
    final double WORLD_LIMIT = 10.0;
    // Internal seed used when selecting random nodes to ping
    final long PING_PRNG_SEED = 123456789;
    // Keep this at 1.0 to make a "disk graph"
    final double NODE_COMM_RANGE = 1.0;

    private Random prng = null;
    private HashSet<Node> graph;


    public Manet(long prng_seed) {
        this.prng = new Random(prng_seed);
        this.graph = new HashSet<Node>();
    }


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
     * TODO: We should discuss this method of generation. It was the best I
     * came up with.
     **/
    public void generateNode() {
        // Generate an x and y between -1 and 1
        double x = prng.nextGaussian();
        double y = prng.nextGaussian();

        // Translate that with respect to the total range of the network
        x = x * (WORLD_LIMIT / 2.0);
        y = y * (WORLD_LIMIT / 2.0);
        Node new_node = new Node(x, y, NODE_COMM_RANGE);

        // Keep track of the closest just in case we don't find one in range
        double minimum_distance = Double.MAX_VALUE;
        Node closest_node = null;

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


        // Check to see if we found one within range
        if (new_node.getNeighbors().isEmpty()) {
            // TODO: Do the walk
        }
    }
}