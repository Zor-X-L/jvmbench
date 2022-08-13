package liu.xiao.zor.jvmbench;

import org.apache.commons.math3.linear.*;
import org.openjdk.jmh.annotations.*;

import java.security.SecureRandom;
import java.util.Random;

@State(Scope.Thread)
public class LuDecomposition {

    // 112^2 * 8 Bytes = 98 KiB, fits in L2 cache, usually yields higher flops than L1 & L3
    // 112 * 8 Bytes % 128 Bytes = 0, so it fits in most common cache line size (64/128)
    // 112^3 * 2 / 3 FLOP = 936,619 FLOP
    @Param({"112"})
    public int luMatDim;

    public RealMatrix coefficients;
    public RealVector constants;

    @Setup(Level.Iteration)
    public void setup() {
        Random random = new SecureRandom();
        coefficients = MatrixUtils.createRealMatrix(luMatDim, luMatDim);
        for (int i = 0; i < luMatDim; ++i) {
            for (int j = 0; j < luMatDim; ++j) {
                coefficients.setEntry(i, j, random.nextDouble());
            }
        }

        double[] constantData = new double[luMatDim];
        for (int i = 0; i < luMatDim; ++i) {
            constantData[i] = random.nextDouble();
        }
        constants = MatrixUtils.createRealVector(constantData);
    }

    public static RealVector solve(LuDecomposition lu) {
        DecompositionSolver solver = new LUDecomposition(lu.coefficients).getSolver();
        return solver.solve(lu.constants);
    }

    @Benchmark
    @Threads(1)
    public static RealVector _01_singleThread(LuDecomposition lu) {
        return solve(lu);
    }

    @Benchmark
    @Threads(Threads.MAX)
    public static RealVector _02_multiThread(LuDecomposition lu) {
        return solve(lu);
    }
}
