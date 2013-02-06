import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.awt.BasicStroke;
import edu.rit.numeric.plot.Plot;
import edu.rit.numeric.ListXYSeries;


public class OLSRWrapper extends ManetWrapper {

    // seed specifically for the MPR generation
    final long MPR_PRNG_SEED = 1122334455;
    final int ADD_NODE_INTERVAL_LIMIT = 8;
    final int TC_INTERVAL = 2;

    HashMap<Node, Double> tp_timer;

    HashMap<Node, Integer> tc_recv_counter;

    HashSet<Node> mpr_set;

    Random mpr_prng;

    public OLSRWrapper(Manet network) {
        super(network);
        this.tp_timer = new HashMap<Node, Double>();
        this.tc_recv_counter = new HashMap<Node, Integer>();

        this.mpr_prng = new Random(this.MPR_PRNG_SEED);
        this.mpr_set = findMPRs(getRandomNode(mpr_prng));

        // Initialize all the counters for metrics
        for (Node node : this.network.getGraph()) {
            tc_recv_counter.put(node, 0);
        }
    }


    public int getTotalPacketsRecieved() {
        int total = 0;
        for (Node node : tc_recv_counter.keySet()) {
            total = total + tc_recv_counter.get(node);
        }

        return total;
    }


    public void floodPing() {

    }


    // Return the number of messages it takes to ping
    public LinkedList<Node> ping(Node source, Node destination) {
        LinkedList<Node> queue = new LinkedList<Node>();
        HashSet<Node> visited = new HashSet<Node>();
        HashMap<Node, Node> predecessors = new HashMap<Node, Node>();

        queue.add(source);
        predecessors.put(source, null);
        visited.add(source);

        // Perform a modified BFS to just get the number of hops
        // from source to destination.
        while (!queue.isEmpty()) {
            Node t = queue.removeFirst();
            
            // if we got there, return number of hops
            if(t == destination) {
                return constructPath(predecessors, destination);
            }


            for (Node neighbor : t.getNeighbors()) {
                // If seen already, skip
                if (visited.contains(neighbor)) { continue; }
                else { 
                    visited.add(neighbor);
                    predecessors.put(neighbor, t);
                }


                if (neighbor == destination) {
                    return constructPath(predecessors, destination);
                }

                // if it's just a normal node, ignore it
                if (!mpr_set.contains(neighbor)) {
                    visited.add(neighbor);
                    continue;
                }

                queue.add(neighbor);
                visited.add(neighbor);
            }
        }
        System.out.println("Error - Could not reach destination.");
        return null;
    }


    // Path generation helper for ping
    private LinkedList<Node> constructPath(HashMap<Node, Node> predecessors, Node destination) {
        Node current = destination;
        LinkedList<Node> path = new LinkedList<Node>();

        while (current != null) {
            current = predecessors.get(current);
            path.push(current);
        }

        return path;
    }



    public void floodTopology(Node source, int reps) { 
        HashSet<Node> visited = floodTopology(source, new HashSet<Node>(), reps);
        return;
    }


    public HashSet<Node> floodTopology(Node source, HashSet<Node> visited, int reps) {
        if(visited.contains(source)) {
            return visited;
        }
        visited.add(source);


        // Message recieved
        int msg = tc_recv_counter.get(source) + reps;
        tc_recv_counter.put(source, msg);   


        // From here on is the default forwarding protocol

        // If this is not an MPR, just return and don't propegate.
        if (!this.mpr_set.contains(source)) {            
            return visited;
        }

        // N1 layer is just neighbors of source
        HashSet<Node> n_one = source.getNeighbors();
        // N2 layer is layer of N1's neighbors
        HashSet<Node> n_two = new HashSet<Node>();
        
        HashSet<Node> coverage = new HashSet<Node>(visited);

        // If there are no "unseen" nodes from this source, return
        HashSet<Node> unseen = new HashSet<Node>(n_one);
        unseen.removeAll(coverage);
        if (unseen.isEmpty()) {
            return visited;
        }

        HashSet<Node> next_mpr = new HashSet<Node>(this.mpr_set);
        next_mpr.removeAll(visited);
        if (next_mpr.isEmpty()) {
            return visited;
        }

        for (Node mpr : next_mpr) {
            visited = floodTopology(mpr, visited, reps);
        }

        return visited; 
    }



