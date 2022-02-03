import concurrent_lib.locks.CLHLock;
import concurrent_lib.locks.MCSLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class TesterOurTryLock {
    static int counter = 0;
    static AtomicInteger failedCountrer = new AtomicInteger(0);

    public static void main(String args[]) throws InterruptedException {
        Thread threads[] = new Thread[4];
//        CLHLock lock = new CLHLock();
         MCSLock lock = new MCSLock();

        for (int i = 0; i < threads.length; i++) {

            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
//                    try {
//                    if(lock.tryLock(10L, TimeUnit.MILLISECONDS)) {
                    if(lock.tryLock()) {
                            counter++;
                            lock.unlock();
                        }else{
                            System.out.println("Fail to lock, counter value: " + counter );
                            failedCountrer.incrementAndGet();
                        }
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
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

        System.out.println("CLH -> Succeeded locks counter = " + counter + "\ttime = " + (end - start));
        System.out.println("CLH -> failed locks counter = " + failedCountrer.get() );
    }
}