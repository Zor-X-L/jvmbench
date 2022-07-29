package liu.xiao.zor.jvmbench;

import org.junit.Assert;
import org.junit.Test;

public class SmallPtTest {

    private static void testERand48(short i1, short i2, short i3, double o, short o1, short o2, short o3) {
        short[] x = new short[3];
        x[0] = i1;
        x[1] = i2;
        x[2] = i3;
        Assert.assertEquals(o, SmallPt.eRand48(x), 0);
        Assert.assertEquals(o1, x[0]);
        Assert.assertEquals(o2, x[1]);
        Assert.assertEquals(o3, x[2]);
    }

    @Test
    public void eRand48() {
        testERand48((short) 32624, (short) -265, (short) 1425, 0.703499264988504791, (short) -7493, (short) -30945, (short) -19432);
        testERand48((short) 1172, (short) 10545, (short) 3393, 0.757036472651687831, (short) -5361, (short) 9323, (short) -15923);
        testERand48((short) -28352, (short) 350, (short) 32426, 0.777358219806313144, (short) 22603, (short) -3389, (short) -14592);
        testERand48((short) 26707, (short) -4760, (short) -22803, 0.277115446280170374, (short) -670, (short) 2482, (short) 18161);
        testERand48((short) 14640, (short) 2660, (short) 2683, 0.615811365999871185, (short) 31099, (short) -12211, (short) -25179);
    }
}
