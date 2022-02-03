package concurrent_lib.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class MCSLock implements Lock {
    private AtomicReference<MCSLock.Node> tail = new AtomicReference<MCSLock.Node>(null);
    private ThreadLocal<MCSLock.Node> myPred;
    private ThreadLocal<MCSLock.Node> myNode;

    private static class Node {
        volatile boolean locked;
        volatile Node next = null;
    }

    public MCSLock() {
        myNode = ThreadLocal.withInitial(() -> new Node());
        myPred = ThreadLocal.withInitial(() -> null);
    }

    @Override
    public void lock() {
        Node node = myNode.get();
        Node pred = tail.getAndSet(node);
        myPred.set(pred);
        if( pred != null) {
            node.locked = true;
            pred.next = node;
            while(node.locked);
        }
    }

    @Override
    public void unlock() {
        Node node = myNode.get();

        if (node.next == null) {
            if (tail.compareAndSet(node,null) == true) return;
            while(node.next == null);
        }

        node.next.locked = false;
        node.next = null;
    }

    @Override
    public boolean tryLock() {
        Node node = myNode.get();
        node.locked = false;
        if (tail.compareAndSet(null, node)) {
            myPred.set(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long waitingDuration = TimeUnit.MILLISECONDS.convert(time, unit);
        Node node = myNode.get();
        node.locked = false;
        while(System.currentTimeMillis() - startTime < waitingDuration) {
            if (tail.compareAndSet(null, node)) {
                myPred.set(null);
                return true;
            }
        }
        return false;
    }

    @Override
    public Condition newCondition() { return null; }

    @Override
    public void lockInterruptibly() throws InterruptedException {}
}
