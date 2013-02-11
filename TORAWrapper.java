import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


/**
 * TORAWrapper extends the ManetWrapper class as the implementation of the 
 * 	Temporally Ordered Routing Algorithm (TORA) for a Mobile Ad-Hoc network 
 * 	(MANET).
 *
 * The TORA algorithm creates a Directed Acyclical Graph represented by:
 * 	listofLists. This structure is used to record the paths between a 
 * 	source and destination node when called with ping().
 * 
 * TORAWrapper also simulates the control packets that are sent between 
 * 	nodes:
 * 	Query Packets - sent from Src to find Dst
 * 	Update Packets - sent from Dst back to Src
 *
 */
public class TORAWrapper extends ManetWrapper {
	
	// The following Maps are used to store the number of overhead packets
	//	used by each node. Keeps both send & received packets.
    HashMap<Node, Integer> QRY_sent_counter;
    HashMap<Node, Integer> QRY_rec_counter;
    HashMap<Node, Integer> UPD_sent_counter;
    HashMap<Node, Integer> UPD_rec_counter;
    
    //Internal Directed Acyclical Graph (DAG) structure
    LinkedList<LinkedList<Node>> listOfPaths;
	
    /**
     * TORAWrapper(network, ping_seed)
     * @param network - Manet network graph
     * @param ping_seed - seed for generating random
     * 
     * Implements the TORA protocol on provided network.
     */
    public TORAWrapper(Manet network, long ping_seed) {
    	super(network, ping_seed);
    	
    	this.QRY_sent_counter = new HashMap<Node, Integer>();
    	this.QRY_rec_counter = new HashMap<Node, Integer>();
    	this.UPD_sent_counter = new HashMap<Node, Integer>();
    	this.UPD_rec_counter = new HashMap<Node, Integer>();
    	
    	this.listOfPaths = new LinkedList<LinkedList<Node>>();
    	
    }

    /**
     * ping(source, destination)
     * @param source - Node message ping sent from
     * @param destination - Node to receive the ping
     * 
     * For the TORA algorithm ping() does most of the computing.
     * 	In this function the DAG is created between the source
     * 	and destination nodes.
     * This function also calculates the overhead using both QRY
     * 	and UPD packets.
     */
    public LinkedList<Node> ping(Node source, Node destination) {
    	HashMap<Node, Node> predecessors = new HashMap<Node, Node>();
        LinkedList<Node> queue = new LinkedList<Node>();
        LinkedList<Node> result = new LinkedList<>();
        this.listOfPaths = new LinkedList<LinkedList<Node>>();

        queue.add(source);
        predecessors.put(source, null);
        
        // The process of sending QRY packets matches is similar to a BFS
        while (!queue.isEmpty()) {
            Node current = queue.removeFirst();
            
            // Reached destination node?
            if (current == destination) {
                // For each path the destination node add to the DAG
                this.listOfPaths.add(constructPath(predecessors, destination));
            }

            // Send QRY packet is broadcast to all neighboring nodes
            if (current != null) {
            	for (Node neighbor : current.getNeighbors()) {

            		//Add QRY packet for each added neighbor
            		incQRYSent(current);
            		incQRYRec(neighbor);

            		// If a node already has QRY packet, drops new one 
            		if(predecessors.containsKey(neighbor)) {
            			continue;
            		}

            		//Get each node ready to send to it's own neighbors
            		predecessors.put(neighbor, current);
            		queue.add(neighbor);

            	}
            }
        }
        
        //Check to see if there is a path between Src and Dst
        if ((this.listOfPaths != null) && (this.listOfPaths.size() != 0)) {
        	
        	//TORA provides multiple paths to a destination
        	result = this.listOfPaths.getFirst();
        	
        	// shortest path is used when available
        	int shortestLength = this.listOfPaths.get(0).size();
        	for (LinkedList<Node> linkedList : this.listOfPaths) {
				if (shortestLength > linkedList.size()) {
					shortestLength = linkedList.size();
					result = linkedList;
				}
			}
        }
        
        //Start link-reversal process, simulate backtracking UPD packets
        for (Map.Entry<Node, Node> entry : predecessors.entrySet()) {
        	if (entry.getValue() == null) {
        		continue;
        	}
        	else {
        		//Add UPD packets to overhead counters.
        		incUPDSent(entry.getKey());
        		incUPDRec(entry.getValue());
        	}
        }

        //Return path from source to destination
        return result;
    }


    /**
     * Path generation helper function, creates the path from destination to source
     * 
     * @param predecessors - HashMap<Node, Node> of linked nodes between Src & Dst
     * @param destination - Node 
     * @return LinkedList<Node> path from DST to Src
     */
    private LinkedList<Node> constructPath(HashMap<Node, Node> predecessors, Node destination) {
        Node current = destination;
        LinkedList<Node> path = new LinkedList<Node>();

        while (current != null) {
            current = predecessors.get(current);
            path.push(current);
        }

        return path;
    }
    
