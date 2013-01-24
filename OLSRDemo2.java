import java.util.ArrayList;
import java.awt.Color;
import edu.rit.numeric.plot.Plot;
import edu.rit.numeric.ListXYSeries;


public class OLSRDemo2 {

    public static void generateGraph(int num_nodes, long seed) {
        Manet network;

        ArrayList<Integer> num_mpr = new ArrayList<Integer>();
        ArrayList<Integer> num_manet = new ArrayList<Integer>();


        for (int rep = 0; rep < 40; rep++) {
            network = new UniformManet(seed);
            num_nodes = rep * 100;
            if (num_nodes == 0) {
                num_nodes = 10;
            }
            for(int i = 0; i < num_nodes; i++) {
                network.generateNode();
            }

            OLSRWrapper network_wrapper = new OLSRWrapper(network);

            System.out.println("Number of Nodes in the MANET: " + network_wrapper.getManetSize());
            System.out.println("Number of MPR Nodes         : " + network_wrapper.getMPRSet().size());
            num_mpr.add(network_wrapper.getMPRSet().size());
            num_manet.add(network_wrapper.getManetSize());
        }

        Plot csclPlot = new Plot();
        csclPlot.plotTitle("MPR Nodes present in a Manet");
        csclPlot.xAxisStart(0);
        csclPlot.xAxisEnd(4000);
        csclPlot.yAxisStart(0);
        csclPlot.yAxisEnd(300);
        
        ListXYSeries series = new ListXYSeries();

        for (int i = 0; i < num_mpr.size(); i++) {
            series.add(num_manet.get(i), num_mpr.get(i));
        }
        csclPlot.xySeries(series);

        csclPlot.seriesColor(java.awt.Color.RED);
        csclPlot.seriesDots(null);
        ListXYSeries modelSeries = new ListXYSeries();
        for (int i = 0; i < num_mpr.size(); i++) {
            modelSeries.add(num_manet.get(i), calculateModel(num_manet.get(i)));
        }
        csclPlot.xySeries(modelSeries);

        csclPlot.getFrame().setVisible(true);
    }

    public static double calculateModel(int i) {
        return 5.0 * Math.pow((double)i, 0.333333);
    }

    public static void main(String args[]) {
        /**if (args.length != 1) {
            usage();
            System.exit(1);
        }**/

        int num_nodes = 1000;
        try {
        	;
            //num_nodes = Integer.parseInt(args[0]);            
        } catch (Exception e) {
            usage();
            System.exit(1);
        }

        generateGraph(num_nodes, 192821236);
    }

    public static void usage() {
        System.out.println("Usage: java OLSRDemo.java <num_nodes>");
    }
}