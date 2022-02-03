package concurrent_lib.locks;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.atomic.AtomicReference;

public class CLHLock implements Lock {
    private AtomicReference<Node> tail = new AtomicReference<Node>(new Node());
    private ThreadLocal<Node> myPred;
    private ThreadLocal<Node> myNode;

    private static class Node {
        volatile boolean locked;
    }

    public CLHLock() {
        myNode = new ThreadLocal<Node>() {
            protected Node initialValue() {
                return new Node();
            }
        };
        myPred = new ThreadLocal<Node>() {
            protected Node initialValue() {
                return null;
            }
        };
    }

    @Override
    public void lock() {
        Node node = myNode.get();
        node.locked = true;
        Node pred = tail.getAndSet(node);
        myPred.set(pred);
        while (pred.locked);
    }

    @Override
    public void unlock() {
        Node node = myNode.get();
        node.locked = false;
        myNode.set(myPred.get());
    }

    @Override
    public boolean tryLock() {
        Node node = myNode.get();
        node.locked = true;
        Node tailNode = tail.get();
        if(!tailNode.locked){
            if(tail.compareAndSet(tailNode, node)) {
                myPred.set(tailNode);
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long waitingDuration = TimeUnit.MILLISECONDS.convert(time, unit);
        Node node = myNode.get();
        node.locked = true;
        while(System.currentTimeMillis() - startTime < waitingDuration) {
            Node tailNode = tail.get();
            if(!tailNode.locked){
                if(tail.compareAndSet(tailNode, node)) {
                    myPred.set(tailNode);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Condition newCondition() { return null; }

    @Override
    public void lockInterruptibly() throws InterruptedException {}
}