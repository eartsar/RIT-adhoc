import edu.rit.numeric.plot.Plot;
import edu.rit.numeric.ListXYSeries;

public class ManetCanvas {

    private Manet manet = null;

    public ManetCanvas(Manet manet) {
        this.manet = manet;
    }

    public void show() {
        Plot csclPlot = new Plot();
        csclPlot.plotTitle("Graphical Representation of this MANET");
        csclPlot.xAxisStart(-1 * (manet.WORLD_LIMIT / 2.0));
        csclPlot.xAxisEnd(1 * (manet.WORLD_LIMIT / 2.0));
        csclPlot.yAxisStart(-1 * (manet.WORLD_LIMIT / 2.0));
        csclPlot.yAxisEnd(1 * (manet.WORLD_LIMIT / 2.0));
        ListXYSeries series = new ListXYSeries();

        for (Node node : manet) {
            for(Node neighbor : node.getNeighbors()) {
                series.add(node.getX(), node.getY());
                series.add(neighbor.getX(), neighbor.getY());
            }
        }

        csclPlot.segmentedSeries(series);
        csclPlot.getFrame().setVisible(true);
    }
}