package liu.xiao.zor.jvmbench;

import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LuDecompositionTest {

    @Test
    public void case1() {
        LuDecomposition state = new LuDecomposition();
        state.setup();
        RealVector solution = LuDecomposition.solve(state);
        assertTrue(state.coefficients.operate(solution).subtract(state.constants).getNorm() < 1e-5);
    }
}
