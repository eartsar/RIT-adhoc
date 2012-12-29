import java.util.HashSet;
import java.awt.geom.Point2D;

public class Node {

    private double x, y, comm_range;
    private HashSet<Node> neighbors = null;


    public Node(double x, double y, double comm_range) {
        this.x = x;
        this.y = y;
        this.comm_range = comm_range;
        this.neighbors = new HashSet<Node>();
    }


    /**
     * Adds a neighbor to the set of neighbors contained in this node.
     *
     * @return true if the neighbor was sucecssfully added.
     **/
    public boolean addNeighbor(Node neighbor) {
        return this.neighbors.add(neighbor);
    }


    /**
     * Removes a neighbor to the set of neighbors contained in this node.
     *
     * @return true if the neighbor was successfully removed.
     **/
    public boolean removeNeighbor(Node neighbor) {
        return this.neighbors.remove(neighbor);
    }


    /**
     * Checks to see if this node can communicate with another node.
     *
     * @return true if the distance of this node to the other node is less 
     * than or equal to both nodes' range.
     **/
    public boolean canCommunicate(Node other) {
        double distance = Point2D.distance(this.x, this.y, other.x, other.y);
        return distance <= comm_range && distance <= other.comm_range;
    }

    public double getX() { return this.x; }
    public double getY() { return this.y; }
    public double getRange() { return this.comm_range; }
    public HashSet<Node> getNeighbors() { return this.neighbors; }

}