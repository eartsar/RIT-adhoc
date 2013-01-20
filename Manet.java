import java.util.Random;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.LinkedList;
import java.util.Iterator;
import java.lang.Math;
import java.awt.geom.Point2D;

public abstract class Manet implements Iterable<Node>{

    // Maximum size of the "world" of the MANET
    public final double WORLD_LIMIT = 10.0;
    // Keep this at 1.0 to make a "disk graph"
    final double NODE_COMM_RANGE = 1.0;

    protected Random prng = null;
    protected HashSet<Node> graph;

    private ManetListener listener;


    public Manet(long prng_seed) {
        this.prng = new Random(prng_seed);
        this.graph = new HashSet<Node>();
        this.listener = null;
    }


    // Convenience Layer Functions
    public abstract void generateNode();

    
    public void show() {
        ManetCanvas canvas = new ManetCanvas(this);
        canvas.show();
    }


    // Accessors and Mutators
    public HashSet<Node> getGraph() { return this.graph; }


    // Iterable Implementation Code
    public class ManetIterator implements Iterator<Node> {
        private Iterator<Node> internal_iter = null;
        public ManetIterator(Manet manet) {
            internal_iter = manet.getGraph().iterator();
        }
        public boolean hasNext() { return internal_iter.hasNext(); }
        public Node next() { return internal_iter.next(); }
        public void remove() { internal_iter.remove(); }
    }

    public Iterator<Node> iterator() {
        return new ManetIterator(this);
    }

    public void setListener(ManetListener listener) {
        this.listener = listener;
    }


    protected void addNode(Node node) {
        this.graph.add(node);
        
        if (this.listener != null) {
            this.listener.addNodeCallback(node);
        }
    }

    protected void removeNode(Node node) {
        this.graph.remove(node);

        if (this.listener != null) {
            this.listener.removeNodeCallback(node);
        }
    }
}