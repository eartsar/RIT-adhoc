import java.util.Random;
import java.util.ArrayList;
import edu.rit.numeric.ListSeries;
import edu.rit.numeric.ListXYSeries;
import edu.rit.numeric.plot.Plot;
import edu.rit.numeric.plot.Strokes;
import edu.rit.numeric.Series;
import edu.rit.numeric.Statistics;
import java.awt.Color;


public class DecayOverheadTest {

    public static void main(String args[]) {

        if (args.length != 4) {
            System.out.println("Usage: java DecayOverheadTest <NU> <NL> <tests> <seed>");
            System.exit(1);
        }

        int NU = Integer.parseInt(args[0]);
        int NL = Integer.parseInt(args[1]);
        int num_tests = Integer.parseInt(args[2]);
        long seed = Long.parseLong(args[3]);

        Random seed_generator = new Random(seed);

        ArrayList< ArrayList<Integer> > tora_results = new ArrayList< ArrayList<Integer> >(num_tests);
        ArrayList< ArrayList<Integer> > olsr_results = new ArrayList< ArrayList<Integer> >(num_tests);


        for (int test = 0; test < num_tests; test++) {
            System.out.println("Test " + (test + 1) + "...");

            tora_results.add(test, new ArrayList<Integer>(NU - NL));
            olsr_results.add(test, new ArrayList<Integer>(NU - NL));

            ArrayList<Integer> tora_test_results = tora_results.get(test);
            ArrayList<Integer> olsr_test_results = olsr_results.get(test);


            // make the MANET
            Manet network = new UniformManet(seed_generator.nextLong());
            network.generateNode();

            for (int i = NL; i <= NU; i++) {
                network.generateNode();
            }

            // Wrap it with the protocols
            TORAWrapper tora = new TORAWrapper(network, seed_generator.nextLong());
            OLSRWrapper olsr = new OLSRWrapper(network, seed_generator.nextLong());

            for (int i = NU; i >= NL; i--) {
                network.removeLastNode();

                // GET OVERHEAD HERE
                int tora_overhead = tora.getTotalPacketsRecieved();
                int olsr_overhead = olsr.getTotalPacketsRecieved();

                // System.out.println("TORA: " + tora_overhead + "   OLSR: " + olsr_overhead);

                tora_test_results.add(tora_overhead);
                olsr_test_results.add(olsr_overhead);
            }
        }

        // Now that we ran through the tests, time to do some stats
        ListXYSeries tora_averages = new ListXYSeries();
        ListXYSeries olsr_averages = new ListXYSeries();

        for (int i = NL; i < NU; i++) {
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

        double[] ttest = Statistics.tTestUnequalVariance(tora_averages.ySeries(), olsr_averages.ySeries());
        System.out.printf ("T Value: %.3f    P Value: %.3f %n", ttest[0], ttest[1]);

        new Plot()
         .xAxisTitle ("Number of Nodes Removed")
         .yAxisTitle ("Number of Messages Recieved")
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