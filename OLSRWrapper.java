import java.util.HashSet;
import java.util.HashMap;
import java.util.Random;


public class OLSRWrapper extends ManetWrapper {

    // seed specifically for the MPR generation
    final long MPR_PRNG_SEED = 1122334455;

    HashMap<Node, Double> tp_timer;

    HashMap<Node, Integer> hello_sent_counter;
    HashMap<Node, Integer> hello_recv_counter;
    HashMap<Node, Integer> tc_sent_counter;
    HashMap<Node, Integer> tc_recv_counter;

    HashSet<Node> mpr_set;

    Random mpr_prng;


    public OLSRWrapper(Manet network) {
        super(network);
        this.tp_timer = new HashMap<Node, Double>();
        this.hello_sent_counter = new HashMap<Node, Integer>();
        this.hello_recv_counter = new HashMap<Node, Integer>();
        this.tc_sent_counter = new HashMap<Node, Integer>();
        this.tc_recv_counter = new HashMap<Node, Integer>();
    
        this.mpr_prng = new Random(this.MPR_PRNG_SEED);
        this.mpr_set = findMPRs(getRandomNode(mpr_prng));

        // Initialize all the counters for metrics
        for (Node node : this.network.getGraph()) {
            hello_sent_counter.put(node, 0);
            hello_recv_counter.put(node, 0);
            tc_sent_counter.put(node, 0);
            tc_recv_counter.put(node, 0);
        }
    }


    public void floodPing() {

    }


    public void floodTopology(Node source) {
        HashSet<Node> remaining = new HashSet<Node>(this.network.getGraph());
        remaining.remove(source);

        while (!remaining.isEmpty()) {
            // TODO: do pseudo-BFS
        }
    }


    public HashSet<Node> findMPRs(Node source) {
        return findMPRs(source, new HashSet<Node>());
    }


    // TODO: use counters
    public HashSet<Node> findMPRs(Node source, HashSet<Node> visited) {

        // N1 layer is just neighbors of source
        HashSet<Node> n_one = source.getNeighbors();
        // N2 layer is the layer of neighbors from the source
        HashSet<Node> n_two = new HashSet<Node>();
        // List of selected nodes for the MPRs on this layer
        HashSet<Node> selectedMPRs = new HashSet<Node>();
        // Coverage seen by MPRs
        HashSet<Node> coverage = new HashSet<Node>();
        coverage.addAll(visited);

        // Base Case
        // If there are no "unseen" nodes from this source, return an empty list
        HashSet<Node> unseen = new HashSet<Node>(n_one);
        unseen.removeAll(coverage);
        if (unseen.isEmpty()) {
            return selectedMPRs;
        }

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


        for (Node mpr : selectedMPRs) {
            // Add the MPRs that come back from the recursion
            selectedMPRs.addAll(findMPRs(mpr, visited));
        }

        return selectedMPRs;
    }


    public void addNodeCallback(Node node) {}
    public void removeNodeCallback(Node node) {}

}