    /**
     * Helper function to increment a Nodes QRY_sent overhead by 1.
     * @param currentNode - node whose count to be incremented
     */
    public void incQRYSent(Node currentNode) {
    	if (this.QRY_sent_counter.containsKey(currentNode)) {
    		int tmp = this.QRY_sent_counter.get(currentNode) + 1;
        	this.QRY_sent_counter.put(currentNode, tmp);
    	}
    	else {
    		this.QRY_sent_counter.put(currentNode, 0);
    	}
    }

    
    /**
     * Helper function to increment a Nodes QRY_received overhead by 1.
     * @param currentNode - node whose count to be incremented
     */
    public void incQRYRec(Node currentNode) {
    	if (this.QRY_rec_counter.containsKey(currentNode)) {
    		int tmp = this.QRY_rec_counter.get(currentNode) + 1;
        	this.QRY_rec_counter.put(currentNode, tmp);
    	}
    	else {
    		this.QRY_rec_counter.put(currentNode, 0);
    	}
    }
    
    
    /**
     * Helper function to increment a Nodes UPD_sent overhead by 1.
     * @param currentNode - node whose count to be incremented
     */
    public void incUPDSent(Node currentNode) {
    	if (this.UPD_sent_counter.containsKey(currentNode)) {
    		int tmp = this.UPD_sent_counter.get(currentNode) + 1;
        	this.UPD_sent_counter.put(currentNode, tmp);
    	}
    	else {
    		this.UPD_sent_counter.put(currentNode, 0);
    	}
    }
    

    /**
     * Helper function to increment a Nodes UPD_received overhead by 1.
     * @param currentNode - node whose count to be incremented
     */
    public void incUPDRec(Node currentNode) {
    	if (this.UPD_rec_counter.containsKey(currentNode)) {
    		int tmp = this.UPD_rec_counter.get(currentNode) + 1;
        	this.UPD_rec_counter.put(currentNode, tmp);
    	}
    	else {
    		this.UPD_rec_counter.put(currentNode, 0);
    	}
    	
    }
    
    
    /**
     * Gets total number of QRY & UPD packets received by all nodes 
     * 	in the network
     * @return Integer count of total overhead
     */
    public int getTotalPacketsRecieved() {
    	int total = getQRYtotal() + getUPDtotal();
    	return total;
    }
    
    
    /**
     * Gets the total of all QRY packets received by all nodes in the network
     * @return Integer
     */
    public int getQRYtotal() {
    	int result = 0;
    	
    	//Add overhead for all current nodes
    	for (Node key : this.QRY_rec_counter.keySet()) {
			result += this.QRY_rec_counter.get(key);
		}
    	
    	return result;
    }
    
    
    /**
     * Gets the total of all QRY packets received by all nodes in the network
     * @return Integer
     */
    public int getUPDtotal() {
    	int result = 0;

    	//Add overhead for all current nodes
    	for (Node key : this.UPD_rec_counter.keySet()) {
			result += this.UPD_rec_counter.get(key);
		}
    	
    	return result;
    }
    
    
    /**
     * Clears all internal overhead counts for all nodes in the network
     */
    public void clearMetrics() {
    	int zero = 0;
    	for (Node key : this.UPD_rec_counter.keySet()) {
			this.QRY_rec_counter.put(key, zero);
			this.QRY_sent_counter.put(key, zero);
			this.UPD_rec_counter.put(key, zero);
			this.UPD_sent_counter.put(key, zero);
		}
    }


    /**
     * Called everytime a new node is added to the network.
     * 	simulates how the TORA protocl would interact with a new node
     * Increments overhead count 
     * 
     * @param node - Node new node added.
     */
	@Override
	public void addNodeCallback(Node node) {
		//Add node to Overhead counters
		if (!this.QRY_sent_counter.containsKey(node)) {
			this.QRY_sent_counter.put(node, 0);
			this.QRY_rec_counter.put(node, 0);
			this.UPD_sent_counter.put(node, 0);
			this.UPD_rec_counter.put(node, 0);
    	}
    	
		//UPD packets are used to communicate with a new node
		for (Node neighbor : node.getNeighbors()) {
			incUPDSent(neighbor);
			incUPDRec(node);
		}	
	}

	
	/**
     * Called everytime a node is removed from the network.
     * 	simulates how the TORA protocol handles dropped nodes
     * Increments overhead count 
     * 
     * @param node - Node to be removed
     */
	@Override
	public void removeNodeCallback(Node node) {
		
		//When a node is removed it either tells all neighboring nodes or
		// they ask if it still exists. These packets are simulated as updates
		for (Node neighbor : node.getNeighbors()) {
			incUPDSent(neighbor);
			incUPDRec(node);
		}
	}
}
