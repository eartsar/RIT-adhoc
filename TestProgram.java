public class TestProgram {

    public static void basicTest() {
        Manet network = new Manet(1234);

        for(int i = 0; i < 200; i++) {
            network.generateNode();
        }

        network.show();

        /*for(Node node : network) {
            System.out.println(node.getX() + ", " + node.getY());
        }*/
    }


    public static void main(String args[]) {
        basicTest();
    }
}