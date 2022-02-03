import concurrent_lib.locks.MCSLock;
import concurrent_lib.pools.ConcurrentStack;
import concurrent_lib.pools.EliminationStack;
import concurrent_lib.pools.SimpleStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ConcurrentStackBench {

    public static void main(String args[]) throws InterruptedException, IOException {
        int Nthreads = Integer.parseInt((args[0]));
        ConcurrentStack<Integer> s;
        int bound = 60000000;

        if (args[1].equals("Simple")) {
            s = new SimpleStack();
        }
        else {
            s = new EliminationStack(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        }

        for (int i = 0; i < 1000000; i++) {
            s.push(i);
            s.pop();
        }

        Thread threads[] = new Thread[Nthreads];

        for (int i = 0; i < threads.length; i++) {

            threads[i] = new Thread(() -> {
                for (int j = 0; j < bound / Nthreads; j++) {
                    try {
                        s.push(j + 1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                for (int j = 0; j < bound / Nthreads; j++) {
                    try {
                       s.pop();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        long start = System.currentTimeMillis();

        for(Thread t: threads){
            t.start();
        }
        for(Thread t: threads){
            t.join();
        }
        long end = System.currentTimeMillis();

        File f = new File( args[1] + "Stack.txt");
        FileWriter fw = new FileWriter(f, true);

        Long throughput = new Long(1000 * (12000000 / (end - start)));
        fw.write(throughput.toString() + "\n");
        fw.close();

    }
}