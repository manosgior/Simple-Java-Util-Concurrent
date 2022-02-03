
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JavaStackBench {

    public static void main(String args[]) throws InterruptedException, IOException {
        int Nthreads = Integer.parseInt((args[0]));
        //ConcurrentLinkedDeque<Integer> q = new ConcurrentLinkedDeque<>();
        Lock l = new ReentrantLock();

        Stack<Integer> s = new Stack<>();

        int bound = 60000000;

        for (int i = 0; i < 1000000; i++) {
            s.push(i);
            s.pop();
        }

        Thread threads[] = new Thread[Nthreads];

        for (int i = 0; i < threads.length; i++) {
            Random r = new Random();
            threads[i] = new Thread(() -> {
                for (int j = 0; j < bound / Nthreads; j++) {
                    l.lock();
                    s.push(j);
                    l.unlock();
                    l.lock();
                    s.pop();
                    l.unlock();
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

        File f = new File( args[1] + ".txt");
        FileWriter fw = new FileWriter(f, true);

        Long throughput = new Long(1000 * (bound / (end - start)));

        fw.write(throughput.toString() + "\n");
        fw.close();

    }
}