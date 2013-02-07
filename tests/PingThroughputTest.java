import java.util.Random;
import java.util.ArrayList;
import java.util.LinkedList;
import edu.rit.numeric.ListSeries;
import edu.rit.numeric.ListXYSeries;
import edu.rit.numeric.plot.Plot;
import edu.rit.numeric.plot.Strokes;
import edu.rit.numeric.Series;
import edu.rit.numeric.Statistics;
import java.awt.Color;


public class PingThroughputTest {

    public static void main(String args[]) {
        int num_tests = 5;
        int num_trials = 10;
        int N = 100;
        long seed = 12345679;

        Random node_prng = new Random(seed);

        ArrayList< ArrayList<Double> > tora_results = new ArrayList< ArrayList<Double> >(num_tests);
        ArrayList< ArrayList<Double> > olsr_results = new ArrayList< ArrayList<Double> >(num_tests);


        for (int test = 0; test < num_tests; test++) {
            tora_results.add(test, new ArrayList<Double>());
            olsr_results.add(test, new ArrayList<Double>());

            tora_results.get(test).add(0, 0.0);
            olsr_results.get(test).add(0, 0.0);

            // make the MANET
            Manet network = new UniformManet(node_prng.nextInt());
            network.generateNode();

            // Wrap it with the protocols
            TORAWrapper tora = new TORAWrapper(network);
            OLSRWrapper olsr = new OLSRWrapper(network);

            for (int i = 1; i <= N; i++) {
                network.generateNode();

                // GET OVERHEAD HERE
                ListSeries toraBSeries = new ListSeries();
                ListSeries olsrBSeries = new ListSeries();

                for (int trial = 0; trial < num_trials; trial++) {

                    Node source = olsr.getRandomNode(node_prng);
                    Node destination = olsr.getRandomNode(node_prng);

                    LinkedList<Node> olsr_path = olsr.ping(source, destination);
                    LinkedList<Node> tora_path = tora.ping(source, destination);

                    toraBSeries.add(tora_path.size());
                    olsrBSeries.add(olsr_path.size());
                }

                Series.Stats toraTrialsStats = toraBSeries.stats();
                Series.Stats olsrTrialsStats = olsrBSeries.stats();

                System.out.println(toraTrialsStats.mean + " VS " + olsrTrialsStats.mean);

                tora_results.get(test).add(i, toraTrialsStats.mean);
                olsr_results.get(test).add(i, olsrTrialsStats.mean);
            }
        }

        ListXYSeries tora_averages = new ListXYSeries();
        ListXYSeries olsr_averages = new ListXYSeries();

        for (int n = 0; n < N; n++ ){
            if(n == 0) {
                tora_averages.add(n, 0);
                olsr_averages.add(n, 0);
                continue;
            }

            ListSeries toraBSeries = new ListSeries();
            ListSeries olsrBSeries = new ListSeries();

            for (int t = 0; t < num_tests; t++) {
                toraBSeries.add(tora_results.get(t).get(n));
                olsrBSeries.add(olsr_results.get(t).get(n));
            }

            Series.Stats toraTestStats = toraBSeries.stats();
            Series.Stats olsrTestStats = olsrBSeries.stats();

            tora_averages.add(n, toraTestStats.mean);
            olsr_averages.add(n, olsrTestStats.mean);
        }
        
        // Now that we ran through the tests, time to do some stats
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