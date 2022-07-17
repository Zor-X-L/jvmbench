package liu.xiao.zor.jvmbench;

import org.junit.Test;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class XzCompression1MiBTest {

    @Test
    public void case1() throws IOException {
        XzCompression1MiB xz = new XzCompression1MiB();
        xz.setup();
        XzCompression1MiB.compress(xz);
        double compressionRatio = (double) xz.compressedOutputStream.size() / xz.size;
        System.out.println("Compression Ratio = " + compressionRatio);

        byte[] decompressedBytes = new byte[xz.size];
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                xz.compressedOutputStream.toByteArray(), 0, xz.compressedOutputStream.size());
        XZInputStream xzInputStream = new XZInputStream(inputStream);

        int numBytesRead = xzInputStream.read(decompressedBytes);
        assertEquals(xz.size, numBytesRead);
        for (int i = 0; i < decompressedBytes.length; ++i) {
            assertEquals(xz.inputBytes[i], decompressedBytes[i]);
        }
    }
}
