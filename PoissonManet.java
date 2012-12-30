import java.util.ArrayList;
import java.util.Random;
import java.lang.Math;
import java.awt.geom.Point2D;

public class PoissonManet extends Manet {

    // POISSON CONSTANTS
    // basically, the minimum distance between nodes
    final double CELL_SIZE = 0.2;
    // defines how tightly nodes are packed around one another, small = sparse
    final int PACKING_SIZE = 10;

    // POISSON VARIABLES
    private int grid_dim_size;
    // The grid defines a "field" around a node that cannot contain another node
    private Node[][] grid;

    // These variable keep track of what iteration the point generation is on
    // They retain state between multiple generateNode calls, don't touch!
    private int pack_iteration;
    private int dequeue_index;

    // Seed for the random queue extraction
    final long RANDOM_QUEUE_SEED = 987654321;

    private Random queue_prng;
    private ArrayList<Node> random_queue;


    public PoissonManet(long prng_seed) {
        super(prng_seed);

        this.queue_prng = new Random(RANDOM_QUEUE_SEED);
        this.random_queue = new ArrayList<Node>();

        this.grid_dim_size = (int)(Math.ceil(super.WORLD_LIMIT / CELL_SIZE));
        this.grid = new Node[this.grid_dim_size][this.grid_dim_size];

        // Initialize the grid structure
        for (int i = 0; i < this.grid_dim_size; i++) {
            for (int j = 0; j < this.grid_dim_size; j++) {
                this.grid[i][j] = null;
            }
        }
    }


    public void generateNode() {

        // Add in the first node, if no nodes have been added
        // For now, start at 0, 0
        if (super.graph.isEmpty()) {
            Node init_node = new Node(0.0, 0.0, super.NODE_COMM_RANGE);
            super.graph.add(init_node);
            addToGrid(init_node);
            random_queue.add(init_node);

            pack_iteration = 0;
            dequeue_index = queue_prng.nextInt(random_queue.size());
            return;
        }

        // If there's no more space...
        if (random_queue.isEmpty()) {
            System.err.println("GRAPH IS FULL - TODO: DECREASE RANGE");
            return;
        }

        Node rand_node = random_queue.get(dequeue_index);
        Node new_node = null;

        // Flag for knowing if things worked. Will recursively call if not true
        boolean node_generated = false;

        // Generate new_node as a node around rand_node
        double angle_mod = super.prng.nextDouble();
        double radius_mod = super.prng.nextDouble();

        // radius from the point between CELL_SIZE and 2*CELL_SIZE
        double radius = CELL_SIZE * (radius_mod + 1);
        double angle = 2 * Math.PI * angle_mod;

        double x = rand_node.getX() + radius + Math.cos(angle);
        double y = rand_node.getY() + radius + Math.sin(angle);

        new_node = new Node(x, y, super.NODE_COMM_RANGE);

        // Check its neighborhood
        int r_start = Math.max(0, getGridRow(y) - 2);
        int c_start = Math.max(0, getGridCol(x) - 2);
        int r_end = Math.min(r_start + 4, this.grid_dim_size);
        int c_end = Math.min(c_start + 4, this.grid_dim_size);

        boolean neighborhood_vacant = true;
        for (int r = r_start; r < r_end; r++) {
            for (int c = c_start; c < c_end; c++) {
                if (this.grid[r][c] != null) {
                    if (Point2D.distance(x, y, grid[r][c].getX(), grid[r][c].getY()) > CELL_SIZE) {
                        neighborhood_vacant = false;
                    }
                }
            }
        }

        // Check to make sure we're within the world bounds...
        boolean within_world = true;
        if (!(new_node.getX() > (-1 * super.WORLD_LIMIT / 2.0)) ||
            !(new_node.getX() < (super.WORLD_LIMIT / 2.0)) || 
            !(new_node.getY() > (-1 * super.WORLD_LIMIT / 2.0)) ||
            !(new_node.getY() < (super.WORLD_LIMIT / 2.0))) {
            within_world = false;
        }

        // If it's okay to add the node here
        if (neighborhood_vacant && within_world) {

            // First, connect the neighbors. What a hassle...
            for (Node candidate : this.graph) {
                if (new_node.canCommunicate(candidate)) {
                    new_node.addNeighbor(candidate);
                    candidate.addNeighbor(new_node);
                }
            }

            // Add the node to the graph, grid, and queue of neighbor-spawners
            super.graph.add(new_node);
            addToGrid(new_node);
            random_queue.add(new_node);
            node_generated = true;
        }

        // This is like the code to be done at the end of a for loop
        pack_iteration ++;
        if (pack_iteration == PACKING_SIZE) {
            pack_iteration = 0;
            random_queue.remove(dequeue_index);
            dequeue_index = queue_prng.nextInt(random_queue.size());
        }

        // If the point was invalid, try again. We don't want this in a loop
        // because we want to only generate one at a time, but don't know how
        // many iterations until it works.
        if (!node_generated) {
            generateNode();
        }

        // If we got here, then it means we found a good node position
        // Explicit return for clarity
        return;
    }


    private void addToGrid(Node node) {
        double x = node.getX();
        double y = node.getY();
        // Convert from x-y to r-c
        int row = getGridRow(y);
        int col = getGridCol(x);

        this.grid[row][col] = node;
    }

    private int getGridRow(double y) {
        double row_precise = ((super.WORLD_LIMIT / 2.0) - y) * (1.0 / CELL_SIZE);
        if(row_precise < 0) { row_precise = Math.abs(row_precise); }
        int row = (int)row_precise;

        return row;
    }

    private int getGridCol(double x) {
        double col_precise = (x + (super.WORLD_LIMIT / 2.0)) * (1.0 / CELL_SIZE);
        if(col_precise < 0) { col_precise = Math.abs(col_precise); }
        int col = (int)col_precise;

        return col;
    }
}