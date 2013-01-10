public class TestProgram {

    public static void uniformTest(int num_nodes, long seed) {
        Manet network = new UniformManet(seed);

        for(int i = 0; i < num_nodes; i++) {
            network.generateNode();
        }

        network.show();

        network.floodBFS(true);
    }


    public static void poissonTest(int num_nodes, long seed) {
        Manet network = new PoissonManet(seed);

        for(int i = 0; i < num_nodes; i++) {
            network.generateNode();
        }

        network.show();
        network.floodBFS(true);
    }


    public static void main(String args[]) {
        if (args.length != 3) {
            usage();
            System.exit(1);
        }

        try {
            long seed = Long.parseLong(args[2]);
            int num_nodes = Integer.parseInt(args[1]);
            if (args[0].equals("poisson")) {
                poissonTest(num_nodes, seed);
            }
            else if (args[0].equals("uniform")) {
                uniformTest(num_nodes, seed);
            }
            else {
                usage();
                System.exit(1);
            }
        } catch (Exception e) {
            usage();
            System.exit(1);
        }
    }

    public static void usage() {
        System.out.println("Usage: java TestProgram.java <gen_type> <num_nodes> <gen_seed>");
    }
}