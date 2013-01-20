import java.awt.geom.Point2D;

public class UniformManet extends Manet {

    public UniformManet(long prng_seed) {
        super(prng_seed);
    }

    /**
     * Generates a new node using the internal PRNG to generate coordinates.
     * The PRNG uses a uniform distribution. If the node is outside the allowed
     * communication range of all nodes, it will "walk" towards the closest 
     * node in the graph. This is done to simulate a device "approaching" the
     * network physically.
     **/
    public void generateNode() {
        // Generate an x and y within the world coordinates
        double x = super.prng.nextDouble();
        double y = super.prng.nextDouble();

         // Translate that with respect to the total range of the network
        x = (x * super.WORLD_LIMIT) - (super.WORLD_LIMIT / 2);
        y = (y * super.WORLD_LIMIT) - (super.WORLD_LIMIT / 2);

        Node new_node = new Node(x, y, super.NODE_COMM_RANGE);

        // Keep track of the closest just in case we don't find one in range
        double minimum_distance = Double.MAX_VALUE;
        Node closest_node = null;

        // If this is the first node added, we're done here
        if (super.graph.isEmpty()) {
            super.addNode(new_node);
            return;
        }

        // Go through each node in the graph. If it's within range, connect
        for (Node node : super.graph) {
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
            double ratio = super.NODE_COMM_RANGE / minimum_distance;
            
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
            new_node = new Node(walk_x, walk_y, super.NODE_COMM_RANGE);

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

        super.addNode(new_node);
    }
}