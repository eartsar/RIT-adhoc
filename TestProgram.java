public class TestProgram {

    public static void basicTest() {
        Manet network = new Manet(1234);

        for(int i = 0; i < 200; i++) {
            network.generateNode();
        }

        network.show();

        network.floodBFS(true);
    }


    public static void main(String args[]) {
        basicTest();
    }
}