package liu.xiao.zor.jvmbench;

import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LuDecomposition32X32Test {

    @Test
    public void case1() {
        LuDecomposition32x32 state = new LuDecomposition32x32();
        state.setup();
        RealVector solution = LuDecomposition32x32.solve(state);
        assertTrue(state.coefficients.operate(solution).subtract(state.constants).getNorm() < 1e-5);
    }
}
