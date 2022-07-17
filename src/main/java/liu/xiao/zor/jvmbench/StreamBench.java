package liu.xiao.zor.jvmbench;

import org.openjdk.jmh.annotations.*;

import java.util.Random;

public class StreamBench {

    public static class State {

        // "Each array must be at least 4 times the size of the available cache memory"
        public int arraySize;
        public int addTriadSize;

        public double scalar;
        public double[] a;
        public double[] b;
        public double[] c;

        public void init(int arraySize, int addTriadSize) {
            this.arraySize = arraySize;
            this.addTriadSize = addTriadSize;

            a = new double[arraySize];
            b = new double[arraySize];
            c = new double[arraySize];

            Random random = new Random();
            scalar = random.nextDouble();
            for (int i = 0; i < arraySize; ++i) {
                a[i] = random.nextDouble();
                b[i] = random.nextDouble();
                c[i] = random.nextDouble();
            }
        }
    }

    @org.openjdk.jmh.annotations.State(Scope.Thread)
    public static class State100MiB extends State {

        @Setup(Level.Trial)
        public void setup() {
            init(6553600, // 1048576 * 100 / 2 / 8 (aligned to 64 bytes)
                    4369064); // 1048576 * 100 / 3 / 8 (aligned to 64 bytes)
        }
    }

    @org.openjdk.jmh.annotations.State(Scope.Thread)
    public static class State1GiB extends State {

        @Setup(Level.Trial)
        public void setup() {
            init(67108864,  // 1048576 * 1024 / 2 / 8 (aligned to 64 bytes)
                    44739240);  // 1048576 * 1024 / 3 / 8 (aligned to 64 bytes)
        }
    }

    public static double[] triad(State state) {
        for (int j = 0; j < state.addTriadSize; j++) {
            state.a[j]  = state.b[j] + state.scalar * state.c[j];
        }
        return state.a;
    }

    public static double[] copy(State state) {
        System.arraycopy(state.a, 0, state.c, 0, state.arraySize);
        return state.c;
    }

    public static double[] scale(State state) {
        for (int j = 0; j < state.arraySize; j++) {
            state.b[j] = state.scalar * state.c[j];
        }
        return state.b;
    }

    public static double[] add(State state) {
        for (int j = 0; j < state.addTriadSize; j++) {
            state.c[j] = state.a[j] + state.b[j];
        }
        return state.c;
    }


    @Benchmark
    @Threads(1)
    public static double[] _01_1GiB_triadSingleThread(State1GiB state) {
        return triad(state);
    }

    @Benchmark
    @Threads(Threads.MAX)
    public static double[] _02_100MiB_triadMultiThread(State100MiB state) {
        return triad(state);
    }



//    @Benchmark
    @Threads(1)
    public static double[] _03_1GiB_copySingleThread(State1GiB state) {
        return copy(state);
    }

//    @Benchmark
    @Threads(Threads.MAX)
    public static double[] _04_100MiB_copyMultiThread(State100MiB state) {
        return copy(state);
    }



//    @Benchmark
    @Threads(1)
    public static double[] _05_1GiB_scaleSingleThread(State1GiB state) {
        return scale(state);
    }

//    @Benchmark
    @Threads(Threads.MAX)
    public static double[] _06_100MiB_scaleMultiThread(State100MiB state) {
        return scale(state);
    }



//    @Benchmark
    @Threads(1)
    public static double[] _07_1GiB_addSingleThread(State1GiB state) {
        return add(state);
    }

//    @Benchmark
    @Threads(Threads.MAX)
    public static double[] _08_100MiB_addMultiThread(State100MiB state) {
        return add(state);
    }
}
