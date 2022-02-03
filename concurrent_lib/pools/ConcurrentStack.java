package concurrent_lib.pools;

public interface ConcurrentStack<T> {

    void push(T data) throws InterruptedException;
    T pop() throws InterruptedException;
}
