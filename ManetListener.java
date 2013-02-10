
/**
 * ManetListener
 * 	This interface is used to define the listener functions used 
 * 	during network simulation
 *	
 *	addNodeCallback()
 *	removeNodeCallback()
 * 
 *
 */
public interface ManetListener {
	/**
	 * Called by Manet when a node is added
	 * @param node
	 */
    public void addNodeCallback(Node node);
    /**
     * Called by Manet when node is removed
     * @param node
     */
    public void removeNodeCallback(Node node);
}