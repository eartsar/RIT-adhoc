import java.util.Random;
import java.util.ArrayList;
import edu.rit.numeric.ListSeries;
import edu.rit.numeric.ListXYSeries;
import edu.rit.numeric.plot.Plot;
import edu.rit.numeric.plot.Strokes;
import java.awt.Color;


public class AddNodeTest {

    public static void main(String args[]) {
        int num_tests = 3;
        int N = 100;
        long seed = 12345679;

        Random seed_generator = new Random(seed);

        ArrayList< ArrayList<Integer> > tora_results = new ArrayList< ArrayList<Integer> >(num_tests);
        ArrayList< ArrayList<Integer> > olsr_results = new ArrayList< ArrayList<Integer> >(num_tests);


        for (int test = 0; test < num_tests; test++) {
            tora_results.add(test, new ArrayList<Integer>(N));
            olsr_results.add(test, new ArrayList<Integer>(N));

            ArrayList<Integer> tora_test_results = tora_results.get(test);
            ArrayList<Integer> olsr_test_results = olsr_results.get(test);

            tora_test_results.add(0, 0);
            olsr_test_results.add(0, 0);

            // make the MANET
            Manet network = new UniformManet(seed_generator.nextInt());
            network.generateNode();

            // Wrap it with the protocols
            TORAWrapper tora = new TORAWrapper(network);
            OLSRWrapper olsr = new OLSRWrapper(network);

            for (int i = 1; i <= N; i++) {
                network.generateNode();

                // GET OVERHEAD HERE
                int tora_overhead = tora.getTotalPacketsRecieved();
                int olsr_overhead = olsr.getTotalPacketsRecieved();

                System.out.println("TORA: " + tora_overhead + "   OLSR: " + olsr_overhead);

                tora_test_results.add(i, tora_overhead);
                olsr_test_results.add(i, olsr_overhead);
            }
        }

        // Now that we ran through the tests, time to do some stats
        ListXYSeries tora_averages = new ListXYSeries();
        ListXYSeries olsr_averages = new ListXYSeries();

        for (int i = 0; i <= N; i++) {

            double n_tora_average = 0.0;
            double n_olsr_average = 0.0;

            for (int j = 0; j < num_tests; j++) {
                n_tora_average = n_tora_average + tora_results.get(j).get(i);
                n_olsr_average = n_olsr_average + olsr_results.get(j).get(i);
            }
            n_tora_average = n_tora_average / num_tests;
            n_olsr_average = n_olsr_average / num_tests;

            tora_averages.add(i, n_tora_average);
            olsr_averages.add(i, n_olsr_average);
        }

        new Plot()
         .xAxisTitle ("Dimension N")
         .yAxisTitle ("Overhead")
         .seriesStroke (Strokes.solid (1))
         .seriesDots (null)
         .seriesColor (Color.RED)
         .xySeries (tora_averages)
         .seriesStroke (Strokes.solid (1))
         .seriesColor (Color.BLUE)
         .xySeries (olsr_averages)
         .getFrame()
         .setVisible (true);
    }

}