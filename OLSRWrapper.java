import java.util.HashSet;
import java.util.Iterator;


public class OLSRWrapper {

    Manet network;

    public OLSRWrapper(Manet network) {
        this.network = network;
    }


    public HashSet<Node> findMPRs(Node source) {
        // N1 layer is just neighbors of source
        HashSet<Node> n_one = source.getNeighbors();
        // N2 layer is the layer of neighbors from the source
        HashSet<Node> n_two = new HashSet<Node>();
        // List of selected nodes for the MPRs
        HashSet<Node> selectedMPRs = new HashSet<Node>();
        // Coverage seen by MPRs
        HashSet<Node> coverage = new HashSet<Node>();

        // Generate N2
        for (Node n : n_one ) {
            if (n != source) {
                n_two.add(n);
            }
        }

        // Single-neighbor nodes in N2
        for (Node n : n_two) {
            if (n.numNeighbors() == 1) {
                coverage.add(n);
            }
        }

        return selectedMPRs;
    }




    public void show() { this.network.show(); }
    public Iterator<Node> iterator() { return this.network.iterator(); }

}