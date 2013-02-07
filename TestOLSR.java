import java.util.Random;
import java.util.LinkedList;

public class TestOLSR {

    public static void main(String args[]) {
        Manet network = new UniformManet(132785151);

        network.generateNode();
        OLSRWrapper wrapper = new OLSRWrapper(network);

        for(int i = 0; i < 200; i++) {
            wrapper.network.generateNode();
            System.out.println(wrapper.getTotalPacketsRecieved());
        }


        /*Random r = new Random();

        while (true) {
            Node source = wrapper.getRandomNode(r);
            Node destination = wrapper.getRandomNode(r);
            LinkedList<Node> route = wrapper.ping(source, destination);
            if(route == null) {
                break;
            }
            System.out.println(route.size());
        }*/
    }
}