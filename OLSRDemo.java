public class OLSRDemo {

    public static void generateGraph(int num_nodes, long seed) {
        Manet network = new UniformManet(seed);

        for(int i = 0; i < num_nodes; i++) {
            network.generateNode();
        }

        OLSRWrapper network_wrapper = new OLSRWrapper(network);
        network_wrapper.show();
        network_wrapper.showMPRs();

        // network_wrapper.floodBFS(false);
        System.out.println("Number of Nodes in the MANET: " + network_wrapper.getManetSize());
        System.out.println("Number of MPR Nodes         : " + network_wrapper.getMPRSet().size());
    }


    public static void main(String args[]) {
        /**if (args.length != 1) {
            usage();
            System.exit(1);
        }**/

        int num_nodes = 1000;
        try {
        	;
            //num_nodes = Integer.parseInt(args[0]);            
        } catch (Exception e) {
            usage();
            System.exit(1);
        }

        generateGraph(num_nodes, 192821236);
    }

    public static void usage() {
        System.out.println("Usage: java OLSRDemo.java <num_nodes>");
    }
}