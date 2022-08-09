package liu.xiao.zor.jvmbench;

import org.junit.Assert;
import org.junit.Test;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class XzCompression1MiBTest {

    @Test
    public void case1() throws IOException {
        XzCompression1MiB xz = new XzCompression1MiB();
        xz.setup();
        double compressionRatio = (double) xz.compressedBytes.length / xz.size;
        System.out.println("Compression Ratio = " + compressionRatio);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                xz.compressedBytes, 0, xz.compressedBytes.length);
        XZInputStream xzInputStream = new XZInputStream(inputStream);

        byte[] decompressedBytes = new byte[xz.size];
        Assert.assertEquals(xz.size, xzInputStream.read(decompressedBytes));
        Assert.assertArrayEquals(decompressedBytes, xz.rawBytes);
    }
}
