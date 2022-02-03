import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TesterJavaLock {

    static int counter = 0;


    public static void main(String args[]) throws InterruptedException {
        Thread threads[] = new Thread[8];
        Lock lock = new ReentrantLock();

        for (int i = 0; i < threads.length; i++) {

            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
                   lock.lock();
                   counter++;
                   lock.unlock();
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

        System.out.println("Synch -> counter = " + counter + "\ttime = " + (end - start));
    }
}

