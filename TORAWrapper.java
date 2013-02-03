import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import edu.rit.numeric.ListXYSeries;
import edu.rit.numeric.plot.Plot;


public class TORAWrapper extends ManetWrapper {
	
	
    HashMap<Node, Integer> QRY_sent_counter;
    HashMap<Node, Integer> QRY_rec_counter;
    HashMap<Node, Integer> UPD_sent_counter;
    HashMap<Node, Integer> UPD_rec_counter;
    
    HashMap<Node, Boolean> route_required_Bit;
    //Map of each node's route_request bit. Each node stores an RR bit for each 
    //	possible destination node in the network.
    //	<source, <destination, set/not>>
    HashMap<Node, HashSet<Node>> routeReq_Dest_bit;
	
	
    public TORAWrapper(Manet network) {
    	super(network);
    	
    	this.QRY_sent_counter = new HashMap<Node, Integer>();
    	this.QRY_rec_counter = new HashMap<Node, Integer>();
    	this.UPD_sent_counter = new HashMap<Node, Integer>();
    	this.UPD_rec_counter = new HashMap<Node, Integer>();
    	
    	//Represents the RRbit, true=node already received qry; false=not received qry
//    	this.route_required_Bit = new HashMap<Node, Boolean>();
    	
    	this.routeReq_Dest_bit = new HashMap<Node, HashSet<Node>>();
    	for (Node node : network) {
			this.routeReq_Dest_bit.put(node, new HashSet<Node>());
			this.QRY_rec_counter.put(node, 0);
			this.QRY_sent_counter.put(node, 0);
			this.UPD_rec_counter.put(node, 0);
			this.UPD_sent_counter.put(node, 0);
		}
    }


    public LinkedList<Node> ping(Node source, Node destination) {
    	HashMap<Node, Node> predecessors = new HashMap<Node, Node>();
        LinkedList<Node> queue = new LinkedList<Node>();

        queue.add(source);
        predecessors.put(source, null);

        // BFS loop
        while (!queue.isEmpty()) {
            Node current = queue.removeFirst();

            // Are we there yet?
            if (current == destination) {
                // generate the path and return it here
                constructPath(predecessors, destination);
            }

            // Go through every neighbor of the current node
            for (Node neighbor : current.getNeighbors()) {

                // If we've seen the node before, throw away the packet
                if(predecessors.containsKey(neighbor)) {
                    continue;
                }

                predecessors.put(neighbor, current);
                queue.add(neighbor);
            }
        }

        // If we get here then that means it's not a fully connected graph. That's bad.
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
    
    
    public LinkedHashSet<Node> recurseQueries(Node source, Node destination) {
    	
    	//Check Neighbors; 
    	HashSet<Node> neighbors = source.getNeighbors();
    	//HashSet to hold all neighbors which have their own neighbors
    	HashSet<Node> newSources = new HashSet<Node>();
    	//HashSet to hold backrack process from dest > source
    	LinkedHashSet<Node> backtrack = new LinkedHashSet<Node>();
    	
    	System.out.println("Called: " + source.toString());
    	if (source == destination) {
    		//Start backtrack process
    		backtrack.add(source);
    		return backtrack;
    	}
    	else {

    		if (!neighbors.isEmpty()) {
    			if (hasUnmetNeighbors(source, destination) == true) {

    				//Check if isLastNeighbor, start sending UPD

    				for (Node neighbor : neighbors) {
    					if (sendQuery(source, destination, neighbor) == true) {
    						//add to HashSet to flood to child nodes
    						newSources.add(neighbor);
    						System.out.println("new neighbors: " + neighbor.toString());
    					}
    					else {
    						//Node already received QRY
    					}
    				}

    				//loop through the hashset created above:
    				for (Node newNeighbor : newSources) {
    					System.out.println("calling neighbors: " + newNeighbor.toString());
    					backtrack =  recurseQueries(newNeighbor, destination);
    					if(backtrack == null) {
//    						return null;
    					}
    					else {
    						backtrack.add(newNeighbor);
    						return backtrack;
    					}

    				}
    			}
    		}
    	}
    	
//    	return neighbors;
    	return null;
    	
    }
    
    
    public boolean hasUnmetNeighbors(Node source, Node destination) {
    	//This function checks if a node has any neighbors who havent' 
    	//	heard the qry.
    	
    	//Get Neighbors; 
    	HashSet<Node> neighbors = source.getNeighbors();
    	//Set to hold all non RRbit nodes
    	HashSet<Node> newNodes = new HashSet<Node>();
    	boolean unmetNeighborFlag = false;
    	
    	if (!neighbors.isEmpty()) {
	    	for (Node neighbor : neighbors) {
				
	    		//If the nodes are held within HashMap<neighbor, HashSet<destination>>
	    		//Then they have already been visited by a qry
	    		if (!this.routeReq_Dest_bit.get(neighbor).contains(destination)) {
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
    public boolean sendQuery(Node source, Node destination, Node neighbor) {
    	
    	boolean RR_alreadyAsked = false;
    	if (this.routeReq_Dest_bit.get(neighbor).contains(destination)) {
    		RR_alreadyAsked = true;
    	}
    		
    	
    	//send query from source node to it's neighbor
    	//Check if QRY RR bit is set: sendQRY returns true if already sent, false if not
    	
    	//Check if RR bit is set; if already set ignore this qry message:
    	if (RR_alreadyAsked == false) {
    	
	    	//increment Qry sent counter
	    	this.QRY_sent_counter.put(source, (this.QRY_sent_counter.get(source)+1));
	    	//increment Qry recieved counter
	    	this.QRY_rec_counter.put(source, (this.QRY_rec_counter.get(source)+1));
	    	
	    	//Set RR flag to prevent duplicate Queries
//	    	this.route_required_Bit.put(source, true);
	    	this.routeReq_Dest_bit.get(neighbor).add(destination);
	    	
	    	//Return true because route bit was not already set
	    	return true;
    	}
    	else {
    		//return false b/c already received qry
    		return false;
    		
    	}
    }

    
    public void showLink (Node source, Node dest) {
    	//Print the linked source to dest nodes:
    	
    	LinkedList<Node> StoDpath = new LinkedList<Node>();
    	
    	StoDpath = ping(source, dest);
    	System.out.println();
    	System.out.println();
    	System.out.println();
    	
    	String result = "Source: " + source.getX() + ", " + source.getY() + "\n";
    	//Display the path
    	for (Node jumpNode : StoDpath) {
			result += "(" + jumpNode.getX() + ", " + jumpNode.getY() + ")\n";
		}
    	result += "Destination: " + dest.getX() + ", " + dest.getY() + "\n"; 
    	
    	System.out.println(result);
    	
    	int totQRY = 0;
    	int totUPD = 0;
    	
    	for (Node node : this.network) {
			totQRY += this.QRY_sent_counter.get(node).intValue();
		}
    	for (Node node : this.network) {
			totQRY += this.UPD_sent_counter.get(node).intValue();
		}
    	
    	System.out.println("Total QRY packets sent: " + totQRY);
    	System.out.println("Total UPD packet sent: " + totUPD);
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