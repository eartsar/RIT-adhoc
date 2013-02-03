import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;


public class TORAWrapper extends ManetWrapper {
	
	
    HashMap<Node, Integer> QRY_sent_counter;
    HashMap<Node, Integer> QRY_rec_counter;
    HashMap<Node, Integer> UPD_sent_counter;
    HashMap<Node, Integer> UPD_rec_counter;
    
    HashMap<Node, Boolean> route_required_Bit;
    //Map of each node's route_request bit. Each node stores an RR bit for each 
    //	possible destination node in the network.
    //	<source, <destination, set/not>>
    HashMap<Node, HashMap<Node, Boolean>> routeReq_Dest_bit;
	
	
    public TORAWrapper(Manet network) {
    	super(network);
    	
    	this.QRY_sent_counter = new HashMap<Node, Integer>();
    	this.QRY_rec_counter = new HashMap<Node, Integer>();
    	this.UPD_sent_counter = new HashMap<Node, Integer>();
    	this.UPD_rec_counter = new HashMap<Node, Integer>();
    	
    	//Represents the RRbit, true=node already received qry; false=not received qry
    	this.route_required_Bit = new HashMap<Node, Boolean>();
    	this.routeReq_Dest_bit = new HashMap<Node, HashMap<Node, Boolean>>();
    	
    }


    public HashSet<Node> findPathToDestination(Node source, Node destination) {
    	
    	HashSet<Node> destPath = new HashSet<Node>();
    	
    	//Check Neighbors; send QRY packet
//    	HashSet<Node> neighbors = source.getNeighbors();
//    	
//    	for (Node neighbor : neighbors) {
//			source.sendQRY(neighbor);	//TODO add sendQRY function
//		}
    	
    	//send QRY to all neighbor-neighbors:
    	recurseQueries(source, destination);
    	//Set the source bit of the original node
    	this.route_required_Bit.put(source, true);
    	
    	//Receiving update packet, add to destPath.
    	
    	//Returns the a/the set of nodes which create a path to the destination
    	return destPath;
    }
    
    
    public HashSet<Node> recurseQueries(Node source, Node destination) {
    	
    	//Check Neighbors; 
    	HashSet<Node> neighbors = source.getNeighbors();
    	//HashSet to hold all neighbors which have their own neighbors
    	HashSet<Node> newSources = new HashSet<Node>();
    	
    	if (source == destination) {
    		
    	}
    	
    	if (!neighbors.isEmpty()) {
    		if (hasUnmetNeighbors(source) == true) {
    			
    			//Check if isLastNeighbor, start sending UPD
    			
		    	for (Node neighbor : neighbors) {
					if (sendQuery(source, neighbor) == true) {
						//add to HashSet to flood to child nodes
						newSources.add(neighbor);
					}
					else {
						//Node already received QRY
					}
		    	}
		    	
		    	//loop through
		    	for (Node newNeighbor : newSources) {
					recurseQueries(newNeighbor, destination);
				}
    		}
    	}
    	
    	return neighbors;
    }
    
    
    public boolean hasUnmetNeighbors(Node source) {
    	//This function checks if a node has any neighbors who havent' 
    	//	hear the qry.
    	
    	//Get Neighbors; 
    	HashSet<Node> neighbors = source.getNeighbors();
    	//Set to hold all non RRbit nodes
    	HashSet<Node> newNodes = new HashSet<Node>();
    	boolean unmetNeighborFlag = true;
    	
    	if (!neighbors.isEmpty()) {
	    	for (Node neighbor : neighbors) {
				
	    		if (this.route_required_Bit.get(neighbor) == false) {
	    			//At least 1 unmet neighbor exists
	    			unmetNeighborFlag = true;
	    		}
			}
    	}
    	
    	return unmetNeighborFlag;
    }
    
    
//    public boolean isLastNeighbor(Node source) {
//    	//A node is the last one if it only has 1 neighbor?
//    	
//    	HashSet<Node> neighbors = source.getNeighbors();
////    	if (neighbors.size(source) == 1)
//    	
//    	return false;
//    }
    
    
    
    /**
     * This function takes in a source and destination node, 
     * 	If the RR bit of the source node is set then this 
     * @param source
     * @param neighbor
     * @return
     */
    public boolean sendQuery(Node source, Node neighbor) {
    	
    	boolean RRbit = this.route_required_Bit.get(neighbor); 
    	
    	//send query from source node to it's neighbor
    	//Check if QRY RR bit is set: sendQRY returns true if already sent, false if not
    	
    	//Check if RR bit is set; if already set ignore this qry message:
    	if (RRbit == false) {
    	
	    	//increment Qry sent counter
	    	this.QRY_sent_counter.put(source, (this.QRY_sent_counter.get(source)+1));
	    	//increment Qry recieved counter
	    	this.QRY_rec_counter.put(source, (this.QRY_rec_counter.get(source)+1));
	    	
	    	//Set RR flag to prevent duplicate Queries
	    	this.route_required_Bit.put(source, true);
	    	
	    	//Return true because route bit was not already set
	    	return true;
    	}
    	else {
    		//return false b/c already received qry
    		return false;
    		
    	}
    }


	@Override
	public void floodPing() {
		// TODO Auto-generated method stub
		
	}


//	public void addNodeCallback() {
//		// TODO Auto-generated method stub
//		
//	}
//
//
//	public void removeNodeCallback() {
//		// TODO Auto-generated method stub
//		
//	}


	@Override
	public void addNodeCallback(Node node) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void removeNodeCallback(Node node) {
		// TODO Auto-generated method stub
		
	}


}