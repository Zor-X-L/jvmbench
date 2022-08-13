package liu.xiao.zor.jvmbench;

import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LuDecompositionTest {

    @Test
    public void case1() {
        LuDecomposition lu = new LuDecomposition();
        lu.setup();
        RealVector solution = LuDecomposition.solve(lu);
        assertTrue(lu.coefficients.operate(solution).subtract(lu.constants).getNorm() < 1e-5);
    }
}
