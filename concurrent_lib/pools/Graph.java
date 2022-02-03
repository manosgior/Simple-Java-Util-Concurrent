package concurrent_lib.pools;

import concurrent_lib.locks.MCSLock;
import concurrent_lib.locks.CLHLock;
import concurrent_lib.locks.TTASLock;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Graph {
    private static final int cpus = Runtime.getRuntime().availableProcessors();
    private volatile State state = new State();
    private volatile Edge graph[][];
    private int vertices;

    private static class State {
        AtomicLong seq;
        volatile long rvals[];

        public State() {
            seq = new AtomicLong(0);
            rvals = new long[cpus];
        }
    }

    private static class Edge {
        volatile long seq;
        volatile long weight;
        volatile long prev[];
        Lock lock;

        public Edge(String lockType) {
            prev = new long[cpus];
            switch (lockType) {
                case "CLH":
                    lock = new CLHLock();
                    break;
                case "MCS":
                    lock = new MCSLock();
                    break;
                case "TTAS":
                    lock = new TTASLock();
                    break;
                case "Java":
                    lock = new ReentrantLock();
                    break;
            }
        }
    }

    public Graph(int vertices, String lockType) {
        this.vertices = vertices;
        graph = new Edge[vertices][vertices];

        for (int i = 0; i < vertices; i++) {
            for (int j = 0; j < vertices; j++) {
                graph[i][j] = new Edge(lockType);
            }
        }
    }

    public long dynamicTraverse(int tid) {
        state.rvals[tid] = state.seq.incrementAndGet();

        return state.rvals[tid];
    }

    public long read(int i, int j, int tid) {
         long val;
         long rval;

        if (i >= vertices || j >= vertices || i < 0 || j < 0) return Integer.MAX_VALUE;

        graph[i][j].lock.lock();

        rval = state.rvals[tid];

        if (graph[i][j].seq > rval) {
            val = graph[i][j].prev[tid];
        }
        else val = graph[i][j].weight;

        graph[i][j].lock.unlock();

        return val;
    }

    public void endTraverse(){
        ;
    }

    public void update(int i, int j, long value){
        long new_seq;
        int k;

        if (i >= vertices || j >= vertices || i < 0 || j < 0) return;

        graph[i][j].lock.lock();

        new_seq = state.seq.incrementAndGet();

        for (k = 0; k < cpus; k++) {
            if (graph[i][j].seq < state.rvals[k]) {
                graph[i][j].prev[k] = graph[i][j].weight;
            }
        }

        if (value != Integer.MAX_VALUE) graph[i][j].weight = value;
        else graph[i][j].weight = new_seq;

        graph[i][j].seq = new_seq;


        graph[i][j].lock.unlock();
    }
}
