package concurrent_lib.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class TTASLock implements Lock {
    private AtomicBoolean  lock = new AtomicBoolean(false);

    public void lock(){
        while (true) {
            while (lock.get() == true);
            if (!lock.getAndSet(true))
                return;
        }
    }

    public void unlock() {
        lock.set(false);
    }


    public boolean tryLock() {
        if (lock.getAndSet(true) == false)
            return true;
        else return false;
    }

    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public Condition newCondition() { return null; }

    @Override
    public void lockInterruptibly() throws InterruptedException {}
}
