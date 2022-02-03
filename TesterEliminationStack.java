import concurrent_lib.locks.MCSLock;
import concurrent_lib.pools.EliminationStack;


public class TesterEliminationStack {
    static long counter = 0;

    public static void main(String args[]) throws InterruptedException {
        EliminationStack<Integer> s = new EliminationStack<Integer>(10, 20);
        int bound = 1000;
        MCSLock lock = new MCSLock();

        Thread threads[] = new Thread[4];

        for (int i = 0; i < threads.length; i++) {

            threads[i] = new Thread(() -> {
                for (int j = 0; j < bound; j++) {
                    try {
                        s.push(j + 1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                for (int j = 0; j < bound; j++) {
                    try {
                        int num = s.pop();
                        lock.lock();
                        counter += num;
                        lock.unlock();
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

        System.out.println(counter);
    }
}