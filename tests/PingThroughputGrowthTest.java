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


public class PingThroughputGrowthTest {

    public static void main(String args[]) {
        if (args.length != 5) {
            System.out.println("Usage: java PingThroughputGrowthTest <NL> <NU> <tests> <trials> <seed>");
            System.exit(1);
        }

        int NL = Integer.parseInt(args[0]);
        int NU = Integer.parseInt(args[1]);
        int num_tests = Integer.parseInt(args[2]);
        int num_trials = Integer.parseInt(args[3]);
        long seed = Long.parseLong(args[4]);

        Random seed_generator = new Random(seed);

        ArrayList< ArrayList<Double> > tora_results = new ArrayList< ArrayList<Double> >(num_tests);
        ArrayList< ArrayList<Double> > olsr_results = new ArrayList< ArrayList<Double> >(num_tests);


        for (int test = 0; test < num_tests; test++) {
            System.out.println("Test " + (test + 1) + "...");

            tora_results.add(test, new ArrayList<Double>());
            olsr_results.add(test, new ArrayList<Double>());

            // make the MANET
            Manet network = new UniformManet(seed_generator.nextLong());
            network.generateNode();

            // Wrap it with the protocols
            TORAWrapper tora = new TORAWrapper(network, seed_generator.nextLong());
            OLSRWrapper olsr = new OLSRWrapper(network, seed_generator.nextLong());


            for (int i = 1; i < NL; i++) {
                network.generateNode();
            }

            for (int i = NL; i <= NU; i++) {
                network.generateNode();

                // GET OVERHEAD HERE
                ListSeries toraBSeries = new ListSeries();
                ListSeries olsrBSeries = new ListSeries();

                for (int trial = 0; trial < num_trials; trial++) {

                    Node source = olsr.getRandomNode();
                    Node destination = olsr.getRandomNode();

                    LinkedList<Node> olsr_path = olsr.ping(source, destination);
                    LinkedList<Node> tora_path = tora.ping(source, destination);

                    toraBSeries.add(tora_path.size());
                    olsrBSeries.add(olsr_path.size());
                }

                Series.Stats toraTrialsStats = toraBSeries.stats();
                Series.Stats olsrTrialsStats = olsrBSeries.stats();

                //System.out.println(toraTrialsStats.mean + " VS " + olsrTrialsStats.mean);

                tora_results.get(test).add(toraTrialsStats.mean);
                olsr_results.get(test).add(olsrTrialsStats.mean);
            }
        }

        ListXYSeries tora_averages = new ListXYSeries();
        ListXYSeries olsr_averages = new ListXYSeries();

        for (int n = NL; n < NU; n++ ){
            
            ListSeries toraBSeries = new ListSeries();
            ListSeries olsrBSeries = new ListSeries();

            for (int t = 0; t < num_tests; t++) {
                toraBSeries.add(tora_results.get(t).get(n));
                olsrBSeries.add(olsr_results.get(t).get(n));
            }

            Series.Stats toraTestStats = toraBSeries.stats();
            Series.Stats olsrTestStats = olsrBSeries.stats();

            tora_averages.add(n - NL, toraTestStats.mean);
            olsr_averages.add(n - NL, olsrTestStats.mean);
        }

        double[] ttest = Statistics.tTestUnequalVariance(tora_averages.ySeries(), olsr_averages.ySeries());
        System.out.printf ("T Value: %.3f    P Value: %.3f %n", ttest[0], ttest[1]);
        
        // Now that we ran through the tests, time to do some stats
        new Plot()
         .xAxisTitle ("Dimension N")
         .yAxisTitle ("Number of Hops")
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