package liu.xiao.zor.jvmbench;

import org.apache.commons.math3.linear.*;
import org.openjdk.jmh.annotations.*;

import java.security.SecureRandom;
import java.util.Random;

@State(Scope.Thread)
public class LuDecomposition32x32 {

    // 32^2 * 8 = 8 KiB，一般数据L1足够放得下，包括超线程情况下
    // 32^3 * 2 / 3 = 22 KFLOP
    public int dimension = 32;

    public RealMatrix coefficients = MatrixUtils.createRealMatrix(dimension, dimension);
    public RealVector constants;

    @Setup(Level.Iteration)
    public void setup() {
        Random random = new SecureRandom();
        for (int i = 0; i < dimension; ++i) {
            for (int j = 0; j < dimension; ++j) {
                coefficients.setEntry(i, j, random.nextDouble());
            }
        }

        double[] constantData = new double[dimension];
        for (int i = 0; i < dimension; ++i) {
            constantData[i] = random.nextDouble();
        }
        constants = MatrixUtils.createRealVector(constantData);
    }

    public static RealVector solve(LuDecomposition32x32 lu) {
        DecompositionSolver solver = new LUDecomposition(lu.coefficients).getSolver();
        return solver.solve(lu.constants);
    }

    @Benchmark
    @Threads(1)
    public static RealVector _01_singleThread(LuDecomposition32x32 lu) {
        return solve(lu);
    }

    @Benchmark
    @Threads(Threads.MAX)
    public static RealVector _02_multiThread(LuDecomposition32x32 lu) {
        return solve(lu);
    }
}
