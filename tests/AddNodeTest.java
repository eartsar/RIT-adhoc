import java.util.Random;
import java.util.ArrayList;
import edu.rit.numeric.ListSeries;
import edu.rit.numeric.ListXYSeries;
import edu.rit.numeric.plot.Plot;
import edu.rit.numeric.plot.Strokes;
import edu.rit.numeric.Series;
import edu.rit.numeric.Statistics;
import java.awt.Color;


public class AddNodeTest {

    public static void main(String args[]) {

        if (args.length != 4) {
            System.out.println("Usage: java AddNodeTest <NL> <NU> <tests> <seed>");
            System.exit(1);
        }

        int NL = Integer.parseInt(args[0]);
        int NU = Integer.parseInt(args[1]);
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

            //tora_test_results.add(0);
            //olsr_test_results.add(0);

            // make the MANET
            Manet network = new UniformManet(seed_generator.nextInt());
            network.generateNode();

            for (int i = 1; i < NL; i++) {
                network.generateNode();
            }

            // Wrap it with the protocols
            TORAWrapper tora = new TORAWrapper(network);
            OLSRWrapper olsr = new OLSRWrapper(network);
            
            for (int i = NL; i <= NU; i++) {
                network.generateNode();

                // GET OVERHEAD HERE
                int tora_overhead = tora.getTotalPacketsRecieved();
                int olsr_overhead = olsr.getTotalPacketsRecieved();

                tora_test_results.add(tora_overhead);
                olsr_test_results.add(olsr_overhead);
            }
        }

        // Now that we ran through the tests, time to do some stats
        ListXYSeries tora_averages = new ListXYSeries();
        ListXYSeries olsr_averages = new ListXYSeries();

        int index = 0;
        for (int i = NL; i <= NU; i++) {

            double n_tora_average = 0.0;
            double n_olsr_average = 0.0;

            for (int j = 0; j < num_tests; j++) {
                n_tora_average = n_tora_average + tora_results.get(j).get(index);
                n_olsr_average = n_olsr_average + olsr_results.get(j).get(index);
            }
            n_tora_average = n_tora_average / num_tests;
            n_olsr_average = n_olsr_average / num_tests;

            tora_averages.add(i, n_tora_average);
            olsr_averages.add(i, n_olsr_average);

            index++;
        }

        double[] ttest = Statistics.tTestUnequalVariance(tora_averages.ySeries(), olsr_averages.ySeries());
        System.out.printf ("T Value: %.3f    P Value: %.3f %n", ttest[0], ttest[1]);

        new Plot()
         .xAxisTitle ("Number of Nodes")
         .yAxisTitle ("Messages Recieved")
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