package liu.xiao.zor.jvmbench;

import org.openjdk.jmh.annotations.*;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@State(Scope.Thread)
public class XzCompression1MiB {

    public int size = 1048576;

    public byte[] inputBytes = new byte[size];
    public ByteArrayOutputStream compressedOutputStream = new ByteArrayOutputStream(size);
    public XZOutputStream xzOutputStream;

    @Setup(Level.Iteration)
    public void setup() throws IOException {
        xzOutputStream = new XZOutputStream(compressedOutputStream, new LZMA2Options());

        Random random = new SecureRandom();
        List<byte[]> dictionary = new ArrayList<>();
        int remainingDictionarySize = size / 2;
        while (remainingDictionarySize > 0) {
            int elementSize = 1 + random.nextInt(remainingDictionarySize);
            byte[] element = new byte[elementSize];
            random.nextBytes(element);
            dictionary.add(element);
            remainingDictionarySize -= elementSize;
        }

        int position = 0;
        while (position < inputBytes.length) {
            int index = random.nextInt(dictionary.size());
            int length = Math.min(dictionary.get(index).length, inputBytes.length - position);
            System.arraycopy(dictionary.get(index), 0, inputBytes, position, length);
            position += length;
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
