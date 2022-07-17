package liu.xiao.zor.jvmbench;

import org.openjdk.jmh.annotations.*;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

@State(Scope.Thread)
public class XzCompression1MiB {

    public int size = 1048576;
    public int chunkSize = 1024;
    public int chunkMaxRepeatTimes = 3;

    public byte[] inputBytes = new byte[size];
    public ByteArrayOutputStream compressedOutputStream = new ByteArrayOutputStream(size);
    public XZOutputStream xzOutputStream;

    @Setup(Level.Iteration)
    public void setup() throws IOException {
        xzOutputStream = new XZOutputStream(compressedOutputStream, new LZMA2Options());
        Random random = new SecureRandom();
        byte[] chunkBytes = new byte[chunkSize];
        for (int inputPos = 0; inputPos < inputBytes.length; ) {
            random.nextBytes(chunkBytes);
            int repeatTimes = 1 + random.nextInt(chunkMaxRepeatTimes);
            for (int i = 0; i < repeatTimes && inputPos < inputBytes.length; ++i) {
                for (int chunkPos = 0; chunkPos < chunkBytes.length && inputPos < inputBytes.length; ++chunkPos) {
                    inputBytes[inputPos] = chunkBytes[chunkPos];
                    ++inputPos;
                }
            }
        }
    }

    public static XZOutputStream compress(XzCompression1MiB xz) throws IOException {
        xz.xzOutputStream.write(xz.inputBytes);
        xz.xzOutputStream.flush();
        return xz.xzOutputStream;
    }

    @Benchmark
    @Threads(1)
    public static XZOutputStream _01_singleThread(XzCompression1MiB xz) throws IOException {
        return compress(xz);
    }

    @Benchmark
    @Threads(Threads.MAX)
    public static XZOutputStream _02_multiThread(XzCompression1MiB xz) throws IOException {
        return compress(xz);
    }
}
