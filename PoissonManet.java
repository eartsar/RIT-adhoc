import java.awt.geom.Point2D;
import java.lang.Math;

public class PoissonManet extends Manet {

    // The premise of the grid is that each cell holds only one item
    private double cell_size;
    private double grid_dim_Size;
    private Node[][] grid;

    public PoissonManet(long prng_seed) {
        super(prng_seed);
        this.cell_size = super.NODE_COMM_RANGE / 4;

        this.grid_dim_size = (int)(Math.ceil(super.WORLD_LIMIT / this.cell_Size));
        this.grid = new Node[this.grid_dim_size][this.grid_dim_size];

        // Initialize the grid structure
        for (int i = 0; i < this.grid_dim_size; i++) {
            for (int j = 0; j < this.grid_dim_size; j++) {
                this.grid[i][j] = null;
            }
        }
    }


    public void generateNode() {

        // Add in the first node
        if (super.graph.isEmpty()) {

        }
    }
}