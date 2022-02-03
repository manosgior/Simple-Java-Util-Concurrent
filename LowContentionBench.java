import concurrent_lib.pools.Graph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class LowContentionBench {

    public static void main(String args[]) throws InterruptedException, IOException {
        int Nthreads = Integer.parseInt(args[0]);
        int bound = 60000000;

        Thread threads[] = new Thread[Nthreads];
        int vertices = 1024;
        Graph g = new Graph(vertices, args[1]);

        for (int i = 0; i < 1000000; i++) {
            g.update(0, 0, 0);
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread( new graphTesting(g, vertices, i, bound / Nthreads));
        }

        long start = System.currentTimeMillis();
        for(Thread t: threads){
            t.start();
        }
        for(Thread t: threads){
            t.join();
        }
        long end = System.currentTimeMillis();

        File f = new File("LowContention" + args[1] + ".txt");
        FileWriter fw = new FileWriter(f, true);

        Long throughput = new Long(1000 * (bound / (end - start)));
        fw.write(throughput.toString() + "\n");
        fw.close();
    }
}

class graphTesting implements Runnable{
    Graph g;
    Random r;
    int vertices;
    int i;
    int bound;

    public graphTesting(Graph g, int v, int i, int bound) {
        this.g = g;
        r = new Random(System.currentTimeMillis());
        vertices = v;
        this.i = i;
        this.bound = bound;
    }

    public void run(){
        if (Thread.currentThread().getId() % 2 == 0) {
            long seq;
            long read;

            for (int j = 0; j < bound; j++ ) {
                seq = g.dynamicTraverse(i);

                int x = r.nextInt(vertices);
                int y = r.nextInt(vertices);

                if ((read = g.read(x, y, i)) >= seq) {
                    System.out.println("T:" + i + " Invalid read in Edges[" + x + ", " + y + "] = " + read + " >= " + seq);
                    System.exit(-1);
                }

                g.endTraverse();
            }
        }
        else {
            for (int k = 0; k < bound; k++) {
                int x = r.nextInt(vertices);
                int y = r.nextInt(vertices);

                g.update(x, y, Integer.MAX_VALUE);
            }
        }
    }
}