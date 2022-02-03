package concurrent_lib.pools;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class EliminationStack<T> implements ConcurrentStack<T> {
    private static int ELIMINATION_ARRAY_SIZE;
    private static int ELIMINATION_DURATION;

    private AtomicReference<StackNode> Top = new AtomicReference<StackNode>(null);
    private ThreadLocal<Random> rand;                   // den tha mou xreiastei giati einai gia to backoff
    private AtomicReferenceArray<ExchangeNode<T>> eliminationArray;

    private enum State {EMPTY, WAITING, BUSY, FAILED};

    private static class ExchangeNode<T> implements Cloneable {
        volatile T data;
        volatile State state;

        ExchangeNode(){
            this.data = null;
            this.state = State.EMPTY;
        }

        ExchangeNode(T data, State state){
            this.data = data;
            this.state = state;
        }

        public ExchangeNode<T> clone() throws CloneNotSupportedException
        {
            ExchangeNode<T> clonedObj = (ExchangeNode<T>) super.clone();
            clonedObj.data = this.data;
            clonedObj.state = this.state;
            return clonedObj;
        }

        @Override
        public String toString() {
            return "Data: " + data + "  State: " + state;
        }
    }

    private static class StackNode<T> {
        T data;
        volatile StackNode<T> next;

        public  StackNode(T data) {
            this.data = data;
        }
    }

    public EliminationStack(int eliminationArraySize, int eliminationDuration) {
        ELIMINATION_ARRAY_SIZE = eliminationArraySize;
        ELIMINATION_DURATION = eliminationDuration;
        eliminationArray = new AtomicReferenceArray<>(ELIMINATION_ARRAY_SIZE);

        for (int i = 0; i < eliminationArray.length(); ++i){
            eliminationArray.set(i, new ExchangeNode<>());
        }
        rand = ThreadLocal.withInitial(() -> new Random(System.currentTimeMillis()));
    }


    ExchangeNode<T> exchange(int index, T myItem, long time, TimeUnit unit) throws InterruptedException, CloneNotSupportedException {
        long duration = TimeUnit.MILLISECONDS.convert(time, unit);
        long timeBound = System.currentTimeMillis() + duration;
        AtomicReference<ExchangeNode<T>> slot = new AtomicReference<>(eliminationArray.get(index));
        ExchangeNode<T> failed = new ExchangeNode<>(null, State.FAILED);
        ExchangeNode<T> myNode = new ExchangeNode<>(myItem, State.WAITING);
        ExchangeNode<T> emptyNode = new ExchangeNode<>();

        while(true){
            if(System.currentTimeMillis() > timeBound){
                return failed;// TIMEOUT
            }

            ExchangeNode<T> slotVal = slot.get().clone();
            //ExchangeNode<T> slotVal = slot.get();               // mporei na mhn xreiazetai clone
            switch(slotVal.state) {
                case EMPTY: {
                    myNode.state = State.WAITING;
                    if (slot.accumulateAndGet(myNode, (curr, given) -> curr.state == State.EMPTY && curr.data == slotVal.data ? given : curr ) == myNode){
                        while (timeBound > System.currentTimeMillis()) {
                            ExchangeNode<T> slotVal2 = slot.get();
                            if (slotVal2.state == State.BUSY) {
                                slot.set(emptyNode);
                                return slotVal2;
                            }
                        }
                        if (slot.accumulateAndGet(emptyNode, (curr, given) -> curr.state == State.WAITING && curr.data == myItem ? given : curr ) == emptyNode){
                            return failed;                      // TIMEOUT
                        }else{
                            ExchangeNode<T> slotVal2 = slot.get();
                            slot.set(emptyNode);
                            return slotVal2;
                        }
                    }
                    break;
                }
                case WAITING:{
                    if( myItem == null ^ slotVal.data == null ){            //XOR
                        return failed;      // gia na apofygw pop me pop kai push me push
                    }
                    myNode.state = State.BUSY;
                    if (slot.accumulateAndGet(myNode, (curr, given) -> curr.state == State.WAITING && curr.data == slotVal.data ? given : curr ) == myNode){ ;
                        return slotVal;
                    }
                    break;
                }
                case BUSY: {
                    break;
                }
            }
        }
    }

    private boolean tryPush(StackNode n) {
        StackNode<T> oldTop = Top.get();
        n.next = oldTop;

        if (Top.compareAndSet(oldTop, n) == true) return true;
        else return false;
    }


    public void push(T data) throws InterruptedException {
        StackNode<T> n = new StackNode(data);
        ExchangeNode<T> otherValue;
        int range;

        while(true) {
            if(tryPush(n) == true){
                return;
            }
            else{
                range = ThreadLocalRandom.current().nextInt(0, ELIMINATION_ARRAY_SIZE);
                try {
                    otherValue = exchange(range, data, ELIMINATION_DURATION, TimeUnit.MILLISECONDS);
                    if(otherValue.data == null && otherValue.state != State.FAILED)
                        return;
                } catch (CloneNotSupportedException e) {
                    System.err.println("Problem with Clone in exchange.");
                    e.printStackTrace();
                }
            }
        }
    }


    private StackNode<T> tryPop() {
        StackNode<T> oldTop = Top.get();
        StackNode<T> newTop;

        if (oldTop == null) return null;
        newTop = oldTop.next;

        if (Top.compareAndSet(oldTop, newTop) == true) return oldTop;
        else return new StackNode(null);
    }


    public T pop() throws InterruptedException {
        StackNode<T> rn;
        ExchangeNode<T> otherValue;
        int range;

        while(true) {
            rn = tryPop();
            if (rn == null) return null;

            if (rn.data != null){
                return rn.data;
            }
            else {
                range = ThreadLocalRandom.current().nextInt(0, ELIMINATION_ARRAY_SIZE);
                try {
                    otherValue = exchange(range, null, ELIMINATION_DURATION, TimeUnit.MILLISECONDS);
                    if(otherValue.data != null) {
                        assert otherValue.state != State.FAILED;
                        return otherValue.data;
                    }
                } catch (CloneNotSupportedException e) {
                    System.err.println("Problem with Clone in exchange.");
                    e.printStackTrace();
                }
            }
        }
    }

}