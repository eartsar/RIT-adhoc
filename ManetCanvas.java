import edu.rit.numeric.plot.Plot;
import edu.rit.numeric.ListXYSeries;

/**
 * ManetCanvas
 * 	Used to graphically display the simulation network
 * 
 * Uses RIT Computer Science Course library Plot and ListXYSeries classes
 * 
 *
 */
public class ManetCanvas {

    private Manet manet = null;

    /**
     * Constructor ManetCanvas(manet)
     * 	Network graph to be displayed
     * @param manet - Manet network
     */
    public ManetCanvas(Manet manet) {
        this.manet = manet;
    }

    
    /**
     * show()
     * 	Shows a graph of the Manet using the Plot library
     */
    public void show() {
        Plot csclPlot = new Plot();
        csclPlot.plotTitle("Graphical Representation of this MANET");
        csclPlot.xAxisStart(-1 * (manet.WORLD_LIMIT / 2.0));
        csclPlot.xAxisEnd(1 * (manet.WORLD_LIMIT / 2.0));
        csclPlot.yAxisStart(-1 * (manet.WORLD_LIMIT / 2.0));
        csclPlot.yAxisEnd(1 * (manet.WORLD_LIMIT / 2.0));
        
        ListXYSeries series = new ListXYSeries();
        
        //Add each node to the series
        for (Node node : manet) {
            for(Node neighbor : node.getNeighbors()) {
                series.add(node.getX(), node.getY());
                series.add(neighbor.getX(), neighbor.getY());
            }
        }
        
        //display series of nodes
        csclPlot.seriesStroke(null);
        csclPlot.segmentedSeries(series);
        csclPlot.getFrame().setVisible(true);
    }
}