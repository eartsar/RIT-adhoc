public class TestProgram {

    public static void uniformTest() {
        Manet network = new UniformManet(1234);

        for(int i = 0; i < 200; i++) {
            network.generateNode();
        }

        network.show();

        network.floodBFS(true);
    }


    public static void poissonTest() {
        Manet network = new PoissonManet(1234);

        for(int i = 0; i < 50; i++) {
            network.generateNode();
        }

        network.show();
        network.floodBFS(true);
    }


    public static void main(String args[]) {
        //basicTest();
        poissonTest();
    }
}