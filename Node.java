import java.util.HashSet;
import java.awt.geom.Point2D;

/*
 * Node class used to define a single point in the network
 * 	A node represent a device that is part of an Ad-Hoc network.
 * 
 */
public class Node {

    private double x, y, comm_range;
    private HashSet<Node> neighbors = null;

    /**
     * Node(x, y, comm_range)
     * 	Each node has coordinates and a communication range to link to
     * 	other nearyby nodes.
     * As a simulated device in the world nodes can only see a limited 
     * 	sub-section of nodes in the entire network. These nodes are kept
     * 	track of as neighbors.
     */
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
        // The 1.01 is a tiny bit of leeway due to the inaccuracies precision
        return distance <= (comm_range * 1.01) && distance <= (other.comm_range * 1.01);
    }

    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }

    public double getX() { return this.x; }
    public double getY() { return this.y; }
    public double getRange() { return this.comm_range; }
    public HashSet<Node> getNeighbors() { return this.neighbors; }
    public int numNeighbors() { return this.neighbors.size(); }


}