    public HashSet<Node> findMPRs(Node source) {
        return findMPRs(source, new HashSet<Node>());
    }


    public HashSet<Node> findMPRs(Node source, HashSet<Node> coverage) {

        // N1 layer is just neighbors of source
        HashSet<Node> n_one = source.getNeighbors();
        // N2 layer is the layer of neighbors from the source
        HashSet<Node> n_two = new HashSet<Node>();
        // List of selected nodes for the MPRs on this layer
        HashSet<Node> selectedMPRs = new HashSet<Node>();

        // Add the initial node to the MPR set.
        if(coverage.isEmpty()) {
            selectedMPRs.add(source);
            coverage.add(source);
        }

        // Generate N2
        for (Node n : n_one ) {
            for (Node neighbor : n.getNeighbors()) {
                if (neighbor != source && !n_one.contains(neighbor)) {
                    n_two.add(neighbor);
                }
            }
        }

        // Base Case
        // If there are no "unseen" nodes from this source, return an empty list
        HashSet<Node> unseen = new HashSet<Node>(n_one);
        unseen.addAll(n_two);
        
        unseen.removeAll(coverage);
        if (unseen.isEmpty()) {
            return selectedMPRs;
        }


        coverage.add(source);
        coverage.addAll(n_one);

        // Single-neighbor nodes in N2
        for (Node n : n_one) {
            for (Node neighbor : n.getNeighbors()) {
                if (neighbor == source || n_one.contains(neighbor)) {
                    continue;
                }
                if (neighbor.numNeighbors() == 1) {
                    selectedMPRs.add(n);
                }
            }

            if (selectedMPRs.contains(n)) {
                coverage.addAll(n.getNeighbors());
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


        HashSet<Node> retSet = new HashSet<Node>();
        retSet.addAll(selectedMPRs);
        for (Node mpr : selectedMPRs) {
            if (mpr != source) {
                // Add the MPRs that come back from the recursion
                retSet.addAll(findMPRs(mpr, coverage));
            }
        }

        return retSet;
    }


    public void addNodeCallback(Node node) {
        tc_recv_counter.put(node, 0);
        
        // Find new MPRs
        boolean update = true;
        for (Node neighbor : node.getNeighbors()) {
            if (this.mpr_set.contains(neighbor)) {
                update = false;
            }
        }

        if (update) {
            this.mpr_set = findMPRs(getRandomNode(mpr_prng));
        }


        int num_reps = 1;

        for (Node mpr : this.mpr_set) {
            floodTopology(mpr, num_reps);
        }
    }


    public void removeNodeCallback(Node node) {
        tc_recv_counter.put(node, 0);
        
        // Find new MPRs
        this.mpr_set = findMPRs(getRandomNode(mpr_prng));

        int num_reps = 1;

        for (Node mpr : this.mpr_set) {
            floodTopology(mpr, num_reps);
        }
    }

    public void showMPRs() {
        Plot csclPlot = new Plot();
        csclPlot.plotTitle("Graphical Representation of this MANET's MBRs");
        csclPlot.xAxisStart(-1 * (network.WORLD_LIMIT / 2.0));
        csclPlot.xAxisEnd(1 * (network.WORLD_LIMIT / 2.0));
        csclPlot.yAxisStart(-1 * (network.WORLD_LIMIT / 2.0));
        csclPlot.yAxisEnd(1 * (network.WORLD_LIMIT / 2.0));
        
        ListXYSeries series = new ListXYSeries();

        for (Node mpr : this.mpr_set) {
            series.add(mpr.getX(), mpr.getY());
        }

        csclPlot.seriesStroke(null);
        csclPlot.xySeries(series);
        csclPlot.getFrame().setVisible(true);
    }


    public HashSet<Node> getMPRSet() { return this.mpr_set; }
    public int getManetSize() { return this.network.getGraph().size(); }

}