package concurrent_lib.pools;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class SimpleStack<T> implements ConcurrentStack<T> {
    private AtomicReference<Node> Top = new AtomicReference<Node>(null);
    private ThreadLocal<Random> rand;

    private static class Node<T> {
        T value;
        volatile Node<T> next;

        public  Node(T value) {
            this.value = value;
        }
    }

    public SimpleStack() {
        rand = ThreadLocal.withInitial(() -> new Random(System.currentTimeMillis()));
    }

    private boolean tryPush(Node n) {
        Node<T> oldTop = Top.get();
        n.next = oldTop;

        if (Top.compareAndSet(oldTop, n) == true) return true;
        else return false;
    }

    public void push(T data) throws InterruptedException {
        Node<T> n = new Node(data);

        while(true) {
            if(tryPush(n) == true) return;
            else Thread.sleep(rand.get().nextInt(15) + 5);
        }
    }

    private Node<T> tryPop() {
        Node<T> oldTop = Top.get();
        Node<T> newTop;

        if (oldTop == null) return null;
        newTop = oldTop.next;

        if (Top.compareAndSet(oldTop, newTop) == true) return oldTop;
        else return new Node(null);
    }

    public T pop() throws InterruptedException {
        Node<T> rn;

        while(true) {
            rn = tryPop();
            if (rn == null) return null;

            if (rn.value != null) return rn.value;
            else Thread.sleep(rand.get().nextInt(15) + 5);
        }
    }
}