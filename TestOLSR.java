import java.util.Random;
import java.util.LinkedList;

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
            LinkedList<Node> route = wrapper.ping(source, destination);
            if(route == null) {
                break;
            }
            System.out.println(route.size());
        }
    }
}