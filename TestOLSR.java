import java.util.Random;

public class TestOLSR {

    public static void main(String args[]) {
        Manet network = new UniformManet(132785151);

        for(int i = 0; i < 10000; i++) {
            network.generateNode();
            System.out.println(i);
        }

        OLSRWrapper wrapper = new OLSRWrapper(network);
        //wrapper.show();

        Random r = new Random();

        while (true) {
            Node source = wrapper.getRandomNode(r);
            Node destination = wrapper.getRandomNode(r);
            int i = wrapper.ping(source, destination);
            if(i == -1) {
                break;
            }
            System.out.println(i);
        }
    }
}