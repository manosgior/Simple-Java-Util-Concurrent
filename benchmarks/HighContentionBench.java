import concurrent_lib.locks.CLHLock;
import concurrent_lib.locks.MCSLock;
import concurrent_lib.locks.TTASLock;

import java.io.*;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class HighContentionBench {

    public static void main(String args[]) throws InterruptedException, IOException {
        int Nthreads = Integer.parseInt(args[0]);
        int bound = 120000000;

        Thread threads[] = new Thread[Nthreads];
        int counters[] = new int[Nthreads];

        Lock locks[];

        switch (args[1]) {
            case "CLH":
                locks = new CLHLock[Nthreads];
                for (int i = 0; i < Nthreads; i++) {
                    locks[i] = new CLHLock();
                }
                break;
            case "MCS":
                locks = new MCSLock[Nthreads];
                for (int i = 0; i < Nthreads; i++) {
                    locks[i] = new MCSLock();
                }
                break;
            case "TTAS":
                locks = new TTASLock[Nthreads];
                for (int i = 0; i < Nthreads; i++) {
                    locks[i] = new TTASLock();
                }
                break;
            case "Java":
                locks = new ReentrantLock[Nthreads];
                for (int i = 0; i < Nthreads; i++) {
                    locks[i] = new ReentrantLock();
                }
                break;
            default:
                locks = new ReentrantLock[Nthreads];
                for (int i = 0; i < Nthreads; i++) {
                    locks[i] = new ReentrantLock();
                }
        }

        for (int i = 0; i < 1000000; i++) {
            locks[0].lock();
            locks[0].unlock();
        }

        for (int i = 0; i < threads.length; i++) {

            threads[i] = new Thread(() -> {
                Random r = new Random();
                for (int j = 0; j < bound / Nthreads; j++) {
                    int index = r.nextInt(Nthreads);
                    locks[index].lock();
                    counters[index]++;
                    locks[index].unlock();
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

        File f = new File("HighContention" + args[1] + ".txt");
        FileWriter fw = new FileWriter(f, true);

        Long throughput = new Long(1000 * (bound / (end - start)));
        fw.write(throughput.toString() + "\n");
        fw.close();
    }
}
