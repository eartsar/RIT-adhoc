import java.util.HashSet;

public class OLSRWrapper extends ManetWrapper implements ManetListener {


    public OLSRWrapper(Manet network) {
        super(network);
    }


    public void floodPing() {

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
            for (Node neighbor : n.getNeighbors()) {
                if (neighbor != source) {
                    n_two.add(neighbor);
                }
            }
        }

        coverage.add(source);
        coverage.addAll(n_one);

        // Single-neighbor nodes in N2
        for (Node n : n_one) {
            for (Node neighbor : n.getNeighbors()) {
                if (neighbor.numNeighbors() == 1) {
                    coverage.add(neighbor);
                    selectedMPRs.add(n);
                }
            }
        }

        while (!coverage.containsAll(n_two)) {

            // Go through all remaining in the N1 layer, find the one with the
            // most amount of uncovered neighbors
            HashSet<Node> remaining = new HashSet<Node>(n_one);
            remaining.removeAll(selectedMPRs);

            Node maximum = null;
            int max_num = 0;

            for (Node n : remaining) {
                if (maximum == null) { maximum = n; }

                HashSet<Node> neighbors_clone = new HashSet<Node>(n.getNeighbors());
                neighbors_clone.removeAll(coverage);

                if (neighbors_clone.size() > max_num) {
                    maximum = n;
                    max_num = neighbors_clone.size();
                }
            }

            selectedMPRs.add(maximum);
            coverage.addAll(maximum.getNeighbors());
        }

        return selectedMPRs;
    }


    public void addNodeCallback() {}
    public void removeNodeCallback() {}

}