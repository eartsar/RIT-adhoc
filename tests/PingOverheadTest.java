import java.util.Random;
import java.util.ArrayList;
import edu.rit.numeric.ListSeries;
import edu.rit.numeric.ListXYSeries;
import edu.rit.numeric.plot.Plot;
import edu.rit.numeric.plot.Strokes;
import edu.rit.numeric.Series;
import edu.rit.numeric.Statistics;
import java.awt.Color;


public class PingOverheadTest {

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
                    olsr.clearMetrics();
                    tora.clearMetrics();
                    
                    Node source = olsr.getRandomNode(node_prng);
                    Node destination = olsr.getRandomNode(node_prng);

                    olsr.ping(source, destination);
                    tora.ping(source, destination);
                    
                    int tora_overhead = tora.getTotalPacketsRecieved();
                    int olsr_overhead = olsr.getTotalPacketsRecieved();

                    toraBSeries.add(tora_overhead);
                    olsrBSeries.add(olsr_overhead);
                }

                Series.Stats toraTrialsStats = toraBSeries.stats();
                Series.Stats olsrTrialsStats = olsrBSeries.stats();

                System.out.println(toraTrialsStats.mean + " VS " + olsrTrialsStats.mean);

                tora_results.get(test).add(i, toraTrialsStats.mean);
                olsr_results.get(test).add(i, toraTrialsStats.mean);
            }
        }
        /*
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
         */
    }

}